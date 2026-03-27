package org.example.board_cafe_kiosk_2603.mapper.kiosk;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.board_cafe_kiosk_2603.domain.kiosk.Menu;

import java.util.List;

@Mapper
public interface MenuMapper {

    // category type(DRINK/FOOD/GAME)으로 메뉴 목록 조회
    List<Menu> findByCategoryType(@Param("type") String type);
}
