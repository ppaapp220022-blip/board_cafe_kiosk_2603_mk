//package org.example.board_cafe_kiosk_2603.controller.common;
//
//import jakarta.servlet.http.HttpSession;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.log4j.Log4j2;
//import org.example.board_cafe_kiosk_2603.service.admin.product.MenuService;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.*;
//
///**
// * 키오스크 페이지 라우팅 컨트롤러.
// *
// * screensaver / headcount / phone_login → 진입 화면
// * drinks / food / games                → kiosk_layout.html + MenuService 더미 데이터
// *
// * 장바구니 → CartController
// * 정산     → PaymentController
// * 패키지   → CafePackageController
// */
//@Log4j2
//@Controller
//@RequestMapping("/kiosk")
//@RequiredArgsConstructor
//public class kioskController {
//
//    private final MenuService menuService;
//
//    // ===========================================================
//    // 진입 화면
//    // ===========================================================
//
//    @GetMapping("/screensaver")
//    public String screensaver(HttpSession session, Model model) {
//        Integer tableNumber = (Integer) session.getAttribute("tableNumber");
//        model.addAttribute("tableNumber", tableNumber != null ? tableNumber : 1);
//        log.info("스크린세이버 접근 - 테이블: {}", tableNumber);
//        return "kiosk/screensaver";
//    }
//
//    @GetMapping("/headcount")
//    public String headcount(
//            @RequestParam(required = false, defaultValue = "1") Integer tableNumber,
//            Model model) {
//        model.addAttribute("tableNumber", tableNumber);
//        log.info("인원수 선택 화면 - 테이블: {}", tableNumber);
//        return "kiosk/headcount";
//    }
//
//    @GetMapping("/phone_login")
//    public String phoneLogin(
//            @RequestParam(required = false, defaultValue = "1") Integer tableNumber,
//            @RequestParam(required = false, defaultValue = "1") Integer size,
//            HttpSession session, Model model) {
//        session.setAttribute("partySize",   size);
//        session.setAttribute("tableNumber", tableNumber);
//        model.addAttribute("tableNumber", tableNumber);
//        model.addAttribute("partySize",   size);
//        log.info("전화번호 입력 화면 - 테이블: {}, 인원: {}", tableNumber, size);
//        return "kiosk/phone_login";
//    }
//
//    // ===========================================================
//    // 메뉴 화면 (kiosk_layout.html + MenuService 더미 데이터)
//    // ===========================================================
//
//    @GetMapping("/drinks")
//    public String drinks(
//            @RequestParam(required = false, defaultValue = "1") Integer tableNumber,
//            HttpSession session, Model model) {
//        initSession(session, tableNumber);
//        buildMenuModel(model, tableNumber, session, "drinks", menuService.getByType("DRINK"));
//        return "layout/kiosk_layout";
//    }
//
//    @GetMapping("/food")
//    public String food(
//            @RequestParam(required = false, defaultValue = "1") Integer tableNumber,
//            HttpSession session, Model model) {
//        initSession(session, tableNumber);
//        buildMenuModel(model, tableNumber, session, "food", menuService.getByType("DRINK"), "음식");
//        return "layout/kiosk_layout";
//    }
//
//    @GetMapping("/games")
//    public String games(
//            @RequestParam(required = false, defaultValue = "1") Integer tableNumber,
//            HttpSession session, Model model) {
//        initSession(session, tableNumber);
//        buildMenuModel(model, tableNumber, session, "games", menuService.getGameItems(), "게임");
//        return "layout/kiosk_layout";
//    }
//
//    // ===========================================================
//    // 헬퍼
//    // ===========================================================
//
//    private void initSession(HttpSession session, Integer tableNumber) {
//        if (session.getAttribute("tableNumber") == null) {
//            session.setAttribute("tableNumber",      tableNumber);
//            session.setAttribute("partySize",        2);
//            session.setAttribute("sessionStartTime", System.currentTimeMillis());
//        }
//        if (session.getAttribute("cart") == null) {
//            session.setAttribute("cart", new ArrayList<>());
//        }
//    }
//
//    private void buildMenuModel(Model model, int tableNumber, HttpSession session,
//                                String menuType, List<Map<String, Object>> menuItems, String title) {
//        Object cart = session.getAttribute("cart");
//        int cartCount = cart instanceof List ? ((List<?>) cart).size() : 0;
//        model.addAttribute("tableNumber", tableNumber);
//        model.addAttribute("partySize",   getPartySize(session));
//        model.addAttribute("currentMenu", menuType);
//        model.addAttribute("menuItems",   menuItems);
//        model.addAttribute("cartCount",   cartCount);
//        model.addAttribute("pageTitle",   title);
//    }
//
//    private int getPartySize(HttpSession session) {
//        Object val = session.getAttribute("partySize");
//        return val instanceof Integer ? (Integer) val : 2;
//    }
//}
