function toggleForms() {
    const login = document.getElementById('login-section');
    const signup = document.getElementById('signup-section');
    const isLoginVisible = login.style.display !== 'none';
    login.style.display = isLoginVisible ? 'none' : 'block';
    signup.style.display = isLoginVisible ? 'block' : 'none';
    hideError();
}

function showError(message) {
    const box = document.getElementById('error-message');
    box.textContent = message;
    box.style.display = 'block';
}

function hideError() {
    const box = document.getElementById('error-message');
    box.style.display = 'none';
}

document.addEventListener('DOMContentLoaded', () => {
    if (MoneyLogAPI.getToken()) {
        location.href = 'transactions.html';
        return;
    }

    document.getElementById('login-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        hideError();
        const email = document.getElementById('login-email').value;
        const password = document.getElementById('login-password').value;
        try {
            const res = await MoneyLogAPI.apiFetch('/api/auth/login', {
                method: 'POST',
                body: JSON.stringify({ email, password })
            });
            MoneyLogAPI.setToken(res.data.accessToken);
            location.href = 'transactions.html';
        } catch (err) {
            showError(err.message);
        }
    });

    document.getElementById('signup-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        hideError();
        const email = document.getElementById('signup-email').value;
        const password = document.getElementById('signup-password').value;
        const nickname = document.getElementById('signup-nickname').value;
        try {
            await MoneyLogAPI.apiFetch('/api/auth/signup', {
                method: 'POST',
                body: JSON.stringify({ email, password, nickname })
            });
            alert('회원가입이 완료되었습니다. 로그인해주세요.');
            toggleForms();
        } catch (err) {
            showError(err.message);
        }
    });
});
