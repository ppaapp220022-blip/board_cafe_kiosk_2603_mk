/**
 * 관리자 대시보드 - 웹소켓 실시간 주문 동기화
 *
 * [제거된 항목]
 * - currentTableId 선언 → dashboard.html에서 선언
 * - openTableModal 래핑 → dashboard.html 원본 사용
 * - closeTableModal 래핑 → dashboard.html 원본 사용
 * - DOMContentLoaded 내 fetchPendingOrders 폴링 → dashboard.html에서 5초 폴링 수행
 */

let stompClient = null;

// ===================================================
// 1. 웹소켓 연결 (자동 실행)
// ===================================================

function connectWebSocket() {
    if (stompClient && stompClient.connected) {
        console.log('이미 연결됨');
        return;
    }

    const socket = new SockJS('/ws/orders');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function(frame) {
        console.log('✅ WebSocket 연결됨');
        subscribeToChannels();
    }, function(error) {
        console.error('❌ WebSocket 연결 실패:', error);
        setTimeout(connectWebSocket, 3000);
    });
}

// ===================================================
// 2. 채널 구독
// ===================================================

function subscribeToChannels() {
    if (!stompClient || !stompClient.connected) return;

    // 신규 주문 알림
    stompClient.subscribe('/topic/new-orders', function(message) {
        const order = JSON.parse(message.body);
        console.log('🚨 신규 주문:', order);
        onNewOrder(order);
    });

    console.log('✅ 채널 구독 완료');
}

// ===================================================
// 3. 테이블 선택 시 주문 구독
// ===================================================

function subscribeToTableOrders(tableId) {
    if (!stompClient || !stompClient.connected) {
        console.warn('WebSocket 미연결 - REST 폴백');
        if (typeof fetchActiveOrders === 'function') fetchActiveOrders();
        return;
    }

    // 테이블별 주문 구독
    stompClient.subscribe(`/topic/orders/${tableId}`, function(message) {
        const orders = JSON.parse(message.body);
        console.log(`📨 테이블 ${tableId} 주문:`, orders);
        onOrdersUpdated(orders, tableId);
    });

    // 초기 데이터 요청
    stompClient.send(`/app/subscribe/${tableId}`, {}, '');
    console.log(`✅ 테이블 ${tableId} 구독`);
}

// ===================================================
// 4. 구독 해제
// ===================================================

function unsubscribeFromOrders() {
    // 현재 구독 해제 로직 (필요 시 구독 ID 관리)
    console.log('📴 주문 구독 해제');
}

// ===================================================
// 5. 신규 주문 수신 처리
// ===================================================

function onNewOrder(order) {
    showNewOrderNotificationModal(order);
    playNotificationSound();
    fetchPendingOrders();
}

function showNewOrderNotificationModal(order) {
    const modal = document.getElementById('newOrderNotificationModal');
    if (!modal) {
        // 대시보드 템플릿에 모달이 없는 버전도 있으므로 예외 없이 종료
        console.warn('⚠️ newOrderNotificationModal 없음 - 모달 표시 생략');
        return;
    }

    const tableId = order.tableId || order.table_id || '?';
    const totalAmount = order.totalAmount || order.total_amount || 0;

    document.getElementById('notificationOrderText').innerText =
        `Table ${tableId}번 - ₩${totalAmount.toLocaleString()}`;

    const items = order.items || order.orderItems || [];
    const itemSummary = items
        .map(i => {
            const menuName = i.menu_name || i.menuName || '상품';
            const qty = i.quantity || i.qty || 0;
            return `${menuName} x${qty}`;
        })
        .join(', ');

    document.getElementById('notificationItemsText').innerText = itemSummary || '항목 없음';

    const detailsHtml = items
        .map(i => {
            const menuName = i.menu_name || i.menuName || '상품명 없음';
            const qty = i.quantity || i.qty || 0;
            const price = i.price || 0;
            const total = price * qty;
            return `<div>• ${menuName} x${qty} = ₩${total.toLocaleString()}</div>`;
        })
        .join('');

    document.getElementById('notificationDetailsText').innerHTML = detailsHtml || '<div>항목 없음</div>';

    modal.style.display = 'flex';

    const content = modal.querySelector('.modal-content');
    content.style.transform = 'scale(0.8)';
    content.style.opacity = '0';
    setTimeout(() => {
        content.style.transform = 'scale(1)';
        content.style.opacity = '1';
        content.style.transition = 'all 0.3s cubic-bezier(0.36, 0, 0.66, 1)';
    }, 10);

    try {
        const utterance = new SpeechSynthesisUtterance(`테이블 ${tableId}번 신규 주문입니다`);
        utterance.lang = 'ko-KR';
        utterance.rate = 1;
        utterance.pitch = 1;
        speechSynthesis.speak(utterance);
    } catch (e) {
        console.log('음성 알림 불가:', e);
    }

    console.log('✅ 신규 주문 알림 모달 표시');
}

function closeNewOrderNotification() {
    const modal = document.getElementById('newOrderNotificationModal');
    if (modal) {
        const content = modal.querySelector('.modal-content');
        if (content) {
            content.style.transform = 'scale(0.8)';
            content.style.opacity = '0';
        }
        setTimeout(() => {
            modal.style.display = 'none';
            document.querySelectorAll('.table-card').forEach(card => {
                card.style.backgroundColor = '';
                card.style.border = '';
                card.style.boxShadow = '';
            });
        }, 300);
    }
    console.log('✅ 신규 주문 알림 모달 닫음');
}

function playNotificationSound() {
    try {
        const audioContext = new (window.AudioContext || window.webkitAudioContext)();
        const now = audioContext.currentTime;

        const osc = audioContext.createOscillator();
        const gain = audioContext.createGain();
        osc.connect(gain);
        gain.connect(audioContext.destination);

        osc.frequency.value = 1000;
        gain.gain.setValueAtTime(0.3, now);
        gain.gain.exponentialRampToValueAtTime(0.01, now + 0.2);
        osc.start(now);
        osc.stop(now + 0.2);

        setTimeout(() => {
            const osc2 = audioContext.createOscillator();
            const gain2 = audioContext.createGain();
            osc2.connect(gain2);
            gain2.connect(audioContext.destination);

            osc2.frequency.value = 1200;
            const now2 = audioContext.currentTime;
            gain2.gain.setValueAtTime(0.3, now2);
            gain2.gain.exponentialRampToValueAtTime(0.01, now2 + 0.2);
            osc2.start(now2);
            osc2.stop(now2 + 0.2);
        }, 250);

        console.log('🔊 사운드 알림 재생');
    } catch (e) {
        console.log('⚠️ 사운드 알림 불가:', e.message);
    }
}

// ===================================================
// 6. 주문 목록 업데이트 처리
// ===================================================

function onOrdersUpdated(orders, tableId) {
    // dashboard.html의 renderOrders 함수 호출
    if (typeof renderOrders === 'function') {
        renderOrders(orders);
    }
    // 대시보드 카드의 상단 주문 상태 배지도 최신화
    if (typeof refreshTableCardOrderBadges === 'function') {
        refreshTableCardOrderBadges();
    }
}

// ===================================================
// 7. 주문 상태 변경 API
// ===================================================

async function updateOrderStatus(orderId, nextStatus) {
    if (!orderId) {
        alert('주문 번호를 찾을 수 없습니다.');
        return;
    }

    let confirmMsg = '주문 상태를 변경하시겠습니까?';
    if (nextStatus === 'CANCELLED') confirmMsg = '정말 이 주문을 취소하시겠습니까? (복구 불가)';
    else if (nextStatus === 'CONFIRMED') confirmMsg = '주문 내역을 주방에서 확인하셨습니까?';
    else if (nextStatus === 'COOKING') confirmMsg = '조리를 시작하시겠습니까?';
    else if (nextStatus === 'DELIVERING') confirmMsg = '서빙을 시작하시겠습니까?';
    else if (nextStatus === 'COMPLETED') confirmMsg = '서빙 완료 처리하시겠습니까?';

    if (!confirm(confirmMsg)) return;

    try {
        console.log('📝 주문 상태 변경 요청:', { orderId, nextStatus });

        // 1) DB 기준 현재 상태/다음 상태를 먼저 조회해서 UI 지연으로 인한 오판을 방지
        let statusToSend = nextStatus;
        let currentStatusFromDb = null;
        const nextStatusRes = await fetch(`/admin/api/dashboard/orders/${orderId}/next-status`, {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        });

        if (nextStatusRes.ok) {
            const nextStatusData = await nextStatusRes.json();
            currentStatusFromDb = nextStatusData.currentStatus || null;

            if (!nextStatusData.canChange || !nextStatusData.nextStatus) {
                console.warn('⚠️ 이미 변경 불가 상태:', nextStatusData);
                if (typeof fetchActiveOrders === 'function') await fetchActiveOrders();
                alert('이미 완료/취소된 주문입니다.');
                return;
            }

            // UI에서 전달된 값보다 서버가 계산한 다음 상태를 우선 사용
            if (statusToSend !== nextStatusData.nextStatus) {
                console.warn('UI 상태와 DB 상태 불일치 - 서버 기준으로 교정', {
                    uiNext: statusToSend,
                    dbCurrent: currentStatusFromDb,
                    dbNext: nextStatusData.nextStatus
                });
                statusToSend = nextStatusData.nextStatus;
            }
        }

        // 2) 관리자 대시보드 전용 API로 상태 변경
        let response = await fetch(`/admin/api/dashboard/orders/${orderId}/status`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({ status: statusToSend })
        });

        if (response.ok) {
            console.log('✅ 주문 상태 변경 성공');
            if (typeof fetchActiveOrders === 'function') await fetchActiveOrders();
            alert('주문 상태가 변경되었습니다.');
            return;
        }

        // 3) 폴백: 기존 API 경로로 한 번 더 시도
        console.warn('⚠️ 관리자 API 실패, 폴백 시도');
        response = await fetch(`/admin/orders/${orderId}/status`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({ status: statusToSend })
        });

        if (response.ok) {
            console.log('✅ 주문 상태 변경 성공 (폴백)');
            if (typeof fetchActiveOrders === 'function') await fetchActiveOrders();
            alert('주문 상태가 변경되었습니다.');
            return;
        }

        const errorData = await response.json().catch(() => ({ message: response.statusText }));
        const errorMessage = errorData.message || errorData.error || "주문 상태 변경에 실패했습니다.";

        // 4) 동시성 충돌(이미 같은 상태/다른 상태로 변경됨) 시 최신값으로 재조회 후 안내
        if (typeof errorMessage === 'string' && errorMessage.includes('허용되지 않는 상태 전이')) {
            console.warn('⚠️ 상태 전이 충돌 감지 - 최신 상태 재조회', {
                orderId, statusToSend, currentStatusFromDb, errorMessage
            });
            if (typeof fetchActiveOrders === 'function') await fetchActiveOrders();
            alert('다른 화면에서 먼저 상태가 변경되었습니다. 최신 상태로 갱신했습니다.');
            return;
        }

        console.error('❌ 상태 변경 실패:', response.status, errorData);
        alert("❌ " + errorMessage);
    } catch (err) {
        console.error('❌ updateOrderStatus 에러:', err);
        alert("서버와 통신 중 오류가 발생했습니다: " + err.message);
    }
}

// ===================================================
// 8. 신규 주문 목록 폴링 (REST 폴백)
// ===================================================

async function fetchPendingOrders() {
    try {
        console.log('📡 신규 주문 폴링: /kiosk/order/pending');

        const response = await fetch('/kiosk/order/pending', {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        });

        console.log('📊 Response Status:', response.status);

        if (!response.ok) {
            const text = await response.text();
            console.error('❌ 서버 오류:', text.substring(0, 200));
            return null;
        }

        const data = await response.json();
        console.log('📥 받은 전체 응답:', JSON.stringify(data, null, 2));

        let orders = [];
        if (data && data.orders && Array.isArray(data.orders)) {
            orders = data.orders;
        } else if (Array.isArray(data)) {
            orders = data;
        } else {
            console.warn('⚠️ 예상하지 못한 응답 구조:', data);
            orders = [];
        }

        if (typeof renderPendingOrders === 'function') {
            renderPendingOrders(orders);
        }

        return orders;
    } catch (error) {
        console.error('❌ fetchPendingOrders 에러:', error);
        return null;
    }
}

// ===================================================
// 9. 초기화 (페이지 로드 시) - WebSocket 연결만 담당
// ===================================================

document.addEventListener('DOMContentLoaded', () => {
    connectWebSocket();
});
