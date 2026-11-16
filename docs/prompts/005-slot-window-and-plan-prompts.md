# 005 — Slot window and plan prompts

Date: 2026-11-16

Type: plan

## Prompt

a few things, even in plan mode, you should note document the prompt (with prompt type as plan).
Also for selecting timeslots, only weekdays 9am to 5pm should be considered, for now assume everything is UTC. Each timeslot is [start, end) so another interview can start at the same time old one ends for candidate/interviewer.

## Outcome

- Logged this prompt during plan mode. Updated `AGENTS.md` so every prompt file has a `Type` line (`plan` or `build`) and plan-mode prompts are written before the turn ends.
- Also logged the earlier design prompt, which had been skipped, as `docs/prompts/004-design-schema.md` with `Type: plan`.
- Updated the plan: slots are UTC, Monday–Friday, 09:00 inclusive through 17:00 exclusive as the end bound, and half-open `[start, end)`. A later interview may start when an earlier one ends. `docs/design.md` and `docs/schema.md` are not rewritten until the plan is executed.
