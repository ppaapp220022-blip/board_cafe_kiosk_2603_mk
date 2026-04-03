package org.example.board_cafe_kiosk_2603.controller.admin.policy;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.dto.admin.policy.PolicyDTO;
import org.example.board_cafe_kiosk_2603.service.admin.policy.PolicyService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@Log4j2
@Controller
@RequestMapping("/admin/policy")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;
    private static final int pageSize = 8;

    // ===========================================================
    // 페이지
    // ===========================================================
    @GetMapping
    public String policyPage(@RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "all") String filter,
                             Model model) {
        int totalCount = policyService.countAll(filter);
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        // 페이지 범위 보정
        if (page < 1) page = 1;
        if (totalPages > 0 && page > totalPages) page = totalPages;

        model.addAttribute("policyList",  policyService.getAllPolicies(page, pageSize, filter));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages",  totalPages);
        model.addAttribute("totalCount",  totalCount);
        model.addAttribute("filter",      filter);

        log.info("패키지 관리 페이지 - filter: {}, page: {}/{}, 총 {}개",
                filter, page, totalPages, totalCount);
        return "admin/package";
    }

    // 패키지 등록
    @PostMapping("/insert")
    @ResponseBody
    public ResponseEntity<PolicyDTO> insert(@RequestBody PolicyDTO dto) {
        try {
            policyService.insert(dto);
            log.info("패키지 등록 완료 - name: {}", dto.getName());
            return ResponseEntity.ok(PolicyDTO.success("패키지가 등록되었습니다."));
        } catch (Exception e) {
            log.error("패키지 등록 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(PolicyDTO.fail("패키지 등록에 실패했습니다."));
        }
    }

    // 활성/비활성 토글
    @PostMapping("/status")
    @ResponseBody
    public ResponseEntity<PolicyDTO> updateStatus(@RequestBody Map<String, Object> req) {
        try {
            int id       = ((Number) req.get("id")).intValue();
            boolean active = (Boolean) req.get("active");
            policyService.updateStatus(id, active);
            log.info("패키지 상태 변경 - id: {}, active: {}", id, active);
            return ResponseEntity.ok(PolicyDTO.success(active ? "활성화되었습니다." : "비활성화되었습니다."));
        } catch (Exception e) {
            log.error("패키지 상태 변경 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(PolicyDTO.fail("상태 변경에 실패했습니다."));
        }
    }
}
