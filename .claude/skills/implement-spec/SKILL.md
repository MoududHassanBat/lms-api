---
name: implement-spec
description: "Implement one approved feature spec from specs/ test-first (strict TDD), keeping ./mvnw verify green, and end with a summary for the human to check — no commit unless asked. Use when asked to implement, build or code a spec, slice or acceptance criterion, e.g. 'implement specs/request/01-create-draft.md'."
---

# Implement a spec (TDD)

Input: the path of one spec, e.g. `specs/request/01-create-draft.md`. Ask for it if missing.

## 1. Read before touching code
- Read the spec, `CLAUDE.md` (its *Feature set* section is the source the spec comes
  from: roles, validation rules, status lifecycle) and the ADRs in `docs/adr/`.
- The spec must have **Status: APPROVED**, an **Issue: #N**, and an empty **Open
  questions** list. If the issue number is missing, ask for it. If anything else is
  missing or ambiguous, list it and **stop** — do not guess business rules.
- Check you are on the spec's branch (`02-leave-request`, …) — `git branch --show-current`.

## 2. Plan
List, briefly: the tests (one or more per acceptance criterion), then the classes,
Liquibase changeSet, BPMN change and `package-info.java` dependencies needed. Check the
plan against the `CLAUDE.md` architecture rules, its *Feature set* and the ADRs. Wait
for the human to approve it.

If the spec turns out to conflict with the plan or an ADR, **stop and report it**. Never
change the *Feature set* or an ADR to make the code fit; that is a spec change
(`/write-spec`) for the human to decide.

## 3. Build — one acceptance criterion at a time
For each criterion:
1. **Red** — write the test, run only it (`./mvnw test -Dtest=<Class>#<method>`), and show
   that it fails for the expected reason.
2. **Green** — write the simplest production code that makes it pass.
3. **Refactor** — clean up with the tests green.

Tests assert observable outcomes: HTTP status, body, database state, and the audit record
(`DENIED` for refused access). Tests never call Keycloak — use `jwt()` and a mocked
`JwtDecoder`.

## 4. Verify
Run `./mvnw verify` (all tests, `ModularityTests`, `SchemaLayoutTests`, coverage gate).
Fix every failure by fixing the code or the test's real cause.

**Never** lower the coverage minimum, disable or delete a test, add `allowedDependencies`
to get around a boundary error, or loosen `@PreAuthorize` to make the build pass. If a
rule itself looks wrong, stop and say so — changing it is a separate, human-reviewed
change.

## 5. Summary for the human — then stop
Do **not** commit or push; the human checks the code in the editor first. Reply with:
- **Acceptance criteria** — each AC → the test(s) that prove it.
- **Files** — created and changed, grouped by layer (web, service, domain, repository,
  Liquibase, tests).
- **Verify** — the `./mvnw verify` result, test count and line coverage.
- **Not verified** — e.g. not tried in Postman, a Keycloak role or claim not set up yet.
- **Decisions** made while building that the spec did not state (should be few).
- **Next step** — "Check the changes, then commit `refs #N` (or ask me to)", followed by
  `/review-change #N` in a fresh session.

Set the spec's **Status** to DONE only when `./mvnw verify` is green. Commit or push only
when the human asks, always with `refs #N`.
