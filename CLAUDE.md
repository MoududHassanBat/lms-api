# CLAUDE.md — LMS (Leave Management System) training scaffold

Guidance for Claude Code (and any coding agent) working in this repository.

## What this project is

**Training Artifact — Not for Production Use.** LMS is the Day 1 example of the GICTA
iDCMS training: a small Leave Management System built with exactly the same structure,
libraries and rules as iDCMS, so trainees practise here and then apply the same patterns
to iDCMS. Synthetic data only — never real people, real credentials or live systems.

Three roles, each the mirror of an iDCMS role:

| Role (Keycloak realm role) | Does | Mirrors in iDCMS |
|---|---|---|
| `EMPLOYEE` | applies for leave, revises a returned request, sees the status of own requests | Ministry Submitter |
| `SUPERVISOR` | reviews requests: approve, reject, or return for revision | Cabinet Secretariat |
| `HR_ADMIN` | dashboard of all requests, leave-balance management, reports | Secretary to Cabinet |

Three business modules form one pipeline, each the mirror of an iDCMS module:

| Module (`gm.gov.lms.*`) | Mirrors in iDCMS | Branch |
|---|---|---|
| `request` — leave application submission | memo submission | `02-leave-request` |
| `review` — review & approval workflow with the return loop | `memoReview` process | `03-leave-review` |
| `tracking` — balances, dashboard, history, audit | decision & action tracking + dashboard | `04-leave-tracking` |

Platform modules: `workflow` (the central Flowable engine, all BPMN), `common` (shared
events, audit, file storage, notifications, errors), `config` (security, web wiring).

### Feature set (the spec source for every slice)

**1. `request` — leave application submission**
- Form: leave type (`ANNUAL` / `SICK` / `CASUAL`), start and end date, reason, optional
  supporting document upload (e.g. a medical certificate).
- Validation: no overlap with the employee's other requests, enough leave balance, no
  dates in the past (Bean Validation on the DTO + service-level checks).
- Save as `DRAFT` → submit.
- "My requests": the employee's own requests with a status badge.
- Shows: DTO + Bean Validation, Controller → Service → Repository, file storage
  (`FileStorage`, local filesystem).

**2. `review` — review & approval workflow**
- Supervisor review queue: submitted requests, oldest first. Only `SUPERVISOR` sees it.
- Three decisions: approve / reject / return for revision — a comment is mandatory on
  return.
- A returned request is revised by the employee and resubmitted — the return loop.
- Flowable changes the status, not the controller (rule 2).

**3. `tracking` — tracking & dashboard**
- HR dashboard: counts by status (pending / approved / rejected); reviews pending for more
  than 3 days are flagged (escalation timer).
- Leave balance per employee, deducted when a request is approved (event from `review`).
- Searchable history: who took what leave, when.
- Notification + audit record on every status change (who, when, what).

Status lifecycle (owned by Flowable, see rule 2):
```
DRAFT → SUBMITTED → UNDER_REVIEW → APPROVED | REJECTED
                         ↓   ↑
                      RETURNED (employee revises, resubmits)
```
Deliberately missing compared with iDCMS: meetings/agenda. In LMS one supervisor decides;
in iDCMS a Cabinet meeting decides — that difference is iDCMS Module 2.

## Branches and how work is done here

- `01-setup` — the scaffold: libraries, empty module packages, Liquibase, security, one
  dummy API. `02-leave-request`, `03-leave-review`, `04-leave-tracking` start from it and
  are the assignment branches — build each module on its own branch, in that order.
- Work is tracked in GitLab: one parent issue per module, one child task per slice
  (e.g. "Create draft leave request"). One task = one spec = one slice.
- Commit messages: `refs #<task>` (setup and harness work: `refs #1`).
- **AI drafts, humans decide**: a human reviews every AI-generated change to logic,
  security, data model or integrations before it is pushed. The agent commits or pushes
  only when the human asks.
- Don't push, force-push, or rewrite branches unless the user asks.
- **Spec-driven loop**, per task:
  1. `/write-spec #N` — discuss the raw requirement, then write
     `specs/<module>/NN-<slice>.md` (Status DRAFT).
  2. The human reviews the spec, sets Status APPROVED and commits `refs #N`.
  3. `/implement-spec specs/<module>/NN-<slice>.md` — TDD, `./mvnw verify`, then a summary
     in chat; no commit.
  4. The human checks the code in the editor and commits `refs #N` (or asks the agent to).
  5. `/review-change #N` in a fresh session — reviews every `refs #N` commit and reports
     issues only.
  6. The human decides; each fix is its own commit `refs #N`; then push.
- **Spec, plan and code never disagree.** Here the plan is the *Feature set* above. A
  requirement that conflicts with it or with an ADR is stopped and decided by a human —
  never resolved silently. A Feature set change goes in the same commit as its spec; an
  architectural change also gets an ADR in `docs/adr/` (see its README). Never edit an
  accepted ADR — supersede it with a new one.
- **Never lower thresholds, disable or delete tests, or weaken rules (`package-info.java`
  dependencies, coverage, security) to make a change pass.** Changing a quality rule is its
  own change, explained and reviewed by a human.

## Stack

- Java 25, Spring Boot 4.1.2 (Spring Framework 7, Jakarta EE 11, Jackson 3, Hibernate 7),
  **one Maven project**; modules are Java packages verified by Spring Modulith 2.1
- Flowable 8.0.0 (`flowable-spring-boot-starter-process`, process engine only)
- PostgreSQL 18 + Liquibase; MapStruct 1.6 (Spring component model) + Lombok;
  springdoc-openapi 3.2; Bean Validation
- Keycloak 26.8, **hosted** at `https://alpha-idp.oss.net.bd`, realm `Leave-MGMT-Test`
  (case-sensitive), public client `leave-mgmt-public` (Authorization Code + PKCE)
- Spring Boot Docker Compose support (Postgres + Mailpit, local only); Testcontainers 2
- Attachments on the local filesystem behind a `FileStorage` interface; Mailpit for dev
  email
- Out of scope: observability (Actuator, metrics, tracing) and resilience (retry,
  circuit breaker)

## Commands

```bash
./mvnw verify                                              # THE gate: all tests + module boundaries + coverage
./mvnw test -Dtest=ModularityTests                         # module boundaries only
./mvnw spring-boot:run -Dspring-boot.run.profiles=local    # run the app
```

- `./mvnw verify` must be green before every commit. It runs the JaCoCo coverage check
  (report: `target/site/jacoco/index.html`); `./mvnw test` skips the gate.
- The `local` profile starts `compose.yaml` (Postgres, Mailpit) with the app and stops it
  on shutdown — never run `docker compose up` by hand.
- API http://localhost:8080 · Swagger `/swagger-ui.html` · Mailpit http://localhost:8026
  (SMTP 1026) · Postgres `localhost:5433`, db/user/password `lms`. Ports differ from
  idcms (5432/1025/8025) so both can run; both APIs use 8080, so run one at a time.
- Dummy API: `GET /api/v1/leaves/whoami` (any token) and `GET /api/v1/leaves/ping-employee`
  (Keycloak role `EMPLOYEE`). Postman OIDC setup is in `README.md`.

## Architecture rules (do not violate)

1. **Module boundaries.** Each module's `package-info.java` declares
   `@ApplicationModule(allowedDependencies = …)`; other modules may use only its `api`
   sub-package (`@NamedInterface("api")`). Current allowed dependencies:
   `request → common, workflow::api` · `review → common, workflow::api, request::api` ·
   `tracking → common` · `workflow → common` · `common`, `config` → nothing.
   `ModularityTests` runs `ApplicationModules.of(LmsApplication.class).verify()`; a
   violation fails the build. Adding a dependency means editing `package-info.java`
   deliberately and having a human review it — never work around it.
2. **Flowable owns state.** Entity `status` fields are projections updated only from
   Flowable listeners. Controllers and services never set `status`; to move state,
   complete a task or signal the process. The one exception is the initial value: a new entity
   starts in its first lifecycle state (e.g. `DRAFT`), set by its constructor; every later
   change goes through Flowable (ADR 0001).
3. **Central workflow engine.** All BPMN lives in `src/main/resources/processes/`
   (e.g. `leaveReview.bpmn20.xml`). Business modules talk to the engine only through
   `workflow.api` (a `WorkflowApi` to start processes / complete tasks / list tasks, plus
   SPI interfaces such as `ProcessStateListener` that the owning module implements).
   `workflow` never imports a business module, BPMN holds no business rules, and no other
   module uses the Flowable API (`RuntimeService`, `TaskService`, …).
4. **Identity is Keycloak's.** Keycloak authenticates; Spring Security authorizes.
   Flowable's IDM engine is off (`flowable.idm.enabled: false`) — never add Flowable
   users/groups. Candidate groups and assignees are plain Keycloak role names and
   usernames (`preferred_username`).
5. **Security.** Every controller method has `@PreAuthorize`. Roles are Keycloak
   **realm roles** (`realm_access.roles`): `EMPLOYEE`, `SUPERVISOR`, `HR_ADMIN`. Client
   roles (`resource_access`) are ignored — never create application roles on a client.
   Keycloak names are plain (`EMPLOYEE`);
   `config/SecurityConfig.java` adds the `ROLE_` prefix, so code checks
   `hasRole('EMPLOYEE')` (authority `ROLE_EMPLOYEE`). Never create a Keycloak role that
   already starts with `ROLE_` — it would become `ROLE_ROLE_...`. Keycloak's built-in roles
   (`offline_access`, `uma_authorization`, `default-roles-<realm>`) are skipped; `whoami`
   returns only application roles, named as in Keycloak. Object-level checks (an employee
   sees only their own requests) live in the service, not only in the URL.
6. **Database.** One schema per module (`request`, `review`, `tracking`, `workflow`,
   `common`); **nothing in `public`**.
   - Schemas are created outside the app by `db/init/01-schemas.sql` (compose runs it on
     an empty volume, Testcontainers runs it, a DBA runs it in prod). Liquibase never
     creates schemas.
   - Every table change is a new Liquibase changeSet, included from
     `db/changelog/db.changelog-master.yaml`; never edit a changeSet that has run on a
     shared database. Entities use `@Table(schema = "<module>")`.
   - No shared tables, no cross-schema foreign keys, joins or views — reference other
     modules' records by ID, so any module could become its own service.
   - Flowable creates its own 32 tables in `workflow`; never write changeSets for them.
7. **Events.** Cross-module reactions (e.g. tracking deducting a balance when leave is
   approved) use domain events in `common` with `@ApplicationModuleListener`. The Spring
   Modulith event publication registry (`common.event_publication`) is the outbox — no
   hand-written outbox tables, no broker. Delivery is at-least-once, so every listener
   must be idempotent.
8. **Audit.** Every status change and mutating command writes an audit record through
   `common` (`AuditApi` / `@Audited`), **in the same transaction** — if the audit insert
   fails, the action fails. Never bypass command services with repository writes from
   controllers.
   - Record action, entity, actor, outcome, comment, changed field *names*. **Never** free
     text such as the leave reason, review comments' content or file contents.
   - Only `common` writes the audit table; it is append-only.
   - Tests assert the audit record for every command and `DENIED` for refused access.
9. **API contract.** Paths under `/api/v1` (Spring Framework 7 API versioning,
   path-segment strategy); errors as RFC 9457 `ProblemDetail`
   (`application/problem+json`); pagination `page,size,sort`. Every response carries
   `X-API-Version` (from `api.version`, added by `config/ApiVersionHeaderFilter`).

## Conventions

- Packages `gm.gov.lms.<module>.{api, domain, service, repository, web, web.dto}`;
  DTOs end in `Request` / `Response`; mapping with MapStruct; Lombok only for entities and
  DTOs.
- JSON is Jackson 3 (`tools.jackson.*`), not `com.fasterxml.jackson.databind`.
- **Strict TDD**: no production code without a failing test first (red → green →
  refactor); bug fixes start with a reproducing test. Show the failing run before the fix.
- Tests: JUnit 6; integration tests import `TestcontainersConfiguration` (Postgres 18 +
  the same init script) — never the dev compose database; mock beans with `@MockitoBean`
  (`@MockBean` is gone in Boot 4); tests never call Keycloak — use
  `SecurityMockMvcRequestPostProcessors.jwt()` and mock `JwtDecoder` in `@SpringBootTest`s.
  Web tests: `@WebMvcTest(...)` + `@Import(SecurityConfig.class)`. Every BPMN path,
  including the return loop and the escalation timer, gets a Flowable test.
- Keep `SchemaLayoutTests` and `ModularityTests` green — they encode rules 1, 4 and 6.
- Coverage gate (JaCoCo, in `verify`): 50% line for the whole app today, excluding
  `LmsApplication`. It goes **up** as modules get code (target ≥70% overall, ≥80%
  domain/service) — never down.

## Configuration

| File | Purpose |
|---|---|
| `application.yaml` | shared: `api.version`, compose off, virtual threads, `ddl-auto: validate`, schema settings, Flowable settings |
| `application-local.yaml` | compose on, hosted Keycloak issuer, Mailpit |
| `application-prod.yaml` | everything from env vars (`DB_URL`, `KEYCLOAK_ISSUER_URI`, `MAIL_*`, …), Swagger off; `SPRING_PROFILES_ACTIVE=prod` |

Never set `spring.profiles.active` in `application.yaml`. Bump `api.version` there — the
startup banner and the response header both read it.

## Gotchas

- Flowable writes unqualified table names, so every pooled connection runs
  `SET search_path TO workflow` (no `public` fallback — a missing schema fails loudly).
  Hibernate (`default_schema: common`) and Liquibase (`default-schema` /
  `liquibase-schema: common`) are qualified explicitly so they don't depend on it.
- The init script runs only on an empty Postgres volume, and Flowable never drops
  tables. After changing `db/init` or Flowable engine flags: `docker compose down -v`.
- Docker Desktop: the project folder must be under *Settings → Resources → File sharing*,
  or compose can't mount `db/init` ("mounts denied").
- Postgres 18 image: the volume mounts at `/var/lib/postgresql`, not `.../data`.
- Boot 4 is modular: test slices and starters have new names (`spring-boot-starter-webmvc`,
  `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`, Testcontainers 2's
  `org.testcontainers.postgresql.PostgreSQLContainer`). Most examples online are Boot 3.
- Flowable 8.0.x is built on Boot 4.0; Boot 4.1 works here (context test proves it). If a
  Boot upgrade breaks it, fall back to Boot 4.0.x.
- `ddl-auto: validate` checks every entity against the real tables at startup — a
  missing changeSet shows up as a startup failure, not later.
- Virtual threads are on (`spring.threads.virtual.enabled=true`). On Java 25
  `synchronized` no longer pins carrier threads (JEP 491), but thousands of virtual
  threads can still exhaust HikariCP — size the DB pool deliberately.
- Flowable 8 stores JSON variables with Jackson 3; if an old Jackson 2 payload breaks,
  `flowable.variable-json-mapper=jackson2` is the fallback.
- Keycloak community has no LTS; keep the hosted server on the latest 26.x minor.
- Liquibase 5 Community is licensed FSL-1.1-ALv2 (fine for our own app; each release
  reverts to Apache 2.0 after two years).
- Swagger UI login (`config/OpenApiConfig`, `springdoc.swagger-ui.oauth.client-id` in
  `application-local.yaml`) uses the OAuth2 **password grant**: training only. It needs
  *Direct access grants* on `leave-mgmt-public` and `http://localhost:8080` allowed in its *Web
  origins* (the browser calls Keycloak's token endpoint). Never enable it in prod; the token
  URL is derived from `issuer-uri`.
