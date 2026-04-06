package org.example.board_cafe_kiosk_2603.service.kiosk.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.common.cafeTableSession.CafeTableSession;
import org.example.board_cafe_kiosk_2603.domain.kiosk.cafePackage.CafePackage;
import org.example.board_cafe_kiosk_2603.domain.kiosk.cart.Cart;
import org.example.board_cafe_kiosk_2603.domain.kiosk.cart.CartItem;
import org.example.board_cafe_kiosk_2603.domain.kiosk.order.OrderItem;
import org.example.board_cafe_kiosk_2603.domain.kiosk.order.Orders;
import org.example.board_cafe_kiosk_2603.domain.kiosk.payment.Payment;
import org.example.board_cafe_kiosk_2603.dto.kiosk.cafePackage.CafePackageDTO;
import org.example.board_cafe_kiosk_2603.dto.kiosk.payment.PaymentDTO;
import org.example.board_cafe_kiosk_2603.mapper.common.cafeTableSession.CafeTableSessionMapper;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.cafePackage.CafePackageMapper;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.cart.CartItemMapper;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.cart.CartMapper;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.order.OrdersMapper;
import org.example.board_cafe_kiosk_2603.service.admin.point.PointService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
@RequiredArgsConstructor
public class PaymentService {

    // === Property Keys ===
    private static final String TOSS_SECRET_KEY_PROPERTY = "${toss.payments.secret-key}";
    private static final String TOSS_CLIENT_KEY_PROPERTY = "${toss.payments.client-key}";
    private static final String TOSS_CONFIRM_URL_PROPERTY = "${toss.payments.confirm-url}";

    private final CafePackageMapper cafePackageMapper;
    private final OrdersMapper ordersMapper;
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;
    private final PointService pointService;
    private final CafeTableSessionMapper tableSessionMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value(TOSS_SECRET_KEY_PROPERTY)
    private String secretKey;

    @Value(TOSS_CLIENT_KEY_PROPERTY)
    private String clientKey;

    @Value(TOSS_CONFIRM_URL_PROPERTY)
    private String confirmUrl;

    private static final double EARN_RATE = 0.05;

    /**
     * 결제 준비 - 토스 결제창 호출 전 준비 단계
     * 테이블, 세션, 장바구니 유효성 검증 후 결제 정보 반환
     */
    public PaymentDTO preparePayment(int tableNumber, PaymentDTO request) {
        try {
            log.info("=== 결제 준비 시작 - tableNumber: {}", tableNumber);

            Integer tableId = cartMapper.findCafeTableIdByTableNumber(tableNumber);
            if (tableId == null) {
                return errorResponse("존재하지 않는 테이블입니다: " + tableNumber);
            }
            log.debug("tableId: {}", tableId);

            CafeTableSession session = tableSessionMapper.findActiveByTableId(tableId);
            if (session == null) {
                return errorResponse("진행 중인 세션이 없습니다. 패키지를 먼저 선택해 주세요.");
            }
            log.debug("sessionId: {}", session.getId());

            Cart cart = cartMapper.findByTableId(tableId);
            if (cart == null) {
                return errorResponse("장바구니가 비어있습니다.");
            }
            log.debug("cartId: {}", cart.getId());

            List<CartItem> items = cartItemMapper.findByCartId(cart.getId());
            if (items.isEmpty()) {
                return errorResponse("장바구니에 상품이 없습니다.");
            }
            log.debug("cartItems count: {}", items.size());

            // === Package 정보 조회 및 설정 ===
            CafePackageDTO cafePackage = null;
            if (session.getPackageId() != null) {
                CafePackage pkg = cafePackageMapper.findById(session.getPackageId());
                if (pkg != null) {
                    cafePackage = CafePackageDTO.builder()
                            .id(pkg.getId())
                            .name(pkg.getName())
                            .type(pkg.getType())
                            .durationMinutes(pkg.getDurationMinutes())
                            .basePrice(pkg.getBasePrice())
                            .extraPricePerMin(pkg.getExtraPricePerMin())
                            .active(pkg.isActive())
                            .updatedAt(pkg.getUpdatedAt())
                            .build();
                    log.debug("Package selected - id: {}, name: {}, price: {}",
                            cafePackage.getId(), cafePackage.getName(), cafePackage.getBasePrice());
                } else {
                    log.warn("Package not found - packageId: {}", session.getPackageId());
                }
            }

            int totalAmount = calculateTotal(items, session);
            int pointUsed = request.getPointUsed() != null ? request.getPointUsed() : 0;
            int finalAmount = Math.max(totalAmount - pointUsed, 0);

            String orderIdToss = generateOrderId(tableNumber);
            String orderName = buildOrderName(items);
            String customerKey = generateCustomerKey(tableNumber, session.getId());

            PaymentDTO response = PaymentDTO.builder()
                    .success(true)
                    .orderIdToss(orderIdToss)
                    .amount(finalAmount)
                    .orderName(orderName)
                    .totalAmount(totalAmount)
                    .pointUsed(pointUsed)
                    .clientKey(clientKey)
                    .customerKey(customerKey)
                    .cafePackage(cafePackage)
                    .build();

            log.info("=== 결제 준비 완료 - orderId: {}, amount: {}, packageId: {}",
                    orderIdToss, finalAmount, cafePackage != null ? cafePackage.getId() : "없음");
            return response;

        } catch (Exception e) {
            log.error("결제 준비 중 오류 - tableNumber: {}", tableNumber, e);
            return errorResponse("결제 준비 중 오류가 발생했습니다.");
        }
    }

    /**
     * 토스 결제 승인 및 DB 저장
     * 토스 API 호출 후 주문, 결제 정보 저장 및 포인트 처리
     */
    @Transactional
    public PaymentDTO confirmPayment(String paymentKey, String orderIdToss,
                                     int amount, int tableNumber,
                                     int pointUsed, String customerPhone) {
        try {
            // 중복 결제 방지
            if (ordersMapper.findByPaymentKey(paymentKey) != null) {
                return errorResponse("이미 처리된 결제입니다.");
            }

            // 토스 API 호출 및 응답 파싱
            TossConfirmResponse tossResponse = callTossConfirmApi(paymentKey, orderIdToss, amount);
            if (tossResponse == null) {
                return errorResponse("토스 API 응답 파싱 실패");
            }

            // DB 데이터 조회
            Integer tableId = cartMapper.findCafeTableIdByTableNumber(tableNumber);
            CafeTableSession session = tableSessionMapper.findActiveByTableId(tableId);
            Cart cart = cartMapper.findByTableId(tableId);
            List<CartItem> cartItems = cartItemMapper.findByCartId(cart.getId());

            // 금액 계산
            int totalAmount = calculateTotal(cartItems, session);
            int finalAmount = Math.max(totalAmount - pointUsed, 0);

            // 주문 생성
            Orders order = createOrder(session, tableId, customerPhone, totalAmount);

            // 주문 항목 생성
            createOrderItems(order, cartItems);

            // 결제 생성 (토스 정보 포함)
            createPayment(session, finalAmount, paymentKey, orderIdToss, tossResponse, tableNumber);

            // 포인트 처리
            int earnedPoints = processPoints(customerPhone, pointUsed, finalAmount, order.getId());

            // 장바구니 비우기
            cartItemMapper.deleteAllByCartId(cart.getId());

            log.info("결제 완료 - orderId: {}, amount: {}", order.getId(), finalAmount);

            return PaymentDTO.builder()
                    .success(true)
                    .orderId((long) order.getId())
                    .totalAmount(totalAmount)
                    .pointUsed(pointUsed)
                    .finalAmount(finalAmount)
                    .earnedPoints(earnedPoints)
                    .paymentKey(paymentKey)
                    .method(tossResponse.method)
                    .build();

        } catch (HttpClientErrorException e) {
            log.error("토스 API 호출 실패 - {}", e.getResponseBodyAsString());
            return errorResponse(parseTossError(e.getResponseBodyAsString()));
        } catch (Exception e) {
            log.error("결제 승인 중 오류", e);
            return errorResponse("결제 승인 중 오류가 발생했습니다.");
        }
    }

    // ===================================================
    // 토스 API 호출 및 헬퍼 메서드
    // ===================================================

    private TossConfirmResponse callTossConfirmApi(String paymentKey, String orderId, int amount) {
        try {
            String encoded = Base64.getEncoder().encodeToString(
                    (secretKey + ":").getBytes(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + encoded);

            Map<String, Object> body = new HashMap<>();
            body.put("paymentKey", paymentKey);
            body.put("orderId", orderId);
            body.put("amount", amount);

            ResponseEntity<String> response = restTemplate.exchange(
                    confirmUrl, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);

            if (response.getBody() == null) {
                return null;
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            return TossConfirmResponse.builder()
                    .method(root.path("method").asText())
                    .approvedAt(root.path("approvedAt").asText())
                    .rawResponse(response.getBody())
                    .build();

        } catch (Exception e) {
            log.error("토스 API 파싱 오류", e);
            return null;
        }
    }

    private String parseTossError(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            String message = root.path("message").asText();
            return !message.isBlank() ? message : "결제 승인에 실패했습니다.";
        } catch (Exception e) {
            return "결제 승인에 실패했습니다.";
        }
    }

    // === 비즈니스 로직 헬퍼 ===

    // 수정된 계산 로직
    private int calculateTotal(List<CartItem> items, CafeTableSession session) {
        // 1. 장바구니 메뉴 합계 계산
        int menuTotal = items.stream().mapToInt(i -> i.getMenuPrice() * i.getQuantity()).sum();

        // 2. 세션에 저장된 패키지 요금 가져오기 (필드명은 도메인 설계를 확인하세요)
        int packagePrice = session.getTotalAmount(); // 예: session 객체에 포함된 금액

        log.debug("Package Price: {}, Menu Total: {}", packagePrice, menuTotal);

        return packagePrice + menuTotal;
    }

    private String generateOrderId(int tableNumber) {
        return "KIOSK-T" + tableNumber + "-" + System.currentTimeMillis();
    }

    private String generateCustomerKey(int tableNumber, long sessionId) {
        return "GUEST-T" + tableNumber + "-S" + sessionId;
    }

    private String buildOrderName(List<CartItem> items) {
        return items.get(0).getMenuName()
                + (items.size() > 1 ? " and " + (items.size() - 1) + " more" : "");
    }

    private Orders createOrder(CafeTableSession session, Integer tableId, String customerPhone, int totalAmount) {
        Orders order = Orders.builder()
                .sessionId(session.getId())
                .tableId(tableId)
                .customerPhone((customerPhone != null && !customerPhone.isBlank()) ? customerPhone : null)
                .status("PAID")
                .totalAmount(totalAmount)
                .build();
        ordersMapper.insertOrder(order);
        return order;
    }

    private void createOrderItems(Orders order, List<CartItem> cartItems) {
        for (CartItem ci : cartItems) {
            ordersMapper.insertOrderItem(OrderItem.builder()
                    .orderId(order.getId())
                    .menuId(ci.getMenuId())
                    .menuName(ci.getMenuName())
                    .price(ci.getMenuPrice())
                    .quantity(ci.getQuantity())
                    .build());
        }
    }

    private void createPayment(CafeTableSession session, int finalAmount,
                               String paymentKey, String orderIdToss,
                               TossConfirmResponse tossResponse, int tableNumber) {
        LocalDateTime approvedAt = tossResponse.approvedAt != null && !tossResponse.approvedAt.isBlank()
                ? LocalDateTime.parse(tossResponse.approvedAt.substring(0, 19))
                : LocalDateTime.now();

        Payment payment = Payment.builder()
                .sessionId(session.getId())
                .tableNumber(tableNumber)
                .status("DONE")
                .finalAmount(finalAmount)
                .paymentKey(paymentKey)
                .orderIdToss(orderIdToss)
                .method(tossResponse.method)
                .rawResponse(tossResponse.rawResponse)
                .approvedAt(approvedAt)
                .paidAt(LocalDateTime.now())
                .build();
        ordersMapper.insertPayment(payment);
        ordersMapper.completePayment(session.getId());
    }

    private int processPoints(String customerPhone, int pointUsed, int finalAmount, int orderId) {
        int earnedPoints = 0;

        if (pointUsed > 0 && isValidPhone(customerPhone)) {
            pointService.usePoint(customerPhone, pointUsed, (long) orderId);
            log.info("포인트 사용 - {}: -{}P", customerPhone, pointUsed);
        }

        if (isValidPhone(customerPhone) && finalAmount > 0) {
            earnedPoints = (int) Math.floor(finalAmount * EARN_RATE);
            pointService.earnPoint(customerPhone, earnedPoints, (long) orderId);
            log.info("포인트 적립 - {}: +{}P", customerPhone, earnedPoints);
        }

        return earnedPoints;
    }

    private boolean isValidPhone(String customerPhone) {
        return customerPhone != null && !customerPhone.isBlank();
    }

    private PaymentDTO errorResponse(String message) {
        return PaymentDTO.builder()
                .success(false)
                .message(message)
                .build();
    }

    // === 내부 응답 클래스 ===
    @Builder
    private static class TossConfirmResponse {
        String method;
        String approvedAt;
        String rawResponse;
    }
}
