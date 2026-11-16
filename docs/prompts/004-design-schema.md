# 004 — Design and schema

Date: 2026-11-16

Type: plan

## Prompt

Ok lets decide on the design/schema for the project. So we have 3 entities - recruiters, candidates and interviewers. Should we add a 4th entity like admin which oversees all interviews or should we delegate it towards the end after building the main stuff?

As for the authorities, what i have in mind is 
Each user would have a regular login page. No signup as im assuming this is some internal tool. Only the admin would be able to change/create users.

Candidates would have their dashboard that shows their upcoming interviews and an option to accept or request rescheduling. The recruiter would decide the interview duration, the candidate that applies for rescheduling would just pick a different start time. The recruiter would then choose another time slot.

Interviewer would just have an option to view their upcoming interviews, like in a calendar format with like a heatmap of available/have an interview mapping.

Recruiter is the one that manages this scheduling, for starting off, they manually map each candidate to interviewer and in future, we can create an algorithm that automatically maps.

Admin for now, can just see all interviews that are happening between various candidates and interviewers and who created them.

So schema wise, lets start off simple
All of entities will have: id, name, email (for unique identification)
Interview would be another entity that maps candidate to interviewer and also the recruiter who created.

Feel free to review this and suggest any changes or modifications for best code practices

## Outcome

Decisions are in the plan `lock_design_schema`, not yet copied into `docs/design.md` or `docs/schema.md`.

- One `app_user` table with role `ADMIN`, `RECRUITER`, `CANDIDATE`, or `INTERVIEWER`. Admin is a role, not a fourth table. A minimal admin screen (create and edit users, list all interviews) is part of the first build.
- One interview row: recruiter, one candidate, one interviewer, duration, official start, and an optional requested start.
- After the recruiter picks a replacement slot, status returns to awaiting the candidate, who must accept again.
- Prompt answers that settled this: reschedule goes back to the candidate; admin is a role with screens now.
