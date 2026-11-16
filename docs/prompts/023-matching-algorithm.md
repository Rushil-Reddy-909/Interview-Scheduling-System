# 023 — Matching algorithm

Date: 2026-11-16

Type: plan

## Prompt

ok read Agents.md and docs/ for essential context. Based on that we will decide how to build the matching algorithm for creating / rescheduling interviews for candidates that a recruiter can initiate. So what im thinking initally is that, it should support 2 types, one is a singular candidate and another is multi candidate scheduling. This should work for both creating and rescheduling. Right now we dont have a mechanism to store available time for candidates/interviewers so they are free throughout the day unless there is an interview scheduled for them. Maybe in the future, we will add that mechanism but right now. For a singular candidate, you can specify any duration and for bulk scheduling, there should be a common duration option for all candidates.

## Outcome

- Plan mode. Logged on the next turn that could write files.
- Two modes, single and bulk, for both create and reschedule.
- The recruiter picks an interviewer pool, and the algorithm assigns an interviewer and a start to each candidate.
- The algorithm proposes and the recruiter confirms. Nothing is saved before confirm.
- Single create takes any duration. Bulk create uses one common duration. A reschedule keeps each interview's existing duration.
- A person is free in the weekday working window unless a blocking interview covers the time. There is still no stored availability.
- Proposed: a greedy bulk algorithm that places the most constrained candidate first at the earliest fitting slot. Ties go to the interviewer with the fewest interviews, then the lowest id.
