# Federal Holidays API Service

REST API for managing USA and Canada federal holidays with JWT authentication, correlation ID tracking, and bulk file upload support.



## Prerequisites

Before you begin, ensure you have the following installed:

- **Java 21** (LTS)
- **Maven 3.9+**
- **Git**
- **Postman** (for API testing)

Verify installations:
```bash
java -version    # Should show Java 21
mvn -version     # Should show Maven 3.9+
git --version    # Should show Git
```

---

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd <project-folder>
```

> **Note:** Replace `<project-folder>` with the name of the cloned directory (typically the repository name).

### 2. Verify Project Structure

```bash
ls -la
# You should see: pom.xml, src/, README.md, etc.
```

---

## Building the Application

### Full Build with Tests

```bash
mvn clean install
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: XX.XXX s
[INFO] Tests run: 87, Failures: 0, Errors: 0, Skipped: 0
```



---

## Running the Application

```bash
mvn spring-boot:run
```

The application starts on **port 8080** with the **local profile** (H2 in-memory database) by default.

### Verify Application is Running

You should see in the console:

```
Started FederalHolidaysApplication in X.XXX seconds
```

**Health Check:**
```bash
curl http://localhost:8080/federal-holidays-api/actuator/health
```

**Expected Response:**
```json
{"status":"UP"}
```

---

## Testing the API

### Using Postman Collection (Recommended)

Import the pre-configured Postman collection with all 20 endpoints:

1. **Open Postman** → File → Import
2. **Select:** `test-data/Federal-Holidays-API.postman_collection.json`
3. **Run:** "1. Generate JWT Token" request
4. Token and Correlation ID are **auto-saved** to collection variables
5. **Test all endpoints** with pre-configured requests

**Collection includes:**
- CRUD operations (Create, Read, Update, Delete)
- Search endpoints (country, year, date range)
- Pagination examples
- Bulk file upload (CSV/JSON)
- Error scenarios (401, 404, 409, 400)

---

## API Documentation

### OpenAPI/Swagger Specification

- **File:** `src/main/resources/federal-holidays-openapi.yml`
- **Format:** OpenAPI 3.0.3
- **View Online:** Use [Swagger Editor](https://editor.swagger.io/) and paste the YAML content

### Available Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/test/generate-token` | Generate JWT token (no auth) |
| `POST` | `/api/v1/holidays` | Create a new holiday |
| `GET` | `/api/v1/holidays` | Get all holidays (with pagination) |
| `GET` | `/api/v1/holidays/{id}` | Get holiday by ID |
| `PUT` | `/api/v1/holidays/{id}` | Update holiday |
| `DELETE` | `/api/v1/holidays/{id}` | Delete holiday |
| `GET` | `/api/v1/holidays/search` | Search holidays (country, year, date range) |
| `POST` | `/api/v1/holidays/upload` | Bulk upload from CSV/JSON file |
| `GET` | `/actuator/health` | Health check (no auth) |
| `GET` | `/actuator/info` | Application info (no auth) |

### Request Headers (Required)

- **Authorization:** `Bearer <JWT_TOKEN>`
- **X-Correlation-ID:** `<UUID>` (for request tracking)

### Supported Countries

- `USA`
- `CANADA`

Country names are normalized automatically (e.g., "usa" → "USA", "Canada" → "CANADA").

---

## Project Structure

```
federal-holidays-api/
├── src/
│   ├── main/
│   │   ├── java/com/rbc/holidays/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── context/         # Request context (ThreadLocal)
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── dto/             # Data Transfer Objects (Records)
│   │   │   ├── entity/          # JPA entities
│   │   │   ├── exception/       # Custom exceptions & handlers
│   │   │   ├── filter/          # JWT validation filter
│   │   │   ├── repository/      # JPA repositories
│   │   │   ├── service/         # Business logic
│   │   │   └── util/            # Utilities (JWT, file parsing)
│   │   └── resources/
│   │       ├── application.yml           # Default config
│   │       ├── application-local.yml     # Local profile
│   │       ├── data.sql                  # Sample data
│   │       └── federal-holidays-openapi.yml  # API spec
│   └── test/
│       └── java/com/rbc/holidays/       # Unit tests (87 tests)
├── pom.xml                               # Maven dependencies
├── README.md                             # This file
└── test-data/                            # Testing artifacts
    ├── Federal-Holidays-API.postman_collection.json
    ├── holidays.csv                      # Sample CSV for upload testing
    └── holidays.json                     # Sample JSON for upload testing
```

---

## Technical Stack

### Core Framework
- **Java 21** (LTS)
- **Spring Boot 3.5.14**
- **Spring Data JPA** (with Hibernate)
- **Spring Web** (REST API)

### Security
- **JWT Authentication** (JJWT 0.12.6)
- **Filter-based validation** (RequestValidationFilter)

### Database
- **H2** (in-memory database)

### Build & Testing
- **Maven 3.9+**
- **JUnit 5**
- **Mockito**
- **Maven Surefire** (test runner)
- **JaCoCo** (code coverage)

### Utilities
- **Apache Commons CSV** (CSV parsing)
- **Jackson** (JSON processing)
- **Lombok** (reduce boilerplate)
- **SLF4J + Logback** (logging)

---

## Configuration

- **Database:** H2 in-memory (auto-configured)
- **Sample Data:** Loaded from `data.sql` on startup
- **H2 Console:** `http://localhost:8080/federal-holidays-api/h2-console` (JDBC URL: `jdbc:h2:mem:holidaysdb`, username: `sa`, no password)

---

## Key Features

- **JWT Authentication** - All endpoints require Bearer token (generate via `/api/v1/test/generate-token`)
- **Correlation ID Tracking** - Required `X-Correlation-ID` header (UUID) for request tracing
- **Country Normalization** - "usa"/"US" → "USA", "canada" → "CANADA"
- **Unique Constraint** - One holiday per country per date (409 Conflict on duplicate)
- **Pagination** - `?offset=0&limit=100` (default)
- **Bulk Upload** - CSV/JSON files (max 10MB) with partial success support
- **Global Error Handling** - Consistent ErrorResponse format (400, 401, 404, 409, 413)

---

## Architecture & Design

- **Clean Layered Architecture:** Controller → Service → Repository
- **87 Passing Tests** - Full test coverage with JUnit 5 & Mockito
- **Lean DTOs** - Java Records, no unnecessary fields
- **Service Layer Validation** - Controllers only map HTTP requests
- **ThreadLocal Request Context** - Correlation tracking throughout request lifecycle



---

## Contact

**Developer:** Nishit Bhansali  
**Interview Assignment:** Royal Bank of Canada (RBC)

---

## License

This project is developed as part of an interview assignment.
