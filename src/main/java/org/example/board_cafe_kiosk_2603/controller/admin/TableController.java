package org.example.board_cafe_kiosk_2603.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.dto.admin.CafeTableDTO;
import org.example.board_cafe_kiosk_2603.service.admin.cafeTable.CafeTableService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Log4j2
@Controller
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class TableController {
    private final CafeTableService cafeTableService;

    /**
     * [GET] 대시보드 메인 페이지
     * 주 설명: 현재 20개 테이블의 실시간 상태(이용중/공석/청소중)와 입실 시간을 조회하여 뷰에 전달
     */
    @GetMapping
    public String dashboard(Model model) {
        List<CafeTableDTO> tables = cafeTableService.getAllTableStatus();
        model.addAttribute("tables", tables);

        log.info("대시보드 조회: 총 {}개의 테이블 상태 로드 완료", tables.size());
        return "admin/dashboard";
    }

    /**
     * [PATCH] 테이블 상태 변경 및 세션 연동 (입실/퇴실/청소)
     * @param id 테이블 PK
     * @param request {"status": "OCCUPIED" | "CLEANING" | "EMPTY"}
     * 상세 설명: 상태가 'OCCUPIED'가 되면 세션이 생성되고, 'CLEANING'이 되면 세션이 마감됩니다.
     */
    @ResponseBody
    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, String>> updateStatus(
            @PathVariable("id") Integer id,
            @RequestBody Map<String, String> request) {

        String status = request.get("status");

        try {
            // 핵심 로직 실행 (세션 생성/종료 및 테이블 포인터 업데이트)
            cafeTableService.changeTableStatus(id, status);

            log.info("상태 변경 성공: 테이블 {}번 -> {}", id, status);
            return ResponseEntity.ok(Map.of("message", "상태가 " + status + "(으)로 변경되었습니다."));

        } catch (Exception e) {
            log.error("상태 변경 실패: 테이블 {}번, 사유: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "상태 변경 중 오류가 발생했습니다."));
        }
    }

    /**
     * [POST] 특정 테이블의 액세스 토큰(UUID) 갱신
     * @param id 테이블 PK
     * 상세 설명: 태블릿의 로그인이 풀렸거나 보안 갱신이 필요할 때 새로운 8자리 토큰 발급
     */
    @ResponseBody
    @PostMapping("/{id}/token")
    public ResponseEntity<Map<String, String>> refreshToken(@PathVariable("id") Integer id) {
        try {
            String newToken = cafeTableService.generateNewToken(id);
            log.info("토큰 갱신 완료: 테이블 {}번 -> {}", id, newToken);

            return ResponseEntity.ok(Map.of(
                    "accessToken", newToken,
                    "message", "새로운 인증 토큰이 발급되었습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * [DELETE] 자정 데이터 리셋 강제 실행 (운영/테스트용)
     * 주 설명: 모든 활성 세션을 강제 종료하고 모든 테이블을 공석(EMPTY)으로 초기화
     */
    @ResponseBody
    @DeleteMapping("/reset")
    public ResponseEntity<Map<String, String>> forceReset() {
        try {
            cafeTableService.resetAllTablesForNewDay();
            log.warn("경고: 관리자에 의한 시스템 전체 강제 리셋이 수행되었습니다.");

            return ResponseEntity.ok(Map.of("message", "모든 테이블과 세션이 초기화되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "리셋 중 오류 발생"));
        }
    }
}

