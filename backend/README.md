# CrowdSense Backend (Java Spring Boot)

This module implements the Java OOP engine, REST APIs, and business logic
for CrowdSense as specified in the project report.

## Member
**Member 2 — Java OOP and Backend**

## Responsibilities
- Design OOP classes (Bus, Metro, Route, Stop, Passenger, Occupancy, Recommendation, Reward/PassengerPreference)
- Apply encapsulation, abstraction, inheritance, interfaces/polymorphism (see `com.crowdsense.model`)
- Implement crowd classification and alternative-bus selection
- Implement the recommendation-ranking service with a documented, configurable scoring formula
- Create Spring Boot REST APIs (controllers/services/repositories/entities/dto/exception)
- Connect to MySQL via Spring Data JPA
- Write unit and integration tests

## Package layout
```
backend/src/main/java/com/crowdsense/
  controller/   # REST endpoints
  service/      # Business logic and recommendation engine
  repository/   # Spring Data JPA repositories
  model/        # OOP domain classes (abstraction/inheritance/polymorphism)
  entity/       # JPA entities mapping to MySQL
  dto/          # Request/response DTOs
  exception/    # Centralised exception handling
  config/       # CORS and configuration properties
```

## Configuration
`src/main/resources/application.properties` reads MySQL connection details from
environment variables. **Do not commit credentials.** See `../../.env.example`.

## Build and run (Windows)
```powershell
# From repo root
$env:Path = "C:\Users\KiTE\tools\apache-maven-3.9.9\bin;$env:Path"
cd backend
mvn clean test
mvn spring-boot:run
```

The API will start on `http://localhost:8080` when MySQL is running and
`database/schema.sql` + `database/data.sql` have been loaded.

## Tests
```powershell
mvn test
```
Tests cover crowd classification, recommendation ranking/tie-breaking/missing-occupancy
handling, and Spring context loading (using H2).
