# Design

Status: design locked, nothing implemented yet. The project is scaffolded (Spring Boot 4.1.2, Java 17, Thymeleaf, H2 file database). No login, users, or scheduling code exists yet.

## Product

Recruiters create an interview by choosing one candidate, one interviewer, a duration, and a first start time. The candidate accepts the slot or asks to reschedule by picking a different start. The recruiter then picks a replacement slot, and the candidate must accept it.

This is an internal tool. There is no public signup.

## Users and roles

All people are rows in one `app_user` table. Each user has one role:

- **Admin** creates and edits users (name, email, role, password). Admin can also list every interview: candidate, interviewer, the recruiter who created it, start, duration, and status. Admin does not manage scheduling.
- **Recruiter** creates interviews, chooses the duration, picks the candidate and interviewer, and handles reschedule requests.
- **Candidate** sees their upcoming interviews and can accept or request a new start.
- **Interviewer** sees a read-only calendar of their interviews.

Automatic candidate-to-interviewer matching is a later feature. For now a recruiter maps each candidate to an interviewer by hand.

## Login

- Every user has a regular login form (email and password).
- There is no signup.
- Only an admin creates or changes users.
- On an empty database, startup creates one admin from local config (`app.admin.email`, `app.admin.password`), so the first login is possible.

## Scheduling rules

All times are UTC. There is no per-user timezone yet.

A slot is half-open: `[starts_at, ends_at)`. An interview may start at the exact moment another ends, for the same candidate or the same interviewer.

Any slot the recruiter sets, and any slot the candidate requests, must:

- start and end on the same UTC date,
- fall on Monday through Friday,
- start at or after 09:00 UTC,
- end at or before 17:00 UTC.

Two slots overlap only when `existing.starts_at < new.ends_at` and `new.starts_at < existing.ends_at`.

A new official slot must not overlap another interview in `AWAITING_CANDIDATE`, `ACCEPTED`, or `RESCHEDULE_REQUESTED` for the same interviewer or the same candidate.

`RESCHEDULE_REQUESTED` does not free the current slot. `starts_at` and `ends_at` stay booked until the recruiter saves a replacement. The candidate's requested start is checked against the weekday and working-hours rules only. It does not reserve a second range.

## Interview flow

1. The recruiter creates an interview with a candidate, an interviewer, a duration, and a first start. The interview is `AWAITING_CANDIDATE`.
2. The candidate either accepts (`ACCEPTED`) or asks for a different start (`RESCHEDULE_REQUESTED`).
3. On a reschedule request, the official slot is unchanged. The recruiter later picks a replacement. That replacement becomes the official slot, the requested start is cleared, and the interview returns to `AWAITING_CANDIDATE`.
4. The candidate must accept the replacement. They can request another reschedule instead.

Status transitions:

- Accept is allowed only from `AWAITING_CANDIDATE`.
- A reschedule request is allowed from `AWAITING_CANDIDATE` or `ACCEPTED`.
- A replacement slot is allowed only from `RESCHEDULE_REQUESTED`.

Details for each table are in `docs/schema.md`.

## Screens

Built later, in this order of need:

- **Login**: one form for all roles.
- **Recruiter**: create an interview, see its status, and on a reschedule request choose the new start.
- **Candidate dashboard**: upcoming interviews, accept, or request a new start.
- **Interviewer calendar**: read-only view. Each cell shows "has an interview" or "free" (no interview in that range). Interviewers do not enter availability yet.
- **Admin**: create and edit users, list all interviews.

Thymeleaf renders the screens. Spring Validation checks forms on the server.

## Left for later

- Cancellation of a confirmed interview.
- Availability that interviewers set themselves.
- More than one interviewer per interview.
- More than one role per person.
- Automatic candidate-to-interviewer matching.
- Per-user timezones.

## Open decisions

None blocking. The decisions above come from the design discussion (prompts 004 and 005).
