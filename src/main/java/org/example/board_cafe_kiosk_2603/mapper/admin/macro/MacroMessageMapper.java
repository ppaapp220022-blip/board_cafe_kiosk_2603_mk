package org.example.board_cafe_kiosk_2603.mapper.admin.macro;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.board_cafe_kiosk_2603.domain.admin.macro.MacroMessage;

import java.util.List;

@Mapper
public interface MacroMessageMapper {
    // 사용 중(is_active = true)인 매크로만 조회
    List<MacroMessage> findAllActive();

    // direction별 페이징 목록 조회
    List<MacroMessage> selectList(@Param("direction") String direction,
                                  @Param("skip") int skip,
                                  @Param("size") int size);

    // direction별 전체 개수 조회
    int selectCount(@Param("direction") String direction);
}
