package org.example.board_cafe_kiosk_2603.kiosk.order.model;
/*
 * 작성자 : 김민기
 * 기능 : 주문 상태 전이 규칙
 * 날짜 : 2026-04-01
 */

public enum OrderStatus {

    ORDERED,    // 주문완료 (고객이 주문, 결제 전)
    CONFIRMED,  // 주문확인 (관리자가 확인)
    COOKING,    // 조리중
    DELIVERING, // 배달/서빙 대기 (조리완료 → 서빙 단계)
    COMPLETED,  // 서빙완료 (최종 상태)
    CANCELLED;  // 취소됨 (최종 상태)
    public void validateTransitionTo(OrderStatus next) {
        boolean allowed = switch (this) {
            case ORDERED    -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED  -> next == COOKING   || next == CANCELLED;
            case COOKING    -> next == DELIVERING || next == CANCELLED;
            case DELIVERING -> next == COMPLETED || next == CANCELLED;
            case COMPLETED  -> false; // 최종 상태 — 변경 불가
            case CANCELLED  -> false; // 최종 상태 — 변경 불가
        };

        if (!allowed) {
            throw new IllegalStateException(
                    String.format("허용되지 않는 상태 전이입니다: %s → %s", this.name(), next.name()));
        }
    }
}
