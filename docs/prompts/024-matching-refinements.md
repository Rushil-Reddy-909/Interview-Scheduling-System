# 024 — Matching refinements

Date: 2026-11-16

Type: plan

## Prompt

in the same plan, 
Search range is good
start grid is good
reschedule pool: Can you explain more how this algo works starting from current interviewer, does it first check if this interviewer has a timeslot free at requested time and if yes, it reschedules, if no, it checks other recruiters who are available at that time? and if no one is available, what then?
Single mode: does it mean it shows upto 5 available interviewers from the pool of selected ones?
Confirm is all or nothing: explain this, what does slot taken in meantime mean?

## Outcome

- Plan mode. Logged on the next turn that could write files.
- Kept the search range (`fromDate` to `toDate`, weekdays only, future starts only) and the 15-minute start grid.
- Reschedule ranking:
  1. The current interviewer at the requested start.
  2. Another pool interviewer at the requested start.
  3. The earliest common slots in the range.
- A reschedule never happens without the recruiter's confirm. If nothing fits, the interview stays `RESCHEDULE_REQUESTED` with its old slot booked.
- Single mode shows the 5 earliest `(interviewer, start)` options, and an interviewer may appear more than once. There is one option per common gap, so a long gap does not fill every row with 15-minute steps.
- A proposal is not stored and reserves nothing. On confirm, the service locks the users and rechecks every slot. If any slot now conflicts, it saves nothing and the recruiter runs the match again.
