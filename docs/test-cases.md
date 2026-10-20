# CrowdSense - Test Cases

| Test ID | Module | Input | Expected Result | Status |
| --- | --- | --- | --- | --- |
| TC-001 | CrowdCategory | 94%, 88%, 42%, 30% | Critical, Critical, Moderate, Low | PASS (unit) |
| TC-002 | Occupancy | null occupancy | UNKNOWN/missing, not treated as zero | PASS (unit) |
| TC-003 | Recommendation | Bus 101 (94%) vs Bus 102 (42%) same route context | Bus 102 ranked higher, marked recommended | PASS (unit) |
| TC-004 | Recommendation | Missing occupancy vs known low | Known preferred over unknown | PASS (unit) |
| TC-005 | Recommendation | Tie on scores | Deterministic tie-break by bus number | PASS (unit) |
| TC-006 | API | POST /api/recommendations {source:"Central Bus Terminal", dest:"Tech Park Gate"} | Returns ranked list, recommended, explanation | PENDING (E2E) |
| TC-007 | Rewards | Confirm journey with better alternative | +5 points, balance updated | PENDING (E2E) |
| TC-008 | Rewards | Confirm same journeyKey twice | No duplicate award, balance unchanged | PENDING (E2E) |
| TC-009 | DB | Load schema.sql/data.sql | All tables created, FKs satisfied, 7 buses loaded | PASS |
| TC-010 | Python | tests/test_crowd_classifier.py | All pass | PASS |

Note: E2E tests require MySQL running and backend/frontend started.
