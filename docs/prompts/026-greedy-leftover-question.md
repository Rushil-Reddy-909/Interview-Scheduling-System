# 026 — Can single mode place a candidate bulk left out

Date: 2026-11-16

Type: ask

## Prompt

ok so a doubt, suppose i use bulk schedule and 1 candidate is left out, but bulk is greedy, if i run single schedule on that left out candidate, is it possible that he will be scheduled or does bulk make sure it tries its best?

## Outcome

- Answered without changing the design.
- After the bulk proposal is confirmed, single mode with the same pool, range, and duration finds nothing for the left-out candidate. Bulk had already checked every option for that candidate. Candidates placed after them only took more time.
- Single mode can find a slot when the inputs change: a wider range, a bigger pool, a shorter duration, or a booking that moved.
- Running single mode before the bulk confirm can propose a slot that the unconfirmed bulk proposal also holds. Whichever confirms second is rejected.
- Bulk does not guarantee its best. A different arrangement of the other candidates can fit everyone. Example: I1 is free only 15:00–17:00. C1 is free from 14:00 on. C2 is free all day except 16:00–17:00. C1 counts as more constrained, so C1 goes first and takes 15:00, and C2 is left out. Placing C2 at 15:00 and C1 at 16:00 fits both.
- Two ways to fix this:
  - a repair step that moves an already placed candidate to free a slot,
  - exact maximum matching for bulk create.
- No decision yet.
