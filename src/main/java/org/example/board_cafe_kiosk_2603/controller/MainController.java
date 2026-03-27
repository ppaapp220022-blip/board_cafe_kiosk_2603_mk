package org.example.board_cafe_kiosk_2603.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.common.TableSession;
import org.example.board_cafe_kiosk_2603.service.admin.TableSessionAdminService;
import org.example.board_cafe_kiosk_2603.service.kiosk.TableSessionKioskService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
@Controller
@RequiredArgsConstructor
public class MainController {
    private final TableSessionAdminService tableSessionAdminService;
    private final TableSessionKioskService tableSessionKioskService;

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
//        return "/admin/dashboard";
    }

    @GetMapping("/kiosk/login")
    public String kioskPage() {
        log.info("키오스크 -> 로그인 ...");
        return "/login/kiosk_login";
    }

    /* 키오스크 로그인 처리 - tableNumber 세션 저장 */
    @PostMapping("/kiosk/login")
    public String kioskLoginProcess(@RequestParam int tableNumber,
                                    @RequestParam String password,
                                    HttpSession session) {
        session.setAttribute("tableId", tableNumber);
        log.info("키오스크 로그인 처리... tableId: {}", tableNumber);
        return "redirect:/kiosk/screensaver";
    }

//    @GetMapping("/kiosk/screensaver")
//    public String kioskIdleScreen() {
//        log.info("키오스크 대기 화면 접근 ...");
//        return "kiosk/screensaver";
//    }

    @GetMapping("/kiosk/headcount")
    public String partySizePage() {
        log.info("키오스크 -> 인원수 선택 화면");
        return "kiosk/headcount";
    }

    /* 인원수 선택 처리 - partySize 세션 저장 */
    @PostMapping("/kiosk/headcount")
    public String headcountProcess(
            @RequestParam int partySize,
            HttpSession httpSession) {
        httpSession.setAttribute("partySize", partySize);
        log.info("인원수 세션 저장 완료 : {}명", partySize);
        return "redirect:/kiosk/phone_login";
    }


    @GetMapping("/kiosk/phone_login")
    public String phoneLoginPage() {
        log.info("키오스크 -> 휴대폰 번호 입력 화면");
        return "kiosk/phone_login";
    }

    /* 전화번호 POST 처리 - phone 세션 저장  */
    @PostMapping("/kiosk/phone_login")
    public String phoneLoginProcess(
            @RequestParam(required = false) String phone,
            HttpSession session) {
        if (phone != null && !phone.isEmpty()) {
            // 전화번호 입력한 경우 -> 세션에 저장
            session.setAttribute("phone", phone);
        } else {
            // 적립없이 계속 버튼 누른 경우 -> phone 없이 진행
            session.removeAttribute("phone");
            log.info("비회원으로 진행");
        }
        return "redirect:/kiosk/package_selection";
    }


    /* 패키지 선택 화면 추가 */
    @GetMapping("/kiosk/package_selection")
    public String packageSelectionPage() {
        log.info("키오스크 -> 패키지 선택 화면");
        return "kiosk/package_selection";
    }

    /* 패키지 선택 처리 - table_session DB 저장 */
    @PostMapping("/kiosk/package_selection")
    public String packageSelectionProcess(
            @RequestParam int packageId,
            HttpSession session) {
        int tableId = (int) session.getAttribute("tableId");
        int partySize = (int) session.getAttribute("partySize");

        // 이미 활성 세션이 있으면 새로 생성하지 않음
        TableSession activeSession = tableSessionAdminService.getActiveSession(tableId);
        if (activeSession != null) {
            session.setAttribute("partySize", activeSession.getInitialGuestCnt());
            log.info("기존 활성 세션 존재 - DB 인원수로 덮어씌움: {}명",
                    activeSession.getInitialGuestCnt());
            return "redirect:/kiosk/menu";
        }

        // 활성 세션 없으면 새로 생성
        tableSessionKioskService.createSession(tableId, packageId, partySize);
        log.info("table_session 생성 완료... tableId: {}, packageId: {}, partySize: {}",
                tableId, packageId, partySize);

        return "redirect:/kiosk/menu";
    }

    @GetMapping("/kiosk/menu")
    public String mainMenuPage(HttpSession session, Model model) {
        model.addAttribute("tableNumber", session.getAttribute("tableId")); // tableId → tableNumber로 변경
        model.addAttribute("partySize", session.getAttribute("partySize"));
        model.addAttribute("phone", session.getAttribute("phone"));
        log.info("키오스크 -> 메인 메뉴 화면 진입");
        return "layout/kiosk_layout";
    }

    @PostMapping("/admin/login-process") // 에러 메시지에 나온 그 경로입니다!
    public String adminLoginProcess() {
        log.info("관리자 로그인 처리 중... 대시보드로 이동합니다.");
        return "redirect:/admin/dashboard"; // 로그인 성공 시 대시보드 GetMapping으로 리다이렉트
    }

    // 관리자 메인 대시보드 (테이블 현황)
    @GetMapping("/admin/main-dashboard")
    public String adminDashboard(Model model) {
        // 1. 사이드바 활성화 태그 (admin_layout.html의 th:classappend와 일치해야 함)
        model.addAttribute("activePage", "tableStatus");

        // 2. 관리자 여부 (true여야 Admin 뱃지가 나타남)
        model.addAttribute("isAdmin", true);

        // 3. 화면에 뿌려줄 임시 데이터 (DB 연동 전까지 더미 데이터 활용)
        List<Map<String, Object>> dummyTables = new ArrayList<>();
        // 예시 데이터 추가 (이 구조가 HTML의 th:text="${table.status}" 등과 매핑됨)
        dummyTables.add(Map.of("tableNumber", 1, "status", "occupied", "guestCount", 2, "elapsedTime", "1h 10m"));
        dummyTables.add(Map.of("tableNumber", 2, "status", "waiting", "guestCount", 4, "elapsedTime", "0h 15m"));
        dummyTables.add(Map.of("tableNumber", 3, "status", "empty"));

        model.addAttribute("tableList", dummyTables);

        return "admin/main_dashbord"; // templates/admin/main_dashbord.html 호출
    }


}
