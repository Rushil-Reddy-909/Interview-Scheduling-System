# 012 — How the recruiter knows free or occupied

Date: 2026-11-16

Type: ask

## Prompt

so right now, how would a recruiter know when a candidate/interviewer is free or occupied? do they just fetch all rows from interview entity for that candidate id/ interviewer id where current time > end_at? And from this result, just mark the times where interview is present as occupied while remaining would be free?

## Outcome

- Answered without editing the project. The `current time > end_at` filter was the wrong way round. It returns finished interviews. Occupied time is any blocking interview whose `[starts_at, ends_at)` overlaps the window on screen.
- Free time is whatever is left in the working window, not a stored flag. Only blocking statuses count, and `RESCHEDULE_REQUESTED` still blocks its official slot.
- Ask mode could not write this file. It is logged here on the next turn that can write.
