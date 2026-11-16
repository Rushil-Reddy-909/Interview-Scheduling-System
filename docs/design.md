# Design

Status: the `app_user` and `interview` tables are mapped, and startup seeds one admin when none exists. The calendar scheduling flow below is designed but not implemented. Login and screens are not built yet.

## Product

A recruiter picks one candidate and one interviewer. A calendar of weekdays appears. Clicking a date shows the times when both people are free. The recruiter picks a start time and a duration that fit inside one of those free times, and creates the interview. The candidate then accepts the slot or asks to reschedule by picking a different start. The recruiter picks a replacement slot from the same calendar, and the candidate must accept it.

This is an internal tool. There is no public signup.

## Users and roles

All people are rows in one `app_user` table. Each user has one role:

- **Admin** creates and edits users (name, email, role, password). Admin can also list every interview: candidate, interviewer, the recruiter who created it, date, start, duration, and status. Admin does not manage scheduling.
- **Recruiter** creates interviews, picks the candidate and interviewer, chooses the date, start, and duration, and handles reschedule requests.
- **Candidate** sees their upcoming interviews and can accept or request a new start.
- **Interviewer** sees a read-only calendar of their interviews.

Automatic candidate-to-interviewer matching is a later feature. For now a recruiter picks both people by hand.

## Login

- Every user has a regular login form (email and password).
- There is no signup.
- Only an admin creates or changes users.
- On an empty database, startup creates one admin from local config (`app.admin.email`, `app.admin.password`), so the first login is possible.

## Scheduling rules

All times are UTC. There is no per-user timezone yet.

**Date and time.** `starts_at` and `ends_at` are UTC timestamps that hold both the date and the time. The interview's date is the UTC date of `starts_at`. There is no separate date column.

**Days.** Only Monday through Friday. A weekend date has no slots.

**Working window.** A slot starts at or after 09:00 UTC and ends at or before 17:00 UTC on its UTC date.

**Half-open intervals.** A slot is `[starts_at, ends_at)`. A new interview may start at the exact moment another one ends, for the same candidate or the same interviewer. Two slots overlap only when `existing.starts_at < new.ends_at` and `new.starts_at < existing.ends_at`.

**Which bookings block time.** An interview in `AWAITING_CANDIDATE`, `ACCEPTED`, or `RESCHEDULE_REQUESTED` blocks its official `[starts_at, ends_at)` for both its candidate and its interviewer.

- `RESCHEDULE_REQUESTED` does not free the current slot. It changes only when the recruiter saves a replacement.
- The candidate's requested start is checked against the weekday and working-hours rules only. It reserves no time.

**Common free time.** For one candidate, one interviewer, and one date, common free time is the working window `[09:00, 17:00)` minus the union of the blocking intervals of both people on that date. The result is a list of gaps. It is calculated when needed, not stored. A weekend date has no gaps.

A new slot is allowed only when `[starts_at, starts_at + duration)` lies entirely inside one common free gap. The recruiter's duration must therefore fit inside a gap, not span two.

## Calendar flow

1. The recruiter picks one candidate and one interviewer.
2. A calendar of weekdays appears.
3. The recruiter clicks a date. The system lists that date's common free gaps, in UTC.
4. The recruiter picks a start time and a duration that fit inside one gap, then creates the interview.
5. The interview is saved as `AWAITING_CANDIDATE`.

## Interview lifecycle

1. The recruiter creates the interview through the calendar flow above.
2. The candidate either accepts (`ACCEPTED`) or asks for a different start (`RESCHEDULE_REQUESTED`).
3. On a reschedule request, the official slot is unchanged. The recruiter later picks a replacement through the same calendar flow for the same candidate and interviewer. The replacement must lie inside one common free gap. The interview's own current slot does not block its replacement. On save, the replacement becomes the official slot, the requested start is cleared, and the interview returns to `AWAITING_CANDIDATE`.
4. The candidate must accept the replacement. They can request another reschedule instead.

Status transitions:

- Accept is allowed only from `AWAITING_CANDIDATE`.
- A reschedule request is allowed from `AWAITING_CANDIDATE` or `ACCEPTED`.
- A replacement is allowed only from `RESCHEDULE_REQUESTED`.

Table details are in `docs/schema.md`.

## Screens

Built later, in this order of need:

- **Login**: one form for all roles.
- **Recruiter scheduling**: pick candidate and interviewer, then a weekday calendar, then a date with its common free gaps, then a create form with start and duration. Replacements use the same screens.
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

None blocking. The decisions above come from the design discussion (prompts 004, 005, and 011 to 013).
