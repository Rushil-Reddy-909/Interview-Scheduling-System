# Schema

Status: proposed only. There are no JPA entities and no tables yet. Local H2 is configured so the application can start; Hibernate is not mapping a domain model.

People are one row per role for now. The same human who both recruits and interviews would be two rows until the open question in `docs/design.md` is decided.

## person

| Column | Notes |
| --- | --- |
| id | Primary key |
| full_name | Required |
| email | Required, unique |
| role | `RECRUITER`, `CANDIDATE`, or `INTERVIEWER` |
| created_at | Set when the row is created |

## interview

| Column | Notes |
| --- | --- |
| id | Primary key |
| recruiter_id | References `person`. That person must have role `RECRUITER` |
| candidate_id | References `person`. That person must have role `CANDIDATE` |
| duration_minutes | Length of each proposed slot |
| timezone | Timezone the window and working hours are interpreted in |
| window_start | First date to search, inclusive |
| window_end | Last date to search, inclusive |
| status | See below |
| created_at | Set when the row is created |

Status values: `PROPOSED`, `CONFIRMED`, `RESCHEDULE_REQUESTED`, `CANCELLED`.

## interview_interviewer

| Column | Notes |
| --- | --- |
| interview_id | References `interview`. Part of the primary key |
| interviewer_id | References `person` with role `INTERVIEWER`. Part of the primary key |

An interview has at least one interviewer.

## proposed_slot

| Column | Notes |
| --- | --- |
| id | Primary key |
| interview_id | References `interview` |
| starts_at | Slot start, stored as an instant |
| ends_at | Slot end. `ends_at - starts_at` equals the interview duration |
| status | `PROPOSED`, `CONFIRMED`, or `WITHDRAWN` |

At most one slot on an interview may be `CONFIRMED`. Confirming a slot withdraws the other `PROPOSED` slots on that interview.

## reschedule_request

| Column | Notes |
| --- | --- |
| id | Primary key |
| interview_id | References `interview` |
| message | The candidate's reason or note. Required |
| preferred_window_start | Optional date |
| preferred_window_end | Optional date. Required when `preferred_window_start` is set, and not before it |
| created_at | Set when the row is created |

Creating a request sets the interview status to `RESCHEDULE_REQUESTED`. It does not delete the current proposals.
