package org.example.board_cafe_kiosk_2603.mapper.admin;

import org.apache.ibatis.annotations.Mapper;
import org.example.board_cafe_kiosk_2603.domain.common.TableSession;

@Mapper
public interface TableSessionAdminMapper {
    // 테이블 ID로 활성 세션 조회
    TableSession selectActiveByTableId(int tableId);

    // 세션 종료 (퇴장 시 - check_out_time 기록, is_active = false)
    void closeSession(int tableId);

    // 최종 정산 금액 업데이트
    void updateTotalAmount(TableSession tableSession);
}
