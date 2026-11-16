# Interview Scheduling

Recruiters choose one candidate and one interviewer, look at a calendar of their common free time, and book a start and duration. The candidate accepts that slot or asks to reschedule.

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

## Bootstrap admin

On startup, if the database has no user with role `ADMIN`, the app inserts one. The password is stored as a BCrypt hash.

| Property | Local default |
| --- | --- |
| `app.admin.email` | `admin@localhost` |
| `app.admin.password` | `change-me` |

Change both before the app is used outside a local database. Login screens are not built yet.

## Tests

```bash
./mvnw test
```

## Project docs

| File | What it holds |
| --- | --- |
| `AGENTS.md` | Instructions Cursor loads for every agent chat in this repo |
| `docs/design.md` | Product behavior, Thymeleaf decision, open questions |
| `docs/schema.md` | `app_user` and `interview`, including overlap rules |
| `docs/prompts/` | Verbatim record of user prompts |

## Layout

```text
src/main/java/com/interviewscheduling/            application entry point
src/main/java/com/interviewscheduling/domain/      JPA entities for users and interviews
src/main/java/com/interviewscheduling/enums/       role and interview status
src/main/java/com/interviewscheduling/repository/  Spring Data repositories
src/main/java/com/interviewscheduling/seed/        bootstrap admin
src/main/java/com/interviewscheduling/web/       page controllers
src/main/resources/templates/                     Thymeleaf templates
src/main/resources/application.properties         local H2, JPA, and admin seed settings
```
