# 009 — Database scaffold and admin seed

Date: 2026-11-16

Type: build

## Prompt

ok commit the changes until now, after that you can start the implementation of scaffolding the db and seeding data, dont touch other parts of code yet other than docs.

## Outcome

- Committed the design, schema, and prompt log as `b2ebc32` (`Lock the interview design and schema.`). That commit does not include the database code.
- Mapped `app_user` and `interview` with JPA. Hibernate still creates the tables with `ddl-auto=update`.
- Startup inserts one admin when none exists, using `app.admin.email` and `app.admin.password`. The password is stored as a BCrypt hash via `spring-security-crypto` only. Spring Security's web filter is not enabled, and no login or scheduling screens were added.
- Tests use an in-memory H2 database so they do not write to `./data`.
