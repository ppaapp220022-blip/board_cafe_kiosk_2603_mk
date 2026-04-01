package org.example.board_cafe_kiosk_2603.service.kiosk.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.common.cafeTableSession.CafeTableSession;
import org.example.board_cafe_kiosk_2603.domain.kiosk.order.OrderStatus;
import org.example.board_cafe_kiosk_2603.domain.kiosk.order.Orders;
import org.example.board_cafe_kiosk_2603.domain.kiosk.payment.Payment;
import org.example.board_cafe_kiosk_2603.dto.kiosk.order.OrdersDTO;
import org.example.board_cafe_kiosk_2603.dto.kiosk.payment.PaymentDTO;
import org.example.board_cafe_kiosk_2603.mapper.common.cafeTableSession.CafeTableSessionMapper;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.cart.CartItemMapper;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.cart.CartMapper;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.order.OrdersMapper;
import org.example.board_cafe_kiosk_2603.service.admin.point.PointService;
import org.example.board_cafe_kiosk_2603.service.kiosk.order.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrdersMapper           ordersMapper;
    private final CartMapper             cartMapper;
    private final CartItemMapper         cartItemMapper;
    private final OrderService           orderService;
    private final PointService           pointService;
    private final CafeTableSessionMapper tableSessionMapper;

    private static final double EARN_RATE = 0.05;

    /**
     * 결제 처리 흐름:
     * 1. 유효성 검증 (테이블 / 세션 / 장바구니)
     * 2. 금액 계산
     * 3. 카트 → 주문 생성 (OrderService.createOrderFromCart) — 카트 자동 비워짐
     * 4. 주문 상태 PENDING → PAID 전이
     * 5. payment 레코드 저장 및 완료 처리
     * 6. 포인트 사용 / 적립
     */
    @Transactional
    public PaymentDTO processPayment(int tableNumber, PaymentDTO request, String customerPhone) {

        // 1. 테이블 확인
        Integer tableId = cartMapper.findCafeTableIdByTableNumber(tableNumber);
        if (tableId == null) {
            return PaymentDTO.builder().success(false).message("존재하지 않는 테이블 번호입니다: " + tableNumber).build();
        }

        // 2. 활성 세션 확인
        CafeTableSession session = tableSessionMapper.findActiveByTableId(tableId);
        if (session == null) {
            return PaymentDTO.builder().success(false).message("진행 중인 세션이 없습니다. 패키지를 먼저 선택해 주세요.").build();
        }

        // 3. 금액 계산 (장바구니 조회는 OrderService 내부에서 수행)
        int cartTotal = calcCartTotal(tableId);
        if (cartTotal < 0) {
            return PaymentDTO.builder().success(false).message("장바구니가 비어있습니다.").build();
        }

        int pointUsed = request.getPointUsed();
        if (pointUsed > 0 && (customerPhone == null || customerPhone.isBlank())) {
            return PaymentDTO.builder().success(false).message("포인트 사용을 위해 전화번호가 필요합니다.").build();
        }
        if (pointUsed > cartTotal) {
            return PaymentDTO.builder().success(false).message("포인트 사용액이 결제 금액을 초과할 수 없습니다.").build();
        }
        int finalAmount = cartTotal - pointUsed;

        // 4. 카트 → 주문 생성 (PENDING, 카트 자동 비워짐)
        OrdersDTO created = orderService.createOrderFromCart(
                tableId, session.getId(), customerPhone, cartTotal);
        if (!created.isSuccess()) {
            return PaymentDTO.builder().success(false).message(created.getMessage()).build();
        }
        int orderId = created.getId();

        // 5. 주문 상태 PENDING → PAID 전이
        OrdersDTO paid = orderService.updateStatus(orderId, OrderStatus.PAID.name());
        if (!paid.isSuccess()) {
            return PaymentDTO.builder().success(false).message("결제 상태 전이 실패: " + paid.getMessage()).build();
        }
        log.info("주문 상태 전이 완료 - orderId: {}, PENDING → PAID", orderId);

        // 6. payment 레코드 저장 및 완료 처리
        Payment payment = Payment.builder()
                .sessionId(session.getId())
                .finalAmount(finalAmount)
                .build();
        ordersMapper.insertPayment(payment);
        ordersMapper.completePayment(session.getId());
        log.info("결제 완료 - sessionId: {}, 수단: {}, 최종금액: ₩{}",
                session.getId(), request.getPaymentMethod(), finalAmount);

        // 7. 포인트 처리
        int earnedPoints = 0;
        if (customerPhone != null && !customerPhone.isBlank()) {
            if (pointUsed > 0) {
                pointService.usePoint(customerPhone, pointUsed, (long) orderId);
                log.info("포인트 사용 - {}: -{}P", customerPhone, pointUsed);
            }
            if (finalAmount > 0) {
                earnedPoints = (int) Math.floor(finalAmount * EARN_RATE);
                pointService.earnPoint(customerPhone, earnedPoints, (long) orderId);
                log.info("포인트 적립 - {}: +{}P", customerPhone, earnedPoints);
            }
        }

        return PaymentDTO.builder()
                .success(true)
                .message("결제가 완료되었습니다.")
                .orderId(orderId)
                .totalAmount(cartTotal)
                .pointUsed(pointUsed)
                .finalAmount(finalAmount)
                .earnedPoints(earnedPoints)
                .build();
    }

    // ===================================================
    // 헬퍼: 장바구니 총액 계산 (-1 이면 카트/아이템 없음)
    // ===================================================

    private int calcCartTotal(int tableId) {
        var cart = cartMapper.findByTableId(tableId);
        if (cart == null) return -1;
        var items = cartItemMapper.findByCartId(cart.getId());
        if (items.isEmpty()) return -1;
        return items.stream().mapToInt(i -> i.getMenuPrice() * i.getQuantity()).sum();
    }
}
