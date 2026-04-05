# Finance Dashboard System (Backend)

Backend for a finance dashboard system built with **Spring Boot 3**, **Spring Security (JWT)**, **Spring Data JPA**, and **PostgreSQL**.

This backend is designed for the assignment **“Finance Data Processing and Access Control Backend”**. It demonstrates:
- clean API design and consistent response formatting
- structured service/repository architecture
- database-backed persistence and aggregation queries (analytics)
- role-based access control (RBAC) + data scoping
- validation and predictable error handling

---

## Architecture Diagram

### Mermaid (renders on GitHub and Mermaid-compatible Markdown viewers)
> If you’re viewing this README in an editor that doesn’t support Mermaid, the diagram may appear as plain text.

```mermaid
flowchart TB
  Client[Frontend / API Client]

  subgraph SpringBoot[Spring Boot Application]
    direction TB

    subgraph Security[security/*]
      JWTFilter[JWTAuthFilter]
      JWTService[JWTService]
      SecurityConfig[WebSecurityConfig]
      AuthService[AuthService]
    end

    subgraph Web[controller/*]
      AuthController[AuthController]
      UserController[UserController]
      RecordController[FinancialRecordController]
      DashboardController[DashboardController]
    end

    subgraph Service[service/*]
      UserService[UserService]
      RecordService[FinancialRecordService]
      DashboardService[DashboardService]
    end

    subgraph Persistence[repository/*]
      UserRepo[UserRepository]
      RecordRepo[FinancialRecordRepository]
    end

    subgraph CrossCutting[advice/*]
      ResponseWrap[GlobalResponseHandler\n(ApiResponse envelope)]
      ExceptionMap[GlobalExceptionHandler\n(ApiError mapping)]
    end
  end

  DB[(PostgreSQL)]

  Client -->|HTTP Request| JWTFilter
  JWTFilter -->|validates token| JWTService
  JWTFilter -->|sets SecurityContext| Web

  Client -->|/auth/*| AuthController --> AuthService --> UserRepo
  AuthService --> JWTService

  UserController --> UserService --> UserRepo
  RecordController --> RecordService --> RecordRepo
  DashboardController --> DashboardService --> RecordRepo

  UserRepo --> DB
  RecordRepo --> DB

  Web --> ResponseWrap
  Web --> ExceptionMap
  ExceptionMap --> ResponseWrap
```

### Plain-text fallback (always visible)
```
Client
  |
  v
JWTAuthFilter  ---> JWTService (parse/validate token)
  |
  v
Controllers (Auth/User/Records/Dashboard)
  |
  v
Services (AuthService/UserService/FinancialRecordService/DashboardService)
  |
  v
Repositories (UserRepository/FinancialRecordRepository)
  |
  v
PostgreSQL

Cross-cutting:
- GlobalResponseHandler wraps success responses into ApiResponse
- GlobalExceptionHandler maps errors into ApiResponse.error with correct status
```

> The advice layer wraps successful responses and maps exceptions into a consistent error envelope.

---

## Requirement Coverage (Assignment → This Project)

1) **User and role management**
   - Users stored in PostgreSQL (`User` entity)
   - Roles: `VIEWER`, `ANALYST`, `ADMIN` (`Role` enum)
   - Status/soft delete: users marked inactive (`active=false`)
   - Role enforcement via Spring Security annotations + service checks

2) **Financial records management**
   - Financial records stored in PostgreSQL (`FinancialRecord` entity)
   - CRUD endpoints + filtering (type/category/date/amount)
   - Ownership via `FinancialRecord.createdBy`

3) **Dashboard summary APIs**
   - totals (income/expense/net)
   - category summary
   - recent activity
   - weekly/monthly trends
   - implemented using **DB aggregation queries** (not in-memory) for summary/trends

4) **Access control logic**
   - JWT authentication
   - Role-based access with `ROLE_<ROLE>` authorities
   - VIEWER is scoped to own records; ADMIN/ANALYST can query across users

5) **Validation and error handling**
   - Bean validation (`@Valid`) on request bodies where applicable
   - Service-level business-rule validation (date/amount bounds)
   - Global exception handler returns consistent JSON error structure

6) **Data persistence**
   - PostgreSQL + Spring Data JPA

Optional enhancements included:
- JWT authentication + refresh endpoint
- pagination + sorting for record listing
- consistent API response envelope
- DB aggregation for analytics endpoints
- soft delete for users

---

## Tech Stack
- Java 21+ (project commonly run on Java 21)
- Spring Boot 3.x
- Spring Web
- Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL
- ModelMapper
- SpringDoc OpenAPI (Swagger)

---

## Roles, Behavior, and Access Control

### Roles
| Role | Dashboard | Records list | Records create/update/delete | Users management |
|------|-----------|--------------|-----------------------------|------------------|
| VIEWER | ✅ (scoped) | ✅ (own only) | ❌ | ❌ |
| ANALYST | ✅ (all) | ✅ (all) | ❌ | ❌ |
| ADMIN | ✅ (all) | ✅ (all) | ✅ | ✅ |

### Data scoping rules
Even when an endpoint allows multiple roles, the service layer enforces scope:
- **VIEWER** → automatically filters results by `createdBy = currentUser`
- **ADMIN/ANALYST** → no `createdBy` filter (can view all)

---

## Authentication (JWT)

### Tokens
- **Access token**: send in header `Authorization: Bearer <token>`
- **Refresh token**: used to fetch a new access token (optional enhancement)

### Claims
Access token contains:
- `sub` (user id)
- `email`
- `role`

### Signup defaults
- Signup creates a user with role **VIEWER** (role is not accepted from the client at signup).

---

## Database & Data Modeling

### Primary relationship
- `FinancialRecord` → `User` via `createdBy` (Many-to-One)

This relationship is intentionally **unidirectional** to avoid bidirectional pitfalls (infinite JSON recursion, N+1 queries, complex lifecycle management).

---

## API Response Format
All endpoints produce a consistent envelope.

### Success response example
```json
{
  "timestamp": "2026-04-05T12:24:22.089",
  "status": 200,
  "path": "/auth/login",
  "data": {
    "id": 2,
    "accessToken": "<jwt-access-token>",
    "refreshToken": null
  },
  "error": null
}
```

### Error response example
```json
{
  "timestamp": "2026-04-05T12:25:41.486",
  "status": 401,
  "path": "/auth/login",
  "data": null,
  "error": {
    "code": "AUTH_BAD_CREDENTIALS",
    "message": "Bad credentials",
    "details": null
  }
}
```

---

## API Overview (No Context Path)
This app does **not** use a servlet context path. All routes are served directly from `/`.

### Auth
- `POST /auth/signup`
- `POST /auth/login`
- `POST /auth/refresh`

### Users
- `GET /users` (ADMIN)
- `GET /users/{id}` (self or ADMIN)
- `POST /users/create` (ADMIN)
- `PUT /users/update` (self or ADMIN)
- `PATCH /users/delete/{id}` (self or ADMIN)
- `PATCH /users/role` (ADMIN)

### Financial Records
- `GET /records` (VIEWER/ANALYST/ADMIN; VIEWER scoped to own)
- `GET /records/{id}` (VIEWER/ANALYST/ADMIN; should be ownership checked)
- `POST /records/create` (ADMIN)
- `PUT /records/{id}` (ADMIN)
- `DELETE /records/{id}` (ADMIN)

### Dashboard
- `GET /dashboard/summary`
- `GET /dashboard/recent-activity?limit=5`
- `GET /dashboard/trends?type=weekly|monthly&startDate=YYYY-MM-DD&endDate=YYYY-MM-DD`

---

## Example Requests

### Signup
```http
POST /auth/signup
Content-Type: application/json

{
  "name": "Vishal",
  "email": "vishal@example.com",
  "password": "StrongPassword@123"
}
```

### Login
```http
POST /auth/login
Content-Type: application/json

{
  "email": "vishal@example.com",
  "password": "StrongPassword@123"
}
```

### Create record (ADMIN)
```http
POST /records/create
Authorization: Bearer <access-token>
Content-Type: application/json

{
  "amount": 2500,
  "type": "INCOME",
  "category": "Salary",
  "date": "2026-04-01",
  "note": "April salary"
}
```

### List records with filters + pagination + sorting
```http
GET /records?type=EXPENSE&startDate=2026-04-01&endDate=2026-04-30&page=0&size=20&sort=date,desc
Authorization: Bearer <access-token>
```

---

## Filtering, Pagination, Sorting
Records listing uses a dynamic JPA `Specification`.

Supported filters:
- `type`: INCOME / EXPENSE
- `category`
- `startDate`, `endDate`
- `minAmount`, `maxAmount`

Pagination/sorting via Spring `Pageable`.
- Default sort (when client doesn’t provide sort): `date DESC`, `id DESC`

---

## Dashboard Aggregations (DB-level)
Dashboard summary and trend APIs use DB aggregation queries in `FinancialRecordRepository`:
- total income: `SUM(amount)` filtered by `type=INCOME`
- total expense: `SUM(amount)` filtered by `type=EXPENSE`
- category totals: `GROUP BY category`
- trends:
  - monthly grouped by key `YYYYMM` = `year*100 + month`
  - weekly grouped by key `YYYYWW` = `year*100 + week`

This avoids loading all records into memory.

---

## Validation Rules (Examples)
- amount must be `> 0`
- `startDate` must not be after `endDate`
- `minAmount` must not be greater than `maxAmount`
- recent-activity limit is clamped to `[1..100]`

---

## Setup & Run

### Prerequisites
- Java 21+
- PostgreSQL

### Configure DB
Create a database (example): `finance_db`

Update `src/main/resources/application.properties`:
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

### Configure JWT
Set:
- `jwt.secretKey` (must be long enough for HMAC)
- `security.jwt.access-ttl-ms`
- `security.jwt.refresh-ttl-ms`

> In production, move secrets to environment variables.

### Run
Windows:
```bat
mvnw.cmd spring-boot:run
```

---

## Swagger / OpenAPI
If enabled:
- Swagger UI: `/swagger-ui.html`
- OpenAPI: `/v3/api-docs`

---

## Optional Improvements (Roadmap)
1) Refresh-token rotation + server-side storage + logout revocation
2) ISO week-year trend grouping (`isoyear`)
3) Ownership enforcement for `GET /records/{id}`
4) Rate limiting for `/auth/login`
5) More unit/integration tests (RBAC and scoping)
6) Lock out / disable inactive users via `UserDetails.isEnabled()`

---

## Assumptions & Tradeoffs
- Refresh token feature is intentionally lightweight for the assignment.
- Trend grouping uses calendar year with week number (not ISO week-year).
- Unidirectional relationships used to keep API safe and avoid recursion.

---

## Notes for Evaluators

### What to look for
- Clean separation of concerns (controller/service/repository)
- RBAC enforcement (annotations + service checks)
- VIEWER data scoping (cannot access other users’ records)
- DB aggregation for summary/trend endpoints
- Consistent error handling and response shapes

### RBAC quick test cases (expected results)
The table below is meant as a fast rubric-style check.

| Endpoint | VIEWER | ANALYST | ADMIN |
|---|---:|---:|---:|
| `POST /auth/signup` | 200 | 200 | 200 |
| `POST /auth/login` | 200 | 200 | 200 |
| `GET /dashboard/summary` | 200 (scoped) | 200 | 200 |
| `GET /dashboard/recent-activity` | 200 (scoped) | 200 | 200 |
| `GET /dashboard/trends` | 200 (scoped) | 200 | 200 |
| `GET /records` | 200 (own only) | 200 | 200 |
| `POST /records/create` | 403 | 403 | 201 |
| `PUT /records/{id}` | 403 | 403 | 200 |
| `DELETE /records/{id}` | 403 | 403 | 204 |
| `GET /users` | 403 | 403 | 200 |
| `PATCH /users/role` | 403 | 403 | 200 |

> Note: `GET /records/{id}` should be ownership-protected for VIEWER; see “Optional Improvements”.

### Suggested evaluation flow
1) Signup → `POST /auth/signup`
2) Login → `POST /auth/login`
3) Use token to test VIEWER scope:
   - `GET /records`
   - `GET /dashboard/summary`
4) With an ADMIN user, test management APIs:
   - `POST /users/create`
   - `PATCH /users/role`
   - `POST /records/create`, `PUT /records/{id}`, `DELETE /records/{id}`

---

## Troubleshooting
- **401**: missing/expired/invalid access token
- **403**: authenticated but role not permitted
- **400**: validation failures (dates/amounts/request body)
- **DB connection**: verify Postgres is running and credentials are correct
