package org.example.board_cafe_kiosk_2603.controller.common;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.common.kioskItem;
import org.example.board_cafe_kiosk_2603.service.admin.cafeTable.TableSessionAdminService;
import org.example.board_cafe_kiosk_2603.service.admin.product.GameService;
import org.example.board_cafe_kiosk_2603.service.admin.product.MenuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 키오스크 통합 컨트롤러
 * <p>
 * [로그인]
 * GET  /kiosk/login            → 로그인 페이지        (MainController 담당)
 * POST /kiosk/login-process    → Spring Security 인증  (SecurityConfig 담당)
 * GET  /kiosk/logout           → 로그아웃             (SecurityConfig 담당)
 * <p>
 * [진입 화면]
 * GET  /kiosk/screensaver      → 스크린세이버
 * GET  /kiosk/headcount        → 인원수 선택
 * GET  /kiosk/phone_login      → 전화번호 입력
 * <p>
 * [메뉴 진입]
 * GET  /kiosk/menu             → 메뉴 메인            (MainController 담당)
 * <p>
 * [메뉴 탭]
 * GET  /kiosk/games            → 게임 목록
 * GET  /kiosk/drinks           → 음료 목록
 * GET  /kiosk/food             → 음식 목록
 * GET  /kiosk/members          → 추가인원 목록
 *
 * ★ 역할 분리 원칙
 *   - POST 흐름(headcount, phone_login, package_selection) → MainController
 *   - /kiosk/menu GET + 세션 검증                          → MainController
 *   - 메뉴 탭 GET (DB 조회 + 렌더링)                       → kioskController (이 파일)
 */
@Log4j2
@Controller
@RequestMapping("/kiosk")
@RequiredArgsConstructor
public class kioskController {

    private final GameService gameService;
    private final MenuService menuService;
    private final TableSessionAdminService tableSessionAdminService;

    // ===========================================================
    // 진입 화면
    // ===========================================================

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

    // ★ 수정: /session/start → kiosk/headcount 반환
    //    기존에 "kiosk/screensaver"를 반환하고 있었으나
    //    이 엔드포인트의 목적은 인원수 입력 화면 진입이므로 수정
    @GetMapping("/session/start")
    public String sessionStart(HttpSession session, Model model) {
        log.info("--- [KioskController] 인원수 입력 화면 진입 | tableNumber: {} ---",
                session.getAttribute("tableNumber"));
        model.addAttribute("tableNumber", session.getAttribute("tableNumber"));
        return "kiosk/headcount";
    }

    // ===========================================================
    // 메뉴 탭 화면 — DB 조회 및 렌더링 담당
    //
    // ★ 제거: @GetMapping("/menu")
    //    MainController에 이미 /kiosk/menu 가 존재하므로
    //    kioskController에 추가하면 Ambiguous mapping 에러 발생
    //    /kiosk/menu 진입·세션 검증은 MainController.mainMenuPage() 담당
    // ===========================================================

    // 게임 목록 조회
    @GetMapping("/games")
    public String games(HttpSession session, Model model) {
        String guardRedirect = guardActiveSession(session);
        if (guardRedirect != null) return guardRedirect;

        initCart(session);

        List<kioskItem> items = gameService.getByIsActive(true).stream()
                .map(g -> kioskItem.builder()
                        .name(g.getName())
                        .price(0)
                        .imageUrl(g.getImageUrl())
                        .stock(g.getGameItemCount())
                        .build())
                .toList();

        log.info("--- [메뉴 -> 게임] 조회 완료 - 데이터 개수: {}개 ---", items.size());
        buildMenuModel(model, session, "games", "게임", items);
        return "layout/kiosk_layout";
    }

    // 음료 목록 조회
    @GetMapping("/drinks")
    public String drinks(HttpSession session, Model model) {
        String guardRedirect = guardActiveSession(session);
        if (guardRedirect != null) return guardRedirect;

        initCart(session);

        List<kioskItem> items = menuService.getByType("DRINK").stream()
                .filter(m -> m.isAvailable() && !m.isDeleted())
                .map(m -> {
                    log.info("변환 중인 메뉴: {}", m.getName());
                    return kioskItem.builder()
                            .name(m.getName())
                            .price(m.getPrice())
                            .imageUrl(m.getImageUrl())
                            .stock(-1)
                            .build();
                })
                .toList();

        log.info("--- [메뉴 -> 음료] 조회 완료 - 데이터 개수: {}개 ---", items.size());
        buildMenuModel(model, session, "drinks", "음료", items);
        return "layout/kiosk_layout";
    }

    // 음식 목록 조회
    @GetMapping("/food")
    public String food(HttpSession session, Model model) {
        String guardRedirect = guardActiveSession(session);
        if (guardRedirect != null) return guardRedirect;

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

        log.info("--- [메뉴 -> 음식] 조회 완료 - 데이터 개수: {}개 ---", items.size());
        buildMenuModel(model, session, "food", "음식", items);
        return "layout/kiosk_layout";
    }

    // 추가인원 목록 조회
    @GetMapping("/members")
    public String members(HttpSession session, Model model) {
        String guardRedirect = guardActiveSession(session);
        if (guardRedirect != null) return guardRedirect;

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

        log.info("--- [메뉴 -> 추가인원] 조회 완료 - 데이터 개수: {}개 ---", items.size());
        buildMenuModel(model, session, "members", "추가인원", items);
        return "layout/kiosk_layout";
    }

    // ===========================================================
    // 공통 유틸 메서드
    // ===========================================================

    /**
     * 화면에 필요한 공통 데이터를 Model에 담는다.
     * ★ 추가: packageId 포함 — 패키지 정보 표시 및 타이머 계산에 필요
     */
    private void buildMenuModel(Model model, HttpSession session,
                                String menuType, String title, List<kioskItem> items) {
        log.info("--- buildMenuModel | menuType: {} ---", menuType);

        Object cart = session.getAttribute("cart");
        int cartCount = cart instanceof List ? ((List<?>) cart).size() : 0;

        log.debug("세션 정보 — 시작시간: {}, 인원수: {}, 패키지: {}",
                session.getAttribute("sessionStartTime"),
                getPartySize(session),
                session.getAttribute("packageId"));

        model.addAttribute("tableNumber",      session.getAttribute("tableNumber"));
        model.addAttribute("partySize",        getPartySize(session));
        model.addAttribute("packageId",        session.getAttribute("packageId"));        // ★ 추가
        model.addAttribute("currentMenu",      menuType);
        model.addAttribute("pageTitle",        title);
        model.addAttribute("menuItems",        items);
        model.addAttribute("cartCount",        cartCount);
        model.addAttribute("sessionStartTime", session.getAttribute("sessionStartTime"));
        model.addAttribute("durationMinutes",  session.getAttribute("durationMinutes"));
    }

    /**
     * partySize 반환
     * ★ 수정: 세션에 값이 없을 때 IllegalStateException throw → 0 반환으로 변경
     *         throw 방식은 /games, /drinks, /food, /members 전부 500 에러 유발
     *         0 반환 시 뷰에서 "인원수 미선택" 상태로 안전하게 표시 가능
     *         /kiosk/menu 진입 시점에 MainController에서 이미 세션 검증을 하므로
     *         정상 흐름에서는 이 분기에 도달하지 않음
     */
    private int getPartySize(HttpSession session) {
        Object val = session.getAttribute("partySize");
        if (!(val instanceof Integer)) {
            log.warn("--- [KioskController] partySize 세션 없음 — 0 반환 ---");
            return 0;
        }
        return (Integer) val;
    }

    /**
     * 장바구니가 세션에 없으면 빈 리스트로 초기화 (null 방지)
     */
    private void initCart(HttpSession session) {
        if (session.getAttribute("cart") == null) {
            log.info("--- HttpSession, 새로운 장바구니(Cart) 생성 ---");
            session.setAttribute("cart", new ArrayList<>());
        }
    }

    private String guardActiveSession(HttpSession session) {
        Object tableIdObj = session.getAttribute("tableId");
        if (!(tableIdObj instanceof Integer tableId)) {
            log.warn("--- [KioskController] 메뉴 탭 접근 차단: tableId 세션 없음 ---");
            return "redirect:/kiosk/session/start";
        }

        if (tableSessionAdminService.getActiveSession(tableId) == null) {
            log.warn("--- [KioskController] 메뉴 탭 접근 차단: 활성 세션 없음 (tableId: {}) ---", tableId);
            session.removeAttribute("partySize");
            session.removeAttribute("packageId");
            session.removeAttribute("sessionStartTime");
            session.removeAttribute("durationMinutes");
            return "redirect:/kiosk/session/start";
        }

        return null;
    }
}
