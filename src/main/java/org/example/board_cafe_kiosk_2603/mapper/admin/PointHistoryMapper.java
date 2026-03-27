package org.example.board_cafe_kiosk_2603.mapper.admin;

import org.apache.ibatis.annotations.Mapper;
import org.example.board_cafe_kiosk_2603.domain.admin.PointHistory;

import java.util.List;

@Mapper
public interface PointHistoryMapper {

    // 포인트 이력 저장
    void insertHistory(PointHistory pointHistory);

    // 포인트 계좌 ID로 이력 조회 (최신순)
    List<PointHistory> selectByPointId(int pointId);
}
