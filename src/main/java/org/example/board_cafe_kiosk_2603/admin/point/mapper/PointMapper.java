package org.example.board_cafe_kiosk_2603.admin.point.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.board_cafe_kiosk_2603.admin.point.model.Point;
import org.example.board_cafe_kiosk_2603.admin.point.model.PointHistory;
import org.example.board_cafe_kiosk_2603.common.pagination.PageRequestDTO;

import java.util.List;

@Mapper

/* 포인트 및 포인트 이력 데이터 접근 인터페이스 */

public interface PointMapper {

    /**
     * 전체 목록 조회합니다.
     *
     * @return 처리 결과
     */
    List<Point> findAll();

    /**
     * 전화번호 기준 조회합니다.
     *
     * @param phone 전달받은 phone 값
     * @return 처리 결과
     */
    Point findByPhone(String phone);

    /**
     * 데이터 등록합니다.
     *
     * @param point 전달받은 point 값
     */
    void insert(Point point);

    /**
     * 포인트 잔액 변경합니다.
     *
     * @param point 전달받은 point 값
     */
    void updateBalance(Point point);

    /* 총 포인트 잔액 합계 조회 */
    int sumTotalBalance();

    /**
     * 전체 건수 조회합니다.
     *
     * @return 처리 결과
     */
    int countAll();

    /**
     * 포인트 이력 조회합니다.
     *
     * @param pointId 전달받은 pointId 값
     * @return 처리 결과
     */
    List<PointHistory> findHistoryByPointId(int pointId);

    /**
     * 포인트 이력 등록합니다.
     *
     * @param history 전달받은 history 값
     */
    void insertHistory(PointHistory history);

    /**
     * 주문별 포인트 사용 이력 건수 조회합니다.
     *
     * @param orderId 전달받은 orderId 값
     * @return 처리 결과
     */
    int countUseHistoryByOrderId(@Param("orderId") Long orderId);

    /* 조건별 목록 조회 */
    List<Point> selectList(PageRequestDTO pageRequestDTO);

    /* 조건별 건수 조회 */
    int selectCount(PageRequestDTO pageRequestDTO);
}
