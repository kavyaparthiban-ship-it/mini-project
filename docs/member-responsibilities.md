# Member Responsibilities

This document maps the five members to actual files and dependencies in the repository.

## Member 1 — Frontend and User Interface
**Files owned:**
- `frontend/index.html`
- `frontend/dashboard.html`
- `frontend/css/style.css`
- `frontend/js/app.js`
- `frontend/js/api.js`
- `frontend/js/dashboard.js`
- `frontend/README.md`

**Responsibilities:** Home page, passenger dashboard, search forms, recommendations display, route diagram, rewards view, validation/loading/error states, responsive layout, API wiring.

**Dependencies:** Backend REST APIs (Member 2), database routes/stops (Member 3).

## Member 2 — Java OOP and Backend
**Files owned:**
- `backend/pom.xml`
- `backend/src/main/java/com/crowdsense/**`
- `backend/src/main/resources/application.properties`
- `backend/src/test/java/com/crowdsense/**`
- `backend/src/test/resources/application.properties`
- `backend/README.md`

**Responsibilities:** OOP design (Bus/Metro/Route/Stop/Passenger/Occupancy/Recommendation/Preference), recommendation engine with scoring, REST APIs, DTOs, exception handling, JPA integration, tests.

**Dependencies:** Database schema/entities compatibility (Member 3), API contract consumed by frontend (Member 1).

## Member 3 — Database and Transport Data
**Files owned:**
- `database/schema.sql`
- `database/data.sql`
- `database/queries.sql`
- `database/README.md` (to be created if needed)

**Responsibilities:** Normalized schema, FKs/indexes, simulated sample data (6+ buses, routes, stops, occupancies), queries for search/alternatives/rewards, JPA alignment.

**Dependencies:** Consumed by backend (Member 2).

## Member 4 — Crowd Prediction and Recommendation Evaluation
**Files owned:**
- `crowd-analysis/data/historical_occupancy.csv`
- `crowd-analysis/src/crowd_classifier.py`
- `crowd-analysis/src/evaluate_recommendations.py`
- `crowd-analysis/tests/test_crowd_classifier.py`
- `crowd-analysis/results/evaluation_results.csv`
- `crowd-analysis/README.md`

**Responsibilities:** Historical data, baseline classifier, evaluation, optional Python model with requirements/training/eval.

**Dependencies:** Uses same thresholds/concepts as backend.

## Member 5 — Integration, Testing, and Documentation
**Files owned:**
- `docs/system-architecture.md`
- `docs/use-case-diagram.mmd`
- `docs/class-diagram.mmd`
- `docs/activity-diagram.mmd`
- `docs/sequence-diagram.mmd`
- `docs/test-plan.md`
- `docs/test-cases.md`
- `docs/integration-guide.md`
- `docs/project-report.md`
- `docs/member-responsibilities.md`
- `README.md` (root)
- `.gitignore`
- `.env.example`
- `scripts/` (helper scripts)

**Responsibilities:** Integration coordination, API contract consistency, Git workflow, end-to-end testing, UML diagrams, test plan/cases, installation/execution docs, report/presentation outline.

**Dependencies:** All modules.
