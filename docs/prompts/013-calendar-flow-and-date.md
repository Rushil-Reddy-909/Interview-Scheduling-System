# 013 — Calendar flow and interview date

Date: 2026-11-16

Type: plan

## Prompt

right the interview should also hold the date of interview field, right now nothing specifies when interview will be other than time.

My idea for interview scheduling right now, is that the recruiter chooses the candidate and interviewer, based on that a calendar shows up first, upon clicking a date, it should show free times for both parties and the recruiter chooses a suitable common free time and duration.

## Outcome

- The plan first proposed deriving the date from `starts_at` and not adding a column. The user asked for an explicit date field, so the docs store `interview_date` instead. The service must keep it equal to the UTC date of `starts_at`.
- Recruiter flow: pick candidate and interviewer, then a weekday calendar, then a date that lists common free gaps, then a create form with start and duration that fits inside one gap.
- Common free time is calculated when needed from the blocking intervals of both people within `[09:00, 17:00)` UTC. It is not stored.
- Replacements use the same calendar flow, and the interview's own current slot does not block its replacement.
- The plan was written in plan mode and the implementation waits for approval.
