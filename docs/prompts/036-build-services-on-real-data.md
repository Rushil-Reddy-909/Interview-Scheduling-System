# 036 — Build services on real data

Date: 2026-11-17

Type: build

## Prompt

User and interview services on real data

Implement the plan as specified, it is attached for your reference. Do NOT edit the plan file itself.

To-do's from the plan have already been created. Do not create them again. Mark them as in_progress as you work, starting with the first one. Don't stop until you have completed all the to-dos.

## Outcome

- New packages: `config` (UTC `Clock`), `exception`, `service`, `dto`, `web/controller`, `web/advice`, `web/form`, `web/model`. `web/preview` and the old `web` classes are deleted.
- `SlotRules` checks the duration, weekday, 09:00 to 17:00 UTC window, 15-minute grid, and future start. Each failure has its own message.
- `UserService`: search, create, and update. Emails are trimmed, lowercased, and unique. Passwords are 8 to 72 characters, and a blank password on edit keeps the hash. The last admin cannot change role, and neither can a user who is on any interview.
- `InterviewService`: admin counts, next interviews, and search; recruiter upcoming interviews and requests; candidate upcoming and past interviews; `accept`; and `requestNewStart`. Another candidate's interview is reported as not found. `Interview.accept` and `Interview.requestNewStart` guard the status. `InterviewerCalendarService` builds the week grid.
- `DomainExceptionAdvice` turns domain errors and form validation errors into a flash `error`. It redirects to the same-host `Referer`, or to `/dashboard`. Added `templates/error.html`.
- Controllers use services and validated form records. The recruiter confirm endpoints and proposal tables are gone. Valid recruiter input shows "Matching is not built yet". The preview banner was removed.
- `DemoDataSeeder` runs when `app.seed.demo=true` (true locally, false in tests). It inserts sample people and interviews around next Monday while the `interview` table is empty.
- Tests: `SlotRulesTests`, `InterviewTests`, `UserServiceTests`, and `InterviewServiceTests`, plus a rewritten `DashboardPageTests` that uses database fixtures and a fixed clock (`TestClockConfiguration`). All 61 tests pass.
- Updated `docs/design.md` (status, screens, code layout, error handling) and the README (demo data, tests, layout).
