# =============================================================================
# CrowdSense - Root README
# =============================================================================
# CrowdSense: Intelligent Crowd Prediction and Personalized Multi-Modal
#              Travel Recommendation System
# =============================================================================

## Overview
CrowdSense helps passengers avoid overcrowded buses by identifying suitable
alternatives and recommending journeys based on source/destination, route
compatibility, occupancy, predicted crowd category, waiting/travel time,
distance to alternative stops, passenger preferences, and virtual rewards.

This is an OOPS mini-project with simulated data only. No real-time transport
integration or live operator feeds are used.

## Technology Stack
- **Frontend:** HTML5, CSS3, JavaScript
- **Backend:** Java 17, Spring Boot 3.3.5, Spring Web, Spring Data JPA, Bean Validation
- **Build:** Maven
- **Database:** MySQL 8.4
- **Testing:** JUnit 5, Spring Boot Test
- **Optional:** Python, Pandas, NumPy, scikit-learn
- **VCS:** Git/GitHub

## Repository Structure
```
mini-project/
  README.md
  .gitignore
  .env.example
  OOPS MINI.pdf        # Project report (reference)
  frontend/            # Member 1 - UI (HTML/CSS/JS)
  backend/             # Member 2 - Java OOP + Spring Boot APIs
  database/            # Member 3 - schema.sql, data.sql, Coimbatore demo network, queries.sql
  crowd-analysis/      # Member 4 - crowd prediction & evaluation
  docs/                # Member 5 - UML, test plan, integration guide, etc.
  scripts/             # Helper scripts
```

## Quick Start (Windows)

### 1) Prerequisites
- Java 17+ (tested with Java 26; target release 17)
- Maven 3.9.x (use the portable install in `C:\Users\KiTE\tools\apache-maven-3.9.9` if not on PATH)
- MySQL 8.4 running on localhost:3306
- Python 3.14 (optional modules)

### 2) Create database and load sample data
```powershell
$mysql = "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe"
Get-Content database\schema.sql -Raw | & $mysql -u root --port=3306
Get-Content database\data.sql   -Raw | & $mysql -u root --port=3306
Get-Content database\coimbatore-data.sql -Raw | & $mysql -u root --port=3306
```

### 3) Configure backend
The backend does not load `.env` files automatically. Set `DB_URL`,
`DB_USERNAME`, and `DB_PASSWORD` in the shell that starts Maven, or add local
overrides in `backend/src/main/resources/application-local.properties`.
Defaults expect `root` with an empty password and database `crowdsense`.

### 4) Build and run backend
```powershell
$env:Path = "C:\Users\KiTE\tools\apache-maven-3.9.9\bin;$env:Path"
cd backend
mvn clean test
mvn spring-boot:run
```
Backend: `http://localhost:8080`. Health: `GET /api/health`

### 5) Run frontend
```powershell
cd frontend
python -m http.server 5500
```
Open `http://localhost:5500` or `http://127.0.0.1:5500`.

### 6) Demo scenario
Search: **Source** `Central Bus Terminal` → **Destination** `Tech Park Gate`
- Bus **101**: ~94% (Critical)
- Recommended **Bus 102**: ~42% (Moderate), est. wait ~6 min
- Confirm journey to earn **5 virtual vouchers** (if improvement >= 15%)

### Coimbatore area coverage
The simulated network also includes major Coimbatore localities and nearby
towns, including Gandhipuram, Ukkadam, RS Puram, Saibaba Colony, Peelamedu,
Singanallur, the Airport, Vadavalli, Thudiyalur, Pollachi, Sulur, and
Mettupalayam. Type a locality or choose one from the dashboard suggestions.
Recommendations can use a direct route or up to three transfers at shared stops.
Routes, distances, occupancy, and travel times are illustrative demo data,
not verified operator schedules or live tracking.

## API Endpoints
| Method | Endpoint | Purpose |
| --- | --- | --- |
| GET | `/api/health` | Health check |
| GET | `/api/buses` | All buses with occupancy |
| GET | `/api/routes` | Routes with stops |
| GET | `/api/buses/search?source=...&destination=...` | Buses serving pair |
| POST | `/api/recommendations` | Ranked recommendations with explanation |
| GET | `/api/passengers/{id}/rewards` | Reward balance |
| GET | `/api/passengers/{id}/rewards/history` | Reward history |
| POST | `/api/passengers/{id}/journeys/confirm` | Confirm journey, award points (duplicate prevented) |

## Key OOP Concepts (Member 2)
- **Abstraction:** `TransportVehicle` base class
- **Inheritance:** `Bus`, `Metro` extend `TransportVehicle`
- **Encapsulation:** `Occupancy`, `Passenger`, `PassengerPreference`
- **Polymorphism:** Vehicles handled via `TransportVehicle` contract
- **Interfaces/Services:** Spring services, repositories

## Testing
```powershell
cd backend
mvn test
```
Covers crowd classification, recommendation ranking, tie-breaking, missing occupancy handling.

## Documentation
See `docs/` for architecture, UML diagrams (Mermaid), test plan/cases, integration guide, project report, and member responsibilities.

## Notes
- All data is **simulated** for college demonstration
- Credentials must not be committed; use environment variables
- MySQL errors are surfaced (no silent failures)
- Rewards are idempotent per `journeyKey`
