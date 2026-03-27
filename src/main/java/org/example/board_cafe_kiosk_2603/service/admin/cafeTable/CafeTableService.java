package org.example.board_cafe_kiosk_2603.service.admin.cafeTable;

import org.example.board_cafe_kiosk_2603.dto.admin.CafeTableDTO;

import java.util.List;

public interface CafeTableService {
    /* 전체 테이블 목록 및 현재 세션 정보 조회 */
    List<CafeTableDTO> getAllTableStatus();

    /* [핵심] 테이블 상태 변경 (입실/퇴실/청소) 및 세션 동기화 */
    void changeTableStatus(Integer id, String status);

    /* 새 액세스 토큰 발급 및 할당 */
    String generateNewToken(Integer id);

    /* 자정 전체 초기화 실행 (세션 마감 및 테이블 공석 처리) */
    void resetAllTablesForNewDay();
}
