package org.example.board_cafe_kiosk_2603.controller.kiosk.payment;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.dto.kiosk.payment.PaymentDTO;
import org.example.board_cafe_kiosk_2603.service.kiosk.KioskPageService;
import org.example.board_cafe_kiosk_2603.service.kiosk.payment.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 정산 페이지 + 결제 REST API.
 *
 * [페이지] GET  /kiosk/checkout     → checkout.html
 * [API]   POST /kiosk/checkout/pay → 주문 + 결제 처리 (PaymentService 위임)
 *
 * 결제 흐름: cart_item → orders + order_item → payment → cart 비우기 → 포인트 처리
 */
@Log4j2
@Controller
@RequestMapping("/kiosk/checkout")
@RequiredArgsConstructor
public class PaymentController {

    private final KioskPageService kioskPageService;
    private final PaymentService   paymentService;

    // ===========================================================
    // 페이지
    // ===========================================================

    @GetMapping
    public String checkoutPage(HttpSession session, Model model) {
        Integer tableNumber = (Integer) session.getAttribute("tableNumber");
        if (tableNumber == null) return "redirect:/kiosk";

        kioskPageService.buildCheckoutModel(model, tableNumber, session);

        log.info("정산 화면 - 테이블: {}", tableNumber);
        return "kiosk/checkout";
    }

    // ===========================================================
    // REST API
    // ===========================================================

    @PostMapping("/pay")
    @ResponseBody
    public ResponseEntity<PaymentDTO> processPayment(
            @RequestBody Map<String, Object> req,
            HttpSession session) {

        Integer tableNumber  = (Integer) session.getAttribute("tableNumber");
        if (tableNumber == null) {
            return ResponseEntity.badRequest().body(
                    PaymentDTO.builder().success(false).message("세션이 만료되었습니다.").build());
        }

        String customerPhone = getCustomerPhone(session);
        PaymentDTO request = PaymentDTO.builder()
                .paymentMethod((String) req.get("paymentMethod"))
                .pointUsed(toInt(req.get("pointUsed")))
                .build();

        PaymentDTO result = paymentService.processPayment(tableNumber, request, customerPhone);

        if (result.isSuccess()) {
            session.removeAttribute("customerPhone");
            log.info("결제 완료 - 테이블: {}, orderId: {}, 수단: {}",
                    tableNumber, result.getOrderId(), request.getPaymentMethod());
        }
        return ResponseEntity.ok(result);
    }

    // ===========================================================
    // 헬퍼
    // ===========================================================

    private String getCustomerPhone(HttpSession session) {
        Object val = session.getAttribute("customerPhone");
        return val instanceof String ? (String) val : "";
    }

    private int toInt(Object val) {
        if (val == null) return 0;
        return ((Number) val).intValue();
    }
}
