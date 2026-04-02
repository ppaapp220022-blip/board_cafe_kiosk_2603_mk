package org.example.board_cafe_kiosk_2603.controller.common;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.common.kioskItem;
import org.example.board_cafe_kiosk_2603.service.admin.cafeTable.CafeTableService;
import org.example.board_cafe_kiosk_2603.service.admin.product.GameService;
import org.example.board_cafe_kiosk_2603.service.admin.product.MenuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 키오스크 통합 컨트롤러
 *
 * [로그인]
 *   GET  /kiosk/login            → 로그인 페이지
 *   POST /kiosk/login-process    → 로그인 처리 (테이블번호 + 비밀번호)
 *   GET  /kiosk/logout           → 로그아웃
 *
 * [진입 화면]
 *   GET /kiosk/screensaver       → 스크린세이버
 *   GET /kiosk/headcount         → 인원수 선택
 *   GET /kiosk/phone_login       → 전화번호 입력
 *
 * [메뉴 화면]
 *   GET /kiosk/games             → 게임 목록
 *   GET /kiosk/drinks            → 음료 목록
 *   GET /kiosk/food              → 음식 목록
 *   GET /kiosk/members           → 추가인원 목록
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

    // 진입 화면 GET만 담당
    @GetMapping("/screensaver")
    public String screensaver(HttpSession session, Model model) {
        model.addAttribute("tableNumber", session.getAttribute("tableNumber"));
        log.info("스크린세이버 접근 - 테이블: {}", session.getAttribute("tableNumber"));
        return "kiosk/screensaver";
    }

    @GetMapping("/headcount")
    public String headcount(HttpSession session, Model model) {
        model.addAttribute("tableNumber", session.getAttribute("tableNumber"));
        log.info("인원수 선택 화면 - 테이블: {}", session.getAttribute("tableNumber"));
        return "kiosk/headcount";
    }

    @GetMapping("/phone_login")
    public String phoneLogin(HttpSession session, Model model) {
        model.addAttribute("tableNumber", session.getAttribute("tableNumber"));
        model.addAttribute("partySize", session.getAttribute("partySize"));
        log.info("전화번호 입력 화면 - 테이블: {}", session.getAttribute("tableNumber"));
        return "kiosk/phone_login";
    }

    // ===========================================================
    // [주연] -> 메뉴 화면 - 실제 DB 데이터
    // ===========================================================
    @GetMapping("/games")
    public String games(HttpSession session, Model model) {
        initCart(session);
        List<kioskItem> items = gameService.getByIsActive(true).stream()
                .map(g -> kioskItem.builder()
                        .name(g.getName())
                        .price(0)
                        .imageUrl(g.getImageUrl())
                        .stock(g.getGameItemCount())
                        .build())
                .toList();
        buildMenuModel(model, session, "games", "게임", items);
        return "layout/kiosk_layout";
    }

    @GetMapping("/drinks")
    public String drinks(HttpSession session, Model model) {
        initCart(session);
        List<kioskItem> items = menuService.getByType("DRINK").stream()
                .filter(m -> m.isAvailable() && !m.isDeleted())
                .map(m -> kioskItem.builder()
                        .name(m.getName())
                        .price(m.getPrice())
                        .imageUrl(m.getImageUrl())
                        .stock(-1)
                        .build())
                .toList();
        buildMenuModel(model, session, "drinks", "음료", items);
        return "layout/kiosk_layout";
    }

    @GetMapping("/food")
    public String food(HttpSession session, Model model) {
        initCart(session);
        List<kioskItem> items = menuService.getByType("FOOD").stream()
                .filter(m -> m.isAvailable() && !m.isDeleted())
                .map(m -> kioskItem.builder()
                        .name(m.getName())
                        .price(m.getPrice())
                        .imageUrl(m.getImageUrl())
                        .stock(-1)
                        .build())
                .toList();
        buildMenuModel(model, session, "food", "음식", items);
        return "layout/kiosk_layout";
    }

    @GetMapping("/members")
    public String members(HttpSession session, Model model) {
        initCart(session);
        List<kioskItem> items = menuService.getByType("GUEST").stream()
                .filter(m -> m.isAvailable() && !m.isDeleted())
                .map(m -> kioskItem.builder()
                        .name(m.getName())
                        .price(m.getPrice())
                        .imageUrl(m.getImageUrl())
                        .stock(-1)
                        .build())
                .toList();
        buildMenuModel(model, session, "members", "추가인원", items);
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

    // 기존 - 파라미터로 받아서 세션에 억지로 넣음
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
    // 수정 - 세션에 이미 로그인 정보가 있으므로 cart만 초기화 (✅)
//    private void initSession(HttpSession session) {
//        if (session.getAttribute("cart") == null) {
//            session.setAttribute("cart", new ArrayList<>());
//        }
//    }

    private void buildMenuModel(Model model, HttpSession session,
                                String menuType, String title, List<kioskItem> items) {
        Object cart = session.getAttribute("cart");
        int cartCount = cart instanceof List ? ((List<?>) cart).size() : 0;
        model.addAttribute("tableNumber", session.getAttribute("tableNumber"));
        model.addAttribute("partySize", getPartySize(session));
        model.addAttribute("currentMenu", menuType);
        model.addAttribute("pageTitle", title);
        model.addAttribute("menuItems", items);
        model.addAttribute("cartCount", cartCount);
        model.addAttribute("sessionStartTime", session.getAttribute("sessionStartTime"));
        model.addAttribute("durationMinutes", session.getAttribute("durationMinutes"));
    }


    private int getPartySize(HttpSession session) {
        Object val = session.getAttribute("partySize");
        return val instanceof Integer ? (Integer) val : 2;
    }

    private void initCart(HttpSession session) {
        if (session.getAttribute("cart") == null) {
            session.setAttribute("cart", new ArrayList<>());
        }
    }
}
