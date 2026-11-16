# 014 — Docs only for calendar design

Date: 2026-11-16

Type: build

## Prompt

Only update the docs, dont write any implementation code yet, we will document everything and then start implementation later.

## Outcome

- `docs/design.md` now describes the calendar flow: pick candidate and interviewer, choose a weekday, see the common free gaps for that date, then create with a start and duration inside one gap.
- `docs/schema.md` documents the common free time query and the replacement rule that excludes the interview's own current slot. The `interview_date` column that this turn first added was removed in prompt 015.
- `README.md` describes the calendar flow in its introduction.
- No Java code, configuration, or tests were changed.
- Prompts 011 to 013 were logged in order, since the earlier Ask and Plan turns could not write them at the time.
