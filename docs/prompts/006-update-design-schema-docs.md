# 006 — Update design and schema docs

Date: 2026-11-16

Type: build

## Prompt

ok only update the docs in this prompt, dont start the implementation yet

## Outcome

- Rewrote `docs/design.md` from the locked plan: one `app_user` table with roles, login with no signup, admin-created users, UTC Monday–Friday 09:00–17:00 slot rules, half-open `[start, end)` intervals, and the interview flow.
- Rewrote `docs/schema.md` for `app_user` and `interview`, with the status transitions, the slot and overlap rules, and indexes.
- No application code, configuration, or tests were changed. Implementation has not started.
- `Type` is `build` because the work is in agent mode and changes project files, but it is docs only.
