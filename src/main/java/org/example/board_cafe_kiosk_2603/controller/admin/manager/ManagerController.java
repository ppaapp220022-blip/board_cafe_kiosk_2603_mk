package org.example.board_cafe_kiosk_2603.controller.admin.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.dto.admin.manager.ManagerRequest;
import org.example.board_cafe_kiosk_2603.dto.admin.manager.ManagerResponse;
import org.example.board_cafe_kiosk_2603.dto.common.pagenation.PageRequestDTO;
import org.example.board_cafe_kiosk_2603.dto.common.pagenation.PageResponseDTO;
import org.example.board_cafe_kiosk_2603.service.admin.manager.ManagerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Log4j2
@Controller
@RequestMapping("/admin/staff")
@RequiredArgsConstructor
public class ManagerController {

    private final ManagerService managerService;
    private static final int PAGE_SIZE = 8;

    // 직원 등록 (모달 폼 → Ajax)
    @PostMapping
    @ResponseBody
    public ResponseEntity<String> createStaff(@RequestBody ManagerRequest managerRequest) {
        log.info("--- 새 직원 등록 요청: {} ---", managerRequest);
        managerService.createManager(managerRequest);
        return ResponseEntity.ok("success");
    }

    // 업데이트 API 만들기
    // 서버에서 상태를 변경해줄 메서드
    @PostMapping("/toggle-status")
    @ResponseBody // JSON 또는 성공 메시지를 반환하기 위해 필요
    public ResponseEntity<String> toggleStaffStatus(@RequestParam("id") Integer id,
                                                    @RequestParam("active") Boolean isActive) {

        log.info("--- 직원 상태 변경 요청 시작 ---");
        log.info("요청 ID: {}, 변경할 상태: {}", id, isActive);

        try {
            // 서비스 계층을 통해 DB 업데이트
            managerService.updateActive(id, isActive);
            log.info("--- DB 업데이트 성공 ---");
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            log.error("--- DB 업데이트 실패: {} ---", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail");
        }
    }

    // 아이디 중복 확인
    @GetMapping("/check-id")
    @ResponseBody
    public ResponseEntity<Boolean> checkId(@RequestParam("loginId") String loginId) {
        // findByLoginId 결과가 있으면(present) 중복된 것이므로 false 반환 또는 존재 여부 반환
        boolean isDuplicate = managerService.isLoginIdDuplicate(loginId);
        log.info("--- 아이디 중복 체크 - ID: {}, 사용가능여부: {} ---", loginId, isDuplicate);
        return ResponseEntity.ok(isDuplicate);
    }

    // 내 정보 수정 페이지 이동
    // 실제 URL: localhost:8080/admin/staff/profile
    @GetMapping("/profile")
    public String profilePage(Model model, Principal principal) {

        if (principal == null) {
            log.error("--- 로그인 정보가 없습니다. ---");
            return "redirect:/login"; // 로그인 안 되어 있으면 리다이렉트
        }

        // 1. 현재 로그인한 ID 추출
        String loginId = principal.getName();

        // 2. 서비스 호출 (서비스에서 이미 ManagerResponse로 변환해서 줌)
        ManagerResponse manager = managerService.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("--- 사용자를 찾을 수 없습니다. ---"));

        // 3. 모델에 담기
        model.addAttribute("manager", manager);

        return "admin/staff_profile";
    }

    // 내 정보 수정 처리 (Ajax)
    @PostMapping("/profile/update")
    @ResponseBody
    public ResponseEntity<String> updateProfile(@RequestBody ManagerRequest request, Principal principal) {
        // 보안을 위해 세션의 ID와 요청 ID가 일치하는지 확인하거나, 세션 ID를 우선 사용
        managerService.updateProfile(principal.getName(), request);
        return ResponseEntity.ok("success");
    }

    /*================페이징============== */
    // 직원 목록 페이지
    @GetMapping
    public String staffPage(PageRequestDTO pageRequestDTO, Model model) {
        log.info("--- 직원 목록 페이지 진입 ---");

        PageResponseDTO<ManagerResponse> responseDTO = managerService.getPagedManagers(pageRequestDTO);

        // 각 탭별 개수 조회
        PageRequestDTO allReq      = PageRequestDTO.builder().page(1).size(1).build();
        PageRequestDTO activeReq   = PageRequestDTO.builder().page(1).size(1).filter("active").build();
        PageRequestDTO inactiveReq = PageRequestDTO.builder().page(1).size(1).filter("inactive").build();

        model.addAttribute("responseDTO", responseDTO);
        model.addAttribute("pageRequestDTO", pageRequestDTO);
        // filter가 null이면 "all"로 기본값 설정
        model.addAttribute("filter", pageRequestDTO.getFilter() != null ? pageRequestDTO.getFilter() : "all");
        model.addAttribute("countAll",      managerService.getCount(allReq));
        model.addAttribute("countActive",   managerService.getCount(activeReq));
        model.addAttribute("countInactive", managerService.getCount(inactiveReq));
        model.addAttribute("activePage", "staffManagement");

        return "admin/staff";  // templates/admin/staff.html
    }
}
