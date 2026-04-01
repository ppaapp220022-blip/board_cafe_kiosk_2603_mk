package org.example.board_cafe_kiosk_2603.controller.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.example.board_cafe_kiosk_2603.domain.common.cafeTableSession.CafeTableSession;
import org.example.board_cafe_kiosk_2603.service.admin.cafeTable.CafeTableService;
import org.example.board_cafe_kiosk_2603.service.admin.cafeTable.TableSessionAdminService;
import org.example.board_cafe_kiosk_2603.service.kiosk.tableSession.TableSessionKioskService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
@Controller
@RequiredArgsConstructor
public class MainController {
    private final TableSessionAdminService tableSessionAdminService;
    private final TableSessionKioskService tableSessionKioskService;
    private final CafeTableService cafeTableService;

    // ===========================================================
    // 공통 로그인 페이지 라우팅
    // ===========================================================

    @GetMapping("/")
    public String root() {
        return "redirect:/common/login";
    }

    @GetMapping("/common/login")
    public String loginPage() {
        log.info("초기 페이지 접근 ...");
        return "common/login";
    }

    @GetMapping("/admin/login")
    public String adminKiosk() {
        log.info("관리자 -> 로그인 ...");
        return "/login/admin_login";
    }

    @GetMapping("/kiosk/login")
    public String kioskPage(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        log.info("키오스크 -> 로그인 ...");

        HttpSession session = request.getSession(false);

        // [차단] 관리자 로그인 상태에서 키오스크 접근 차단
        if (session != null && session.getAttribute("SPRING_SECURITY_CONTEXT") != null) {
            log.warn("관리자 로그인 상태에서 키오스크 접근 차단");
            redirectAttributes.addFlashAttribute("error",
                    "관리자로 로그인된 상태입니다. 먼저 관리자 로그아웃 후 시도해주세요.");
            return "redirect:/common/login";
        }
        return "/login/kiosk_login";
    }

    // ===== ⛔️ 디버깅용 ⛔️ =====
    // MainController에 임시 추가 (테스트 전용)
    @GetMapping("/kiosk/force-logout")
    public String kioskForceLogout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            log.info("강제 로그아웃 - 테이블: {}", session.getAttribute("tableNumber"));
            session.invalidate();
        }
        return "redirect:/kiosk/login";
    }
    // ===== //⛔️ 디버깅용 ⛔️ =====

    @PostMapping("/logout")
    public String adminLogoutPage() {
        log.info("admin 로그인 -> 로그아웃 ...");
        return "redirect:/common/login";
    }

    // ===========================================================
    // 키오스크 POST 처리 흐름
    // screensaver → headcount → phone_login → package_selection → menu
    // ===========================================================

    /* [1단계] 키오스크 로그인 처리 - 테이블번호 + 비밀번호 검증 후 세션 저장 */
    /*
    1. 키오스크 로그인 상태 → 다른 테이블 로그인 차단
    2. 키오스크 로그인 상태 → 관리자 로그인 차단
    3. 관리자 로그인 상태 → 키오스크 로그인 차단
     */
    @PostMapping("/kiosk/login")
    public String kioskLoginProcess(@RequestParam int tableNumber,
                                    @RequestParam String password,
                                    HttpServletRequest request,
                                    RedirectAttributes redirectAttributes) {

        log.info("====== 키오스크 로그인 시도 - 테이블: {} ======", tableNumber);

        // 기존 세션 무효화 (관리자 로그인 세션 제거)
        HttpSession existingSession = request.getSession(false);

        // [차단 1] 이미 키오스크 로그인 상태
        if (existingSession != null && existingSession.getAttribute("tableNumber") != null) {
            int loggedInTable = (int) existingSession.getAttribute("tableNumber");
            log.warn("이미 {}번 테이블로 로그인된 상태 - {}번 테이블 로그인 차단", loggedInTable, tableNumber);
            redirectAttributes.addFlashAttribute("error",
                    loggedInTable + "번 테이블로 이미 로그인되어 있습니다. 먼저 로그아웃 해주세요.");
            return "redirect:/kiosk/login";
        }

        // [차단 2] 관리자 로그인 상태에서 키오스크 로그인 시도
        if (existingSession != null &&
                existingSession.getAttribute("SPRING_SECURITY_CONTEXT") != null) {
            log.warn("관리자 로그인 상태에서 키오스크 로그인 시도 차단");
            redirectAttributes.addFlashAttribute("error",
                    "관리자로 로그인된 상태입니다. 먼저 관리자 로그아웃 후 시도해주세요.");
            return "redirect:/kiosk/login";
        }

        // 기존 세션 무효화 후 새 세션 생성
        if (existingSession != null) {
            existingSession.invalidate();
        }

        return cafeTableService.login(tableNumber, password)
                .map(cafeTable -> {
                    HttpSession newSession = request.getSession(true);
                    newSession.setAttribute("tableNumber", cafeTable.getTableNumber());
                    newSession.setAttribute("tableId", cafeTable.getId());
                    newSession.setAttribute("cart", new ArrayList<>());
                    newSession.setMaxInactiveInterval(60 * 60 * 8);
                    log.info("키오스크 로그인 성공 - 테이블: {}", tableNumber);
                    return "redirect:/kiosk/screensaver";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "테이블 번호 또는 비밀번호가 올바르지 않습니다.");
                    return "redirect:/kiosk/login";
                });
    }

    /* [2단계] 인원수 선택 처리 - partySize 세션 저장 */
    @PostMapping("/kiosk/headcount")
    public String headcountProcess(@RequestParam int partySize,
                                   HttpSession session) {
        session.setAttribute("partySize", partySize);
        log.info("인원수 세션 저장 완료 : {}명", partySize);
        return "redirect:/kiosk/phone_login";
    }

    /* [3단계] 전화번호 처리 - customerPhone 세션 저장 */
    @PostMapping("/kiosk/phone_login")
    public String phoneLoginProcess(@RequestParam(required = false) String phone,
                                    HttpSession session) {
        if (phone != null && !phone.isEmpty()) {
            session.setAttribute("customerPhone", phone);
            log.info("회원 전화번호 세션 저장: {}", phone);
        } else {
            session.removeAttribute("customerPhone");
            log.info("비회원으로 진행");
        }
        return "redirect:/kiosk/package_selection";
    }

    /* [4단계] 패키지 선택 처리 - table_session DB 저장 */
    @PostMapping("/kiosk/package_selection")
    public String packageSelectionProcess(@RequestParam int packageId,
                                          HttpSession session) {
        // 세션 키 통일: "tableId" 사용 (cafe_table PK)
        int tableId = (int) session.getAttribute("tableId");
        int partySize = (int) session.getAttribute("partySize");

        // 이미 활성 세션이 있으면 새로 생성하지 않음
        CafeTableSession activeSession = tableSessionAdminService.getActiveSession(tableId);
        if (activeSession != null) {
            session.setAttribute("partySize", activeSession.getInitialGuestCnt());
            log.info("기존 활성 세션 존재 - 기존 인원수 유지: {}명", activeSession.getInitialGuestCnt());
            return "redirect:/kiosk/menu";
        }

        // 활성 세션 없으면 새로 생성
        tableSessionKioskService.createSession(tableId, packageId, partySize);
        log.info("table_session 생성 완료 - tableId: {}, packageId: {}, partySize: {}",
                tableId, packageId, partySize);

        return "redirect:/kiosk/menu";
    }

    /* [5단계] 메인 메뉴 페이지 */
    @GetMapping("/kiosk/menu")
    public String mainMenuPage(HttpSession session, Model model) {
        model.addAttribute("tableNumber", session.getAttribute("tableNumber")); // 세션 키 통일
        model.addAttribute("partySize", session.getAttribute("partySize"));
        model.addAttribute("customerPhone", session.getAttribute("customerPhone"));
        log.info("키오스크 -> 메인 메뉴 화면 진입");
        return "layout/kiosk_layout";
    }

    // ===========================================================
    // 관리자
    // ===========================================================

    @PostMapping("/admin/login-process")
    public String adminLoginProcess() {
        log.info("관리자 로그인 처리 중... 대시보드로 이동합니다.");
        return "redirect:/admin/dashboard";
    }
}
