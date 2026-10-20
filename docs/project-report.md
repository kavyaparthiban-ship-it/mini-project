# CrowdSense - Project Report (Summary)

## Title
**CrowdSense: Intelligent Crowd Prediction and Personalized Multi-Modal Travel Recommendation System**

## Objective
Provide an intelligent system that moves beyond displaying crowd info to actively recommending suitable alternatives and incentivizing less-crowded travel.

## Methodology
1. Data (simulated): buses, routes, stops, route-stop ordering, occupancy records
2. Crowd classification: baseline thresholds (Critical >=85, High >=65, Moderate >=40, Low <40)
3. Alternative identification: buses serving destination (feasible)
4. Recommendation: score = w_crowd*(1-occ%) + w_wait*(1-wait/max) + w_travel*(1-dist/max) + w_walk*walkScore; penalties for missing/stale occupancy
5. Personalization: passenger preferences
6. Incentives: virtual rewards for meaningful improvements, duplicate prevention

## Implementation
- Frontend: HTML/CSS/JS (static)
- Backend: Java OOP (abstraction/inheritance/polymorphism/encapsulation) + Spring Boot
- DB: MySQL normalized schema
- Analysis: Python baseline + evaluation

## Key Features
- Ranked recommendations with explanation
- Crowd categories and occupancy
- Reward balance + history
- Duplicate reward prevention
- Configurable weights/thresholds
- Route visualization

## Demo
Bus 101 (94%) → Bus 102 (42%), est. wait 6 min, reward 5 virtual vouchers (simulated).

## Conclusion
Functional prototype meeting requirements with real implementation (no placeholders).
