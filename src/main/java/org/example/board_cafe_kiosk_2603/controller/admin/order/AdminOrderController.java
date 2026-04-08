package org.example.board_cafe_kiosk_2603.controller.admin.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.dto.kiosk.order.OrdersDTO;
import org.example.board_cafe_kiosk_2603.service.kiosk.order.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable int orderId,
            @RequestBody Map<String, String> body) {

        // ✅ null 체크 추가
        if (body == null || body.get("status") == null || body.get("status").isBlank()) {
            log.warn("상태값이 없음 - orderId: {}", orderId);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", "상태값이 필요합니다."
                    ));
        }

        String status = body.get("status").trim();
        log.info("주문 상태 변경 - orderId: {}, status: {}", orderId, status);

        try {
            OrdersDTO result = orderService.updateStatus(orderId, status);

            if (result.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "orderId", result.getId(),
                        "status", result.getStatus(),
                        "message", "상태가 변경되었습니다."
                ));
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", result.getMessage()
                        ));
            }
        } catch (Exception e) {
            log.error("상태 변경 실패 - orderId: {}", orderId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "상태 변경 실패: " + e.getMessage()
                    ));
        }
    }
}