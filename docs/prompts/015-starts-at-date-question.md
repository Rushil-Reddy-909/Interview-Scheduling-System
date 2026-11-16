# 015 — Does starts_at include the date

Date: 2026-11-16

Type: ask

## Prompt

wait so in previous schema, did starts_at also include the date field or only the timestamp? Also does it mena request starts at would include the full date + time?

## Outcome

- Answered: `starts_at`, `ends_at`, and `requested_starts_at` are UTC timestamps. Each holds the full date and time, so `interview_date` duplicates information already in `starts_at`.
- `requested_starts_at` also holds a full date and time.
- The user chose to remove `interview_date`. The date comes from `starts_at`, so `docs/design.md` and `docs/schema.md` no longer have that column.
