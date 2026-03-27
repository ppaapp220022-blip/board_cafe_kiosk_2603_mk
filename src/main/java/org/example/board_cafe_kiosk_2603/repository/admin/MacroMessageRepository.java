package org.example.board_cafe_kiosk_2603.repository.admin;

import org.apache.ibatis.annotations.Mapper;
import org.example.board_cafe_kiosk_2603.domain.admin.MacroMessage;

import java.util.List;

@Mapper
public interface MacroMessageRepository {
    // 사용 중(is_active = true)인 매크로만 조회
    List<MacroMessage> findAllActive();
}
