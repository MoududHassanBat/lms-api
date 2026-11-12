# Architecture Decision Records (ADR)

An ADR records **one** important decision: what was decided and **why**. The *Feature set*
in `CLAUDE.md` describes the design as it is now; the ADRs keep the decisions that led there, so a
later reader (human or agent) can see why the code is the way it is and does not undo a
decision by accident.

## When to write one
Write an ADR when someone could later reasonably ask "why didn't you do X instead?":
- architecture or module dependencies (`package-info.java`)
- roles and RBAC
- the workflow (BPMN, who changes status)
- data ownership (which module/schema owns what)
- a rule in `CLAUDE.md`, added or changed

Do **not** write one for small details (a field length, a name, a bug fix) — those go in
the spec or the *Feature set* in `CLAUDE.md`.

## How
- Copy [`0000-template.md`](0000-template.md) to `NNNN-kebab-title.md` (next free number).
- Write it in the same commit as the spec that needs it (`refs #<task>`), together with
  the *Feature set* update in `CLAUDE.md`.
- Status: `Proposed` while the spec is in review → `Accepted` once a human approves.
- **Never edit an accepted ADR.** If the decision changes, write a new ADR and set the
  old one's status to `Superseded by NNNN` (the only edit allowed).

## Index
| # | Decision | Status |
|---|---|---|
| [0001](0001-initial-status-set-by-constructor.md) | The initial status is set by the entity constructor | Accepted |
