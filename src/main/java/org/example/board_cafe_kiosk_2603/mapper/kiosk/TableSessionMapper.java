package org.example.board_cafe_kiosk_2603.mapper.kiosk;

import org.apache.ibatis.annotations.Mapper;
import org.example.board_cafe_kiosk_2603.domain.kiosk.TableSession;

@Mapper
public interface TableSessionMapper {

    // 세션 생성 (체크인)
    void insert(TableSession session);

    // 활성 세션 조회 (테이블 ID로)
    TableSession findActiveByTableId(int tableId);

    // 세션 ID로 단건 조회
    TableSession findById(long id);

    // 체크아웃 — is_active=false, check_out_time 설정, total_amount 확정
    void checkOut(TableSession session);

    // total_amount 업데이트 (주문 추가 시)
    void updateTotalAmount(long id, int totalAmount);
}
