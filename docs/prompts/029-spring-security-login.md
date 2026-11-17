# 029 — Spring Security form login

Date: 2026-11-17

Type: build

## Prompt

what are the steps to setup spring security login as per our requirements (refer docs/), i added spring security and test to pom.xml and reloaded, i tried the default username and password generated but i cant login, 

o.s.s.c.bcrypt.BCryptPasswordEncoder     : Encoded password does not look like BCrypt

## Outcome

- The generated `user` password failed because `BCryptPasswordEncoder` was already a bean. Spring Boot 4 then stored that generated password as plain text, and BCrypt rejected it.
- Form login loads `app_user` by email, checks the stored BCrypt hash once, and keeps the email and `ROLE_` authority on the HTTP session. Later requests use that session.
- `/` and `/login` are public. Admin lands on `/admin`. The other roles land on `/dashboard/recruiter`, `/dashboard/candidate`, or `/dashboard/interviewer`. Logout invalidates the session.
- The local admin email is `admin@localhost`. `application.properties` uses password `12345678`, written only when the admin row is first created. Tests still use `change-me`.
