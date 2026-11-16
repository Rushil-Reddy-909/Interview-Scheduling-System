# 017 — Session login over Basic or JWT

Date: 2026-11-16

Type: plan

## Prompt

AS for auth, im thinking of using Basic auth like email:password, so can spring interpret this and attach a header or context to that request lifecycle for the role involved or is jwt with role better to save a db fetch?

## Outcome

- Plan mode recommended form login with an HTTP session, using Spring Security's standard mechanism. The role is kept on the request's `Authentication` for the session, so the database is read once at login.
- HTTP Basic was rejected because the email and password go with every request and the design already calls for a login page. JWT was rejected for this server-rendered app because it adds signing and revocation work, and role changes stay stale until expiry.
- Prompt 019 wrote this decision into the Login section of `docs/design.md`. No application code was changed.
