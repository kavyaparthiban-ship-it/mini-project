# System Architecture

## Overview
CrowdSense follows a layered architecture suitable for a college OOPS mini-project:

```
[Browser (HTML/CSS/JS)] --REST/JSON--> [Spring Boot API]
                                        |-- Controllers
                                        |-- Services (RecommendationEngine, CrowdClassification, Reward)
                                        |-- Repositories (Spring Data JPA)
                                        +-- DTOs/Exceptions
                                               |
                                               v
                                            [MySQL DB]
```

## Modules by Member
| Member | Module | Key Components |
| --- | --- | --- |
| Member 1 | `frontend/` | `index.html`, `dashboard.html`, `css/style.css`, `js/api.js`, `js/dashboard.js` |
| Member 2 | `backend/` | Java OOP models (`model/`), JPA entities, services, controllers, DTOs, exception handling, config |
| Member 3 | `database/` | `schema.sql`, `data.sql`, `queries.sql` |
| Member 4 | `crowd-analysis/` | Historical data, baseline classifier, evaluation, optional Python model |
| Member 5 | `docs/` | Diagrams, test plan/cases, integration guide, report, responsibilities |

## Data Flow (Recommendation)
1. Passenger enters source/destination in UI
2. Frontend calls `POST /api/recommendations`
3. Backend resolves stops, finds direct services or a one-transfer itinerary via the route-stop graph
4. Latest occupancy fetched and classified (missing/stale handled)
5. Recommendation engine scores and ranks alternatives (configurable weights)
6. UI displays ranked list with explanation and route diagram
7. Passenger confirms journey -> rewards checked, balance updated, duplicate prevented

When no direct service exists, the recommender can join up to four services
(three transfers) through shared stops. Each leg is returned in the recommendation
itinerary so the frontend can show each transfer clearly.

## Design Notes
- DTOs prevent entity leakage
- Centralised exception handling with proper HTTP status codes
- CORS enabled only for local frontend origins
- Schema/seed data owned by SQL files; Hibernate validates
- Reward logic is idempotent by `journey_key`
