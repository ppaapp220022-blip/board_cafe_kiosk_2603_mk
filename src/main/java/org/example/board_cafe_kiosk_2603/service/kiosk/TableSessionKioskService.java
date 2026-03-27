package org.example.board_cafe_kiosk_2603.service.kiosk;

public interface TableSessionKioskService {
    // 패키지 선택 완료 시 세션 생성
    void createSession(int tableId, int packageId, int initialGuestCnt);
}
