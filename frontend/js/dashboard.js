document.addEventListener('DOMContentLoaded', () => {
    const searchForm = document.getElementById('search-form');
    const sourceInput = document.getElementById('source');
    const destInput = document.getElementById('destination');
    const passengerSelect = document.getElementById('passenger-select');
    const btnClear = document.getElementById('btn-clear');

    const prefLow = document.getElementById('pref-low-crowd');
    const prefWait = document.getElementById('pref-short-wait');
    const prefTravel = document.getElementById('pref-short-travel');
    const prefWalk = document.getElementById('pref-short-walk');
    const prefMaxWalk = document.getElementById('pref-max-walk');

    const loadingEl = document.getElementById('loading');
    const errorEl = document.getElementById('error');
    const infoEl = document.getElementById('info');
    const resultsEl = document.getElementById('results');

    const routeSummaryEl = document.getElementById('route-summary');
    const recommendedEl = document.getElementById('recommended');
    const explanationEl = document.getElementById('explanation');
    const scoringTableEl = document.getElementById('scoring-table');
    const demoNoteEl = document.getElementById('demo-note');

    const allBusesBody = document.querySelector('#all-buses-table tbody');
    const selectedBusSelect = document.getElementById('selected-bus');
    const boardStopSelect = document.getElementById('board-stop');
    const journeyKeyInput = document.getElementById('journey-key');
    const confirmForm = document.getElementById('confirm-form');
    const confirmResultEl = document.getElementById('confirm-result');

    const rewardBalanceEl = document.getElementById('reward-balance');
    const rewardPassengerEl = document.getElementById('reward-passenger');
    const rewardHistoryBody = document.querySelector('#reward-history-table tbody');

    const routeDiagramEl = document.getElementById('route-diagram');
    const backendStatusEl = document.getElementById('backend-status');

    let lastRecommendation = null;

    const api = window.api;

    function show(el, message) {
        if (!el) return;
        el.textContent = message || '';
        el.classList.remove('hidden');
    }

    function hide(el) {
        if (!el) return;
        el.classList.add('hidden');
    }

    function setLoading(loading) {
        if (!loadingEl) return;
        if (loading) {
            loadingEl.classList.remove('hidden');
        } else {
            loadingEl.classList.add('hidden');
        }
    }

    function clearAlerts() {
        hide(errorEl);
        hide(infoEl);
    }

    function crowdTag(category) {
        const c = (category || 'Unknown').toLowerCase();
        switch (c) {
            case 'low': return '<span class="tag tag-low">Low</span>';
            case 'moderate': return '<span class="tag tag-moderate">Moderate</span>';
            case 'high': return '<span class="tag tag-high">High</span>';
            case 'critical': return '<span class="tag tag-critical">Critical</span>';
            default: return '<span class="tag tag-unknown">Unknown</span>';
        }
    }

    function formatPercent(p) {
        if (p === null || p === undefined || Number.isNaN(p)) {
            return 'N/A';
        }
        return `${Math.round(p)}%`;
    }

    function formatNumber(n, decimals = 2) {
        if (n === null || n === undefined || Number.isNaN(n)) {
            return 'N/A';
        }
        return n.toFixed(decimals);
    }

    function generateJourneyKey() {
        const d = new Date();
        const ts = `${d.getFullYear()}${String(d.getMonth()+1).padStart(2,'0')}${String(d.getDate()).padStart(2,'0')}-${String(d.getHours()).padStart(2,'0')}${String(d.getMinutes()).padStart(2,'0')}${String(d.getSeconds()).padStart(2,'0')}`;
        return `JRN-${ts}`;
    }

    async function checkBackend() {
        if (!backendStatusEl || !api) return;
        try {
            const h = await api.health();
            backendStatusEl.textContent = `API: ${h.status || 'UP'}`;
            backendStatusEl.style.color = 'var(--success)';
        } catch (e) {
            backendStatusEl.textContent = 'API: UNREACHABLE (start backend on :8080)';
            backendStatusEl.style.color = 'var(--danger)';
        }
    }

    async function populateStops() {
        try {
            const routes = await api.getRoutes();
            if (!Array.isArray(routes) || !sourceInput || !destInput) return;
            const seen = new Set();
            const list = document.getElementById('stop-list');
            if (list) list.innerHTML = '';
            routes.forEach(r => {
                if (Array.isArray(r.stops)) {
                    r.stops.forEach(s => {
                        if (s.stopName && !seen.has(s.stopName)) {
                            seen.add(s.stopName);
                            const opt = document.createElement('option');
                            opt.value = s.stopName;
                            if (list) list.appendChild(opt);
                        }
                    });
                }
            });
        } catch (e) {
            // non-fatal
        }
    }

    async function loadRewards(passengerId) {
        if (!passengerId) return;
        try {
            const bal = await api.getRewardBalance(passengerId);
            if (rewardBalanceEl) rewardBalanceEl.textContent = String(bal.rewardBalance || 0);
            if (rewardPassengerEl) rewardPassengerEl.textContent = `${bal.passengerName || ''} (#${bal.passengerId || ''})`;
            const hist = await api.getRewardHistory(passengerId);
            if (rewardHistoryBody) {
                rewardHistoryBody.innerHTML = '';
                (hist || []).forEach(tx => {
                    const tr = document.createElement('tr');
                    tr.innerHTML = `
                        <td>${tx.createdAt ? new Date(tx.createdAt).toLocaleString() : ''}</td>
                        <td>${tx.journeyKey || ''}</td>
                        <td>${tx.transactionType || ''}</td>
                        <td>${tx.points || 0}</td>
                        <td>${tx.description || ''}</td>
                    `;
                    rewardHistoryBody.appendChild(tr);
                });
            }
        } catch (e) {
            // non-fatal
        }
    }

    function buildPreferenceOverride() {
        const pref = {};
        if (prefLow && prefLow.value !== '') pref.preferLowCrowd = parseInt(prefLow.value);
        if (prefWait && prefWait.value !== '') pref.preferShortWait = parseInt(prefWait.value);
        if (prefTravel && prefTravel.value !== '') pref.preferShortTravel = parseInt(prefTravel.value);
        if (prefWalk && prefWalk.value !== '') pref.preferShortWalk = parseInt(prefWalk.value);
        if (prefMaxWalk && prefMaxWalk.value !== '') pref.maxWalkDistanceKm = parseFloat(prefMaxWalk.value);
        const hasAny = Object.keys(pref).length > 0;
        return hasAny ? pref : undefined;
    }

    async function doRecommend() {
        if (!sourceInput || !destInput) return;
        const source = sourceInput.value.trim();
        const destination = destInput.value.trim();
        const passengerIdVal = passengerSelect ? parseInt(passengerSelect.value) : null;
        if (!source || !destination) {
            show(errorEl, 'Source and destination are required.');
            return;
        }

        clearAlerts();
        setLoading(true);
        hide(resultsEl);
        lastRecommendation = null;

        try {
            const resp = await api.recommend({
                source,
                destination,
                passengerId: Number.isNaN(passengerIdVal) ? undefined : passengerIdVal,
                preference: buildPreferenceOverride()
            });
            lastRecommendation = resp;
            renderRecommendation(resp);
            if (!resultsEl.classList.contains('hidden')) {
                // already shown
            }
            resultsEl.classList.remove('hidden');
            if (journeyKeyInput) journeyKeyInput.value = generateJourneyKey();
            await loadRewards(Number.isNaN(passengerIdVal) ? undefined : passengerIdVal);
        } catch (e) {
            show(errorEl, e.message || 'Failed to fetch recommendations.');
        } finally {
            setLoading(false);
        }
    }

    function renderRecommendation(resp) {
        if (routeSummaryEl) {
            routeSummaryEl.textContent = `${resp.sourceStopName || resp.source} → ${resp.destinationStopName || resp.destination}`;
        }
        // Recommended
        if (recommendedEl && resp.recommended) {
            const r = resp.recommended;
            recommendedEl.innerHTML = `
                <div class="recommended-grid">
                    <div class="recommended-item">
                        <h4>${r.busNumber} — ${r.routeName} (${r.transportMode})</h4>
                        <p class="muted">Route: ${r.routeCode}</p>
                        <p>${crowdTag(r.crowdCategory)} &nbsp; ${formatPercent(r.occupancyPercent)} occupancy ${r.occupancyStale ? ' (stale)' : ''} ${r.occupancyMissing ? ' (missing)' : ''}</p>
                    </div>
                    <div class="recommended-item">
                        <h4>Times &amp; Distance</h4>
                        <p>Wait: <strong>${r.estimatedWaitMinutes} min</strong> &nbsp; Travel: <strong>${r.estimatedTravelMinutes} min</strong></p>
                        <p>Walk: <strong>${formatNumber(r.walkDistanceKm,2)} km</strong> &nbsp; Trip dist: <strong>${r.tripDistanceKm === null || r.tripDistanceKm === undefined ? 'N/A' : formatNumber(r.tripDistanceKm,2)+' km'}</strong></p>
                        <p>Board at: <strong>${r.boardStopName || 'N/A'}</strong> (ID ${r.boardStopId || 'N/A'})</p>
                    </div>
                    <div class="recommended-item">
                        <h4>Score</h4>
                        <p><strong>${formatNumber(r.score,3)}</strong> (higher is better)</p>
                        <p>Breakdown: Crowd ${formatNumber(r.crowdScore,2)} • Wait ${formatNumber(r.waitScore,2)} • Travel ${formatNumber(r.travelScore,2)} • Walk ${formatNumber(r.walkScore,2)}</p>
                    </div>
                </div>
                <ol class="recommended-reasons">
                    ${(r.reasons || []).map(x => `<li>${x}</li>`).join('')}
                </ol>
            `;
        }
        // Explanation
        if (explanationEl) {
            explanationEl.innerHTML = `
                <p>${resp.explanation || ''}</p>
            `;
        }
        if (demoNoteEl) {
            demoNoteEl.textContent = resp.demoNote || '';
        }
        // Scoring table
        if (scoringTableEl && resp.scoring) {
            scoringTableEl.innerHTML = `
                <table class="table">
                    <thead><tr><th>Weight (Crowd)</th><th>Weight (Wait)</th><th>Weight (Travel)</th><th>Weight (Walk)</th><th>Formula</th></tr></thead>
                    <tbody><tr>
                        <td>${formatNumber(resp.scoring.weightCrowd,3)}</td>
                        <td>${formatNumber(resp.scoring.weightWait,3)}</td>
                        <td>${formatNumber(resp.scoring.weightTravel,3)}</td>
                        <td>${formatNumber(resp.scoring.weightWalk,3)}</td>
                        <td>${resp.scoring.formula}</td>
                    </tr></tbody>
                </table>
            `;
        }
        // All buses table
        if (allBusesBody) {
            allBusesBody.innerHTML = '';
            (resp.availableBuses || []).forEach((item, idx) => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${idx + 1}</td>
                    <td><strong>${item.busNumber}</strong></td>
                    <td>${item.routeCode} — ${item.routeName}</td>
                    <td>${item.transportMode}</td>
                    <td>${crowdTag(item.crowdCategory)}</td>
                    <td>${formatPercent(item.occupancyPercent)}${item.occupancyStale ? ' (stale)' : ''}${item.occupancyMissing ? ' (missing)' : ''}</td>
                    <td>${item.estimatedWaitMinutes} min</td>
                    <td>${item.estimatedTravelMinutes} min</td>
                    <td>${formatNumber(item.walkDistanceKm,2)} km</td>
                    <td>${item.tripDistanceKm === null || item.tripDistanceKm === undefined ? 'N/A' : formatNumber(item.tripDistanceKm,2)+' km'}</td>
                    <td><strong>${formatNumber(item.score,3)}</strong>${item.recommended ? ' ✓' : ''}</td>
                    <td>${item.boardStopName || ''}</td>
                    <td>${(item.reasons || []).join('; ')}</td>
                    <td><button class="btn btn-ghost btn-small" data-select-bus="${item.busId}" data-board="${item.boardStopId || ''}">Select</button></td>
                `;
                allBusesBody.appendChild(tr);
            });
        }
        // Selected bus dropdown
        if (selectedBusSelect) {
            selectedBusSelect.innerHTML = '';
            (resp.availableBuses || []).forEach(item => {
                const opt = document.createElement('option');
                opt.value = item.busId;
                opt.textContent = `${item.busNumber} — ${item.routeCode} (${item.crowdCategory}, ${formatPercent(item.occupancyPercent)})`;
                if (item.recommended) opt.selected = true;
                selectedBusSelect.appendChild(opt);
            });
        }
        // Board stop dropdown
        if (boardStopSelect) {
            boardStopSelect.innerHTML = '';
            const seenStops = new Set();
            (resp.availableBuses || []).forEach(item => {
                if (item.boardStopId && !seenStops.has(item.boardStopId)) {
                    seenStops.add(item.boardStopId);
                    const opt = document.createElement('option');
                    opt.value = item.boardStopId;
                    opt.textContent = item.boardStopName || `Stop ${item.boardStopId}`;
                    if (resp.recommended && resp.recommended.boardStopId === item.boardStopId) {
                        opt.selected = true;
                    }
                    boardStopSelect.appendChild(opt);
                }
            });
            if (boardStopSelect.options.length === 0 && resp.recommended && resp.recommended.boardStopId) {
                const opt = document.createElement('option');
                opt.value = resp.recommended.boardStopId;
                opt.textContent = resp.recommended.boardStopName || `Stop ${resp.recommended.boardStopId}`;
                opt.selected = true;
                boardStopSelect.appendChild(opt);
            }
        }
        // Route diagram
        if (routeDiagramEl && resp.availableBuses) {
            const rec = resp.recommended;
            if (rec) {
                // Render a simple diagram from stops on rec's route if available
                api.getRoutes().then(routes => {
                    const route = (routes || []).find(r => r.routeCode === rec.routeCode);
                    if (route && Array.isArray(route.stops) && route.stops.length > 0) {
                        routeDiagramEl.innerHTML = '';
                        route.stops.forEach((s, i) => {
                            const stopEl = document.createElement('div');
                            stopEl.className = 'route-stop';
                            stopEl.innerHTML = `
                                <div class="route-dot"></div>
                                <div class="route-stop-name">${s.stopName}</div>
                                <div class="route-stop-code">${s.stopCode}</div>
                            `;
                            routeDiagramEl.appendChild(stopEl);
                            if (i < route.stops.length - 1) {
                                const line = document.createElement('div');
                                line.className = 'route-line';
                                routeDiagramEl.appendChild(line);
                            }
                        });
                    } else {
                        routeDiagramEl.innerHTML = '<p class="muted">Route diagram unavailable for this route.</p>';
                    }
                }).catch(() => {
                    routeDiagramEl.innerHTML = '';
                });
            }
        }
    }

    // Event delegation for select buttons
    if (allBusesBody) {
        allBusesBody.addEventListener('click', (e) => {
            const btn = e.target.closest('button[data-select-bus]');
            if (!btn) return;
            const busId = btn.getAttribute('data-select-bus');
            const boardId = btn.getAttribute('data-board');
            if (selectedBusSelect && busId) {
                selectedBusSelect.value = busId;
            }
            if (boardStopSelect && boardId) {
                boardStopSelect.value = boardId;
            }
            if (confirmForm) {
                confirmForm.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
        });
    }

    if (searchForm) {
        searchForm.addEventListener('submit', (e) => {
            e.preventDefault();
            doRecommend();
        });
    }

    if (btnClear) {
        btnClear.addEventListener('click', () => {
            if (sourceInput) sourceInput.value = '';
            if (destInput) destInput.value = '';
            hide(resultsEl);
            clearAlerts();
        });
    }

    if (passengerSelect) {
        passengerSelect.addEventListener('change', async () => {
            const pid = parseInt(passengerSelect.value);
            await loadRewards(Number.isNaN(pid) ? undefined : pid);
        });
    }

    if (confirmForm) {
        confirmForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            if (!lastRecommendation) {
                show(confirmResultEl, 'Please search for recommendations first.');
                confirmResultEl.className = 'alert alert-error';
                return;
            }
            const source = sourceInput ? sourceInput.value.trim() : (lastRecommendation.source || '');
            const destination = destInput ? destInput.value.trim() : (lastRecommendation.destination || '');
            const passengerIdVal = passengerSelect ? parseInt(passengerSelect.value) : null;
            const selectedBusIdVal = selectedBusSelect ? parseInt(selectedBusSelect.value) : null;
            const boardStopIdVal = boardStopSelect && boardStopSelect.value ? parseInt(boardStopSelect.value) : null;
            const journeyKey = journeyKeyInput ? journeyKeyInput.value.trim() : '';
            if (!source || !destination || Number.isNaN(passengerIdVal) || Number.isNaN(selectedBusIdVal) || !journeyKey) {
                show(confirmResultEl, 'Missing required fields for confirmation.');
                confirmResultEl.className = 'alert alert-error';
                return;
            }
            try {
                const res = await api.confirmJourney({
                    passengerId: passengerIdVal,
                    selectedBusId: selectedBusIdVal,
                    source,
                    destination,
                    boardStopId: Number.isNaN(boardStopIdVal) ? undefined : boardStopIdVal,
                    journeyKey
                });
                const statusClass = res.awarded ? 'alert alert-success' : 'alert alert-info';
                confirmResultEl.className = statusClass;
                show(confirmResultEl, `${res.reason || ''} (New balance: ${res.newRewardBalance})`);
                await loadRewards(passengerIdVal);
                if (journeyKeyInput) journeyKeyInput.value = generateJourneyKey();
            } catch (err) {
                confirmResultEl.className = 'alert alert-error';
                show(confirmResultEl, err.message || 'Confirmation failed.');
            }
        });
    }

    // Prefill from URL params
    const params = new URLSearchParams(window.location.search);
    const srcParam = params.get('source');
    const dstParam = params.get('destination');
    if (srcParam && sourceInput) sourceInput.value = srcParam;
    if (dstParam && destInput) destInput.value = dstParam;
    if (srcParam && dstParam) {
        setTimeout(() => doRecommend(), 200);
    }

    // Init
    checkBackend();
    populateStops();
    const pidInit = passengerSelect ? parseInt(passengerSelect.value) : undefined;
    loadRewards(Number.isNaN(pidInit) ? undefined : pidInit);
});
