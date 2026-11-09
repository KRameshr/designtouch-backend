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
â”œâ”€â”€ config/       CORS, Security, dev-only data seeder
â”œâ”€â”€ entity/       @Entity classes (18 total) + enums/ subpackage
â”œâ”€â”€ repository/   Spring Data JPA repositories (18 total)
â”œâ”€â”€ dto/          Request/response records â€” keeps entities out of the API contract
â”œâ”€â”€ service/      Business logic that needs @Transactional (e.g. BlogPostService)
â”œâ”€â”€ security/     JWT generation/validation, auth filter, UserDetailsService
â”œâ”€â”€ controller/   REST endpoints (@RestController)
â””â”€â”€ exception/    Global exception handling â†’ consistent JSON error responses

src/main/resources/
â”œâ”€â”€ application.properties          shared config, reads from env vars
â”œâ”€â”€ application-dev.properties      local-only overrides with real Neon creds (gitignored)
â””â”€â”€ db/migration/
    â””â”€â”€ V1__create_tables.sql       18 tables, 8 enum types, indexes
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

- **Backend:** Render Web Service using the repository's `Dockerfile` (Java 21 and
  Spring Boot 3.3). Connect the backend GitHub repository, choose Docker as the
  runtime, and use the repository root as the Docker build context. The container
  listens on Spring's `server.port=${PORT:8080}`. Set the Render service's `PORT`
  to `8080` (or use another Render-assigned value consistently).
- Set these in the Render service environment. Never commit real credentials:
  `SPRING_PROFILES_ACTIVE=prod`, `DATABASE_URL`, `DATABASE_USERNAME`,
  `DATABASE_PASSWORD`, and `JWT_SECRET` (at least 32 bytes). For Neon, use the
  production branch's JDBC URL with `sslmode=require`.
- Configure the enabled integrations in Render as needed:
  `CORS_ALLOWED_ORIGINS` (or `FRONTEND_URL`), `CLOUDINARY_CLOUD_NAME`,
  `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`, `RESEND_API_KEY`,
  `RESEND_FROM_EMAIL`, `RESEND_ENABLED`, `CONTACT_RECIPIENT_EMAIL`, `AI_PROVIDER`,
  and `GEMINI_API_KEY` or `OPENAI_API_KEY` according to the selected provider.
- **Database:** Neon PostgreSQL. Existing migrations remain managed by Flyway.
- **Frontend:** Cloudflare Workers using the existing OpenNext adapter. Set
  `NEXT_PUBLIC_API_URL` to the Render service URL in Cloudflare's build variables
  so it is inlined during the Next.js build. Set the production frontend origin
  in Render's CORS configuration. Keep the current Vercel deployment active
  until Cloudflare deployment and production testing succeed.

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
- [ ] Push reviewed backend changes to GitHub
- [ ] Deploy backend to Render and verify production APIs/database connection
- [ ] Deploy frontend to Cloudflare and connect it to the Render API
- [ ] Complete production testing
- [ ] Next.js frontend
