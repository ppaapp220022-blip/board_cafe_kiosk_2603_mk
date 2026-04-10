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
 * - GET  /kiosk/checkout         : 정산 페이지
 * - POST /kiosk/payment/prepare  : 토스 결제창 준비 (위젯 초기화용)
 * - GET  /kiosk/toss/success     : 토스 결제 성공 콜백 → 승인 + DB 저장
 * - GET  /kiosk/toss/fail        : 토스 결제 실패 콜백
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
        if (tableNumber == null) {
            return "redirect:/kiosk";
        }

        kioskPageService.buildCheckoutModel(model, tableNumber, session);
        model.addAttribute("tableNumber", tableNumber);

        log.info("정산 화면 - 테이블: {}", tableNumber);
        return "kiosk/checkout";
    }

    // ===================================================
    // 토스페이먼츠 결제 API
    // ===================================================

    /**
     * 1단계: 결제 준비
     * 토스 결제창을 띄우기 전에 필요한 정보 반환
     * (orderIdToss, amount, orderName, clientKey 등)
     */
    @PostMapping("/payment/prepare")
    @ResponseBody
    public ResponseEntity<PaymentDTO> tossPrepare(
            @RequestParam("tableNumber") int tableNumber,
            @RequestBody @Valid PaymentDTO request,
            HttpSession session) {

        log.info("토스 결제 준비 요청 - tableNumber: {}", tableNumber);

        // 포인트 사용액을 세션에 저장 (success 콜백에서 사용)
        int pointUsed = request.getPointUsed() != null ? request.getPointUsed() : 0;
        session.setAttribute("pointUsed", pointUsed);

        PaymentDTO response = paymentService.preparePayment(tableNumber, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 2단계: 결제 성공 콜백
     * 토스 위젯에서 결제 완료 후 리다이렉트되는 URL
     * 여기서 토스 승인 API 호출 + DB 저장 + 포인트 처리
     */
    @GetMapping("/toss/success")
    public String tossSuccess(
            @RequestParam("paymentKey") String paymentKey,
            @RequestParam("orderId") String orderIdToss,
            @RequestParam("amount") int amount,
            @RequestParam(value = "pointUsed", defaultValue = "0") int pointUsedParam,
            HttpSession session,
            Model model) {
        try {
            Integer tableNumber = (Integer) session.getAttribute("tableNumber");
            if (tableNumber == null) {
                model.addAttribute("errorMessage", "세션이 만료되었습니다. 다시 시도해주세요.");
                return "kiosk/toss_fail";
            }

            String customerPhone = (String) session.getAttribute("customerPhone");

            // 포인트 사용액: URL 파라미터 우선, 없으면 세션에서 가져오기
            int pointUsed = pointUsedParam;
            if (pointUsed == 0) {
                Integer sessionPointUsed = (Integer) session.getAttribute("pointUsed");
                if (sessionPointUsed != null) {
                    pointUsed = sessionPointUsed;
                }
            }

            log.info("토스 결제 승인 요청 - orderIdToss: {}, amount: {}, pointUsed: {}, table: {}",
                    orderIdToss, amount, pointUsed, tableNumber);

            // 결제 승인 (토스 API 호출 + DB 저장)
            PaymentDTO confirmResponse = paymentService.confirmPayment(
                    paymentKey, orderIdToss, amount, tableNumber, pointUsed, customerPhone);

            if (confirmResponse.isSuccess()) {
                model.addAttribute("orderId", confirmResponse.getOrderId());
                model.addAttribute("finalAmount", confirmResponse.getFinalAmount());
                model.addAttribute("earnedPoints", confirmResponse.getEarnedPoints());
                model.addAttribute("pointUsed", pointUsed);
                model.addAttribute("tableNumber", tableNumber);

                // 결제 완료 후 세션성 상태 데이터 정리
                session.removeAttribute("pointUsed");
                session.removeAttribute("partySize");
                session.removeAttribute("packageId");
                session.removeAttribute("selectedPackageId");
                session.removeAttribute("selectedPackageName");
                session.removeAttribute("selectedPackagePrice");
                session.removeAttribute("sessionStartTime");
                session.removeAttribute("durationMinutes");

                log.info("결제 성공 - orderIdToss: {}, finalAmount: {}", orderIdToss, confirmResponse.getFinalAmount());
                return "kiosk/toss_success";
            } else {
                model.addAttribute("errorMessage", confirmResponse.getMessage());
                log.warn("결제 승인 실패 - {}", confirmResponse.getMessage());
                return "kiosk/toss_fail";
            }
        } catch (Exception e) {
            log.error("결제 성공 처리 중 오류", e);
            model.addAttribute("errorMessage", "결제 처리 중 오류가 발생했습니다.");
            return "kiosk/toss_fail";
        }
    }

    /**
     * 결제 실패/취소 콜백
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
