# CrowdSense Frontend

This module contains the HTML, CSS and JavaScript for the CrowdSense user interface.

## Member
**Member 1 — Frontend and User Interface**

## Files
- `index.html` — Welcome page and entry point
- `auth.html` — Sign-in and account registration page
- `dashboard.html` — Signed-in passenger dashboard with search, recommendations, comparison, route diagram and rewards
- `css/style.css` — Responsive, accessible styles
- `js/api.js` — API client that calls the real backend endpoints
- `js/auth-session.js` — Browser session profile and dashboard access gate
- `js/auth.js` — Sign-in and registration interactions
- `js/app.js` — Welcome page interactions
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
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/routes`
- `POST /api/recommendations`
- `POST /api/passengers/{id}/journeys/confirm`
- `GET /api/passengers/{id}/rewards` and `/rewards/history`

Update the API base URL if needed via `localStorage.setItem('crowdsense.api.baseUrl', 'http://...')`.
