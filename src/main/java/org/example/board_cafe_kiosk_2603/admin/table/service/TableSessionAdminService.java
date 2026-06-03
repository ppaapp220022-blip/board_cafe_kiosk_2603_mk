package org.example.board_cafe_kiosk_2603.admin.table.service;

import org.example.board_cafe_kiosk_2603.common.tableSession.model.CafeTableSession;

public interface TableSessionAdminService {
    // 테이블 ID로 활성 세션 조회
    CafeTableSession getActiveSession(int tableId);

    // 세션 종료 (퇴장 처리)
    void closeSession(int tableId);

    // 최종 정산 금액 업데이트
    void updateTotalAmount(Long sessionId, int totalAmount);
}
