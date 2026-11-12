---
name: write-spec
description: "Turn a raw requirement for one GitLab task into a spec in specs/, by discussing it with the human first and writing only after the decisions are final. Checks the requirement against the CLAUDE.md Feature set and docs/adr/ and stops on conflicts. Use when asked to write, draft or create a spec, e.g. '/write-spec #4 create a draft leave request'."
---

# Write a spec (discuss first, write last)

Input: the task number (`#4`) and the raw requirement (a sentence, the issue text, notes).
Ask for whichever is missing. Never write code, approve a spec, or commit.

## 1. Read
- `CLAUDE.md` (its *Feature set* is the plan: roles, module features, validation,
  lifecycle), every ADR in `docs/adr/`, `specs/_template.md`, and the existing
  specs of the module (`specs/<module>/`) — for numbering, and so slices do not overlap.

## 2. Discuss — no files yet
Reply in chat with:
1. **The requirement, restated** in one or two sentences.
2. **Proposed decisions**: request/response fields and limits, allowed and refused roles
   (403), invalid input (400), state changes (only via Flowable; first state per ADR 0001),
   data (tables/columns in the module's schema), audit, module boundaries.
3. **Out of scope** — what this slice will not do (often another task's work).
4. **Conflicts** with the plan or an ADR, each one quoted with its section. **Do not
   resolve a conflict yourself.** The human decides:
   - change the requirement — the plan stays; or
   - change the plan — update the *Feature set* in `CLAUDE.md`, and if the change is
     architectural (see `docs/adr/README.md`), add an ADR.
5. **Questions** — short, numbered, only what the plan does not already answer.

Keep discussing until the human says the decisions are final. In plan mode, this
discussion is the plan; write the files after the plan is approved.

## 3. Write
- `specs/<module>/NN-<slug>.md` from the template: **Status: DRAFT**, **Issue: #N**,
  Branch, acceptance criteria as Given/When/Then with observable outcomes (status code,
  body, DB state, audit), API table, Rules, **Plan impact**, Out of scope, Open questions.
- If the plan changes: edit the *Feature set* in `CLAUDE.md` in the same change, and for an
  architectural change add `docs/adr/NNNN-<title>.md` from the template, status
  **Proposed**, Issue #N, and add it to the index in `docs/adr/README.md`.
- Anything still undecided goes under **Open questions** (`/implement-spec` will refuse to
  start until it is empty).

## 4. Hand over
End with a short summary: the files written, the decisions taken, and the next step for
the human: *review the spec, set Status to APPROVED (and any new ADR to Accepted), commit
`refs #N`*. Then `/implement-spec specs/<module>/NN-<slug>.md`.
