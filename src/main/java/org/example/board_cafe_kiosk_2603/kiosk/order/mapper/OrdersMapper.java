package org.example.board_cafe_kiosk_2603.kiosk.order.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.board_cafe_kiosk_2603.kiosk.order.model.OrderItem;
import org.example.board_cafe_kiosk_2603.kiosk.order.model.Orders;

import java.util.List;

@Mapper

/*
 * 작성자 : 김민기
 * 기능 : 주문 데이터 접근 인터페이스
 * 날짜 : 2026-03-27
 */
public interface OrdersMapper {

    /**
     * 주문 등록합니다.
     *
     * @param order 전달받은 order 값
     */
    void insertOrder(Orders order);

    /**
     * 주문 상품 등록합니다.
     *
     * @param item 전달받은 item 값
     */
    void insertOrderItem(OrderItem item);

    /**
     * 주문 상태 변경합니다.
     *
     * @param order 전달받은 order 값
     */
    void updateOrderStatus(Orders order);

    /**
     * 주문 ID 기준 조회합니다.
     *
     * @param orderId 전달받은 orderId 값
     * @return 처리 결과
     */
    Orders findByOrderId(int orderId);

    /**
     * 세션 ID 기준 조회합니다.
     *
     * @param sessionId 전달받은 sessionId 값
     * @return 처리 결과
     */
    List<Orders> findBySessionId(long sessionId);

    /**
     * 테이블의 최신 주문 조회합니다.
     *
     * @param tableId 전달받은 tableId 값
     * @return 처리 결과
     */
    Orders findLatestByTableId(int tableId);

    /**
     * 상태 기준 조회합니다.
     *
     * @param status 전달받은 status 값
     * @return 처리 결과
     */
    List<Orders> findByStatus(String status);

    /**
     * 주문별 상품 목록 조회합니다.
     *
     * @param orderId 전달받은 orderId 값
     * @return 처리 결과
     */
    List<OrderItem> findItemsByOrderId(int orderId);

}
