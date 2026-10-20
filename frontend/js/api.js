/**
 * API helper for CrowdSense frontend. Assumes backend runs on http://localhost:8080
 * (configurable via localStorage). All endpoints match the REST contract defined.
 */
class ApiClient {
    constructor(baseUrl = this.getBaseUrl()) {
        this.baseUrl = baseUrl.replace(/\/$/, '');
    }

    getBaseUrl() {
        return localStorage.getItem('crowdsense.api.baseUrl') || 'http://localhost:8080';
    }

    setBaseUrl(url) {
        if (url) {
            localStorage.setItem('crowdsense.api.baseUrl', url.replace(/\/$/, ''));
        }
    }

    async request(path, options = {}) {
        const url = `${this.baseUrl}${path.startsWith('/') ? path : `/${path}`}`;
        const headers = {
            'Content-Type': 'application/json',
            ...(options.headers || {})
        };

        const response = await fetch(url, {
            method: options.method || 'GET',
            headers,
            body: options.body ? JSON.stringify(options.body) : undefined
        });

        if (!response.ok) {
            let errorMessage = `Request failed: ${response.status} ${response.statusText}`;
            try {
                const err = await response.json();
                if (err && err.message) {
                    errorMessage = err.message;
                } else if (err && err.error) {
                    errorMessage = `${err.error}: ${err.message || ''}`;
                }
            } catch (e) {
                // Ignore JSON parse error
            }
            throw new Error(errorMessage);
        }

        // Some endpoints may return empty (204)
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            return response.json();
        }
        return null;
    }

    health() {
        return this.request('/api/health');
    }

    getBuses() {
        return this.request('/api/buses');
    }

    getRoutes() {
        return this.request('/api/routes');
    }

    searchBuses(source, destination) {
        const params = new URLSearchParams({ source, destination });
        return this.request(`/api/buses/search?${params.toString()}`);
    }

    recommend({ source, destination, passengerId, preference }) {
        const body = { source, destination };
        if (passengerId !== undefined && passengerId !== null) {
            body.passengerId = passengerId;
        }
        if (preference) {
            body.preference = preference;
        }
        return this.request('/api/recommendations', {
            method: 'POST',
            body
        });
    }

    getRewardBalance(passengerId) {
        return this.request(`/api/passengers/${passengerId}/rewards`);
    }

    getRewardHistory(passengerId) {
        return this.request(`/api/passengers/${passengerId}/rewards/history`);
    }

    confirmJourney({ passengerId, selectedBusId, source, destination, boardStopId, journeyKey }) {
        const body = { selectedBusId, source, destination, journeyKey };
        if (boardStopId !== undefined && boardStopId !== null) {
            body.boardStopId = boardStopId;
        }
        return this.request(`/api/passengers/${passengerId}/journeys/confirm`, {
            method: 'POST',
            body
        });
    }
}

window.ApiClient = ApiClient;
window.api = new ApiClient();
