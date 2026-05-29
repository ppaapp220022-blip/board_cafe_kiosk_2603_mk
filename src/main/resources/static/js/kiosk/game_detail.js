lucide.createIcons();

const gameDetailState = window.gameDetailState || {};
const tableNumber = Number(gameDetailState.tableNumber || 0);
const orderId = Number(gameDetailState.orderId || 0);
const orderData = gameDetailState.order || {};
const requestedGames = Array.isArray(gameDetailState.requestedGames)
    ? gameDetailState.requestedGames
    : [];

function applyResponsiveLayout() {
    const layout = document.querySelector('.layout');
    const listCard = document.querySelector('.list-card');
    const detailCard = document.querySelector('.detail-card');
    const listEl = document.getElementById('game-list');
    const imageFrame = document.querySelector('.game-image-frame');
    const metaGrid = document.querySelector('.meta-grid');

    if (!layout || !listCard || !detailCard || !listEl || !imageFrame || !metaGrid) {
        return;
    }

    const width = window.innerWidth || document.documentElement.clientWidth || 1024;

    listEl.style.maxHeight = 'none';
    listEl.style.overflow = 'visible';
    listCard.style.height = 'fit-content';
    detailCard.style.height = 'fit-content';
    listCard.style.minWidth = '0';
    detailCard.style.minWidth = '0';
    imageFrame.style.maxWidth = '100%';

    if (width <= 640) {
        layout.style.display = 'flex';
        layout.style.flexDirection = 'column';
        layout.style.gridTemplateColumns = 'none';
        listCard.style.width = '100%';
        detailCard.style.width = '100%';
        imageFrame.style.width = '100%';
        imageFrame.style.aspectRatio = width <= 480 ? '1 / 1' : '4 / 3';
        metaGrid.style.gridTemplateColumns = '1fr';
    } else if (width <= 1024) {
        layout.style.display = 'grid';
        layout.style.flexDirection = '';
        layout.style.gridTemplateColumns = 'minmax(170px, 0.58fr) minmax(0, 1.42fr)';
        listCard.style.width = '100%';
        detailCard.style.width = '100%';
        imageFrame.style.width = 'min(100%, 260px)';
        imageFrame.style.aspectRatio = '4 / 3';
        metaGrid.style.gridTemplateColumns = '1fr';
    } else {
        layout.style.display = 'grid';
        layout.style.flexDirection = '';
        layout.style.gridTemplateColumns = 'minmax(240px, 0.72fr) minmax(0, 1.28fr)';
        imageFrame.style.width = '100%';
        imageFrame.style.aspectRatio = '16 / 9';
        metaGrid.style.gridTemplateColumns = 'repeat(2, minmax(0, 1fr))';
    }
}

function startTableStatusWatcher() {
    async function checkTableStatus() {
        try {
            const res = await fetch('/kiosk/table/status', {
                headers: { 'Accept': 'application/json' },
                credentials: 'same-origin'
            });
            if (!res.ok) return;

            const contentType = res.headers.get('content-type') || '';
            if (!contentType.includes('application/json')) {
                if (res.redirected && res.url) {
                    window.location.href = res.url;
                }
                return;
            }

            const data = await res.json();
            if (data?.success && data.status === 'CLEANING') {
                window.location.href = '/kiosk/cleaning_wait';
            }
        } catch (error) {
            console.error('테이블 상태 확인 실패:', error);
        }
    }

    checkTableStatus();
    setInterval(checkTableStatus, 3000);
}

function renderDetail(game) {
    const image = document.getElementById('game-image');
    image.src = game.imageUrl || '';
    image.style.display = game.imageUrl ? 'block' : 'none';

    const minPlayers = game.minPlayers ?? '-';
    const maxPlayers = game.maxPlayers ?? '-';
    const playTime = game.playTime ?? '-';
    document.getElementById('players-meta').innerText = `${minPlayers} ~ ${maxPlayers}명`;
    document.getElementById('time-meta').innerText = `${playTime}분`;
    document.getElementById('desc-meta').innerText = game.description && String(game.description).trim()
        ? game.description
        : '설명이 등록되지 않았습니다.';
}

function renderList() {
    const listEl = document.getElementById('game-list');
    if (!Array.isArray(requestedGames) || requestedGames.length === 0) {
        listEl.innerHTML = '<div style="padding:10px; color:#64748b;">요청된 게임이 없습니다.</div>';
        applyResponsiveLayout();
        return;
    }

    listEl.innerHTML = requestedGames.map((game, idx) => `
      <div class="game-item ${idx === 0 ? 'active' : ''}" data-idx="${idx}">
        <div class="game-name">${game.name || '게임'}</div>
        <div class="game-qty">수량: ${game.quantity || 1}개</div>
      </div>
    `).join('');

    document.querySelectorAll('.game-item').forEach(el => {
        el.addEventListener('click', () => {
            document.querySelectorAll('.game-item').forEach(x => x.classList.remove('active'));
            el.classList.add('active');
            const idx = Number(el.dataset.idx);
            renderDetail(requestedGames[idx]);
        });
    });

    renderDetail(requestedGames[0]);
    applyResponsiveLayout();
}

function goToMenu() {
    window.location.href = `/kiosk/games?tableNumber=${tableNumber}`;
}

startTableStatusWatcher();
renderList();
window.addEventListener('resize', applyResponsiveLayout);
