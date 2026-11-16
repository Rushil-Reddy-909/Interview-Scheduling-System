# 016 — Completed and cancelled

Date: 2026-11-16

Type: ask

## Prompt

add and commit everything until now. 

after that, answer this - Should we have a completed status for interviews or would it be redundant if we take current time as reference and only fetch future ones based on that? Also how would cancelled be implemented, does it mean that specific interview is cancelled (like when rescheduled, but we only store active interviews, not past ones and there is no audit log) or that user is withdrawing from any interviews hence forth and we should cancel all their interviews?

## Outcome

- Committed the schema, seed, and calendar-flow docs as `2b951a5` before answering. This prompt was not part of that commit.
- Recommended no `COMPLETED` status for now. Upcoming versus past is `ends_at` compared with the current time. Past rows stay in the table.
- Recommended `CANCELLED` as one interview's status, with the row kept and excluded from free-time checks. Cancelling every interview for a person would be a separate action. Design docs were not changed.
