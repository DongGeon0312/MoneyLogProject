// 공통 fetch 래퍼: accessToken 저장/첨부, 에러 처리
const MoneyLogAPI = (() => {
    const TOKEN_KEY = 'moneylog_access_token';

    function getToken() {
        return localStorage.getItem(TOKEN_KEY);
    }

    function setToken(token) {
        localStorage.setItem(TOKEN_KEY, token);
    }

    function clearToken() {
        localStorage.removeItem(TOKEN_KEY);
    }

    async function apiFetch(path, options = {}) {
        const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
        const token = getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const res = await fetch(path, { ...options, headers });
        const body = await res.json().catch(() => null);

        if (!res.ok || (body && body.success === false)) {
            if (res.status === 401) {
                clearToken();
                if (!location.pathname.endsWith('index.html') && location.pathname !== '/') {
                    location.href = 'index.html';
                }
            }
            const message = (body && body.message) ? body.message : `요청에 실패했습니다. (${res.status})`;
            throw new Error(message);
        }
        return body;
    }

    return { getToken, setToken, clearToken, apiFetch };
})();
