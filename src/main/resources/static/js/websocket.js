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
        console.error('❌ newOrderNotificationModal을 찾을 수 없음');
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
        content.style.transform = 'scale(0.8)';
        content.style.opacity = '0';
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

        let response = await fetch(`/kiosk/order/${orderId}/status`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({ status: nextStatus })
        });

        console.log('📊 응답 상태:', response.status);

        if (response.ok) {
            console.log('✅ 주문 상태 변경 성공');
            if (typeof fetchActiveOrders === 'function') await fetchActiveOrders();
            alert('주문 상태가 변경되었습니다.');
        } else {
            console.warn('⚠️ 키오스크 API 실패, 관리자 API 시도');

            response = await fetch(`/admin/orders/${orderId}/status`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({ status: nextStatus })
            });

            if (response.ok) {
                console.log('✅ 주문 상태 변경 성공 (관리자 API)');
                if (typeof fetchActiveOrders === 'function') await fetchActiveOrders();
                alert('주문 상태가 변경되었습니다.');
            } else {
                const errorData = await response.json().catch(() => ({ message: response.statusText }));
                console.error('❌ 상태 변경 실패:', response.status, errorData);
                alert("❌ " + (errorData.message || errorData.error || "주문 상태 변경에 실패했습니다."));
            }
        }
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