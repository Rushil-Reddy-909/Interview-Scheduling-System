# 007 — Indexes and overlap

Date: 2026-11-16

Type: ask

## Prompt

A few doubts, (even ask mode prompts should be logged. So we have indexes on (candidate, starts_at) and similarly in interviewer. Why is starts_at an index? how would it be used? Also how is timeslot non overlapping for a candidate/ interviewer enforced? is that purely server side or can we enforce it in DB level?

## Outcome

Answered without editing the project. Ask mode could not write this file; it was logged on the following turn.

- `(candidate_id, starts_at)` and `(interviewer_id, starts_at)` are lookup indexes. They serve the dashboards and the overlap range scan. They do not prevent two rows from sharing a time.
- Non-overlap is a service check. A unique index cannot express range overlap. H2 cannot add a PostgreSQL-style exclusion constraint. Concurrent bookings need the check and the write in one transaction after locking the two `app_user` rows.
- The written overlap check omitted `RESCHEDULE_REQUESTED`, which disagreed with "the original slot stays booked." Prompt 008 resolves that.
