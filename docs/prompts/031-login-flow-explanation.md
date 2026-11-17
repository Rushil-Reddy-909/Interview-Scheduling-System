# 031 — Login flow and Spring defaults

Date: 2026-11-17

Type: ask

## Prompt

So i want to understand the login flow, in security/ there are 3 java files, i understand some parts like roles having ROLE_ prefix, authorize http requests, but explain them about the flow and default spring config settings.

Like UserDetailsService, AuthenticationSuccessHandler, are these the default that spring sets up and we are supposed to override these for our own custom setup?

## Outcome

- Explained the session login flow: `POST /login` loads the user once, checks the BCrypt hash, stores the `Authentication` on the session, then redirects by role.
- `UserDetailsService` and `AuthenticationSuccessHandler` are interfaces. A `UserDetailsService` bean replaces Boot's in-memory `user`. A success handler replaces form login's saved-request redirect.
- A `SecurityFilterChain` bean replaces Boot's default chain, so form login, public URLs, logout, and CSRF have to be stated again. HTTP Basic is left out.
