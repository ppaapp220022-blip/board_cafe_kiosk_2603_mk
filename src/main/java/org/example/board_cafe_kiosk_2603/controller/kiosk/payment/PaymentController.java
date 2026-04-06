package org.example.board_cafe_kiosk_2603.controller.kiosk.payment;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.dto.kiosk.payment.PaymentDTO;
import org.example.board_cafe_kiosk_2603.service.kiosk.KioskPageService;
import org.example.board_cafe_kiosk_2603.service.kiosk.payment.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * 결제 관련 컨트롤러
 * - GET /kiosk/checkout: 정산 페이지 (결제 준비)
 * - POST /kiosk/payment/prepare: 토스 결제창 준비 (tosspayments widget API)
 * - POST /kiosk/payment/confirm: 토스 결제 승인 + DB 저장
 */
@Log4j2
@Controller
@RequestMapping("/kiosk")
@RequiredArgsConstructor
public class PaymentController {

    private final KioskPageService kioskPageService;
    private final PaymentService paymentService;

    // ===================================================
    // 정산 페이지
    // ===================================================

    @GetMapping("/checkout")
    public String checkoutPage(HttpSession session, Model model) {
        Integer tableNumber = (Integer) session.getAttribute("tableNumber");
        if (tableNumber == null) return "redirect:/kiosk";

        kioskPageService.buildCheckoutModel(model, tableNumber, session);
        model.addAttribute("tableNumber", tableNumber);  // 테이블 번호 추가

        log.info("정산 화면 - 테이블: {}", tableNumber);
        return "kiosk/checkout";
    }

    // ===================================================
    // 토스페이먼츠 결제 API
    // ===================================================

    /**
     * 1단계: 결제 준비
     * 토스 결제창을 띄우기 전에 필요한 정보 반환
     * - orderIdToss, amount, orderName, clientKey 등
     */
    @PostMapping("/payment/prepare")
    @ResponseBody
    public ResponseEntity<PaymentDTO> tossPrepare(
            @RequestParam("tableNumber") int tableNumber,
            @RequestBody @Valid PaymentDTO request,
            HttpSession session) {
        log.info("토스 결제 준비 요청 - tableNumber: {}", tableNumber);
        PaymentDTO response = paymentService.preparePayment(tableNumber, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 2단계: 결제 승인
     * 토스에서 전달받은 결제 정보를 검증하고 DB에 저장
     * - orderId 생성, payment 레코드 저장, 포인트 처리 등
     */
    @PostMapping("/payment/confirm")
    @ResponseBody
    public ResponseEntity<PaymentDTO> tossConfirm(
            @RequestParam("paymentKey") String paymentKey,
            @RequestParam("orderIdToss") String orderIdToss,
            @RequestParam("amount") int amount,
            @RequestParam("tableNumber") int tableNumber,
            @RequestParam(value = "pointUsed", defaultValue = "0") int pointUsed,
            @RequestParam(value = "customerPhone", required = false) String customerPhone,
            HttpSession session) {
        log.info("토스 결제 승인 요청 - orderId: {}, amount: {}", orderIdToss, amount);
        PaymentDTO response = paymentService.confirmPayment(paymentKey, orderIdToss, amount, tableNumber, pointUsed, customerPhone);
        return ResponseEntity.ok(response);
    }

    /**
     * 결제 성공 콜백
     * 토스 위젯에서 결제 완료 후 리다이렉트
     */
    @GetMapping("/toss/success")
    public String tossSuccess(
            @RequestParam("paymentKey") String paymentKey,
            @RequestParam("orderId") String orderIdToss,
            @RequestParam("amount") int amount,
            HttpSession session,
            Model model) {
        try {
            Integer tableNumber = (Integer) session.getAttribute("tableNumber");
            String customerPhone = (String) session.getAttribute("customerPhone");

            // 결제 승인 (DB 저장)
            PaymentDTO confirmResponse = paymentService.confirmPayment(
                    paymentKey, orderIdToss, amount, tableNumber, 0, customerPhone);

            if (confirmResponse.isSuccess()) {
                model.addAttribute("orderId", confirmResponse.getOrderId());
                model.addAttribute("finalAmount", confirmResponse.getFinalAmount());
                model.addAttribute("earnedPoints", confirmResponse.getEarnedPoints());
                model.addAttribute("tableNumber", tableNumber);
                log.info("결제 성공 - orderId: {}, amount: {}", orderIdToss, amount);
                return "kiosk/toss_success";
            } else {
                model.addAttribute("errorMessage", confirmResponse.getMessage());
                return "kiosk/toss_fail";
            }
        } catch (Exception e) {
            log.error("결제 성공 처리 중 오류", e);
            model.addAttribute("errorMessage", "결제 처리 중 오류가 발생했습니다.");
            return "kiosk/toss_fail";
        }
    }

    /**
     * 결제 실패 콜백
     * 토스 위젯에서 결제 실패 또는 취소 후 리다이렉트
     */
    @GetMapping("/toss/fail")
    public String tossFail(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "message", required = false) String message,
            Model model) {
        model.addAttribute("errorCode", code);
        model.addAttribute("errorMessage", message != null ? message : "결제가 실패했습니다.");
        log.warn("결제 실패 - code: {}, message: {}", code, message);
        return "kiosk/toss_fail";
    }
}
