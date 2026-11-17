# Interview Scheduling

Recruiters choose one or more candidates, a pool of interviewers, a duration, and a date range. The system proposes an interviewer and a start for each candidate, and the recruiter confirms. The candidate accepts that slot or asks to reschedule, and rescheduling uses the same matching.

## Stack

- Java 17
- Spring Boot 4.1.2
- Maven
- Spring Web MVC and Thymeleaf
- Spring Data JPA and Jakarta Bean Validation
- H2 file database for local development, stored in `./data`

Thymeleaf is the UI for this version. The product is forms and lists (pick people, review slots, confirm, reschedule), so pages and validation stay in this application. See `docs/design.md`.

## Java

The project compiles for Java 17. That is the newest JDK on this machine. The `java` command on the PATH is a Java 21 JRE and has no compiler.

Leave `JAVA_HOME` unset so Maven uses `javac` from the PATH, or set it to the Java 17 JDK:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
```

Do not point `JAVA_HOME` at the Java 21 JRE.

## Run

```bash
./mvnw spring-boot:run
```

Open <http://localhost:8080>.

The H2 console is at <http://localhost:8080/h2-console>. JDBC URL: `jdbc:h2:file:./data/interview-scheduling`. User: `sa`. Password: empty. Use that console only on a local machine.

Data lives in `./data/`, so it survives restarts. Delete that folder to start with an empty database. The folder is git-ignored.

## Bootstrap users

On startup, for each role that has no user, the app inserts one. The password is stored as a BCrypt hash.

| Role | Email | Password |
| --- | --- | --- |
| Admin | `admin@localhost` | `app.admin.password` (`12345678`) |
| Recruiter | `recruiter@localhost` | `app.admin.password` (`12345678`) |
| Candidate | `candidate@localhost` | `app.admin.password` (`12345678`) |
| Interviewer | `interviewer@localhost` | `app.admin.password` (`12345678`) |

The password is hashed only when that role's row is created. Changing the property later does not change an existing user. Delete `./data` to seed again.

Change the password before the app is used outside a local database.

## Login

Open <http://localhost:8080/login>. Sign in with one of the seeded emails and the password that was hashed when that user was created. The generated Spring Security user (`user` and the password printed at startup) is not used.

Each role lands on its own dashboard. Logout is the button in the top bar. It ends the session.

The dashboards read and write the database. Admins manage users. Candidates accept interviews or request a new start. Recruiter matching is not built yet, so the schedule and reschedule pages stop after checking their inputs.

## Demo data

With `app.seed.demo=true` (the local default), startup also inserts sample recruiters, candidates, interviewers, and interviews around next Monday. It runs only while the `interview` table is empty, and it reuses the four `role@localhost` accounts. New sample users get the `app.admin.password` password. Set `app.seed.demo=false` to start without them. Tests always run with it off.

## Tests

```bash
./mvnw clean test
```

Tests use an in-memory H2 database and a fixed clock (Monday 23 November 2026, 08:00 UTC) from `TestClockConfiguration`. Run `clean` after moving classes, so stale files in `target/classes` do not create duplicate beans.

## Project docs

| File | What it holds |
| --- | --- |
| `AGENTS.md` | Instructions Cursor loads for every agent chat in this repo |
| `docs/design.md` | Product behavior, Thymeleaf decision, open questions |
| `docs/schema.md` | `app_user` and `interview`, including overlap rules |
| `docs/prompts/` | Verbatim record of user prompts |

## Layout

```text
src/main/java/com/interviewscheduling/                application entry point
src/main/java/com/interviewscheduling/config/         UTC clock bean
src/main/java/com/interviewscheduling/domain/         JPA entities and interview status transitions
src/main/java/com/interviewscheduling/enums/          role and interview status
src/main/java/com/interviewscheduling/exception/      DomainException and its subclasses
src/main/java/com/interviewscheduling/repository/     Spring Data repositories
src/main/java/com/interviewscheduling/service/        user, interview, and calendar services, and slot rules
src/main/java/com/interviewscheduling/dto/            view records the services return to pages
src/main/java/com/interviewscheduling/security/       form login and session
src/main/java/com/interviewscheduling/seed/           bootstrap users and demo data
src/main/java/com/interviewscheduling/web/controller/ page controllers
src/main/java/com/interviewscheduling/web/advice/     current user and error-to-flash handling
src/main/java/com/interviewscheduling/web/form/       validated form objects
src/main/java/com/interviewscheduling/web/model/      page-only records
src/main/resources/templates/fragments/              shared layout and UI fragments
src/main/resources/templates/                        pages for home, login, error, admin, recruiter, candidate, interviewer
src/main/resources/static/                           app.css and app.js
src/main/resources/application.properties            local H2, JPA, admin seed, and demo seed settings
```
