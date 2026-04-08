package org.example.board_cafe_kiosk_2603.mapper.admin.macro;

import org.apache.ibatis.annotations.Mapper;
import org.example.board_cafe_kiosk_2603.domain.admin.macro.MacroMessage;

import java.util.List;

@Mapper
public interface MacroMessageMapper {
    // 사용 중(is_active = true)인 매크로만 조회
    List<MacroMessage> findAllActive();

    // 상세 조회를 위해 추가
    MacroMessage findById(Integer id);

    // 메세지 등록
    void insertMacro(MacroMessage macroMessage);

    // Soft Delete 용도
    void deactivateMacro(Integer id);
}
