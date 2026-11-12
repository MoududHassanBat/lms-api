# 0001 — The initial status is set by the entity constructor

**Status:** Accepted
**Date:** 2026-11-10
**Issue:** #1

## Context
`CLAUDE.md` rule 2: Flowable owns state — entity `status` fields are projections updated
only from Flowable listeners, never by controllers or services. But a new leave request
must be `DRAFT` as soon as it is saved, and the `leaveReview` process only starts when the
employee submits it. The same holds for every entity with a lifecycle.

## Decision
We will let the entity constructor set the **first** lifecycle state (e.g. `DRAFT`). That
is the only status assignment outside Flowable; every later change goes through a
Flowable task or signal and its listener. `CLAUDE.md` rule 2 and the `review-change`
checklist state this exception.

## Consequences
- \+ Creating a draft needs no process instance: simpler, and a draft that is never
  submitted leaves nothing in the `workflow` schema.
- \+ The rule stays easy to check in review: one allowed place (the constructor).
- − A `DRAFT` leave request has no process instance; code must not assume one exists
  before submit.
- − Submitting must start the process and move the status in one transaction.
