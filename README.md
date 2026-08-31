# DesignTouch+ Backend

Spring Boot REST API for the DesignTouch+ creative agency website.

## Stack
- Java 21, Spring Boot 3.3
- PostgreSQL (Neon, free tier)
- Spring Data JPA + Flyway migrations
- Spring Security + JWT (admin routes)

## Local setup

1. Copy the dev properties template and fill in your Neon connection string:
   ```
   cp src/main/resources/application-dev.properties.example src/main/resources/application-dev.properties
   ```
   Get the connection string from your Neon dashboard -> your project -> "Connect" button
   (convert `postgresql://user:pass@host/db?...` to the `jdbc:postgresql://host/db?...`
   format shown in the template).

2. Run with the dev profile.

   **If `mvn` is on your PATH:**
   ```
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

   **If not** (e.g. VS Code's Java extension manages Maven internally and `mvn`
   isn't a recognized terminal command): use the included `.vscode/launch.json`
   configuration -- open the Run and Debug panel (`Ctrl+Shift+D`), select
   "DesigntouchBackendApplication (dev)", and press `F5`.

3. On first run, Flyway creates all 18 tables automatically, and `DevDataSeeder`
   (dev profile only) inserts a handful of sample rows so there's something to
   look at immediately -- team members, clients, a testimonial, stats, one
   service, FAQs, pricing plans, about content, and company info.

4. API runs at `http://localhost:8080`. Verify it's working:
   ```
   http://localhost:8080/api/team
   ```
   should return the 3 seeded team members as JSON (not an empty array, not an
   error).

## Project structure

```
src/main/java/com/designtouch/backend/
├── config/       CORS, Security, dev-only data seeder
├── entity/       @Entity classes (18 total) + enums/ subpackage
├── repository/   Spring Data JPA repositories (18 total)
├── dto/          Request/response records — keeps entities out of the API contract
├── service/      Business logic that needs @Transactional (e.g. BlogPostService)
├── security/     JWT generation/validation, auth filter, UserDetailsService
├── controller/   REST endpoints (@RestController)
└── exception/    Global exception handling → consistent JSON error responses

src/main/resources/
├── application.properties          shared config, reads from env vars
├── application-dev.properties      local-only overrides with real Neon creds (gitignored)
└── db/migration/
    └── V1__create_tables.sql       18 tables, 8 enum types, indexes
```

## Database migrations

Flyway runs migrations automatically on startup. To add a new one:
```
src/main/resources/db/migration/V{next_number}__{description}.sql
```
Never edit a migration that has already run in any environment -- add a new one instead.

## API overview

All public content endpoints are `GET` and require no auth. Full list:

| Area | Endpoints |
|---|---|
| Team | `GET /api/team` |
| Clients | `GET /api/clients` |
| Testimonials | `GET /api/testimonials` |
| Stats | `GET /api/stats` |
| Values | `GET /api/values` |
| Process steps | `GET /api/process-steps?pageType=ABOUT\|SERVICES\|PRICING` |
| Tools | `GET /api/tools` |
| Add-ons | `GET /api/addons` |
| FAQs | `GET /api/faqs?category=PRICING\|GENERAL` |
| Portfolio | `GET /api/portfolio/featured`, `GET /api/portfolio?contentType=...&category=...&sort=latest` |
| Services | `GET /api/services`, `GET /api/services/{slug}` |
| Pricing | `GET /api/pricing-plans?type=ONE_TIME\|MONTHLY` |
| Blog | `GET /api/blogs`, `/search?q=`, `/popular`, `/categories`, `/{slug}` |
| About | `GET /api/about` |
| Company info | `GET /api/company-info` |
| Contact | `POST /api/contact` (public, validated) |
| Newsletter | `POST /api/newsletter/subscribe` (public, validated) |
| Auth | `POST /api/auth/login`, `POST /api/auth/register-first-admin` (one-time only) |

## First-time admin setup

Once deployed, create the first admin account by calling this endpoint ONCE
(it stops working after the first admin exists):

```
POST /api/auth/register-first-admin
Content-Type: application/json

{ "username": "your-admin-username", "password": "your-strong-password" }
```

Then log in normally:

```
POST /api/auth/login
Content-Type: application/json

{ "username": "your-admin-username", "password": "your-strong-password" }
```

The response includes a JWT -- send it as `Authorization: Bearer <token>` on any
future admin (write) requests once those endpoints are built.

## Deployment

- Backend: Render or Railway (free tier), set env vars: `DATABASE_URL`, `DATABASE_USERNAME`,
  `DATABASE_PASSWORD`, `FRONTEND_URL`, `JWT_SECRET`
- Database: Neon (free tier, PostgreSQL) -- already set up and verified working
- Frontend: Vercel

## Status

- [x] Project skeleton, CORS, base security config
- [x] Flyway migration V1 (18 tables, 8 enums, indexes)
- [x] Entities (18 total)
- [x] Repositories (18 total)
- [x] Public GET endpoints (all content areas)
- [x] Contact form + newsletter POST endpoints
- [x] Admin JWT auth (login, one-time bootstrap register, JwtAuthFilter, SecurityConfig)
- [x] Connected to real Neon database -- migration + sample data verified end-to-end
- [ ] Admin write endpoints (POST/PUT/DELETE per entity, behind JWT)
- [ ] Cloudinary integration for image/video upload
- [ ] Deploy to Render/Railway
- [ ] Next.js frontend
