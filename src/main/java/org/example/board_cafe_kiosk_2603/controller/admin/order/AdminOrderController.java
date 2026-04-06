package org.example.board_cafe_kiosk_2603.controller.admin.order;

import lombok.RequiredArgsConstructor;
import org.example.board_cafe_kiosk_2603.dto.kiosk.order.OrdersDTO;
import org.example.board_cafe_kiosk_2603.service.kiosk.order.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrdersDTO> updateOrderStatus(
            @PathVariable int orderId,
            @RequestBody Map<String, String> body) {

        String status = body.get("status");
        OrdersDTO result = orderService.updateStatus(orderId, status);
        return result.isSuccess() ?
                ResponseEntity.ok(result) :
                ResponseEntity.badRequest().body(result);
    }
}