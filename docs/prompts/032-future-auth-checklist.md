# 032 — What to write for a future auth setup

Date: 2026-11-17

Type: ask

## Prompt

So suppose im building another spring project some time in the future with my own auth setup, what all things should i keep in mind that i have to write or override?

## Outcome

- For session form login, the pieces to write are a `SecurityFilterChain`, a `UserDetailsService`, and a `PasswordEncoder`. A success handler is only needed when the default redirect is wrong.
- Session storage, CSRF, the password field name, failure redirect, logout, and session-fixation protection can stay on Spring's defaults.
- Noted the usual first-login failures: plain generated password, a short password column, encoding the hash twice, doubling the `ROLE_` prefix, and a missing CSRF field.
