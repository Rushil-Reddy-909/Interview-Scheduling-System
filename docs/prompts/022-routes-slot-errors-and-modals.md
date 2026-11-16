# 022 — Routes, slot errors, and modals

Date: 2026-11-16

Type: ask

## Prompt

so for the pages, we will have a simple landing page(/) and login (/login) as static pages.

As for role based pages.

All admin pages will come under (/admin/*) we will decide admin pages later, you can suggest any ideas.
AS for other roles, im thinking whether a common route (/dashboard) that routes to appropriate user dashboard based on credentials or role based dashboard (/dashboard/candidate and so on...)

How complex would a dynamic /dashboard be with all the events of rendering different stuff?

As for slot rejection, we will create custom exceptions, for all slot rejection, we will use a common exception with a tailored message that is returned to the user.

Also does thymeleaf support nested conditional rendering? Like clicking on something opens a modal, and after submitting in that modal, another block is shown in that modal.

## Outcome

- Answered without changing the design or the code.
- `/` and `/login` are public Thymeleaf pages. Login cannot be a file under `static/` because Spring Security has to handle the form post.
- Recommended `/dashboard` as a redirect to `/dashboard/candidate`, `/dashboard/recruiter`, or `/dashboard/interviewer`, each with its own template. One template that branches on every role gets hard to change. Admin stays under `/admin/*`, with users and all-interviews as the first two pages.
- One slot-rejection exception with a message is enough. A controller advice puts that message back on the calendar page. It should not be a server error page.
- Thymeleaf nested `th:if` works. A modal is HTML already on the page, shown by the browser. After a submit, the server renders the page again with the next block visible. Thymeleaf does not update the open modal by itself.
