# PlayEdu Microservice Progress

Last updated: 2026-05-07

## Overall Status

| Area | Status | Notes |
| --- | --- | --- |
| `edu-common-starter` | Done | Shared starter reused by business services |
| `edu-user-svc` | Done | Read-only user and department bridge service |
| `edu-course-svc` | Done | Local profile + course query APIs + admin integration |
| `edu-exam-svc` | Done | Question bank, paper management, exam flow, auto grading |
| `edu-train-svc` | Done | Train project management + project stats aggregation |
| `edu-live-svc` | Done | Live room metadata service and local profile |
| `PlayEdu/playedu-admin` | Done | Admin UI for course, exam, train, live; train effect page validated |
| `PlayEdu/playedu-h5` | In progress | Learner exam flow + train task detail flow are running end-to-end |

## Current Milestones

### 1. Backend microservice baseline is stable
- 6 services compile successfully.
- Local single-node profiles are available for `edu-exam-svc`, `edu-train-svc`, `edu-course-svc`, and `edu-live-svc`.
- H2 seed data supports local demonstrations without Nacos, MySQL, or Redis clusters.

### 2. Admin-side product story is complete
- Admin can manage course, exam, train, and live data through the React admin app.
- Train project detail page shows project progress, task completion, and student progress.
- Real exam statistics are already wired into train project effect data.

### 3. Learner-side H5 story has started running
- Added H5 exam pages:
  - `/exam`
  - `/exam/room/:paperId`
  - `/exam/result/:paperId`
- Added H5 train entry page:
  - `/train`
- Added H5 train detail page:
  - `/train/:projectId`
  - supports task-level routing into course and exam pages
- Added local learner bypass for development:
  - `VITE_LOCAL_DEV_BYPASS=true`
  - `VITE_LOCAL_USER_ID=10005`

## Validated End-to-End Flows

### Admin effect page
- Validated URL: `http://127.0.0.1:3001/train/projects/train-local-001`
- Verified UI states:
  - overview progress card
  - task completion table
  - student progress table

### Learner H5 exam flow
- Validated URL: `http://127.0.0.1:3002/exam`
- Real flow completed:
  1. learner `10005` entered exam `paper-local-001`
  2. answered all 3 questions
  3. submitted paper
  4. received result page with score and answer review

### Learner H5 train detail flow
- Validated URL: `http://127.0.0.1:3002/train/train-local-001`
- Verified UI states:
  - project title and description
  - training time range
  - overall progress bar
  - task list with status tags
- Verified task routing:
  - exam task routes to `/exam/room/paper-local-001`
  - completed course task routes to `/course/101`

### Stats change confirmed after learner submission
- `edu-exam-svc`
  - API: `GET /api/v1/exam-records/papers/paper-local-001/stats`
  - latest state: `passedCount=3`, `passRate=60`
- `edu-train-svc`
  - API: `GET /api/v1/train-projects/train-local-001/stats`
  - latest state: `overallCompletionRate=80`
  - learner `10005` now shows `已通过(20分)`

## Key Fixes Landed In This Round

- Fixed local CORS for H5 by allowing `http://127.0.0.1:3002` and `http://localhost:3002` on:
  - `edu-exam-svc`
  - `edu-train-svc`
  - `edu-course-svc`
  - `edu-live-svc`
- Fixed H5 HTTP client to accept backend success code `"0"` in addition to numeric `0`.
- Fixed H5 train list time display to use local time formatting instead of forcing UTC offset.

## Evidence Artifacts

- Admin screenshots:
  - `/home/haoran/qixuexing_project/admin-train-detail-overview-after-h5.png`
  - `/home/haoran/qixuexing_project/admin-train-detail-students-after-h5.png`
- H5 screenshots:
  - `/home/haoran/qixuexing_project/h5-exam-list-after-fix.png`
  - `/home/haoran/qixuexing_project/h5-exam-room-q1.png`
  - `/home/haoran/qixuexing_project/h5-exam-submit-confirm.png`
  - `/home/haoran/qixuexing_project/h5-exam-result.png`
  - `/home/haoran/qixuexing_project/h5-train-list-time-fixed.png`
- Earlier admin effect screenshots:
  - `/home/haoran/qixuexing_project/output/playwright/train-project-detail-overview.png`
  - `/home/haoran/qixuexing_project/output/playwright/train-project-detail-students.png`

## Current Dev Entry Points

- Admin: `http://127.0.0.1:3001`
- H5: `http://127.0.0.1:3002`
- Exam service: `http://127.0.0.1:8081`
- Train service: `http://127.0.0.1:8082`
- Course service: `http://127.0.0.1:8083`
- Live service: `http://127.0.0.1:8084`

## Remaining Gaps

- H5 still uses local bypass instead of real login and user context.
- H5 exam list state is derived from local storage, not a server-side "my records" API.
- Course learning and live participation pages on H5 are still placeholders.
- Admin still has non-blocking frontend warnings:
  - deprecated `Button.Group`
  - React Router future warnings

## Recommended Next Step

1. Replace H5 local learner state with real `my exams` and `my train tasks` APIs.
2. Fill the H5 live participation and richer course learning path beyond the current compatibility bridge.
3. Add one short demo script or screen recording checklist for team presentation.
