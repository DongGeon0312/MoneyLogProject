let categories = [];

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

    const today = new Date();
    document.getElementById('month-label').textContent = `${today.getFullYear()}년 ${today.getMonth() + 1}월 거래내역`;
    document.getElementById('tx-date').value = today.toISOString().slice(0, 10);

    document.getElementById('tx-type').addEventListener('change', renderCategoryOptions);
    document.getElementById('transaction-form').addEventListener('submit', onSubmitTransaction);

    loadCategories().then(loadTransactions);
});

async function loadCategories() {
    try {
        const res = await MoneyLogAPI.apiFetch('/api/categories');
        categories = res.data;
        renderCategoryOptions();
    } catch (err) {
        showError(err.message);
    }
}

function renderCategoryOptions() {
    const type = document.getElementById('tx-type').value;
    const select = document.getElementById('tx-category');
    select.innerHTML = '';
    categories.filter((c) => c.type === type).forEach((c) => {
        const option = document.createElement('option');
        option.value = c.id;
        option.textContent = c.name;
        select.appendChild(option);
    });
}

async function loadTransactions() {
    const yearMonth = new Date().toISOString().slice(0, 7);
    try {
        const res = await MoneyLogAPI.apiFetch(`/api/transactions?yearMonth=${yearMonth}&page=0&size=50`);
        renderTransactions(res.data.transactions);
    } catch (err) {
        showError(err.message);
    }
}

function renderTransactions(transactions) {
    const tbody = document.getElementById('transaction-list');
    tbody.innerHTML = '';
    transactions.forEach((tx) => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${tx.transactionDate}</td>
            <td>${tx.type === 'INCOME' ? '수입' : '지출'}</td>
            <td>${tx.categoryName}</td>
            <td>${tx.amount.toLocaleString()}원</td>
            <td>${tx.description ?? ''}</td>
            <td><button data-id="${tx.id}" class="delete-btn">삭제</button></td>
        `;
        tbody.appendChild(tr);
    });

    tbody.querySelectorAll('.delete-btn').forEach((btn) => {
        btn.addEventListener('click', () => deleteTransaction(btn.dataset.id));
    });
}

async function onSubmitTransaction(e) {
    e.preventDefault();
    const payload = {
        type: document.getElementById('tx-type').value,
        amount: Number(document.getElementById('tx-amount').value),
        categoryId: Number(document.getElementById('tx-category').value),
        description: document.getElementById('tx-description').value,
        transactionDate: document.getElementById('tx-date').value
    };
    try {
        await MoneyLogAPI.apiFetch('/api/transactions', {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        document.getElementById('transaction-form').reset();
        document.getElementById('tx-date').value = new Date().toISOString().slice(0, 10);
        renderCategoryOptions();
        loadTransactions();
    } catch (err) {
        showError(err.message);
    }
}

async function deleteTransaction(id) {
    if (!confirm('삭제하시겠습니까?')) return;
    try {
        await MoneyLogAPI.apiFetch(`/api/transactions/${id}`, { method: 'DELETE' });
        loadTransactions();
    } catch (err) {
        showError(err.message);
    }
}
