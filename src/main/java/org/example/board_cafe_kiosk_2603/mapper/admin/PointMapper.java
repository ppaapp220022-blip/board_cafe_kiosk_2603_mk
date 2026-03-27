package org.example.board_cafe_kiosk_2603.mapper.admin;

import org.apache.ibatis.annotations.Mapper;
import org.example.board_cafe_kiosk_2603.domain.admin.Point;

@Mapper
public interface PointMapper {

    // 포인트 계좌 생성
    void insertPoint(Point point);

    // 전화번호로 포인트 계좌 조회
    Point selectByPhone(String phone);

    // 포인트 잔액 업데이트
    void updateBalance(Point point);
}
