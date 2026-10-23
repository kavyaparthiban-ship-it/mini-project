document.addEventListener('DOMContentLoaded', () => {
    const user = window.CrowdSenseSession.getUser();
    const continueLink = document.getElementById('continue-link');
    const signInLink = document.getElementById('welcome-sign-in');

    if (user && continueLink) {
        continueLink.href = 'dashboard.html';
        continueLink.querySelector('span').textContent = 'Continue to recommendations';
        if (signInLink) {
            signInLink.textContent = `Signed in as ${user.name} · Sign out`;
            signInLink.href = '#';
            signInLink.addEventListener('click', (event) => {
                event.preventDefault();
                window.CrowdSenseSession.signOut();
            });
        }
    }
});
