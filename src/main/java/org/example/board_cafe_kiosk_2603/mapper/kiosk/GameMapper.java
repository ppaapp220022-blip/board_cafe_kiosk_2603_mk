package org.example.board_cafe_kiosk_2603.mapper.kiosk;

import org.apache.ibatis.annotations.Mapper;
import org.example.board_cafe_kiosk_2603.domain.kiosk.Game;

import java.util.List;

@Mapper
public interface GameMapper {

    // 활성화된 게임 목록 전체 조회
    List<Game> findAllActive();
}
