# 028 — Load in minutes and exact tie-breaking

Date: 2026-11-16

Type: build

## Prompt

This log covers two prompts. At the user's request they are logged as one.

First prompt (Ask mode):

a few clarifications, dont document this prompt

Lower load is calculated by sorting interviewers that have higher free minutes?

in reschedule, a few doubts
every other options means, when a candidate's requested slot cannt be allocated to any interviewer, then based on every other interviewer's schedule, the earliest free slot is selected?

among those options, the ucrrent interviewers comes first?
and if not then lower load and lower id?

Second prompt (Agent mode):

actually log this prompt and previous prompt as one prompt, and clearly clarify it in docs, also i want lower load as interview minutes rather than interview count. As for the clarifications, they should clearly explain the exact tie breaking mechanism, you can include any examples if you wish.

## Outcome

- Answered in Ask mode:
  - Load was an interview count, not free minutes.
  - "Every other option" means every option not at the requested start, from all pool interviewers.
  - The current interviewer wins only at an equal start. Load, then id, break the remaining ties.
- Load is now the booked minutes in the date range, not the interview count.
  - In bulk mode it includes minutes already proposed in the same run.
  - A reschedule does not count the interview being moved.
- Rewrote the Ranking section in `docs/design.md`:
  - Reschedule options are split into group A (at the requested start) and group B (every other option). All of group A ranks above group B.
  - Each group has an exact key order.
  - Added an example table, plus a create example.
- No code was changed.
