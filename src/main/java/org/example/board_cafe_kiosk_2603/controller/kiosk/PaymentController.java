package org.example.board_cafe_kiosk_2603.controller.kiosk;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.dto.kiosk.PaymentDTO;
import org.example.board_cafe_kiosk_2603.service.kiosk.KioskPageService;
import org.example.board_cafe_kiosk_2603.service.kiosk.PaymentService;
import org.springframework.web.bind.annotation.*;

/**
 * 키오스크 일반 결제 REST API 컨트롤러.
 * URL: /kiosk/checkout/*
 */
@Log4j2
@RestController
@RequestMapping("/kiosk/checkout")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService   paymentService;
    private final KioskPageService kioskPageService;

    @PostMapping("/pay")
    public PaymentDTO processPayment(
            @RequestBody @Valid PaymentDTO request,
            @RequestParam(required = false, defaultValue = "1") Integer tableNumber,
            HttpSession session) {

        tableNumber = kioskPageService.resolveTableNumber(tableNumber, session);
        String customerPhone = (String) session.getAttribute("customerPhone");

        log.info("=== 결제 요청 === 테이블: {}, 수단: {}, 포인트: {}P",
                tableNumber, request.getPaymentMethod(), request.getPointUsed());

        PaymentDTO response = paymentService.processPayment(tableNumber, request, customerPhone);
        if (response.isSuccess()) {
            session.removeAttribute("customerPhone");
        }
        return response;
    }
}
