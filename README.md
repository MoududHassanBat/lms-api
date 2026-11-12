# LMS — Leave Management System (training scaffold)

> **Training Artifact — Not for Production Use.** Synthetic data only.

The Day 1 example of the GICTA iDCMS training. LMS mirrors iDCMS module for module:

| LMS module (`gm.gov.lms.*`) | Mirrors in iDCMS | Branch |
|---|---|---|
| `request` — leave application submission | memo submission | `02-leave-request` |
| `review` — review & approval with return loop | memoReview | `03-leave-review` |
| `tracking` — balances, escalation, history | decision & action tracking | `04-leave-tracking` |
| `workflow` — Flowable engine (BPMN in `resources/processes`) | `workflow` | — |
| `common` — shared events, audit, files, notifications | `common` | — |

Branch `01-setup` is the scaffold: libraries, empty module packages, Liquibase, security
and one dummy API. The later branches are the starting points for the assignments.

## Stack
Java 25 · Spring Boot 4.1 · Spring Modulith 2.1 · Flowable 8 · PostgreSQL 18 + Liquibase ·
Keycloak (hosted, OIDC) · MapStruct + Lombok · springdoc · Testcontainers 2 · Mailpit.

## Run it

Prerequisites: Java 25 and Docker. If you use Docker Desktop, the project folder must be
listed under *Settings → Resources → File sharing* (compose mounts `db/init`).

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

The `local` profile starts `compose.yaml` (Postgres + Mailpit) with the app and stops it
on shutdown — no manual `docker compose up`.

| What | Where |
|---|---|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Mailpit inbox | http://localhost:8026 (SMTP on 1026) |
| Postgres | localhost:5433, db/user/password `lms` |

Ports differ from idcms (5432 / 1025 / 8025) so both projects can run side by side.

```bash
./mvnw verify    # all tests; Testcontainers starts its own Postgres
```

## Database layout — nothing in `public`
- `db/init/01-schemas.sql` creates one schema per module (`request`, `review`,
  `tracking`, `workflow`, `common`). It is the DBA step: compose runs it on an empty
  volume, Testcontainers runs it in tests, a DBA runs it in prod.
- Liquibase only creates tables (`common.event_publication`, `common.system_info`); its
  own `databasechangelog*` tables live in `common`.
- Flowable creates its `ACT_*` tables itself, in `workflow` (its connections use
  `search_path TO workflow`) — 32 tables: repository (`ACT_RE_*`), runtime (`ACT_RU_*`),
  history (`ACT_HI_*`), general (`ACT_GE_*`) and engine internals.
- Flowable keeps **no identity**: `flowable.idm.enabled: false` (no `ACT_ID_*` user/group/
  password tables) — Keycloak authenticates, Spring Security authorizes. Candidate groups
  and assignees in `ACT_RU_IDENTITYLINK` are just Keycloak role names / usernames as
  strings. The event registry (Kafka/JMS channels) is off too.
- Changed the init script, or the volume predates it? Reset once: `docker compose down -v`.

## Profiles
| File | Used for |
|---|---|
| `application.yaml` | shared settings; Docker Compose support off |
| `application-local.yaml` | your machine: compose on, hosted Keycloak, Mailpit |
| `application-prod.yaml` | deployed: every value from env vars (`DB_URL`, `KEYCLOAK_ISSUER_URI`, `MAIL_*`, …), Swagger off — activate with `SPRING_PROFILES_ACTIVE=prod` |

## Try it in Swagger UI (username / password — training only)

Run with the `local` profile, open http://localhost:8080/swagger-ui.html, then:

1. *Authorize* → under **keycloak (OAuth2, password)** enter a Keycloak username and
   password (e.g. `testemployee`). `client_id` is prefilled (`leave-mgmt-public`); leave
   `client_secret` empty.
2. *Authorize* → *Close*. Every request now sends `Authorization: Bearer …`.
3. `GET /api/v1/leaves/whoami` → *Try it out* → *Execute* → your username and roles (e.g. `["EMPLOYEE"]`).

This uses the OAuth2 password grant (Keycloak *Direct access grants* on the client). It is
deprecated in OAuth 2.1 and only for local training — in a real app use the
Authorization Code + PKCE login below. Swagger UI is switched off in prod.

## Try the dummy API from Postman (OIDC login)

Keycloak: `https://alpha-idp.oss.net.bd`, realm `Leave-MGMT-Test`, public client
`leave-mgmt-public` (Authorization Code + PKCE).

In Postman, *Authorization → OAuth 2.0 → Configure New Token*:

| Field | Value |
|---|---|
| Grant type | Authorization Code (With PKCE) |
| Callback URL | `https://oauth.pstmn.io/v1/callback` (tick *Authorize using browser*) |
| Auth URL | `https://alpha-idp.oss.net.bd/realms/Leave-MGMT-Test/protocol/openid-connect/auth` |
| Access Token URL | `https://alpha-idp.oss.net.bd/realms/Leave-MGMT-Test/protocol/openid-connect/token` |
| Client ID | `leave-mgmt-public` (no secret) |
| Code Challenge Method | SHA-256 |
| Scope | `openid` |

*Get New Access Token* → log in → *Use Token*. The callback URL must be an allowed
redirect URI on the client.

| Request | Without token | Wrong role / no role | Keycloak role `EMPLOYEE` |
|---|---|---|---|
| `GET /api/v1/leaves/whoami` | 401 | 200 + username/roles | 200 + username/roles |
| `GET /api/v1/leaves/ping-employee` | 401 | 403 | 200 |

## Module rules
- Each module is a package with a `package-info.java` (`@ApplicationModule`) listing its
  `allowedDependencies`; other modules may use only its `api` sub-package.
- `ModularityTests` runs `ApplicationModules.of(LmsApplication.class).verify()` — a
  boundary violation fails the build.
- Every controller method has `@PreAuthorize`. Roles are Keycloak **realm roles**
  (`realm_access.roles`) with plain names (`EMPLOYEE`, `SUPERVISOR`, `HR_ADMIN`) — create
  them under *Realm roles* and assign them under *Users → Role mapping*, not on the client. `SecurityConfig` adds the `ROLE_`
  prefix, so they become `ROLE_EMPLOYEE`, … and `hasRole('EMPLOYEE')` matches. Keycloak's
  built-in roles (`offline_access`, `uma_authorization`, `default-roles-…`) and client
  roles are skipped, so `whoami` returns e.g.
  `{"username": "testemployee", "roles": ["EMPLOYEE"]}` — or `"roles": []` for a user with
  no role.

## Working with a coding agent
The feature set (roles, validation rules, lifecycle) is in [`CLAUDE.md`](CLAUDE.md).

- `CLAUDE.md` — the rules every agent (and human) follows here.
- `./mvnw verify` is the single gate: all tests, module boundaries, coverage (JaCoCo,
  starts at 50% line, report in `target/site/jacoco/index.html`).
- Decisions: [`docs/adr/`](docs/adr/) — why the design is the way it is.
- Spec-driven loop, one GitLab task (one small slice) at a time:
  1. `/write-spec #N` — discuss the requirement, then write `specs/<module>/NN-<slice>.md`
     from [`specs/_template.md`](specs/_template.md); a human approves it and commits `refs #N`
  2. `/implement-spec specs/<module>/NN-<slice>.md` — plan, TDD per acceptance criterion,
     `./mvnw verify`, then a summary in chat (no commit)
  3. a human checks the code in the editor and commits `refs #N`
  4. `/review-change #N` in a fresh session — reviews every `refs #N` commit, reports issues only
  5. a human decides, each fix is its own `refs #N` commit, then push
