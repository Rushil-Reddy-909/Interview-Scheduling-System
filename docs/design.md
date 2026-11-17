# Design

Status: the `app_user` and `interview` tables are mapped, and startup seeds one user for each role when that role is missing. Form login is in place: email and password, an HTTP session, and a landing page per role. Every screen runs on the database through services (prompt 036). Admins manage real users. Candidates accept or request a new start, and those changes are saved. Matching, confirm, and replacement saves are not built yet. They are the next plan.

## Product

A recruiter picks one or more candidates, a pool of interviewers, a duration, and a date range. The system proposes an interviewer and a start for each candidate. The recruiter reviews the proposal and confirms it, which creates the interviews. A candidate then accepts the slot or asks to reschedule by picking a different start. The recruiter runs the same matching for the reschedule request, confirms a replacement, and the candidate must accept it.

This is an internal tool. There is no public signup.

## Users and roles

All people are rows in one `app_user` table. Each user has one role:

- **Admin** creates and edits users (name, email, role, password). Admin can also list every interview: candidate, interviewer, the recruiter who created it, date, start, duration, and status. Admin does not manage scheduling.
- **Recruiter** picks candidates, an interviewer pool, a duration, and a date range. Runs matching, confirms proposals, and handles reschedule requests.
- **Candidate** sees their upcoming interviews and can accept or request a new start.
- **Interviewer** sees a read-only calendar of their interviews.

The recruiter chooses which interviewers are eligible, as the pool. Matching picks the interviewer from that pool.

## Login

Spring Security form login with an HTTP session. There is no HTTP Basic auth and no JWT.

- The login page posts email and password. There is no signup.
- Only an admin creates or changes users.
- On startup, each missing role gets one user. The email is the role in lowercase plus `@localhost` (`admin@localhost`, `recruiter@localhost`, `candidate@localhost`, `interviewer@localhost`). The password comes from `app.admin.password` and is stored as a BCrypt hash. The local default is `12345678`.
- Spring Security checks the BCrypt hash once, at login, and stores an `Authentication` in the HTTP session. That object carries the email and the role (`ADMIN`, `RECRUITER`, `CANDIDATE`, or `INTERVIEWER`).
- Later requests restore that `Authentication` from the session cookie. They do not load the user again for authorization. The top bar reads only the display name from the database.
- Pages and actions are allowed or refused from that role.
- A role or password change applies on the user's next login. The current session keeps the previous role until then.
- Logout ends the session.

`SecurityConfig` is the form-login filter. `AppUserDetailsService` loads the user by email at login and copies the stored BCrypt hash onto the `Authentication`. `User.roles` adds the `ROLE_` prefix. The login page is `GET /login`. After a successful login, admin goes to `/admin`, and the other roles go to `/dashboard/recruiter`, `/dashboard/candidate`, or `/dashboard/interviewer`.

## Scheduling rules

All times are UTC. There is no per-user timezone yet.

**Date and time.** `starts_at` and `ends_at` are UTC timestamps that hold both the date and the time. The interview's date is the UTC date of `starts_at`. There is no separate date column.

**Days.** Only Monday through Friday. A weekend date has no slots.

**Working window.** A slot starts at or after 09:00 UTC and ends at or before 17:00 UTC on its UTC date.

**Future only.** A new or replacement slot must start after the current time.

**Availability.** There is no stored availability yet. A person is free throughout the working window unless a blocking interview covers that time.

**Half-open intervals.** A slot is `[starts_at, ends_at)`. A new interview may start at the exact moment another one ends, for the same candidate or the same interviewer. Two slots overlap only when `existing.starts_at < new.ends_at` and `new.starts_at < existing.ends_at`.

**Which bookings block time.** An interview in `AWAITING_CANDIDATE`, `ACCEPTED`, or `RESCHEDULE_REQUESTED` blocks its official `[starts_at, ends_at)` for both its candidate and its interviewer.

- `RESCHEDULE_REQUESTED` does not free the current slot. It changes only when the recruiter saves a replacement.
- The candidate's requested start is checked against the weekday and working-hours rules only. It reserves no time.

**Common free time.** For one candidate, one interviewer, and one date, common free time is the working window `[09:00, 17:00)` minus the union of the blocking intervals of both people on that date. The result is a list of gaps. It is calculated when needed, not stored. A weekend date has no gaps.

A new slot is allowed only when `[starts_at, starts_at + duration)` lies entirely inside one common free gap. The recruiter's duration must therefore fit inside a gap, not span two.

## Matching

Matching proposes slots. It never saves anything. The recruiter confirms a proposal, and only then are interviews created or changed.

### Inputs

| Mode | Who is scheduled | Duration |
| --- | --- | --- |
| Single create | One candidate | Any duration the recruiter enters |
| Bulk create | Many candidates | One common duration for all |
| Single reschedule | One `RESCHEDULE_REQUESTED` interview | The interview's existing duration |
| Bulk reschedule | Many `RESCHEDULE_REQUESTED` interviews | Each interview's existing duration |

Every mode also takes:

- An interviewer pool. For a reschedule, the pool starts with the interview's current interviewer, and the recruiter may add more.
- A date range, `fromDate` to `toDate` (UTC dates). Weekends in the range are skipped.

### Candidate starts

For each weekday in the range and each pool interviewer, matching takes the common free gaps of the candidate and that interviewer. Inside each gap, it takes the earliest start on the 15-minute grid (`:00`, `:15`, `:30`, `:45`) after the current time where `[start, start + duration)` still fits. Each gap yields at most one option, so one long gap does not fill the list with 09:00, 09:15, 09:30, and so on.

### Ranking

Each option is one interviewer and one start. Options are sorted by the keys below, in order. A later key is looked at only when two options are equal on every earlier key.

**Load.** An interviewer's load is the total booked minutes of their blocking interviews in the date range. It is not the number of interviews. An interviewer with one 240-minute interview (load 240) ranks after one with four 15-minute interviews (load 60).

- In bulk mode, load also counts the minutes of slots already proposed earlier in the same run.
- In a reschedule, the interview being rescheduled is not counted toward its current interviewer's load.

#### Create

1. Earlier start.
2. Lower load.
3. Lower interviewer id.

#### Reschedule

Options fall into two groups. The whole first group ranks above the whole second group, even when a second-group option starts earlier.

**Group A: options at the candidate's requested start.** This group exists only when `requested_starts_at` passes the slot rules and the candidate and at least one pool interviewer are free for `[requested_starts_at, requested_starts_at + duration)`. All options in it share the same start, and they are ordered:

1. The current interviewer.
2. Lower load.
3. Lower interviewer id.

**Group B: every other option.** This means every option at any other start, from every pool interviewer, including the current one. Group B is ordered:

1. Earlier start.
2. The current interviewer, only between options with the same start.
3. Lower load.
4. Lower interviewer id.

So the current interviewer is preferred only at an equal start. An earlier slot with another interviewer still ranks above a later slot with the current interviewer.

If group A is empty, because the request is invalid or nobody is free then, the list is group B alone.

#### Examples

In each example, I1 is the current interviewer, the duration is 60 minutes, and the candidate asked for Wed 14:00.

| Situation | Order |
| --- | --- |
| I1 and I2 both free Wed 14:00 | I1 Wed 14:00 (current interviewer), then I2 Wed 14:00, then group B |
| I1 busy Wed 14:00. I2 (load 120) and I3 (load 60) free then | I3 Wed 14:00 (lower load), then I2 Wed 14:00, then group B |
| I2 and I3 free Wed 14:00, both load 60 | I2 Wed 14:00 (lower id), then I3 Wed 14:00 |
| Nobody free Wed 14:00. I2's earliest is Tue 09:00, I1's is Tue 11:00 | I2 Tue 09:00 (earlier start), then I1 Tue 11:00 |
| Nobody free Wed 14:00. I1 and I2 both free Tue 09:00 | I1 Tue 09:00 (current interviewer at an equal start), then I2 Tue 09:00 |
| Request is Wed 16:30, which would end at 17:30 | Group A is empty, so the list is group B only |
| I2 free Wed 14:00, and I1 free Tue 09:00 | I2 Wed 14:00 (group A), then I1 Tue 09:00 (group B) |

For create, with no requested start and no current interviewer: if I1 (load 240) and I2 (load 60) are both free Tue 09:00, I2 ranks first. If both have load 60, I1 ranks first by lower id.

### Single mode

Shows the top 5 options, each a pair of interviewer and start. The same interviewer may appear more than once, for a different gap or day. The recruiter picks one and confirms.

### Bulk mode

Greedy, with one proposed slot per candidate:

1. Load the blocking intervals for every candidate and every pool interviewer over the range.
2. Order the candidates most constrained first: fewest free working minutes in the range, then lower candidate id.
3. For each candidate in that order, take the top-ranked option. Reserve that slot in memory for both people, so later candidates in the same run cannot overlap it.
4. A candidate with no option goes to an unplaced list, with the reason.

Greedy is not guaranteed to place everyone when some placement exists. It is predictable, fast, and places everyone in most real cases.

### No slot found

- **Create:** the candidate is listed as unplaced. No interview is created for them.
- **Reschedule:** nothing changes. The interview stays `RESCHEDULE_REQUESTED`, and its official slot stays booked.

The recruiter can widen the date range, add interviewers to the pool, or stop.

### Confirm

A proposal is not stored and reserves nothing. Another recruiter can book one of the same people before this recruiter confirms. Confirm therefore runs in one transaction:

1. Lock every involved `app_user` row, ordered by id.
2. Recheck each slot against the slot rules, the database, and the other slots in the batch.
3. Save all slots, or none.

If any slot conflicts, nothing is saved. The message names the slot that conflicts, and the recruiter runs matching again.

## Interview lifecycle

1. The recruiter creates interviews by confirming a matching proposal. Each is saved as `AWAITING_CANDIDATE`.
2. The candidate either accepts (`ACCEPTED`) or asks for a different start (`RESCHEDULE_REQUESTED`).
3. On a reschedule request, the official slot is unchanged. The recruiter runs reschedule matching and confirms a replacement. The interview's own current slot does not block its replacement. The replacement may have a different interviewer from the pool. On save:
   - the replacement becomes the official slot,
   - the interviewer is updated if it changed,
   - the requested start is cleared,
   - the interview returns to `AWAITING_CANDIDATE`.
4. The candidate must accept the replacement. They can request another reschedule instead.

Status transitions:

- Accept is allowed only from `AWAITING_CANDIDATE`.
- A reschedule request is allowed from `AWAITING_CANDIDATE` or `ACCEPTED`.
- A replacement is allowed only from `RESCHEDULE_REQUESTED`.

Table details are in `docs/schema.md`.

## Screens

Login is built: one Spring Security form for all roles, backed by a session. After login each role lands on its own dashboard.

The UI for every screen was built on sample data in prompt 034. Prompt 036 removed that sample data, and every screen now reads and writes the database through services:

- Admin user forms create and edit users, with the rules in [Error handling](#error-handling).
- The candidate's accept and request-new-start forms save the change.
- The interviewer calendar shows the stored interviews.
- The recruiter schedule and reschedule pages list real candidates, interviewers, and requests, and they check the inputs. Valid input then shows "Matching is not built yet". There are no confirm endpoints yet. The proposal tables from prompt 034 were removed from the templates. Git history (commit `43f2033`) has them for the matching plan.

For local use, `app.seed.demo=true` inserts sample people and interviews around next Monday, only while the `interview` table is empty. Tests set it to `false` and build their own rows.

| Route | Role | Screen |
| --- | --- | --- |
| `/admin` | Admin | Users by role, interviews by status, next interviews |
| `/admin/users` | Admin | Search and filter users. Add and edit users in dialogs |
| `/admin/interviews` | Admin | All interviews, filtered by status, date range, and name |
| `/dashboard/recruiter` | Recruiter | Status counts, reschedule requests, upcoming interviews |
| `/dashboard/recruiter/schedule` | Recruiter | Pick candidates, a pool, a duration, and a range. Input errors are listed. Valid input shows "Matching is not built yet" |
| `/dashboard/recruiter/reschedule` | Recruiter | Pick reschedule requests and extra interviewers. Same input checks and not-built-yet state |
| `/dashboard/candidate` | Candidate | Upcoming interviews with accept and request new start, plus past interviews |
| `/dashboard/interviewer` | Interviewer | Read-only week calendar ("has an interview" or free) and upcoming interviews |

All pages share `templates/fragments/layout.html` (top bar and alerts), `templates/fragments/ui.html` (status badge, role tag, person, slot), `static/css/app.css`, and `static/js/app.js` (dialogs and select-all). `templates/error.html` is the page for unhandled errors such as 404.

Thymeleaf renders the screens. Spring Validation checks forms on the server.

## Code layout

Packages under `com.interviewscheduling`:

| Package | Holds |
| --- | --- |
| `config` | `TimeConfig`, the UTC `Clock` bean. Services read the time only from it, so tests can fix it |
| `domain` | JPA entities. `Interview.accept` and `Interview.requestNewStart` guard the status transitions |
| `enums` | `UserRole`, `InterviewStatus` |
| `exception` | `DomainException` and its subclasses |
| `repository` | Spring Data repositories. Dashboard queries fetch the three people in the same query with `@EntityGraph` |
| `service` | `UserService`, `InterviewService`, `InterviewerCalendarService`, `SlotRules` |
| `dto` | `UserView`, `InterviewView` (UTC `LocalDateTime`), `WeekCalendar`. Services return these, never entities, because `open-in-view` is off |
| `security` | Form login and the session |
| `seed` | Per-role users (`AdminSeedConfiguration`, order 0) and demo data (`DemoDataSeeder`, order 1) |
| `web.controller` | Page controllers. They call services and add view objects to the model |
| `web.advice` | `CurrentAccountAdvice` (top-bar user) and `DomainExceptionAdvice` |
| `web.form` | `CreateUserForm`, `UpdateUserForm`, `NewStartForm`, checked with `@Validated` |
| `web.model` | Page-only records such as `StatCard` |

`SlotRules` holds the weekday, working-window, 15-minute-grid, and future checks. Matching will reuse it.

## Error handling

A broken rule throws a subclass of `DomainException`. Its message is written for the user:

| Exception | When |
| --- | --- |
| `NotFoundException` | The interview or user does not exist, or the interview belongs to another candidate |
| `SlotRejectedException` | A slot breaks a rule. Each rule has its own message, such as "Pick a weekday" or "Start on a quarter hour" |
| `InvalidInterviewStateException` | The status does not allow the action, or the interview has started |
| `DuplicateEmailException` | Another user has that email |
| `UserChangeRejectedException` | The password is not 8 to 72 characters, the last admin would lose the role, or a user on any interview would change role |

`DomainExceptionAdvice` catches these and form validation errors. It redirects to the page the form was posted from (the `Referer` path, only when it points at this host; otherwise `/dashboard`) with the message as a flash `error`. Other errors go to `error.html`.

User rules in `UserService`:

- Emails are trimmed, lowercased, and unique.
- A blank password on edit keeps the current one.
- The last admin cannot change role.
- A user who appears on any interview cannot change role, so past interviews keep consistent roles.

## Left for later

- Cancellation of a confirmed interview.
- Availability that interviewers set themselves.
- More than one interviewer per interview.
- More than one role per person.
- Optimal (non-greedy) bulk assignment.
- Per-user timezones.

## Open decisions

None blocking. The decisions above come from the design discussion (prompts 004, 005, 011 to 013, 017, 023, and 024).
