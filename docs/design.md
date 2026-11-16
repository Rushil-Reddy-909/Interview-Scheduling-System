# Design

Status: starting design for a project that is only scaffolded. No scheduling behavior is implemented yet.

## Product

A recruiter selects a candidate and one or more interviewers. The system proposes interview slots. The candidate confirms one slot or asks to reschedule.

## UI

Thymeleaf, served by this Spring Boot application.

The first screens are server-rendered forms and lists: create an interview, show proposed slots, confirm a slot, and submit a reschedule request. Spring Validation can check those forms in the same application that stores the data. A separate frontend build is not needed for that.

## Actors

- **Recruiter** — creates an interview by picking a candidate, interviewers, a duration, a timezone, and a date window.
- **Candidate** — sees the proposed slots for an interview and either confirms one or asks to reschedule.
- **Interviewer** — is selected onto an interview. In this version an interviewer does not have their own screens. Their confirmed interviews are what block later proposals.

## Main flow

1. The recruiter creates an interview and chooses the people, duration, timezone, and the window to search.
2. The system proposes slots inside that window.
3. The candidate confirms one proposed slot, or submits a reschedule request.
4. A confirmation marks that slot confirmed and withdraws the other proposals for that interview.
5. A reschedule request records the candidate's note and optional preferred window. The recruiter can then ask the system for a new set of proposals.

## How slots are proposed

Until someone specifies a real availability source, proposals use this rule:

- Consider weekdays from 09:00 to 17:00 in the interview's timezone, inside the recruiter's date window.
- Each slot is exactly `duration_minutes` long.
- Skip any time that overlaps a slot already confirmed for one of the selected interviewers.
- Offer a small set of proposals (aim for up to five), earliest first.

The candidate does not publish availability in this version. A reschedule request is how they reject the set.

## Not decided yet

- Sign-in, and how a candidate receives the link to their slots.
- Whether one person can hold more than one role.
- Interviewer-managed calendars, external calendar sync, or buffers between interviews.
- More than one round per candidate.
- What happens if every slot in the window is blocked.
- Cancellation after a slot is confirmed.

Record a decision in this file when one of these is settled.
