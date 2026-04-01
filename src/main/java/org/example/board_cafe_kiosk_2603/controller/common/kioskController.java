package org.example.board_cafe_kiosk_2603.controller.common;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.common.kioskItem;
import org.example.board_cafe_kiosk_2603.service.admin.product.GameService;
import org.example.board_cafe_kiosk_2603.service.admin.product.MenuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 키오스크 페이지 라우팅 컨트롤러.
 *
 * screensaver / headcount / phone_login → 진입 화면
 * drinks / food / games                → kiosk_layout.html + MenuService 더미 데이터
 *
 * 장바구니 → CartController
 * 정산     → PaymentController
 * 패키지   → CafePackageController
 */
@Log4j2
@Controller
@RequestMapping("/kiosk")
@RequiredArgsConstructor
public class kioskController {

    private final GameService gameService;
    private final MenuService menuService;

    // ===========================================================
    // 진입 화면
    // ===========================================================

    @GetMapping("/screensaver")
    public String screensaver(HttpSession session, Model model) {
        Integer tableNumber = (Integer) session.getAttribute("tableNumber");
        model.addAttribute("tableNumber", tableNumber != null ? tableNumber : 1);
        log.info("스크린세이버 접근 - 테이블: {}", tableNumber);
        return "kiosk/screensaver";
    }

    @GetMapping("/headcount")
    public String headcount(HttpSession session, Model model) {
        Integer tableNumber = (Integer) session.getAttribute("tableId");
        model.addAttribute("tableNumber", tableNumber);
        log.info("인원수 선택 화면 - 테이블: {}", tableNumber);
        return "kiosk/headcount";
    }

    @GetMapping("/phone_login")
    public String phoneLogin(HttpSession session, Model model) {
        Integer tableNumber = (Integer) session.getAttribute("tableId");
        Integer partySize = (Integer) session.getAttribute("partySize"); // 세션에서만 꺼냄
        model.addAttribute("tableNumber", tableNumber);
        model.addAttribute("partySize", partySize);
        log.info("전화번호 입력 화면 - 테이블: {}, 인원: {}", tableNumber, partySize);
        return "kiosk/phone_login";
    }

    // ===========================================================
    // [주연] -> 메뉴 화면 - 실제 DB 데이터
    // ===========================================================
    @GetMapping("/games")
    public String games(
            HttpSession session, Model model) {
        Integer tableNumber = (Integer) session.getAttribute("tableId");

        List<kioskItem> items = gameService.getByIsActive(true).stream()
                .map(g -> kioskItem.builder()
                        .name(g.getName())
                        .price(0)
                        .imageUrl(g.getImageUrl())
                        .stock(g.getGameItemCount())
                        .build())
                .toList();

        model.addAttribute("menuItems", items);
        model.addAttribute("currentMenu", "games");
        model.addAttribute("pageTitle", "게임");
        model.addAttribute("tableNumber", tableNumber);
        model.addAttribute("partySize", getPartySize(session));
        return "layout/kiosk_layout";
    }

    @GetMapping("/drinks")
    public String drinks(
            HttpSession session, Model model) {
        Integer tableNumber = (Integer) session.getAttribute("tableId");


        List<kioskItem> items = menuService.getByType("DRINK").stream()
                .filter(m -> m.isAvailable() && !m.isDeleted())
                .map(m -> kioskItem.builder()
                        .name(m.getName())
                        .price(m.getPrice())
                        .imageUrl(m.getImageUrl())
                        .stock(-1)
                        .build())
                .toList();

        model.addAttribute("menuItems", items);
        model.addAttribute("currentMenu", "drinks");
        model.addAttribute("pageTitle", "음료");
        model.addAttribute("tableNumber", tableNumber);
        model.addAttribute("partySize", getPartySize(session));
        return "layout/kiosk_layout";
    }

    @GetMapping("/food")
    public String food(
            HttpSession session, Model model) {
        Integer tableNumber = (Integer) session.getAttribute("tableId");


        List<kioskItem> items = menuService.getByType("FOOD").stream()
                .filter(m -> m.isAvailable() && !m.isDeleted())
                .map(m -> kioskItem.builder()
                        .name(m.getName())
                        .price(m.getPrice())
                        .imageUrl(m.getImageUrl())
                        .stock(-1)
                        .build())
                .toList();

        model.addAttribute("menuItems", items);
        model.addAttribute("currentMenu", "food");
        model.addAttribute("pageTitle", "음식");
        model.addAttribute("tableNumber", tableNumber);
        model.addAttribute("partySize", getPartySize(session));
        return "layout/kiosk_layout";
    }

    @GetMapping("/members")
    public String members(
            HttpSession session, Model model) {
        Integer tableNumber = (Integer) session.getAttribute("tableId");

        List<kioskItem> items = menuService.getByType("GUEST").stream()
                .filter(m -> m.isAvailable() && !m.isDeleted())
                .map(m -> kioskItem.builder()
                        .name(m.getName())
                        .price(m.getPrice())
                        .imageUrl(m.getImageUrl())
                        .stock(-1)
                        .build())
                .toList();

        model.addAttribute("menuItems", items);
        model.addAttribute("currentMenu", "members");
        model.addAttribute("pageTitle", "추가인원");
        model.addAttribute("tableNumber", tableNumber);
        model.addAttribute("partySize", getPartySize(session));
        return "layout/kiosk_layout";
    }
    // ===========================================================
    // [/주연]
    // ===========================================================

    // ===========================================================
    // 메뉴 화면 (kiosk_layout.html + MenuService 더미 데이터)
    // ===========================================================

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

    // ===========================================================
    // 헬퍼
    // ===========================================================


    private void buildMenuModel(Model model, int tableNumber, HttpSession session,
                                String menuType, List<Map<String, Object>> menuItems, String title) {
        Object cart = session.getAttribute("cart");
        int cartCount = cart instanceof List ? ((List<?>) cart).size() : 0;
        model.addAttribute("tableNumber", tableNumber);
        model.addAttribute("partySize",   getPartySize(session));
        model.addAttribute("currentMenu", menuType);
        model.addAttribute("menuItems",   menuItems);
        model.addAttribute("cartCount",   cartCount);
        model.addAttribute("pageTitle",   title);
    }

    private int getPartySize(HttpSession session) {
        Object val = session.getAttribute("partySize");
        return val instanceof Integer ? (Integer) val : 2;
    }
}
