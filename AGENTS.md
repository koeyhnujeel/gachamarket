# Repository Guidelines

## Project Structure & Module Organization
This repository is currently plan-first: working documents live in `docs/superpowers/specs/` and `docs/superpowers/plans/`. The approved MVP spec is `docs/superpowers/specs/2026-03-29-gachamarket-mvp-core-platform-design.md`, and implementation must follow the plan index in `docs/superpowers/plans/2026-03-29-gachamarket-implementation-plan-index.md` in order.

Planned runtime code is split by service:
- `backend/`: Spring Boot + Modulith application, migrations, and JUnit/Testcontainers tests
- `frontend/`: Next.js App Router app, UI components, and Vitest/RTL tests

## Build, Test, and Development Commands
Implementation has not been scaffolded yet, so these commands become valid as the corresponding plan tasks land:
- `cd backend && ./gradlew test`: run backend tests
- `cd backend && ./gradlew bootRun`: start the backend locally
- `cd frontend && pnpm dev`: run the frontend locally
- `cd frontend && pnpm test`: run frontend unit tests

Before adding new commands, document them in the relevant plan file.

## Coding Style & Naming Conventions
Follow the languages and structure defined in the plans: Java 21 for `backend/`, TypeScript for `frontend/`. Use standard naming patterns:
- Java classes: `PascalCase`; methods and fields: `camelCase`
- React components: `PascalCase`
- Test files: `*Test.java` and `*.test.ts(x)`

Until formatter configs are added, keep Java/Kotlin indentation at 4 spaces and frontend code at 2 spaces where the framework expects it. Prefer small, focused files over large cross-cutting modules.

## Testing Guidelines
Backend tests should use JUnit 5 and Testcontainers where database behavior matters. Frontend tests should use Vitest and React Testing Library. Add or update tests in the same change as production code, and keep tests close to the feature they cover, for example `backend/src/test/java/...` or `frontend/src/components/__tests__/...`.

## Commit & Pull Request Guidelines
Existing history includes short imperative commits, but contributors should now write commit messages in Korean. Keep commits small, descriptive, and action-focused, for example `MVP 코어 플랫폼 설계 스펙 추가` or `반응형 웹 설계 반영`. If you adopt scoped conventional commits during implementation, keep the scope format consistent and write the summary in Korean.

Pull requests should include:
- a short problem/solution summary
- links to the relevant spec and plan files
- test results or an explicit note that code is not scaffolded yet
- screenshots for visible frontend changes

## Planning Workflow
Do not skip the docs workflow. Update specs in `docs/superpowers/specs/`, write executable tasks in `docs/superpowers/plans/`, then implement in the documented order.
