package org.example.board_cafe_kiosk_2603.controller.common;

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
    public String kioskPage() {
        log.info("키오스크 -> 로그인 ...");
        return "/login/kiosk_login";
    }

    // ===========================================================
    // 키오스크 POST 처리 흐름
    // screensaver → headcount → phone_login → package_selection → menu
    // ===========================================================

    /* [1단계] 키오스크 로그인 처리 - 테이블번호 + 비밀번호 검증 후 세션 저장 */
    @PostMapping("/kiosk/login")
    public String kioskLoginProcess(@RequestParam int tableNumber,
                                    @RequestParam String password,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {

        log.info("키오스크 로그인 처리... tableNumber: {}", tableNumber);

        return cafeTableService.login(tableNumber, password)
                .map(cafeTable -> {
                    session.setAttribute("tableNumber", cafeTable.getTableNumber());
                    session.setAttribute("tableId", cafeTable.getId());
                    session.setAttribute("cart", new ArrayList<>());
                    session.setMaxInactiveInterval(60 * 60 * 8);
                    log.info("키오스크 로그인 성공 - 테이블: {}", tableNumber);
                    return "redirect:/kiosk/screensaver";
                })
                .orElseGet(() -> {
                    log.warn("키오스크 로그인 실패 - 테이블: {}", tableNumber);
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
