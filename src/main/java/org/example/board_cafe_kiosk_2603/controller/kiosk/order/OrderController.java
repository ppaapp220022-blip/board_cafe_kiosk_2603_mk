package org.example.board_cafe_kiosk_2603.controller.kiosk.order;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.dto.kiosk.order.OrdersDTO;
import org.example.board_cafe_kiosk_2603.service.kiosk.order.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 키오스크 주문 조회 · 상태 변경 REST API.
 * 주문 생성은 PaymentController / TossPaymentController 에서 담당합니다.
 *
 * GET    /kiosk/order/{orderId}            → 주문 단건 조회
 * GET    /kiosk/order/latest               → 테이블 최근 주문 조회 (세션 기반)
 * GET    /kiosk/order/session/{sessionId}  → 세션 전체 주문 목록 조회
 * PATCH  /kiosk/order/{orderId}/status     → 주문 상태 변경
 * DELETE /kiosk/order/{orderId}            → 주문 취소
 */
@Log4j2
@RestController
@RequestMapping("/kiosk/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ===========================================================
    // 조회
    // ===========================================================

    @GetMapping("/{orderId}")
    public ResponseEntity<OrdersDTO> getOrder(@PathVariable int orderId) {
        log.info("주문 조회 - orderId: {}", orderId);
        OrdersDTO result = orderService.getOrder(orderId);
        return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }

    @GetMapping("/latest")
    public ResponseEntity<OrdersDTO> getLatestOrder(HttpSession session) {
        Integer tableNumber = tableNumber(session);
        if (tableNumber == null) return ResponseEntity.badRequest().build();
        log.info("최근 주문 조회 - tableNumber: {}", tableNumber);
        OrdersDTO result = orderService.getLatestOrder(tableNumber);
        return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }

    @GetMapping("/session/{sessionId}")
    public List<OrdersDTO> getOrdersBySession(@PathVariable long sessionId) {
        log.info("세션 주문 목록 조회 - sessionId: {}", sessionId);
        return orderService.getOrdersBySession(sessionId);
    }

    // ===========================================================
    // 상태 변경 / 취소
    // ===========================================================

    /**
     * 주문 상태 변경 (관리자용).
     * body: { "status": "CONFIRMED" }
     */
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrdersDTO> updateStatus(@PathVariable int orderId,
                                                   @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || status.isBlank()) return ResponseEntity.badRequest().build();
        log.info("주문 상태 변경 요청 - orderId: {}, status: {}", orderId, status);
        OrdersDTO result = orderService.updateStatus(orderId, status);
        return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    /**
     * 주문 취소.
     * DELETE /kiosk/order/{orderId}
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<OrdersDTO> cancelOrder(@PathVariable int orderId) {
        log.info("주문 취소 요청 - orderId: {}", orderId);
        OrdersDTO result = orderService.cancelOrder(orderId);
        return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    // ===========================================================
    // 헬퍼
    // ===========================================================

    private Integer tableNumber(HttpSession session) {
        return (Integer) session.getAttribute("tableNumber");
    }
}

