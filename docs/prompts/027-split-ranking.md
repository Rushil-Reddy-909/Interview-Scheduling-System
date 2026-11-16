# 027 — Split the ranking by event

Date: 2026-11-16

Type: build

## Prompt

In ranking section in docs/design.md , you listed 6 points, does a reschedule follow 3,5,6 or only 1,2,4? if reschedule follows only the ones marked as Reschedule only: then split the ranking as 2 types, one for each kind of event

## Outcome

- Answered: a reschedule uses all six keys. A create uses only earlier start, lower load, and lower interviewer id.
- Split the Ranking section in `docs/design.md` into two complete lists, one for create and one for reschedule. The single mixed list was hard to read.
- Defined "load" once, above both lists.
- Stated how several pool interviewers who are free at the requested start are ordered: lower load, then lower id. This was not written down before.
- No code was changed.
