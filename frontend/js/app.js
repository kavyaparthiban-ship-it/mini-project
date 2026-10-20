// Home page interactions
document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('quick-search-form');
    const sourceInput = document.getElementById('qs-source');
    const destInput = document.getElementById('qs-destination');

    if (form && sourceInput && destInput) {
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            const source = sourceInput.value.trim();
            const destination = destInput.value.trim();
            if (!source || !destination) {
                return;
            }
            // Navigate to dashboard with pre-filled values
            const params = new URLSearchParams({ source, destination });
            window.location.href = `dashboard.html?${params.toString()}`;
        });
    }

    // Populate stop list from routes
    if (window.api) {
        window.api.getRoutes()
            .then((routes) => {
                const list = document.getElementById('stop-list');
                if (!list || !Array.isArray(routes)) return;
                const seen = new Set();
                routes.forEach((r) => {
                    if (Array.isArray(r.stops)) {
                        r.stops.forEach((s) => {
                            if (s.stopName && !seen.has(s.stopName)) {
                                seen.add(s.stopName);
                                const opt = document.createElement('option');
                                opt.value = s.stopName;
                                list.appendChild(opt);
                            }
                        });
                    }
                });
            })
            .catch(() => {
                // Non-fatal
            });
    }
});
