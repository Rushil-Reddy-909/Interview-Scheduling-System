# 030 — Seed one user per role

Date: 2026-11-17

Type: build

## Prompt

Change the seed data function to create 1 user for each role automatically instead of only admin, it should follow the same format like role@localhost and 12345678 as password

## Outcome

- Startup inserts one user for each missing role. Emails are `admin@localhost`, `recruiter@localhost`, `candidate@localhost`, and `interviewer@localhost`.
- Every seeded user gets the password from `app.admin.password`. The local value is `12345678`, stored as a BCrypt hash. An existing role is left unchanged.
