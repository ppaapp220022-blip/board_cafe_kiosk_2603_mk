package org.example.board_cafe_kiosk_2603.service.kiosk;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.kiosk.*;
import org.example.board_cafe_kiosk_2603.dto.kiosk.PaymentDTO;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.CartItemMapper;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.CartMapper;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.OrdersMapper;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.TableSessionMapper;
import org.example.board_cafe_kiosk_2603.service.admin.PointService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrdersMapper       ordersMapper;
    private final CartMapper         cartMapper;
    private final CartItemMapper     cartItemMapper;
    private final CartService        cartService;
    private final PointService       pointService;
    private final TableSessionMapper tableSessionMapper;

    private static final double EARN_RATE = 0.05;

    @Transactional
    public PaymentDTO processPayment(int tableNumber, PaymentDTO request, String customerPhone) {

        Integer tableId = cartMapper.findCafeTableIdByTableNumber(tableNumber);
        if (tableId == null) {
            return PaymentDTO.fail("존재하지 않는 테이블 번호입니다: " + tableNumber);
        }

        TableSession session = tableSessionMapper.findActiveByTableId(tableId);
        if (session == null) {
            return PaymentDTO.fail("진행 중인 세션이 없습니다. 패키지를 먼저 선택해 주세요.");
        }

        Cart cart = cartMapper.findByTableId(tableId);
        if (cart == null) {
            return PaymentDTO.fail("장바구니가 비어있습니다.");
        }

        List<CartItem> cartItems = cartItemMapper.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            return PaymentDTO.fail("장바구니에 상품이 없습니다.");
        }

        int totalAmount = cartItems.stream()
                .mapToInt(i -> i.getMenuPrice() * i.getQuantity())
                .sum();

        int pointUsed = request.getPointUsed();
        if (pointUsed > 0 && (customerPhone == null || customerPhone.isBlank())) {
            return PaymentDTO.fail("포인트 사용을 위해 전화번호가 필요합니다.");
        }
        if (pointUsed > totalAmount) {
            return PaymentDTO.fail("포인트 사용액이 결제 금액을 초과할 수 없습니다.");
        }
        int finalAmount = totalAmount - pointUsed;

        Orders order = Orders.builder()
                .sessionId(session.getId())
                .tableId(tableId)
                .customerPhone((customerPhone != null && !customerPhone.isBlank()) ? customerPhone : null)
                .status("PAID")
                .totalAmount(totalAmount)
                .build();
        ordersMapper.insertOrder(order);
        log.info("주문 생성 - orderId: {}, sessionId: {}, 금액: ₩{}", order.getId(), session.getId(), totalAmount);

        for (CartItem ci : cartItems) {
            ordersMapper.insertOrderItem(OrderItem.builder()
                    .orderId(order.getId())
                    .menuId(ci.getMenuId())
                    .menuName(ci.getMenuName())
                    .price(ci.getMenuPrice())
                    .quantity(ci.getQuantity())
                    .build());
        }

        ordersMapper.insertPayment(Payment.builder()
                .sessionId(session.getId())
                .finalAmount(finalAmount)
                .build());
        ordersMapper.completePayment(session.getId());
        log.info("결제 완료 - sessionId: {}, 수단: {}, 최종금액: ₩{}", session.getId(), request.getPaymentMethod(), finalAmount);

        if (pointUsed > 0) {
            pointService.usePoint(customerPhone, pointUsed, order.getId());
            log.info("포인트 사용 - {}: -{}P", customerPhone, pointUsed);
        }

        int earnedPoints = 0;
        if (customerPhone != null && !customerPhone.isBlank() && finalAmount > 0) {
            earnedPoints = (int) Math.floor(finalAmount * EARN_RATE);
            pointService.earnPoint(customerPhone, earnedPoints, order.getId());
            log.info("포인트 적립 - {}: +{}P", customerPhone, earnedPoints);
        }

        cartItemMapper.deleteAllByCartId(cart.getId());
        log.info("장바구니 비우기 완료 - tableNumber: {}", tableNumber);

        return PaymentDTO.success(order.getId(), totalAmount, pointUsed, finalAmount, earnedPoints);
    }
}
