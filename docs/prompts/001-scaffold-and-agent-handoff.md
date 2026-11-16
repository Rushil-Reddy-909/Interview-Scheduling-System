# 001 — Scaffold and agent handoff

Date: 2026-11-16

## Prompt

So a few instructions for this chat, every prompt (including this) used should be documented in a docs/ folder, this folder should also provide documentation for schema, design and other essential information about the project.

Tech stack , we will be using spring boot and for frontend, im thinking whether using thymeleaf would be good as frontend.

Build a system where recruiters pick candidates and interviewers, and the system proposes interview slots. Candidates can confirm a slot or ask to reschedule.

This is the requirement we will build. So first scaffold the project, like setting up maven, the important packages like spring validation, jpa etc, gitignore, README.md.

So will all agents read AGENTS.md file? if so create one, if not, how to point all agents to read a file for context before starting? This agents.md will contain instructions like documenting prompts so even a new chat can takeover easily. For now agents.md will have only that, we will decide and add more tools/skills later.

## Outcome

- Scaffolded a Maven Spring Boot 4.1.2 application with Web MVC, Thymeleaf, Spring Data JPA, Bean Validation, H2, and DevTools. The first pass targeted Java 21; prompt 002 changed that to Java 17.
- Chose Thymeleaf for the first UI and recorded why in `docs/design.md`.
- Added `AGENTS.md` at the repo root. Cursor loads that file as an always-on rule for agent chats in this project. It only tells agents to read and extend `docs/prompts/` plus the design and schema docs.
- Wrote the starting product design and proposed schema. No scheduling features are implemented.
