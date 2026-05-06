# Repository Guidelines

## Project Structure & Module Organization
`PlayEdu/` is the active codebase. `PlayEdu/playedu-api/` is the Java 17 / Spring Boot 3 backend; `PlayEdu/playedu-admin/`, `PlayEdu/playedu-pc/`, and `PlayEdu/playedu-h5/` are React/Vite/TypeScript clients. Root `*.md` files guide the microservice redesign and are not runtime code. Keep changes scoped and preserve upstream copyright notices.

## Architecture & Design References
The root `*.md` files are authoritative design specifications. Consult them in this order:
1. `.clinerules` for engineering rules.
2. Domain standards: `microservice-dev.md`, `api-design.md`, `db-schema.md`, `react-frontend.md`, and `k8s-deploy.md`.
3. `playedu-microservice-architecture.md` for target topology and data flow.
4. `playedu-dev-implementation-plan.md` for delivery order and dependencies.

New code must comply with `.clinerules` and the relevant domain standard document.

## AI Collaboration Protocol
Before making multi-file changes inside a service, read these live docs in order:
1. `{service}/ENTITY_INDEX.md` — for entity inventory and field overview
2. `{service}/API_CONTRACT.md` — for request and response contracts
3. `{service}/DECISIONS.md` — for design decisions and rationale
4. `{service}/TODO.md` — for open follow-up work and known gaps

If a required live doc is missing or stale, create or update it in the same session before handoff.

## Build, Test, and Development Commands
`cd PlayEdu && docker compose up -d` starts MySQL and the stack. For frontend work, use `cd PlayEdu/playedu-admin && pnpm install && pnpm dev`; the same `pnpm dev` and `pnpm build` flow applies to `playedu-pc` and `playedu-h5`. For backend validation, run `cd PlayEdu/playedu-api && ./mvnw test`; build deliverables with `./mvnw clean package`.

## Coding Style & Standards
Frontend code uses TypeScript, 2-space indentation, functional React components, and request modules under `src/api/`. Backend code uses Java 17, 4-space indentation, layered Controller/Service/Mapper boundaries, and Spotless AOSP formatting through Maven. Follow `.clinerules` and `microservice-dev.md` for service boundaries, cache conventions, and transaction strategy. Preserve existing naming, including legacy paths such as `src/compenents/`, unless cleanup is the task.

## Testing Guidelines
Backend logic changes should add or update Maven-driven tests where practical. The frontend apps do not ship a committed test harness, so validate UI changes with `pnpm build` plus manual smoke checks for touched routes, forms, and API flows.

## Commit & Pull Request Guidelines
Recent history uses short, scoped commit subjects, often in Chinese, with `fixed:` or module-first prefixes. Keep commits focused on one module or service. Pull requests should list touched areas, config or env changes, database or API impact, and screenshots for UI changes.

## Project Evolution
Current state: the monolithic `PlayEdu/` codebase is the only runnable application. Planned evolution starts by extracting the user and course domains from `PlayEdu/playedu-api`, then adding services beside `PlayEdu/`: `edu-exam-svc/`, `edu-live-svc/`, `edu-train-svc/`, `edu-community-svc/`, `edu-point-svc/`, `edu-talent-svc/`, `edu-stats-svc/`, `edu-file-svc/`, and `edu-msg-svc/`. Docs may use `user-svc` or `course-svc`, but concrete services should follow the `edu-*-svc` pattern. New services use Spring Boot 3 / Java 17, share the common-starter approach, communicate through REST/OpenFeign plus MQ, and must never access another service's database directly.
