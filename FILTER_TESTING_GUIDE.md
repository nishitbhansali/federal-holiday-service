# Request Validation Filter - Testing Guide

## Overview
The `RequestValidationFilter` validates all incoming requests and manages request context using ThreadLocal storage. This follows the same pattern as the credit-cards-api project from CIBC.

## Filter Components

### 1. **RequestValidationFilter** 
- Highest precedence filter
- Validates required headers
- Validates JWT tokens
- Sets up RequestContext in ThreadLocal
- Tracks request execution time
- Handles exceptions via GlobalExceptionHandler

### 2. **RequestContext** (Record)
- Immutable container for request metadata
- Stores: correlationId, userId, platform, requestPath

### 3. **RequestContextHolder**
- ThreadLocal wrapper for RequestContext
- Provides thread-safe access across the request lifecycle

### 4. **JwtUtil**
- JWT token generation and validation
- Extracts claims (userId) from tokens

---

## Required HTTP Headers

All API requests (except bypass paths) must include:

| Header | Format | Description | Example |
|--------|--------|-------------|---------|
| `X-Correlation-ID` | UUID (36 chars) | Request tracking ID | `550e8400-e29b-41d4-a716-446655440000` |
| `Authorization` | Bearer {token} | JWT authentication token | `Bearer eyJhbGc...` |
| `X-Platform` | String (optional) | Platform identifier | `web`, `mobile`, `internal-service` |

---

## Testing the Filter

### Step 1: Generate a Test JWT Token

**Endpoint:** `GET /api/v1/test/generate-token?userId=john.doe`

**Response:**
```json
{
  "userId": "john.doe",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZS...",
  "authorizationHeader": "Bearer eyJhbGciOiJIUzI1NiJ9...",
  "correlationId": "550e8400-e29b-41d4-a716-446655440000",
  "expiresInMs": 3600000,
  "usage": {
    "X-Correlation-ID": "550e8400-e29b-41d4-a716-446655440000",
    "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9...",
    "X-Platform": "web (optional)"
  }
}
```

### Step 2: Test with Generated Headers

**Example: Get all holidays**
```bash
curl -X GET "http://localhost:8080/federal-holidays-api/api/v1/holidays" \
  -H "X-Correlation-ID: 550e8400-e29b-41d4-a716-446655440000" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "X-Platform: web"
```

### Step 3: Verify Request Context

**Endpoint:** `GET /api/v1/test/context`

**Response:**
```json
{
  "correlationId": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "john.doe",
  "platform": "web",
  "requestPath": "/federal-holidays-api/api/v1/test/context",
  "message": "Request context successfully retrieved from ThreadLocal"
}
```

---

## Bypass Paths (No Authentication Required)

The following paths skip filter validation:

- `/actuator/health`
- `/actuator/info`
- `/swagger-ui/**`
- `/v3/api-docs/**`
- `/api-docs/**`

---

## Error Scenarios

### Missing X-Correlation-ID
```json
{
  "timestamp": "2026-07-03T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "code": "RBC_VALIDATION_ERROR",
  "message": "Missing X-Correlation-ID header",
  "path": "/federal-holidays-api/api/v1/holidays"
}
```

### Invalid UUID Format
```json
{
  "timestamp": "2026-07-03T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "code": "RBC_VALIDATION_ERROR",
  "message": "Invalid X-Correlation-ID format - expected 36-character UUID",
  "path": "/federal-holidays-api/api/v1/holidays"
}
```

### Missing Authorization Header
```json
{
  "timestamp": "2026-07-03T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "code": "RBC_VALIDATION_ERROR",
  "message": "Missing Authorization header",
  "path": "/federal-holidays-api/api/v1/holidays"
}
```

### Invalid/Expired JWT Token
```json
{
  "error": "Invalid or expired JWT token"
}
```

---

## Postman Collection Setup

### Environment Variables
```
base_url: http://localhost:8080/federal-holidays-api
userId: test-user
```

### Pre-request Script (Auto-generate headers)
```javascript
// Generate correlation ID
pm.environment.set("correlationId", pm.variables.replaceIn('{{$guid}}'));

// Generate JWT token (call /test/generate-token first)
pm.sendRequest({
    url: pm.environment.get("base_url") + "/api/v1/test/generate-token?userId=" + pm.environment.get("userId"),
    method: 'GET'
}, function (err, response) {
    const jsonData = response.json();
    pm.environment.set("jwt_token", jsonData.token);
});
```

### Headers (in Postman request)
```
X-Correlation-ID: {{correlationId}}
Authorization: Bearer {{jwt_token}}
X-Platform: postman
```

---

## Configuration (application.yml)

```yaml
jwt:
  secret: rbc-federal-holidays-secret-key-must-be-at-least-256-bits-long-for-hs256
  expiration-ms: 3600000  # 1 hour

logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%X{correlationId}] [%thread] %-5level %logger{36} - %msg%n"
```

**Note:** The correlation ID is automatically included in log output via MDC (Mapped Diagnostic Context).

---

## Sample Log Output

```
2026-07-03 14:30:15 [550e8400-e29b-41d4-a716-446655440000] [http-nio-8080-exec-1] INFO  c.r.h.filter.RequestValidationFilter - Request validated - Path: /api/v1/holidays, User: john.doe, CorrelationId: 550e8400-e29b-41d4-a716-446655440000, Platform: web
2026-07-03 14:30:16 [550e8400-e29b-41d4-a716-446655440000] [http-nio-8080-exec-1] INFO  c.r.h.filter.RequestValidationFilter - Request completed - Path: /api/v1/holidays, Duration: 234ms, Status: 200
```

---

## Production Considerations

1. **Disable Test Endpoints**: Remove or restrict `/api/v1/test/**` endpoints in production
2. **Externalize JWT Secret**: Use Azure Key Vault or environment variables
3. **Monitoring**: Track filter metrics (request count, avg duration, error rate)
4. **Rate Limiting**: Add rate limiting based on userId/correlationId
5. **Audit Logging**: Store correlation IDs for audit trail

---

## Comparison with Credit Cards API

| Feature | Credit Cards API | Federal Holidays API |
|---------|------------------|----------------------|
| Filter Pattern | ✅ OncePerRequestFilter | ✅ OncePerRequestFilter |
| Request Context | ✅ ThreadLocal | ✅ ThreadLocal |
| Authentication | AuthSdk (CIBC) | JWT Tokens |
| Validation | RequestValidator class | Inline in filter |
| Exception Handling | HandlerExceptionResolver | ✅ Same pattern |
| Correlation ID | ✅ MDC logging | ✅ MDC logging |
| Order | HIGHEST_PRECEDENCE | ✅ Same |

---

## Next Steps

After verifying the filter works correctly:
1. Update controllers to remove header parameters (use RequestContextHolder instead)
2. Add MDC correlation ID to all service layer logs
3. Create integration tests for filter validation
4. Add request/response logging interceptor
5. Implement rate limiting based on userId
