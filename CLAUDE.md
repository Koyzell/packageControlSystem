# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

末端驿站包裹管理系统 — a parcel station management system. Spring Boot 3.5.16 + MyBatis + MySQL, static HTML/Bootstrap frontend. Java 17, Maven, package `com.ustb`.

## Commands

```bash
mvn clean compile              # Build
mvn test                       # Run all tests (H2 in-memory DB, no MySQL needed)
mvn test -Dtest=PackageControllerTest                     # Single test class
mvn test -Dtest=PackageControllerTest#test01_checkIn_normal  # Single test method
mvn spring-boot:run            # Start (requires MySQL with parcel_station database)
```

## Architecture

**Backend (Controller → Service → Mapper, classic three-layer):**

| Layer | Files | Role |
|-------|-------|------|
| Controller | `AdminController`, `UserController`, `AuthController`, `MetaController` | REST endpoints |
| Service | `PackageService` interface + `PackageServiceImpl` | Business logic, pickup code generation, operation logging |
| Mapper | `PackageMapper`, `UserMapper`, `OperationLogMapper`, `ShelfMapper`, `CourierMapper` | Mostly annotation-based MyBatis; `PackageMapper` uses XML for complex pagination queries |

**Key API endpoints:**

| Method | Path | Auth | Purpose |
|--------|------|:---:|---------|
| POST | `/api/auth/login` | public | Login, returns JWT |
| GET/POST/DELETE | `/api/admin/shelves` | ADMIN | List/add/delete shelves |
| GET/POST/DELETE | `/api/admin/couriers` | ADMIN | List/add/delete couriers |
| GET | `/api/admin/dashboard` | ADMIN | Stats: today check-in/pickup, overdue, awaiting |
| GET | `/api/admin/packages` | ADMIN | Paginated list (params: page, pageSize, keyword, status) |
| POST | `/api/admin/packages/check-in` | ADMIN | Check-in with auto-generated pickup code |
| GET | `/api/user/packages?keyword=` | any | Search by phone/pickup-code/tracking-number |
| PUT | `/api/{admin\|user}/packages/{id}/pickup` | any | Confirm pickup |

**Frontend (static HTML in `static/`, Bootstrap 5 CDN + vanilla JS):**

| Page | Purpose |
|------|---------|
| `index.html` | Smart router: checks JWT, redirects to login/admin/user |
| `login.html` | Login form, POSTs to `/api/auth/login`, stores JWT+role to both sessionStorage and localStorage |
| `admin.html` | Dashboard stats, check-in form (dynamic shelves/couriers dropdowns with modal add/delete), paginated package table with status filters, overdue highlighting |
| `user.html` | Keyword search (phone/pickup-code/tracking-number), pickup confirmation |
| `css/style.css` | Custom styles: overdue rows, fixed table layout, stat cards, login page |

**Key patterns:**

- **JWT auth**: `LoginInterceptor` (HandlerInterceptor, NOT Spring Security) validates Bearer tokens on `/api/**` except `/api/auth/**`. Returns HTTP 401/403 with JSON body. `app.auth.enabled=false` in test profile skips auth.
- **Unified response**: `Result<T>` wrapper `{"code":200,"message":"...","data":{...}}`. Business errors use code field (400/404/409), HTTP status always 200 for controller responses, 401/403 for interceptor rejections.
- **Pickup code**: Format `YYMMDD-L-NNN` (date 6 digits + letter A-Z + serial 001-999). Generated in `PackageServiceImpl.checkIn()` by counting today's packages. Single-day capacity: 26x999=25,974. `pickup_code` has UNIQUE constraint; `insertWithPickupCodeRetry()` catches `DuplicateKeyException` and regenerates on collision (up to 5 retries).
- **Concurrency**: `checkIn()` and `pickup()` are `@Transactional`. Pickup uses optimistic locking: `UPDATE ... WHERE id = #{id} AND status = 'AWAITING_PICKUP'` — rows affected = 0 means already picked up. Check-in uses `DuplicateKeyException` catch for both tracking_number (→409) and pickup_code (→retry).
- **Overdue**: Computed in `toVO()` as `checkInTime + 48h < now`, never persisted.
- **Pagination**: `PageResult<T>` wrapper with fields `list`, `total`, `page`, `pageSize`, `totalPages`. Complex SQL in `mapper/PackageMapper.xml` using MyBatis `<where>` tag.
- **Frontend state**: `sessionStorage` (per-tab, primary) for JWT token/role to prevent cross-tab login interference; `localStorage` (cross-tab, fallback) for initial sync. Page state (currentPage, currentFilter) in `sessionStorage` survives refresh.
- **JWT expiry guard**: Both admin.html and user.html decode JWT payload on load, check `exp` claim, redirect to login if expired before any API call.
- **Shelves/couriers management**: Modal-based add and delete. Add uses `text/plain` content type (not `application/json`) to avoid `StringHttpMessageConverter` quote-wrapping on `@RequestBody String`.
- **Form reset**: After check-in, only text inputs are cleared — dropdown selections preserved for batch operations.

**Database (MySQL `parcel_station`):**

| Table | Purpose |
|-------|---------|
| `packages` | Core: tracking_number (UNIQUE), pickup_code (UNIQUE), recipient_phone, courier_company, shelf_location, status (AWAITING_PICKUP/PICKED_UP), timestamps |
| `users` | Auth: username (UNIQUE), password (BCrypt), role (ADMIN/USER). Seeded by `DataInitializer`: admin/admin123, user/user123 |
| `operation_logs` | Audit: package_id, tracking_number, operation_type (CHECK_IN/PICK_UP), operator, details |
| `shelves` | Shelf names (UNIQUE), seeded with A-01~C-01 |
| `couriers` | Courier company names (UNIQUE), seeded with 9 companies |

`schema.sql` at `src/main/resources/schema.sql` — full DDL + seed data. `test-data.sql` at `src/main/resources/test-data.sql` — 30 sample packages for development.

**Tests (29 cases in `PackageControllerTest`):**

Uses H2 in-memory DB (MySQL mode) via `src/test/resources/application.yaml` + `schema.sql`. Auth disabled via `app.auth.enabled=false`. Tests run in method-name order via `@TestMethodOrder`.

Covers: check-in (with pickup code format validation), keyword search (phone/pickup-code/tracking-number), pickup (user + admin paths), overdue detection, duplicate tracking number, invalid phone, missing fields, double pickup, empty query, non-existent pickup, dashboard stats, operation logs, login success/failure, pagination, shelves CRUD (list/add/delete/duplicate/not-found), couriers CRUD (list/add/delete/duplicate/not-found).
