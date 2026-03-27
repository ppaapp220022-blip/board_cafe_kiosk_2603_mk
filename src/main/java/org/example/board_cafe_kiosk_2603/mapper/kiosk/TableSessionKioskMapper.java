package org.example.board_cafe_kiosk_2603.mapper.kiosk;

import org.apache.ibatis.annotations.Mapper;
import org.example.board_cafe_kiosk_2603.domain.common.TableSession;

@Mapper
public interface TableSessionKioskMapper {

    // 세션 생성 (패키지 선택 완료 시)
    void insertSession(TableSession tableSession);
}
