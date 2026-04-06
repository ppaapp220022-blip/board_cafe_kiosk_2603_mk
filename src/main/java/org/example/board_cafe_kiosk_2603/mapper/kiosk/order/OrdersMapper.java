package org.example.board_cafe_kiosk_2603.mapper.kiosk.order;

import org.apache.ibatis.annotations.Mapper;
import org.example.board_cafe_kiosk_2603.domain.kiosk.order.OrderItem;
import org.example.board_cafe_kiosk_2603.domain.kiosk.order.Orders;
import org.example.board_cafe_kiosk_2603.domain.kiosk.payment.Payment;

import java.util.List;

@Mapper
public interface OrdersMapper {

    // 주문 생성
    void insertOrder(Orders order);

    // 주문 아이템 단건 삽입
    void insertOrderItem(OrderItem item);

    // 결제 레코드 생성 (READY) — session_id 기반
    void insertPayment(Payment payment);

    // 결제 완료 처리 (READY → DONE)
    void completePayment(long sessionId);

    // 주문 상태 업데이트
    void updateOrderStatus(Orders order);

    // 주문 ID로 단건 조회
    Orders findByOrderId(int orderId);

    // 세션 ID로 주문 목록 조회
    List<Orders> findBySessionId(long sessionId);

    // 테이블 ID로 최근 주문 조회
    Orders findLatestByTableId(int tableId);

    // 주문 ID로 아이템 목록 조회
    List<OrderItem> findItemsByOrderId(int orderId);

    // 결제 키로 결제 조회 (중복 승인 방지)
    Payment findByPaymentKey(String paymentKey);
}
