# 035 — Services, exceptions, and real data

Date: 2026-11-17

Type: plan

## Prompt

now plan implementing the service for AppUser and interview, with custom exceptions and controller advices all segregated neatly. Right now, all services are under web, segregate them and modify/remove web.preview to include real data.

## Outcome

- Answers: matching (single, bulk, reschedule proposals, and confirm) is left for the next plan. Recruiter schedule and reschedule pages keep their pickers with real users and requests, and say matching is not built yet. Demo users and interviews are inserted on startup only when `app.seed.demo=true`, which is on locally and off in tests.
- Planned packages: `service` (user, interview, calendar, slot rules), `exception`, `dto` read models, `web/controller`, `web/advice`, `web/form`, `web/model`, and a `Clock` bean. `web/preview` is deleted.
- Planned rules: unique email, password of at least 8 characters, the last admin cannot lose the role, and a user's role cannot change once they appear on an interview. Candidates accept only from `AWAITING_CANDIDATE`, before the start. Requested starts are checked against weekdays, 09:00 to 17:00 UTC, the 15-minute grid, and the future.
- The uncommitted `InterviewRepository` and `AppUserDetailsService` edits are kept and built on.
- Plan only. No code changed.
