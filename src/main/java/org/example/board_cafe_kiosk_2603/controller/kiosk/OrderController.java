package org.example.board_cafe_kiosk_2603.controller.kiosk;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.dto.kiosk.OrdersDTO;
import org.example.board_cafe_kiosk_2603.service.kiosk.KioskPageService;
import org.example.board_cafe_kiosk_2603.service.kiosk.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 키오스크 주문 조회 REST API 컨트롤러.
 * 주문 생성은 PaymentController / TossPaymentController 에서 담당합니다.
 * URL: /kiosk/order/*
 */
@Log4j2
@RestController
@RequestMapping("/kiosk/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService     orderService;
    private final KioskPageService kioskPageService;

    /**
     * 주문 단건 조회
     * GET /kiosk/order/{orderId}
     */
    @GetMapping("/{orderId}")
    public OrdersDTO getOrder(@PathVariable int orderId) {
        log.info("주문 조회 - orderId: {}", orderId);
        return orderService.getOrder(orderId);
    }

    /**
     * 테이블의 가장 최근 주문 조회
     * GET /kiosk/order/latest?tableNumber=1
     */
    @GetMapping("/latest")
    public OrdersDTO getLatestOrder(
            @RequestParam(required = false, defaultValue = "1") Integer tableNumber,
            HttpSession session) {
        tableNumber = kioskPageService.resolveTableNumber(tableNumber, session);
        log.info("최근 주문 조회 - tableNumber: {}", tableNumber);
        return orderService.getLatestOrder(tableNumber);
    }

    /**
     * 세션의 전체 주문 목록 조회
     * GET /kiosk/order/session/{sessionId}
     */
    @GetMapping("/session/{sessionId}")
    public List<OrdersDTO> getOrdersBySession(@PathVariable long sessionId) {
        log.info("세션 주문 목록 조회 - sessionId: {}", sessionId);
        return orderService.getOrdersBySession(sessionId);
    }
}
