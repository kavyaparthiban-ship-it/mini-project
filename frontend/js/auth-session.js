(function () {
    const storageKey = 'crowdsense.user';

    function getUser() {
        const raw = localStorage.getItem(storageKey);
        if (!raw) return null;
        try {
            const user = JSON.parse(raw);
            if (user && Number.isSafeInteger(Number(user.passengerId))
                    && Number(user.passengerId) > 0 && user.name && user.email) {
                return user;
            }
        } catch (error) {
            localStorage.removeItem(storageKey);
        }
        return null;
    }

    function saveUser(user) {
        localStorage.setItem(storageKey, JSON.stringify(user));
    }

    function signOut() {
        localStorage.removeItem(storageKey);
        window.location.href = 'index.html';
    }

    function requireUser() {
        const user = getUser();
        if (!user) {
            window.location.replace('auth.html?mode=login');
        }
        return user;
    }

    window.CrowdSenseSession = { getUser, saveUser, signOut, requireUser };
})();
