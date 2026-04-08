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
import org.example.board_cafe_kiosk_2603.domain.kiosk.order.OrderStatus;
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
import java.util.stream.Collectors;

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
     * 테이블, 세션, 주문 유효성 검증 후 결제 정보 반환
     *
     * [특징]
     *   - 세션 내 여러 주문을 한 번의 결제로 처리 가능
     *   - 패키지 요금 + 주문 금액 합산
     *   - 포인트 사용 가능
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

            // 세션 주문 목록 조회 (카트가 비어있어도 결제 가능)
            List<Orders> sessionOrders = ordersMapper.findBySessionId(session.getId());
            List<Orders> activeOrders = sessionOrders.stream()
                    .filter(o -> !OrderStatus.CANCELLED.name().equals(o.getStatus()))
                    .collect(Collectors.toList());
            if (activeOrders.isEmpty()) {
                return errorResponse("주문 내역이 없습니다.");
            }
            log.debug("activeOrders count: {}", activeOrders.size());

            // === Package 정보 조회 및 설정 ===
            CafePackageDTO cafePackage = null;
            int pkgPrice = 0;
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
                    pkgPrice = pkg.getBasePrice() * (session.getInitialGuestCnt() != null ? session.getInitialGuestCnt() : 1);
                    log.debug("Package selected - id: {}, name: {}, price: {}",
                            cafePackage.getId(), cafePackage.getName(), pkgPrice);
                } else {
                    log.warn("Package not found - packageId: {}", session.getPackageId());
                }
            }

            int menuTotal = activeOrders.stream().mapToInt(Orders::getTotalAmount).sum();
            int totalAmount = pkgPrice + menuTotal;
            int pointUsed = request.getPointUsed() != null ? request.getPointUsed() : 0;
            int finalAmount = Math.max(totalAmount - pointUsed, 0);

            String orderIdToss = generateOrderId(tableNumber);
            String orderName = buildOrderNameFromOrders(activeOrders);
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
     * 토스 API 호출 후 세션의 모든 주문, 결제 정보 저장 및 포인트 처리
     *
     * [복수 주문 결제]
     *   - 세션 내 여러 주문(Order)을 한 번의 결제(Payment)로 처리
     *   - Payment.session_id가 유일하므로 세션당 최종 1회만 결제
     *   - 포인트는 최신 주문을 기준으로 적립
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

            // 세션 주문 목록으로 금액 계산
            List<Orders> sessionOrders = ordersMapper.findBySessionId(session.getId());
            List<Orders> activeOrders = sessionOrders.stream()
                    .filter(o -> !OrderStatus.CANCELLED.name().equals(o.getStatus()))
                    .collect(Collectors.toList());

            int pkgPrice = 0;
            if (session.getPackageId() != null) {
                CafePackage pkg = cafePackageMapper.findById(session.getPackageId());
                if (pkg != null) {
                    pkgPrice = pkg.getBasePrice() * (session.getInitialGuestCnt() != null ? session.getInitialGuestCnt() : 1);
                }
            }
            int menuTotal = activeOrders.stream().mapToInt(Orders::getTotalAmount).sum();
            int totalAmount = pkgPrice + menuTotal;
            int finalAmount = Math.max(totalAmount - pointUsed, 0);

            // 최신 주문 ID 조회 (포인트 참조용)
            int latestOrderId = activeOrders.stream()
                    .mapToInt(Orders::getId)
                    .max()
                    .orElse(0);

            // 결제 생성 (토스 정보 포함)
            createPayment(session, finalAmount, paymentKey, orderIdToss, tossResponse, tableNumber);

            // 포인트 처리
            int earnedPoints = processPoints(customerPhone, pointUsed, finalAmount, latestOrderId);

            log.info("결제 완료 - latestOrderId: {}, amount: {}", latestOrderId, finalAmount);

            return PaymentDTO.builder()
                    .success(true)
                    .orderId((long) latestOrderId)
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

    private String buildOrderNameFromOrders(List<Orders> orders) {
        if (orders.isEmpty()) return "주문";
        List<OrderItem> items = ordersMapper.findItemsByOrderId(orders.get(0).getId());
        if (items.isEmpty()) return "주문";
        int extra = orders.size() - 1;
        return items.get(0).getMenuName() + (extra > 0 ? " 외 " + extra + "건" : "");
    }

    private Orders createOrder(CafeTableSession session, Integer tableId, String customerPhone, int totalAmount) {
        Orders order = Orders.builder()
                .sessionId(session.getId())
                .tableId(tableId)
                .customerPhone((customerPhone != null && !customerPhone.isBlank()) ? customerPhone : null)
                .status(OrderStatus.ORDERED.name())
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
