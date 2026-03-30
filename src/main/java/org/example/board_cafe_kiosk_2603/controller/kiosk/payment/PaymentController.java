package org.example.board_cafe_kiosk_2603.controller.kiosk.payment;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.kiosk.*;
import org.example.board_cafe_kiosk_2603.domain.kiosk.cart.Cart;
import org.example.board_cafe_kiosk_2603.domain.kiosk.cart.CartItem;
import org.example.board_cafe_kiosk_2603.domain.kiosk.order.OrderItem;
import org.example.board_cafe_kiosk_2603.domain.kiosk.order.Orders;
import org.example.board_cafe_kiosk_2603.domain.kiosk.payment.Payment;
import org.example.board_cafe_kiosk_2603.dto.admin.point.PointAdminDTO;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.*;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.cart.CartItemMapper;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.cart.CartMapper;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.order.OrdersMapper;
import org.example.board_cafe_kiosk_2603.service.admin.point.PointService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 정산 페이지 + 결제 REST API (DB 연동).
 * <p>
 * [페이지] GET  /kiosk/checkout      → checkout.html
 * [API]   POST /kiosk/checkout/pay  → 주문 + 결제 DB 저장
 * <p>
 * 결제 흐름:
 * 1. cart_item → orders + order_item 저장
 * 2. payment 저장 (DONE)
 * 3. cart_item 비우기
 */
@Log4j2
@Controller
@RequestMapping("/kiosk/checkout")
@RequiredArgsConstructor
public class PaymentController {

    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;
    private final OrdersMapper ordersMapper;
    private final TableSessionMapper tableSessionMapper;
    private final PointService pointService;

    // ===========================================================
    // 페이지
    // ===========================================================

    @GetMapping
    public String checkoutPage(
            @RequestParam(required = false, defaultValue = "1") Integer tableNumber,
            HttpSession session, Model model) {

        // DB에서 장바구니 조회
        Integer tableId = cartMapper.findCafeTableIdByTableNumber(tableNumber);
        List<CartItem> cartItems = new ArrayList<>();
        int total = 0;

        if (tableId != null) {
            Cart cart = cartMapper.findByTableId(tableId);
            if (cart != null) {
                cartItems = cartItemMapper.findByCartId(cart.getId());
                total = cartItems.stream().mapToInt(i -> i.getMenuPrice() * i.getQuantity()).sum();
            }
        }

        int sessionDuration = getSessionDuration(session);
        String customerPhone = session.getAttribute("customerPhone") != null
                ? (String) session.getAttribute("customerPhone") : "";

        // 포인트 잔액 DB 조회
        int pointBalance = 0;
        if (!customerPhone.isBlank()) {
            PointAdminDTO point = pointService.getPointByPhone(customerPhone);
            if (point != null) {
                pointBalance = point.getBalance();
            }
        }

        model.addAttribute("tableNumber", tableNumber);
        model.addAttribute("partySize", getPartySize(session));
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", total);
        model.addAttribute("cartCount", cartItems.size());
        model.addAttribute("sessionHours", sessionDuration / 60);
        model.addAttribute("sessionMinutes", sessionDuration % 60);
        model.addAttribute("pointBalance", pointBalance);
        model.addAttribute("customerPhone", customerPhone);

        log.info("정산 화면 - 테이블: {}, 아이템: {}개, 총액: ₩{}, 포인트: {}P ({})",
                tableNumber, cartItems.size(), total, pointBalance,
                customerPhone.isBlank() ? "비회원" : customerPhone);
        return "kiosk/checkout";
    }

    // ===========================================================
    // REST API
    // ===========================================================

    @PostMapping("/pay")
    @ResponseBody
    public Map<String, Object> processPayment(
            @RequestBody Map<String, Object> req,
            @RequestParam(required = false, defaultValue = "1") Integer tableNumber,
            HttpSession session) {

        String paymentMethod = (String) req.get("paymentMethod");
        int pointUsed = toInt(req.get("pointUsed"));
        String customerPhone = (String) session.getAttribute("customerPhone");

        Map<String, Object> res = new LinkedHashMap<>();

        // 1. tableId 조회
        Integer tableId = cartMapper.findCafeTableIdByTableNumber(tableNumber);
        if (tableId == null) {
            res.put("success", false);
            res.put("message", "테이블을 찾을 수 없습니다.");
            return res;
        }

        // 2. 활성 세션 조회
        TableSession activeSession = tableSessionMapper.findActiveByTableId(tableId);
        if (activeSession == null) {
            res.put("success", false);
            res.put("message", "활성 세션이 없습니다.");
            return res;
        }
        long sessionId = activeSession.getId();

        // 3. cart_item 조회
        Cart cart = cartMapper.findByTableId(tableId);
        if (cart == null) {
            res.put("success", false);
            res.put("message", "장바구니가 없습니다.");
            return res;
        }
        List<CartItem> cartItems = cartItemMapper.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            res.put("success", false);
            res.put("message", "장바구니가 비어있습니다.");
            return res;
        }

        // 4. 금액 계산
        int totalAmount = cartItems.stream().mapToInt(i -> i.getMenuPrice() * i.getQuantity()).sum();
        int finalAmount = Math.max(0, totalAmount - pointUsed);

        // 5. orders 저장
        Orders order = Orders.builder()
                .sessionId(sessionId)
                .tableId(tableId)
                .customerPhone(customerPhone)
                .status("PAID")
                .totalAmount(finalAmount)
                .build();
        ordersMapper.insertOrder(order);
        log.info("주문 저장 완료 - orderId: {}, 총액: ₩{}", order.getId(), finalAmount);

        // 6. order_item 저장
        for (CartItem item : cartItems) {
            OrderItem orderItem = OrderItem.builder()
                    .orderId(order.getId())
                    .menuId(item.getMenuId())
                    .menuName(item.getMenuName())
                    .price(item.getMenuPrice())
                    .quantity(item.getQuantity())
                    .build();
            ordersMapper.insertOrderItem(orderItem);
        }
        log.info("주문 아이템 {}개 저장 완료", cartItems.size());

        // 7. payment 저장
        Payment payment = Payment.builder()
                .sessionId(sessionId)
                .finalAmount(finalAmount)
                .build();
        ordersMapper.insertPayment(payment);
        ordersMapper.completePayment(sessionId);
        log.info("결제 저장 완료 - paymentId: {}, 수단: {}", payment.getId(), paymentMethod);

        // 8. cart_item 비우기
        cartItemMapper.deleteAllByCartId(cart.getId());
        cartMapper.updateTimestamp(cart.getId());

        // 9. 포인트 처리
        int earnedPoints = 0;
        if (customerPhone != null && !customerPhone.isBlank()) {
            // 포인트 사용
            if (pointUsed > 0) {
                try {
                    pointService.usePoint(customerPhone, pointUsed, (long) order.getId());
                    log.info("포인트 사용 - {}: -{}P", customerPhone, pointUsed);
                } catch (Exception e) {
                    log.warn("포인트 사용 실패: {}", e.getMessage());
                }
            }
            // 포인트 적립 (최종 결제금액의 5%)
            if (finalAmount > 0) {
                earnedPoints = (int) (finalAmount * 0.05);
                pointService.earnPoint(customerPhone, earnedPoints, (long) order.getId());
                log.info("포인트 적립 - {}: +{}P", customerPhone, earnedPoints);
            }
        }

        session.removeAttribute("customerPhone");

        res.put("success", true);
        res.put("message", "결제가 완료되었습니다.");
        res.put("orderId", order.getId());
        res.put("paymentMethod", paymentMethod);
        res.put("totalAmount", totalAmount);
        res.put("pointUsed", pointUsed);
        res.put("finalAmount", finalAmount);
        res.put("earnedPoints", earnedPoints);
        return res;
    }

    // ===========================================================
    // 헬퍼
    // ===========================================================

    private int getPartySize(HttpSession session) {
        Object val = session.getAttribute("partySize");
        return val instanceof Integer ? (Integer) val : 2;
    }

    private int getSessionDuration(HttpSession session) {
        Object start = session.getAttribute("sessionStartTime");
        if (!(start instanceof Long)) return 0;
        return (int) ((System.currentTimeMillis() - (Long) start) / 60000);
    }

    private int toInt(Object val) {
        if (val == null) return 0;
        return ((Number) val).intValue();
    }
}
