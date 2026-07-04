# Federal Holidays API - Repository

This is a mono-repo containing the Federal Holidays API project for Royal Bank of Canada (RBC) interview.

## Projects

### 📂 federal-holidays-api/
Spring Boot REST API for managing US and Canada federal holidays with JWT authentication, correlation ID tracking, and bulk file upload support.

**👉 See [federal-holidays-api/README.md](federal-holidays-api/README.md) for complete documentation**

---

## Quick Start

```bash
# Navigate to project
cd federal-holidays-api

# Build and run
mvn spring-boot:run

# Application starts on http://localhost:8080
```

---

## Repository Structure

```
.
├── .git/                         # Git repository
├── federal-holidays-api/         # Main Spring Boot project
│   ├── src/                     # Source code
│   ├── test-data/               # Postman collection & test files
│   ├── pom.xml                  # Maven configuration
│   └── README.md                # 📖 Project documentation
└── README.md                    # This file
```

---

## Testing

Import Postman collection for all 20 API endpoints:
```
federal-holidays-api/test-data/Federal-Holidays-API.postman_collection.json
```

---

**Developer:** Nishit Bhansali  
**Assignment:** Royal Bank of Canada (RBC)
