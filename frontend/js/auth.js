document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('auth-form');
    const nameField = document.getElementById('name-field');
    const nameInput = document.getElementById('auth-name');
    const emailInput = document.getElementById('auth-email');
    const passwordInput = document.getElementById('auth-password');
    const loginTab = document.getElementById('login-tab');
    const registerTab = document.getElementById('register-tab');
    const title = document.getElementById('auth-title');
    const subtitle = document.getElementById('auth-subtitle');
    const submitButton = document.getElementById('auth-submit');
    const passwordHint = document.getElementById('password-hint');
    const message = document.getElementById('auth-message');
    let mode = new URLSearchParams(window.location.search).get('mode') === 'signup' ? 'register' : 'login';

    if (window.CrowdSenseSession.getUser()) {
        window.location.replace('dashboard.html');
        return;
    }

    function setMode(nextMode) {
        mode = nextMode;
        const registering = mode === 'register';
        nameField.classList.toggle('hidden', !registering);
        nameInput.required = registering;
        nameInput.disabled = !registering;
        passwordInput.autocomplete = registering ? 'new-password' : 'current-password';
        passwordHint.classList.toggle('hidden', !registering);
        title.textContent = registering ? 'Create your account' : 'Welcome back';
        subtitle.textContent = registering
            ? 'A few details, then your next journey is closer.'
            : 'Sign in to continue to your travel recommendations.';
        submitButton.firstElementChild.textContent = registering ? 'Create account' : 'Sign in';
        loginTab.classList.toggle('active', !registering);
        registerTab.classList.toggle('active', registering);
        loginTab.setAttribute('aria-selected', String(!registering));
        registerTab.setAttribute('aria-selected', String(registering));
        message.classList.add('hidden');
        message.textContent = '';
    }

    loginTab.addEventListener('click', () => setMode('login'));
    registerTab.addEventListener('click', () => setMode('register'));

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        message.classList.add('hidden');
        message.textContent = '';

        const name = nameInput.value.trim();
        const email = emailInput.value.trim();
        const password = passwordInput.value;
        if (mode === 'register' && !name) {
            showMessage('Please enter your name.');
            nameInput.focus();
            return;
        }
        if (!emailInput.validity.valid || !email) {
            showMessage('Please enter a valid email address.');
            emailInput.focus();
            return;
        }
        if (!password || (mode === 'register' && password.length < 8)) {
            showMessage(mode === 'register'
                ? 'Your password must be at least 8 characters.'
                : 'Please enter your password.');
            passwordInput.focus();
            return;
        }

        submitButton.disabled = true;
        submitButton.firstElementChild.textContent = mode === 'register' ? 'Creating account...' : 'Signing in...';
        try {
            const user = mode === 'register'
                ? await window.api.register({ name, email, password })
                : await window.api.login({ email, password });
            window.CrowdSenseSession.saveUser(user);
            window.location.replace('dashboard.html');
        } catch (error) {
            showMessage(error.message || 'Unable to sign in. Please try again.');
            submitButton.disabled = false;
            submitButton.firstElementChild.textContent = mode === 'register' ? 'Create account' : 'Sign in';
        }
    });

    function showMessage(text) {
        message.textContent = text;
        message.className = 'alert alert-error auth-message';
    }

    setMode(mode);
});
