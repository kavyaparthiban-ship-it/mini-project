# CrowdSense - Test Plan

## Objective
Validate the core functionality as specified: crowd classification, alternative selection/ranking, reward logic, API contracts, and end-to-end passenger journey.

## Test Scope
- Java unit/integration tests (JUnit 5, H2)
- API behavior (controllers)
- Database queries and mappings
- Frontend-to-backend integration (manual)
- Python classifier unit tests
- Reward duplicate prevention

## Test Environment
- Backend: Spring Boot 3.3.5, MySQL 8.4
- Tests use H2 in-memory
- Frontend: Chrome/Edge
- Python 3.14

## Automated Tests
- `CrowdCategoryTest` - thresholds and unknown handling
- `OccupancyTest` - percentage/count/missing/stale
- `RecommendationEngineServiceTest` - ranking, missing occupancy, tie-breaking
- `CrowdSenseApplicationTests` - Spring context loads

## Manual Test Cases
Covered in `test-cases.md`. Key: demo scenario CBT→TPG (Bus 101 vs 102), rewards, duplicate prevention.

## Pass/Fail Criteria
All automated tests pass, core demo scenario works end-to-end.
