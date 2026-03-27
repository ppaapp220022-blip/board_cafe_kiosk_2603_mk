package org.example.board_cafe_kiosk_2603.controller.kiosk;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.dto.kiosk.TossPaymentDTO;
import org.example.board_cafe_kiosk_2603.service.kiosk.KioskPageService;
import org.example.board_cafe_kiosk_2603.service.kiosk.TossPaymentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * 토스페이먼츠 연동 컨트롤러.
 * - POST /kiosk/toss/prepare  → TossPaymentDTO (REST)
 * - GET  /kiosk/toss/success  → 뷰 반환
 * - GET  /kiosk/toss/fail     → 뷰 반환
 */
@Log4j2
@Controller
@RequestMapping("/kiosk/toss")
@RequiredArgsConstructor
public class TossPaymentController {

    private final TossPaymentService tossPaymentService;
    private final KioskPageService   kioskPageService;

    @PostMapping("/prepare")
    @ResponseBody
    public TossPaymentDTO tossPrepare(
            @RequestBody @Valid TossPaymentDTO request,
            @RequestParam(required = false, defaultValue = "1") Integer tableNumber,
            HttpSession session) {

        tableNumber = kioskPageService.resolveTableNumber(tableNumber, session);
        TossPaymentDTO response = tossPaymentService.preparePayment(tableNumber, request);

        if (response.isSuccess()) {
            session.setAttribute("toss_orderIdToss", response.getOrderIdToss());
            session.setAttribute("toss_amount",      response.getAmount());
            session.setAttribute("toss_pointUsed",   response.getPointUsed());
        }
        return response;
    }

    @GetMapping("/success")
    public String tossSuccess(
            @RequestParam String  paymentKey,
            @RequestParam String  orderId,
            @RequestParam int     amount,
            @RequestParam(required = false, defaultValue = "1") Integer tableNumber,
            HttpSession session, Model model) {

        tableNumber = kioskPageService.resolveTableNumber(tableNumber, session);

        Integer savedAmount    = (Integer) session.getAttribute("toss_amount");
        Integer savedPointUsed = (Integer) session.getAttribute("toss_pointUsed");
        int     pointUsed      = savedPointUsed != null ? savedPointUsed : 0;

        if (savedAmount != null && savedAmount != amount) {
            log.warn("결제 금액 불일치 - 세션: {}원, 토스: {}원", savedAmount, amount);
            model.addAttribute("errorMessage", "결제 금액이 일치하지 않습니다.");
            model.addAttribute("tableNumber",  tableNumber);
            return "kiosk/toss_fail";
        }

        String customerPhone = (String) session.getAttribute("customerPhone");
        TossPaymentDTO response = tossPaymentService.confirmPayment(
                paymentKey, orderId, amount, tableNumber, pointUsed, customerPhone);

        if (response.isSuccess()) {
            clearTossSession(session);
            model.addAttribute("orderId",      response.getOrderId());
            model.addAttribute("finalAmount",  response.getFinalAmount());
            model.addAttribute("earnedPoints", response.getEarnedPoints());
            model.addAttribute("tableNumber",  tableNumber);
            return "kiosk/toss_success";
        }

        model.addAttribute("errorMessage", response.getMessage());
        model.addAttribute("tableNumber",  tableNumber);
        return "kiosk/toss_fail";
    }

    @GetMapping("/fail")
    public String tossFail(
            @RequestParam(required = false) String  code,
            @RequestParam(required = false) String  message,
            @RequestParam(required = false, defaultValue = "1") Integer tableNumber,
            HttpSession session, Model model) {

        tableNumber = kioskPageService.resolveTableNumber(tableNumber, session);
        log.warn("토스 결제 실패 - code: {}, message: {}", code, message);
        model.addAttribute("errorMessage", message != null ? message : "결제가 취소되었습니다.");
        model.addAttribute("tableNumber",  tableNumber);
        return "kiosk/toss_fail";
    }

    private void clearTossSession(HttpSession session) {
        session.removeAttribute("customerPhone");
        session.removeAttribute("toss_orderIdToss");
        session.removeAttribute("toss_amount");
        session.removeAttribute("toss_pointUsed");
    }
}
