# 003 — H2 persistence question

Date: 2026-11-16

## Prompt

one thing, is h2 jut an in memory storage as DB? i was thinking of using sqlite if h2 doesn't persist data on disk

## Outcome

- Answered: the current `jdbc:h2:mem:` URL is in-memory and loses data on restart. H2 can also run in file mode (`jdbc:h2:file:./data/interview-scheduling`), which persists on disk.
- Compared H2 file mode with SQLite. SQLite needs an extra driver and a community Hibernate dialect, and it allows only one writer at a time.
- The user chose H2 file mode. Changed `spring.datasource.url` to `jdbc:h2:file:./data/interview-scheduling;AUTO_SERVER=TRUE`, git-ignored `/data/`, and updated the README.
