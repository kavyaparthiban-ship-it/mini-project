# CrowdSense Frontend

This module contains the HTML, CSS and JavaScript for the CrowdSense user interface.

## Member
**Member 1 — Frontend and User Interface**

## Files
- `index.html` — Home page with project overview, demo scenario and quick search
- `dashboard.html` — Passenger dashboard with search, recommendations, comparison, route diagram and rewards
- `css/style.css` — Responsive, accessible styles
- `js/api.js` — API client that calls the real backend endpoints
- `js/app.js` — Home page interactions
- `js/dashboard.js` — Dashboard logic (search, ranking display, confirmation, rewards)

## Running locally
The frontend is static HTML/JS/CSS — no Node.js/React required. Serve it with any static server:

```powershell
# From frontend directory (example using Python)
python -m http.server 5500
# or use VS Code Live Server
```

Then open `http://localhost:5500` (or the origin configured in `backend/src/main/resources/application.properties` for CORS).

## API connectivity
All UI actions call the backend:
- `GET /api/health`
- `GET /api/routes`
- `POST /api/recommendations`
- `POST /api/passengers/{id}/journeys/confirm`
- `GET /api/passengers/{id}/rewards` and `/rewards/history`

Update the API base URL if needed via `localStorage.setItem('crowdsense.api.baseUrl', 'http://...')`.
