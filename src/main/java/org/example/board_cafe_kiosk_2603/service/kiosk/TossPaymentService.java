package org.example.board_cafe_kiosk_2603.service.kiosk;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.kiosk.*;
import org.example.board_cafe_kiosk_2603.dto.kiosk.TossPaymentDTO;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.CartItemMapper;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.CartMapper;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.OrdersMapper;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.TableSessionMapper;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.TossPaymentMapper;
import org.example.board_cafe_kiosk_2603.service.admin.PointService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Log4j2
@Service
@RequiredArgsConstructor
public class TossPaymentService {

    private final TossPaymentMapper  tossPaymentMapper;
    private final OrdersMapper       ordersMapper;
    private final CartMapper         cartMapper;
    private final CartItemMapper     cartItemMapper;
    private final PointService       pointService;
    private final TableSessionMapper tableSessionMapper;
    private final RestTemplate       restTemplate;

    @Value("${toss.payments.secret-key}")
    private String secretKey;

    @Value("${toss.payments.confirm-url}")
    private String confirmUrl;

    private static final double EARN_RATE = 0.05;

    // ===================================================
    // 1단계: 결제창 요청 전 준비
    // ===================================================

    public TossPaymentDTO preparePayment(int tableNumber, TossPaymentDTO request) {
        Integer tableId = cartMapper.findCafeTableIdByTableNumber(tableNumber);
        if (tableId == null) {
            return TossPaymentDTO.fail("존재하지 않는 테이블입니다: " + tableNumber);
        }

        TableSession session = tableSessionMapper.findActiveByTableId(tableId);
        if (session == null) {
            return TossPaymentDTO.fail("진행 중인 세션이 없습니다. 패키지를 먼저 선택해 주세요.");
        }

        Cart cart = cartMapper.findByTableId(tableId);
        if (cart == null) {
            return TossPaymentDTO.fail("장바구니가 비어있습니다.");
        }

        List<CartItem> items = cartItemMapper.findByCartId(cart.getId());
        if (items.isEmpty()) {
            return TossPaymentDTO.fail("장바구니에 상품이 없습니다.");
        }

        int totalAmount = items.stream().mapToInt(i -> i.getMenuPrice() * i.getQuantity()).sum();
        int pointUsed   = request.getPointUsed();
        int finalAmount = Math.max(totalAmount - pointUsed, 0);

        String orderIdToss = "KIOSK-T" + tableNumber + "-" + System.currentTimeMillis();
        String orderName   = items.get(0).getMenuName()
                + (items.size() > 1 ? " 외 " + (items.size() - 1) + "건" : "");

        return TossPaymentDTO.prepared(orderIdToss, finalAmount, orderName, totalAmount, pointUsed);
    }

    // ===================================================
    // 2단계: 토스 결제 승인 + DB 저장
    // ===================================================

    @Transactional
    public TossPaymentDTO confirmPayment(String paymentKey, String orderIdToss,
                                         int amount, int tableNumber,
                                         int pointUsed, String customerPhone) {
        if (tossPaymentMapper.findByPaymentKey(paymentKey) != null) {
            return TossPaymentDTO.fail("이미 처리된 결제입니다.");
        }

        String rawResponse;
        String approvedMethod;
        LocalDateTime approvedAt;
        try {
            rawResponse    = callTossConfirmApi(paymentKey, orderIdToss, amount);
            approvedMethod = extractField(rawResponse, "method");
            String approvedAtStr = extractField(rawResponse, "approvedAt");
            approvedAt = (approvedAtStr != null)
                    ? LocalDateTime.parse(approvedAtStr.substring(0, 19))
                    : LocalDateTime.now();
            log.info("토스 승인 성공 - paymentKey: {}, method: {}", paymentKey, approvedMethod);
        } catch (HttpClientErrorException e) {
            log.error("토스 승인 실패 - {}", e.getResponseBodyAsString());
            return TossPaymentDTO.fail(parseTossError(e.getResponseBodyAsString()));
        } catch (Exception e) {
            log.error("토스 API 호출 오류 - {}", e.getMessage());
            return TossPaymentDTO.fail("결제 승인 중 오류가 발생했습니다.");
        }

        Integer tableId = cartMapper.findCafeTableIdByTableNumber(tableNumber);
        TableSession session = tableSessionMapper.findActiveByTableId(tableId);
        Cart cart = cartMapper.findByTableId(tableId);
        List<CartItem> cartItems = cartItemMapper.findByCartId(cart.getId());

        int totalAmount = cartItems.stream().mapToInt(i -> i.getMenuPrice() * i.getQuantity()).sum();
        int finalAmount = Math.max(totalAmount - pointUsed, 0);

        Orders order = Orders.builder()
                .sessionId(session.getId())
                .tableId(tableId)
                .customerPhone((customerPhone != null && !customerPhone.isBlank()) ? customerPhone : null)
                .status("PAID")
                .totalAmount(totalAmount)
                .build();
        ordersMapper.insertOrder(order);

        for (CartItem ci : cartItems) {
            ordersMapper.insertOrderItem(OrderItem.builder()
                    .orderId(order.getId())
                    .menuId(ci.getMenuId())
                    .menuName(ci.getMenuName())
                    .price(ci.getMenuPrice())
                    .quantity(ci.getQuantity())
                    .build());
        }

        Payment payment = Payment.builder()
                .sessionId(session.getId())
                .finalAmount(finalAmount)
                .build();
        ordersMapper.insertPayment(payment);
        ordersMapper.completePayment(session.getId());

        tossPaymentMapper.insert(TossPayment.builder()
                .paymentId(payment.getId())
                .paymentKey(paymentKey)
                .orderIdToss(orderIdToss)
                .method(approvedMethod)
                .rawResponse(rawResponse)
                .approvedAt(approvedAt)
                .build());

        if (pointUsed > 0 && customerPhone != null && !customerPhone.isBlank()) {
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

        return TossPaymentDTO.confirmed(
                order.getId(), totalAmount, pointUsed, finalAmount, earnedPoints,
                paymentKey, approvedMethod);
    }

    // ===================================================
    // 토스 승인 API 호출
    // ===================================================

    private String callTossConfirmApi(String paymentKey, String orderId, int amount) {
        String encoded = Base64.getEncoder().encodeToString(
                (secretKey + ":").getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + encoded);

        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", paymentKey);
        body.put("orderId",    orderId);
        body.put("amount",     amount);

        ResponseEntity<String> response = restTemplate.exchange(
                confirmUrl, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        return response.getBody();
    }

    private String extractField(String json, String key) {
        if (json == null) return null;
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx + search.length());
        if (colon < 0) return null;
        int start = json.indexOf('"', colon + 1);
        if (start < 0) return null;
        int end = json.indexOf('"', start + 1);
        if (end < 0) return null;
        return json.substring(start + 1, end);
    }

    private String parseTossError(String body) {
        if (body == null) return "결제 승인에 실패했습니다.";
        String msg = extractField(body, "message");
        return (msg != null && !msg.isBlank()) ? msg : "결제 승인에 실패했습니다.";
    }
}
