# 019 — Document session login

Date: 2026-11-16

Type: build

## Prompt

this design is fine, document it in docs with the prompts

## Outcome

- Wrote the accepted auth design into the Login section of `docs/design.md`: Spring Security form login, HTTP session, role carried on `Authentication`, no HTTP Basic, no JWT.
- Noted that `spring-boot-starter-security` is not added yet. Only `spring-security-crypto` is present.
- The earlier plan and question were already logged as `docs/prompts/017-session-login.md` and `docs/prompts/018-spring-security-question.md`.
- No application code was changed.
