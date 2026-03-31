package org.example.board_cafe_kiosk_2603.mapper.admin.table;

import org.apache.ibatis.annotations.Mapper;
import org.example.board_cafe_kiosk_2603.domain.admin.table.CafeTableSession;

@Mapper
public interface TableSessionAdminMapper {
    // 테이블 ID로 활성 세션 조회
    CafeTableSession selectActiveByTableId(int tableId);

    // 세션 종료 (퇴장 시 - check_out_time 기록, is_active = false)
    void closeSession(int tableId);

    // 최종 정산 금액 업데이트
    void updateTotalAmount(CafeTableSession cafeTableSession);
}
