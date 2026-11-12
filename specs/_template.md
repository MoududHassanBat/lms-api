# <Module> NN — <Slice name>

> Copy this file to `specs/<module>/NN-<slice>.md` (or let `/write-spec #<task>` draft it).
> One spec = one GitLab task = one small slice that a TDD loop can finish in about an
> hour. A human approves it (Status: APPROVED) before any plan or code.

**Status:** DRAFT | APPROVED | IN_PROGRESS | DONE
**Issue:** #<task> (every commit for this spec: `refs #<task>`)
**Branch:** `0N-leave-<module>`

## Goal
One or two sentences: what the user can do after this slice, and why it matters.

## Roles
Who may do it (Keycloak realm role names, without `ROLE_`), and who must be refused.

## Acceptance criteria
Each criterion becomes at least one test. Cover success, authorization, validation and
failure; assert the observable outcome (HTTP status, response body, database state,
audit record), not just "no exception".

- **AC1 — <name>**
  Given <state>
  When <action>
  Then <observable outcome>
- **AC2 — <refused access>**
  Given <a user without the role / not the owner>
  When <action>
  Then 403 `application/problem+json`, nothing changed, audit `DENIED`
- **AC3 — <invalid input>**
  Given …
  When …
  Then 400 `ProblemDetail` naming the invalid field

## API
| Method & path | Role | Request | Response |
|---|---|---|---|
| `POST /api/v1/...` | … | `XxxRequest { … }` | `201` `XxxResponse { … }` |

## Rules
- **State:** which status changes, and which Flowable task/process changes it (never the
  controller or service directly).
- **Data:** new tables/columns (module schema only, Liquibase changeSet, IDs to other
  modules — no cross-schema FKs).
- **Audit:** action name and outcome written; what must NOT be stored (free text, file
  content).
- **Module boundaries:** any other module's `api` used; any new event.

## Plan impact
One of:
- None — follows the *Feature set* in `CLAUDE.md` (and ADR NNNN, if relevant).
- Updates the *Feature set* in `CLAUDE.md` — changed in the same commit as this spec.
- ADR NNNN — an architectural decision (`docs/adr/`), written in the same commit.

## Out of scope
What this slice deliberately does not do (so the agent does not build it).

## Open questions
Anything unclear. `implement-spec` stops while this list is not empty.
