# 020 — Thymeleaf clicks and ROLE_ prefix

Date: 2026-11-16

Type: ask

## Prompt

can thymeleaf support interactive pages like clicking on something does an action like in react? how would we render calendar clicking and showing free times? Also if we use spring security then each should should be prepended with ROLE_ right?

## Outcome

- Answered without changing the design or the code. Thymeleaf renders HTML on the server. A date click is a normal request that returns the same page with that date's free gaps. React-style client state is not required.
- `UserRole` stays `ADMIN`, `RECRUITER`, `CANDIDATE`, `INTERVIEWER`. Spring Security's `hasRole` looks for an authority named `ROLE_` plus that value. The prefix is added when the login builds the `Authentication`, not stored in the database, and not written twice in the template.
