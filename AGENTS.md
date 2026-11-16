# Agent instructions

A new chat should be able to take over this project from `docs/` without the previous conversation.

## Before you start

Read these, in order:

1. `docs/prompts/` — every user prompt, oldest first
2. `docs/design.md`
3. `docs/schema.md`

## After every user prompt

Add a new Markdown file in `docs/prompts/` before you finish the task.

- Name it `NNN-short-title.md`, using the next number.
- Paste the user prompt verbatim.
- Add a `Type` line. Use `plan` when the message is handled in plan mode, including plan revisions. Use `ask` when the message is a question and no project files are changed, including Ask mode. Use `build` when the message is handled by changing the application.
- Record the date and a short note of what was decided or changed.

Do this before the turn ends, including in plan mode and Ask mode. Do not wait until a plan is executed. If Ask mode blocks writing files, log that prompt on the next turn that can write.

Do not skip a prompt because it was only a question or a planning message.
