# 034 — Dashboard UI with sample data

Date: 2026-11-17

Type: build

## Prompt

ok for the next thing, i want you to design the UI for all dashboard pages, maintian consistent theming and based on design.md, add all essentail features, right now we haven't setup any backend service that actually works yet, this is mainly for creating UI with expected features only.

## Outcome

- Built Thymeleaf pages for every role with one shared layout and stylesheet (`static/css/app.css`) and a small dialog script (`static/js/app.js`).
- Admin: overview, users (search, role filter, add and edit dialogs), and all interviews (status and date filters).
- Recruiter: overview with reschedule requests, scheduling (single and bulk, top 5 options or one slot per candidate plus unplaced), and reschedule requests (single and bulk, requested-start group first).
- Candidate: upcoming interviews with accept and request-new-start, plus past interviews. Interviewer: read-only week calendar (free or has an interview) and upcoming list.
- Data comes from `web/preview/PreviewData`, an in-memory sample set. Proposals are simple sample output, not the real matching service. Every form action redirects with a "preview only, nothing was saved" notice.
- Added `DashboardPageTests` (13 tests). Checked every page in headless Chrome. The calendar legend now shows the status colours.
- Updated the Screens section of `docs/design.md` with the route map and the README layout.
