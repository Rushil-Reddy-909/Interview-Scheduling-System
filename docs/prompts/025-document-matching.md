# 025 — Document matching

Date: 2026-11-16

Type: build

## Prompt

Write the documentation files only and after that can you provider few examples that cover various cases of scheduling and tie breaking

## Outcome

- Wrote prompts 023 and 024, which plan mode could not save earlier.
- `docs/design.md`:
  - The manual-pairing calendar flow is replaced with pool-based matching, single and bulk, for create and reschedule.
  - Added the ranking rules, the bulk greedy order, all-or-nothing confirm, and that a reschedule may change the interviewer.
  - Removed automatic matching from "Left for later".
- `README.md`: the introduction now describes the matching flow.
- `docs/schema.md`: no new columns. Added the blocking-interval range query, that a replacement may change `interviewer_id`, and the batch lock order.
- Gave worked examples of scheduling and tie-breaking in chat. They were not written to the docs.
- No Java code, configuration, or tests were changed.
