function showError(message) {
    const box = document.getElementById('error-message');
    box.textContent = message;
    box.style.display = 'block';
}

document.addEventListener('DOMContentLoaded', () => {
    if (!MoneyLogAPI.getToken()) {
        location.href = 'index.html';
        return;
    }

    document.getElementById('logout-btn').addEventListener('click', () => {
        MoneyLogAPI.clearToken();
        location.href = 'index.html';
    });

    loadStatistics();
});

async function loadStatistics() {
    const yearMonth = new Date().toISOString().slice(0, 7);
    try {
        const res = await MoneyLogAPI.apiFetch(`/api/statistics/monthly?yearMonth=${yearMonth}`);
        const { income, expense, balance, byCategory } = res.data;
        document.getElementById('stat-income').textContent = income.toLocaleString() + '원';
        document.getElementById('stat-expense').textContent = expense.toLocaleString() + '원';
        document.getElementById('stat-balance').textContent = balance.toLocaleString() + '원';
        renderChart(byCategory);
    } catch (err) {
        showError(err.message);
    }
}

// 도전(F-12) 통계 시각화: 카테고리별 지출 파이차트 (Chart.js CDN)
function renderChart(byCategory) {
    const canvas = document.getElementById('category-chart');
    if (!canvas || typeof Chart === 'undefined') return;

    if (!byCategory || byCategory.length === 0) {
        const ctx = canvas.getContext('2d');
        ctx.font = '14px sans-serif';
        ctx.fillText('이번 달 지출 내역이 없습니다.', 10, 30);
        return;
    }

    new Chart(canvas, {
        type: 'pie',
        data: {
            labels: byCategory.map((c) => c.categoryName),
            datasets: [{
                data: byCategory.map((c) => c.total),
                backgroundColor: ['#4e79a7', '#f28e2b', '#e15759', '#76b7b2', '#59a14f', '#edc949', '#af7aa1', '#ff9da7']
            }]
        }
    });
}
