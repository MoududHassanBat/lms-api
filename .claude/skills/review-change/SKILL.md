---
name: review-change
description: "Review every commit of one GitLab task (refs #N) against its spec, the CLAUDE.md Feature set, the ADRs and the CLAUDE.md rules, as an independent reviewer. Reports a numbered summary of issues, never edits. Use when asked to review a task, a slice or 'the change', e.g. '/review-change #4'."
---

# Review a task's change

You are the reviewer, not the author. Start from the code and the spec — not from the
author's explanation or chat history.

Input: the task number, e.g. `#4`. Ask for it if missing.

## 1. Gather
- Base branch: `01-setup` (the scaffold every module branch starts from; `main` here is
  only the Initializr commit).
- Uncommitted changes are not reviewed: if `git status --short` shows any, say so and ask
  the human to commit first (or confirm they are unrelated).
- The task's commits — exact match, so `#4` does not pick up `#40`:
  `git log --reverse --format='%h %s' -E --grep='refs #N([^0-9]|$)' 01-setup..HEAD`
  Review each with `git show <hash>` (this includes fix commits made after an earlier
  review).
- Also list commits in `01-setup..HEAD` with no `refs #` at all — report them, do not review
  them.
- The spec with **Issue: #N** (`grep -rl 'Issue:\*\* #N' specs/`), `CLAUDE.md` (including
  its *Feature set*), and `docs/adr/`.
- Run `./mvnw verify` and note the result.

## 2. Check
**Against the spec**
- Every acceptance criterion has a test that would fail without the change, and asserts
  the outcome (status, body, DB state, audit) — not just "no exception".
- Nothing outside the spec's scope was built.

**Against the plan and the ADRs**
- The code matches the *Feature set* in `CLAUDE.md` and the accepted ADRs. Code that matches the
  spec but contradicts the plan is a finding.
- A plan change is in the same commit as its spec; an architectural change has an ADR
  (`docs/adr/README.md` says which); no accepted ADR was edited (other than its status
  set to Superseded).

**Against `CLAUDE.md`**
- Every new controller method has `@PreAuthorize` with the right role (`EMPLOYEE`,
  `SUPERVISOR`, `HR_ADMIN`); object-level checks (an employee sees only their own
  requests) are in the service.
- No code sets an entity `status` directly — only Flowable listeners do (the constructor
  setting the first lifecycle state, e.g. `DRAFT`, is the one allowed exception — ADR 0001).
- Module boundaries: other modules used only through their `api` package; no new
  `allowedDependencies` without a reason; no Flowable API outside `workflow`.
- Database: new tables in the module's own schema via a new Liquibase changeSet; no
  cross-schema FKs/joins; no edited applied changeSet.
- Audit: every command and sensitive read audited in the same transaction; no free text
  or file content in the audit log.
- API: `/api/v1`, DTOs named `…Request`/`…Response`, errors as `ProblemDetail`.
- Jackson 3 (`tools.jackson.*`) only; `@MockitoBean`, not `@MockBean`.
- No weakened checks: coverage minimum, disabled/deleted tests, loosened security.

## 3. Report — do not fix
Start with the commits reviewed (hash + subject) and any commits without `refs #`.
Then a numbered list of issues, most severe first. For each:
- file:line
- what is wrong and which spec criterion, Feature set rule, ADR or `CLAUDE.md` rule it breaks
- a concrete failure scenario (input → wrong result)
- severity: **must fix** / **should fix** / **question**

End with the `./mvnw verify` result. If there are no issues, say so plainly. The human
decides what gets fixed; each fix is its own commit with `refs #N`, and can be reviewed
again with the same command.
