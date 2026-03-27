package org.example.board_cafe_kiosk_2603.controller.kiosk;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.service.kiosk.CafePackageService;
import org.example.board_cafe_kiosk_2603.service.kiosk.KioskPageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * 키오스크 화면 페이지 라우팅 컨트롤러.
 * 순수하게 뷰 이름만 반환하며, Model 구성은 KioskPageService 에 위임합니다.
 *
 * URL: /kiosk/*  (페이지)
 */
@Log4j2
@Controller
@RequestMapping("/kiosk")
@RequiredArgsConstructor
public class KioskPageController {

    private final KioskPageService   kioskPageService;
    private final CafePackageService cafePackageService;

    @GetMapping("/headcount")
    public String headcount(
            @RequestParam(required = false, defaultValue = "1") Integer tableNumber,
            Model model) {
        kioskPageService.buildHeadcountModel(model, tableNumber);
        return "kiosk/headcount";
    }

    @GetMapping("/phone_login")
    public String phoneLogin(
            @RequestParam(required = false, defaultValue = "1") Integer tableNumber,
            @RequestParam(required = false, defaultValue = "1") Integer size,
            HttpSession session, Model model) {
        kioskPageService.buildPhoneLoginModel(model, tableNumber, size, session);
        return "kiosk/phone_login";
    }

    @GetMapping("/package_selection")
    public String packageSelection(
            @RequestParam(required = false, defaultValue = "1") Integer tableNumber,
            @RequestParam(required = false, defaultValue = "1") Integer size,
            HttpSession session, Model model) {
        kioskPageService.buildPackageSelectionModel(
                model, tableNumber, size, session, cafePackageService.getActivePackages());
        return "kiosk/package_selection";
    }

    @GetMapping("/drinks")
    public String drinks(
            @RequestParam(required = false, defaultValue = "5") Integer tableNumber,
            HttpSession session, Model model) {
        kioskPageService.initSessionIfNeeded(session, tableNumber);
        kioskPageService.buildMenuModel(
                model, tableNumber, session, "drinks", kioskPageService.getDrinkItems(), "음료");
        return "layout/kiosk_layout";
    }

    @GetMapping("/food")
    public String food(
            @RequestParam(required = false, defaultValue = "5") Integer tableNumber,
            HttpSession session, Model model) {
        kioskPageService.initSessionIfNeeded(session, tableNumber);
        kioskPageService.buildMenuModel(
                model, tableNumber, session, "food", kioskPageService.getFoodItems(), "음식");
        return "layout/kiosk_layout";
    }

    @GetMapping("/games")
    public String games(
            @RequestParam(required = false, defaultValue = "5") Integer tableNumber,
            HttpSession session, Model model) {
        kioskPageService.initSessionIfNeeded(session, tableNumber);
        kioskPageService.buildMenuModel(
                model, tableNumber, session, "games", kioskPageService.getGameItems(), "게임");
        return "layout/kiosk_layout";
    }

    @GetMapping("/cart")
    public String cart(
            @RequestParam(required = false, defaultValue = "1") Integer tableNumber,
            HttpSession session, Model model) {
        tableNumber = kioskPageService.resolveTableNumber(tableNumber, session);
        kioskPageService.buildCartModel(model, tableNumber, session);
        return "kiosk/cart";
    }

    @GetMapping("/checkout")
    public String checkout(
            @RequestParam(required = false, defaultValue = "1") Integer tableNumber,
            HttpSession session, Model model) {
        tableNumber = kioskPageService.resolveTableNumber(tableNumber, session);
        kioskPageService.buildCheckoutModel(model, tableNumber, session);
        return "kiosk/checkout";
    }

    @GetMapping("/screensaver")
    public String screensaver(HttpSession session, Model model) {
        kioskPageService.buildScreensaverModel(model, session);
        return "kiosk/screensaver";
    }
}
