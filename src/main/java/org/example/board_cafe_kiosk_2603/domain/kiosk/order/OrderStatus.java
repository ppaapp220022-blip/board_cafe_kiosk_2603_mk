package org.example.board_cafe_kiosk_2603.domain.kiosk.order;

/**
 * 주문 상태 전이 규칙
 *
 * PENDING  → PAID      : 결제 완료
 * PAID     → CONFIRMED : 관리자 확인
 * CONFIRMED→ COOKING   : 조리 시작
 * COOKING  → DELIVERING: 서빙 시작
 * DELIVERING→COMPLETED : 서빙 완료
 * (모든 상태) → CANCELLED : 취소 (COMPLETED 제외)
 */
public enum OrderStatus {

    PENDING,
    PAID,
    CONFIRMED,
    COOKING,
    DELIVERING,
    COMPLETED,
    CANCELLED;

    /**
     * 현재 상태에서 목표 상태로의 전이가 허용되는지 검증합니다.
     *
     * @param next 전이하려는 목표 상태
     * @throws IllegalStateException 허용되지 않는 상태 전이일 때
     */
    public void validateTransitionTo(OrderStatus next) {
        boolean allowed = switch (this) {
            case PENDING    -> next == PAID      || next == CANCELLED;
            case PAID       -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED  -> next == COOKING   || next == CANCELLED;
            case COOKING    -> next == DELIVERING|| next == CANCELLED;
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
