# 018 — Are we using Spring Security for auth

Date: 2026-11-16

Type: ask

## Prompt

So we are using existing spring security mechanism for auth?

## Outcome

- Answered yes: form login with Spring Security's session handling, not a custom role header, Basic auth, or JWT.
- The project does not yet include the Spring Security starter. `pom.xml` has only `spring-security-crypto`, which provides the BCrypt encoder. Adding `spring-boot-starter-security` is part of the login build step.
- No code or docs were changed in this turn.
