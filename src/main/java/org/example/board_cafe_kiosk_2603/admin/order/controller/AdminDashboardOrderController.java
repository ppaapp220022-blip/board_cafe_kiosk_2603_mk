package org.example.board_cafe_kiosk_2603.admin.order.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.kiosk.order.dto.OrdersDTO;
import org.example.board_cafe_kiosk_2603.kiosk.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
/*
 * 작성자 : 김민기
 * 기능 : 관리자 대시보드 주문 REST API
 * 날짜 : 2026-04-06
 */

@Log4j2
@RestController
@RequestMapping("/admin/api/dashboard")
@RequiredArgsConstructor
public class AdminDashboardOrderController {

    private final OrderService orderService;
    @GetMapping("/orders/latest")
    public ResponseEntity<Map<String, Object>> getLatestOrders() {
        log.info("신규 주문(ORDERED) 목록 조회");
        try {
            List<OrdersDTO> orders = orderService.getNewOrders();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "orders", orders,
                    "count", orders.size()
            ));
        } catch (Exception e) {
            log.error("주문 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "조회 실패"));
        }
    }
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrdersDTO> getOrder(@PathVariable int orderId) {
        log.info("주문 조회 - orderId: {}", orderId);
        OrdersDTO result = orderService.getOrder(orderId);
        return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }
    @PatchMapping("/orders/{orderId}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable int orderId,
            @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");

        if (newStatus == null || newStatus.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "상태값이 필요합니다."));
        }

        log.info("주문 상태 변경 - orderId: {}, newStatus: {}", orderId, newStatus);

        try {
            OrdersDTO result = orderService.updateStatus(orderId, newStatus);
            // updateStatus() 내부에서 이미 웹소켓 호출

            if (result.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "orderId", result.getId(),
                        "status", result.getStatus(),
                        "message", "상태가 변경되었습니다."
                ));
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", result.getMessage()));
            }
        } catch (Exception e) {
            log.error("상태 변경 실패 - orderId: {}", orderId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "상태 변경 실패"));
        }
    }
    @GetMapping("/orders/{orderId}/next-status")
    public ResponseEntity<Map<String, Object>> getNextStatus(@PathVariable int orderId) {
        log.info("다음 상태 조회 - orderId: {}", orderId);

        OrdersDTO order = orderService.getOrder(orderId);
        if (!order.isSuccess()) {
            return ResponseEntity.notFound().build();
        }

        String currentStatus = order.getStatus();
        String nextStatus = getNextStatusForTransition(currentStatus);
        String buttonText = getButtonTextForStatus(currentStatus);
        boolean canChange = !currentStatus.equals("COMPLETED") && !currentStatus.equals("CANCELLED");

        return ResponseEntity.ok(Map.of(
                "success", true,
                "orderId", orderId,
                "currentStatus", currentStatus,
                "nextStatus", nextStatus,
                "buttonText", buttonText,
                "canChange", canChange
        ));
    }
    private String getNextStatusForTransition(String currentStatus) {
        return switch (currentStatus) {
            case "ORDERED"   -> "CONFIRMED";
            case "CONFIRMED" -> "COOKING";
            case "COOKING"   -> "DELIVERING";
            case "DELIVERING"    -> "COMPLETED";
            default -> null;
        };
    }
    private String getButtonTextForStatus(String status) {
        return switch (status) {
            case "ORDERED"   -> "주문 완료";
            case "CONFIRMED" -> "주문 확인";
            case "COOKING"   -> "조리 시작";
            case "DELIVERING"-> "서빙 시작";
            case "COMPLETED" -> "서빙 완료";
            case "CANCELLED" -> "취소됨";
            default -> "상태 변경";
        };
    }
    public static String getStatusDisplayName(String status) {
        return switch (status) {
            case "ORDERED"   -> "주문완료";
            case "CONFIRMED" -> "주문확인";
            case "COOKING"   -> "조리중";
            case "DELIVERING"-> "서빙중";
            case "COMPLETED" -> "서빙완료";
            case "CANCELLED" -> "취소";
            default -> "상태확인";
        };
    }
}
