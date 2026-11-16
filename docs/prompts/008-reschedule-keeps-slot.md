# 008 — Reschedule keeps the slot

Date: 2026-11-16

Type: build

## Prompt

right update the docs and agents.md, reschedule_Requested means the existing timeslot is still valid and will only change after recruiter changes it

## Outcome

- `RESCHEDULE_REQUESTED` keeps the official `[starts_at, ends_at)` booked. Those columns change only when the recruiter saves a replacement. The candidate's requested start does not reserve a second range.
- The overlap check in `docs/design.md` and `docs/schema.md` now includes `AWAITING_CANDIDATE`, `ACCEPTED`, and `RESCHEDULE_REQUESTED`.
- `docs/schema.md` also records what the `starts_at` indexes are for, and that H2 enforces non-overlap in the service transaction rather than with a unique index.
- `AGENTS.md` now has prompt type `ask`, and says Ask-mode prompts are logged before the turn ends, or on the next turn that can write files.
- Logged the previous question as `docs/prompts/007-indexes-and-overlap.md`.
