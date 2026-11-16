# 010 — Duplicate repository bean

Date: 2026-11-16

Type: build

## Prompt

The bean 'appUserRepository', defined in com.interviewscheduling.domain.AppUserRepository defined in @EnableJpaRepositories declared on DataJpaRepositoriesRegistrar.EnableJpaRepositoriesConfiguration, could not be registered. A bean with that name has already been defined in com.interviewscheduling.repository.AppUserRepository defined in @EnableJpaRepositories declared on DataJpaRepositoriesRegistrar.EnableJpaRepositoriesConfiguration and overriding is disabled.

i get this error, but i already moved appuser from domain to repository, domain just has 2 classes

## Outcome

- The source already had a single `AppUserRepository`, in `com.interviewscheduling.repository`. `domain` only has `AppUser` and `Interview`.
- `target/classes` still contained `com.interviewscheduling.domain.AppUserRepository`, plus the old `domain` copies of `UserRole` and `InterviewStatus`. Maven incremental compile does not delete class files after a source file is moved, so Spring registered both repositories.
- Ran `./mvnw clean test`. The stale classes are gone and the tests pass.
- Updated the README layout for `enums` and `repository`.
