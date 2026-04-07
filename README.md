# Zorvyn Finance Manager
### Finance Data Processing and Access Control Backend
**Submitted by:** Gautam Govind  
**Role applied for:** Backend Developer Intern  
**Tech Stack:** Spring Boot 3.3.4 · Spring Security 6 · JWT · JPA/Hibernate · H2

---

## Quick Start

The entire application runs with a single command. No database setup, no environment configuration, no external dependencies.

```bash
git clone <your-repo-url>
cd finance
mvn spring-boot:run
```

The moment the application starts, the console will print:

```text
─────────────────────────────────────────────
TEST CREDENTIALS:
ADMIN    → admin@zorvyn.com    / Admin@123
ANALYST  → analyst@zorvyn.com  / Analyst@123
VIEWER   → viewer@zorvyn.com   / Viewer@123
─────────────────────────────────────────────
```

Use these credentials immediately with the API endpoints below. No manual setup required.

**API Documentation:** http://localhost:8080/swagger-ui.html

**H2 Console:**        http://localhost:8080/h2-console

**Requirements:** Java 17+, Maven 3.6+

---

## What This System Does

A finance dashboard backend where three types of users interact with financial
records based on their role. The system enforces role-based access at two
independent layers — HTTP route level and service method level — and provides
aggregated dashboard analytics computed entirely server-side.

---

## Access Control Matrix

```text
| Action                  | VIEWER | ANALYST | ADMIN |
|-------------------------|--------|---------|-------|
| View financial records  | ✓      | ✓       | ✓     |
| Filter records          | ✓      | ✓       | ✓     |
| View dashboard summary  | ✗      | ✓       | ✓     |
| Create records          | ✗      | ✗       | ✓     |
| Update records          | ✗      | ✗       | ✓     |
| Delete records (soft)   | ✗      | ✗       | ✓     |
| Manage users            | ✗      | ✗       | ✓     |
```

This is a deliberate implementation of **segregation of duties** — a standard
control in financial systems where read access and write access are independently
governed. An analyst can interrogate data without being able to modify it.

---

## API Reference

### Authentication

POST /api/auth/login

```json
{
  "email": "admin@zorvyn.com",
  "password": "Admin@123"
}
```
Returns a Bearer token. Include it in all subsequent requests:

Authorization: Bearer <TOKEN>

---

### User Management  _(ADMIN only)_

```text
| Method | Endpoint               | Description               |
|--------|------------------------|---------------------------|
| POST   | `/api/users`           | Create a new user         |
| GET    | `/api/users`           | List users (filterable)   |
| GET    | `/api/users/{id}`      | Get user by ID            |
| PUT    | `/api/users/{id}`      | Update name or status     |
| PATCH  | `/api/users/{id}/role` | Change user role          |
| DELETE | `/api/users/{id}`      | Soft delete user          |
```

**GET /api/users — optional filters:**

```text
?role=ROLE_ADMIN
?active=true
?role=ROLE_ANALYST&active=false
```

**POST /api/users — request body:**

```text
{
  "fullName": "Jane Doe",
  "email": "jane@zorvyn.com",
  "password": "Secure@123",
  "role": "ROLE_ANALYST"
}
```

**Note:** Role change is a separate `PATCH /role` endpoint intentionally.
Changing a user's role is a sensitive, auditable operation and should not
be bundled into a general update.

---

### Financial Records  _(Read: all roles · Write: ADMIN only)_

```text
| Method | Endpoint          | Description                    |
|--------|-------------------|--------------------------------|
| POST   | `/api/records`    | Create a financial record      |
| GET    | `/api/records`    | List records (paginated)       |
| GET    | `/api/records/{id}` | Get record by ID             |
| PUT    | `/api/records/{id}` | Update record                |
| DELETE | `/api/records/{id}` | Soft delete record           |
```

**GET /api/records — all parameters optional:**

```text
?type=INCOME
?type=EXPENSE
?category=salary
?dateFrom=2026-01-01&dateTo=2026-04-30
?page=0&size=10
```

**POST /api/records — request body:**

```text
{
  "amount": 85000.00,
  "type": "INCOME",
  "category": "salary",
  "transactionDate": "2026-04-01",
  "notes": "Monthly salary"
}
```

**Validation rules:**
- `amount` — required, positive, max 2 decimal places
- `type` — required, must be `INCOME` or `EXPENSE`
- `category` — required, max 50 characters
- `transactionDate` — required, cannot be in the future
- `notes` — optional, max 500 characters

---

### Dashboard Summary  _(ANALYST and ADMIN only)_

```text
URL:      http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:financedb
Username: admin
Password: Admin@Zorvyn
```

This allows live inspection of all tables, seeded data, and query
execution during evaluation.

---

## Error Responses

Every error — regardless of type — returns the same predictable shape:

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "You do not have permission to perform this action",
  "path": "/api/records",
  "timestamp": "2026-04-06 18:30:00",
  "validationErrors": null
}
```

Validation errors include a field-level breakdown:

```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "One or more fields are invalid",
  "path": "/api/records",
  "timestamp": "2026-04-06 18:30:00",
  "validationErrors": {
    "amount": "Amount must be positive with at most 2 decimal places",
    "transactionDate": "Transaction date cannot be in the future"
  }
}
```

---

## Architecture Overview:

```text
controller/      HTTP boundary — request intake and response dispatch only
service/         All business logic, transaction management, RBAC enforcement
repository/      Data access — custom JPQL queries and Spring Data interfaces
model/           JPA entities with auditing and soft delete
dto/
  request/       Inbound API contract with validation annotations
  response/      Outbound API contract decoupled from entity schema
  projection/    Lightweight query result interfaces for aggregations
mapper/          Explicit translation layer between entity and DTO
security/        JWT generation, validation, and request filter
exception/       Centralised error handling via @RestControllerAdvice
validator/       Custom constraint annotations for domain-specific rules
config/          Spring Security and JPA auditing configuration
```

**Request lifecycle:**

```text
HTTP Request
→ JwtAuthFilter        (validates token, populates SecurityContext)
→ SecurityConfig       (enforces route-level RBAC)
→ Controller           (deserialises, validates, delegates)
→ Service (@PreAuthorize) (enforces method-level RBAC, business logic)
→ Repository           (JPQL query execution)
→ Mapper               (entity → response DTO)
→ HTTP Response
```

---

## Design Decisions and Tradeoffs

**H2 in-memory database**
Chosen deliberately to eliminate setup friction for evaluation. An evaluator
can clone and run with zero configuration. In a production system this would
be PostgreSQL with connection pooling (HikariCP) and proper transaction
isolation levels. The switch requires changing two lines in
`application.properties` and one Maven dependency.

**Two-layer RBAC**
Access control is enforced at two independent points — URL pattern rules in
`SecurityConfig` and `@PreAuthorize` annotations on every service method.
This is defence in depth: a misconfigured route rule does not create a
security gap because the service method is a second, independent gate.

**Soft delete via `@SQLRestriction`**
Deleted records are filtered at the Hibernate SQL generation level, not in
application code. Every repository query — including custom JPQL — automatically
excludes deleted records without any `AND deleted = false` clause in the query
itself. This makes accidental exposure of deleted data structurally impossible.

**`BigDecimal` for all monetary values**
`double` and `float` cannot represent 0.1 exactly in binary floating point.
For a financial system, this is not acceptable. `BigDecimal` with
`precision=15, scale=2` supports amounts up to 9,999,999,999,999.99 with
exact decimal representation.

**DTO and entity separation**
The API contract (`dto/response/`) is explicitly decoupled from the database
schema (`model/`). A change to an entity field does not automatically change
what the API returns — the mapper is the visible, intentional translation point.
This decoupling is what makes large codebases maintainable over time.

**Database-level aggregations**
Dashboard totals, category breakdowns, and monthly trends are computed using
JPQL aggregate queries with `SUM`, `GROUP BY`, and `COALESCE`. No records are
loaded into application memory for computation. This approach scales correctly
as data volume grows.

**Category normalisation**
All category values are stored in lowercase and trimmed at the mapper layer
before persistence. This ensures `"Salary"`, `"SALARY"`, and `" salary "` are
treated as the same category in `GROUP BY` aggregations.

**Segregation of duties**
The Analyst role can view records and access dashboard analytics but cannot
create, modify, or delete records. This mirrors a real financial system control
where the person who reads data should not be the same person who writes it.

---

## Known Limitations and Production Considerations

- **No token refresh** — JWT tokens expire after 24 hours and require
  re-login. A production system would implement refresh token rotation.

- **No audit log table** — sensitive operations (role changes, deletions)
  are logged to console but not persisted. A production system would write
  to an immutable audit table.

- **Single-node session** — JWT secret is stored in `application.properties`.
  A production system would use a secrets manager (AWS Secrets Manager,
  HashiCorp Vault) and support distributed token validation.

- **H2 is not thread-safe under high concurrency** — acceptable for
  evaluation, not for production. PostgreSQL with proper isolation levels
  would be the production choice.

- **No rate limiting** — the `/api/auth/login` endpoint has no brute-force
  protection. A production system would apply rate limiting at the gateway
  or filter level.

---

## Project Structure:

```text
src/main/java/com/zorvyn/finance/
├── ZorvynFinanceManagerApplication.java
├── DataSeeder.java
├── config/
│   ├── AuditConfig.java
│   └── SecurityConfig.java
├── controller/
│   ├── AuthController.java
│   ├── UserController.java
│   ├── RecordController.java
│   └── DashboardController.java
├── service/
│   ├── AuthService.java
│   ├── UserService.java
│   ├── RecordService.java
│   └── DashboardService.java
├── repository/
│   ├── UserRepository.java
│   └── FinancialRecordRepository.java
├── model/
│   ├── BaseEntity.java
│   ├── User.java
│   └── FinancialRecord.java
├── dto/
│   ├── projection/
│   │   ├── CategorySummaryProjection.java
│   │   ├── MonthlyTrendProjection.java
│   │   └── RecordSummaryProjection.java
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── CreateUserRequest.java
│   │   ├── UpdateUserRequest.java
│   │   └── RecordRequest.java
│   └── response/
│       ├── AuthResponse.java
│       ├── UserResponse.java
│       ├── RecordResponse.java
│       └── DashboardResponse.java
├── mapper/
│   ├── UserMapper.java
│   ├── RecordMapper.java
│   └── DashboardMapper.java
├── security/
│   ├── CustomUserDetailsService.java
│   ├── JwtUtil.java
│   └── JwtAuthFilter.java
├── exception/
│   ├── ApiError.java
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── DuplicateResourceException.java
│   └── UnauthorizedActionException.java
└── validators/
    ├── ValidTransactionAmount.java
    ├── TransactionAmountValidator.java
    ├── ValidTransactionType.java
    └── TransactionTypeValidator.java
```

---
