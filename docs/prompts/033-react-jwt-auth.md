# 033 — React frontend with JWT

Date: 2026-11-17

Type: ask

## Prompt

suppose im building the frontend on another framework like react and im using JWT, what then?

also log these prompts

## Outcome

- Explained the JWT setup for a separate React app. `UserDetailsService` and `PasswordEncoder` stay. Form login, the session, the login page, and the success-handler redirect go away.
- Added the pieces JWT requires: a login endpoint that returns a signed token, a resource-server filter that checks `Authorization: Bearer`, stateless session policy, CORS, and 401 responses instead of login redirects.
- Logged the two earlier Ask-mode questions as `docs/prompts/031-login-flow-explanation.md` and `docs/prompts/032-future-auth-checklist.md`. No application code was changed.
