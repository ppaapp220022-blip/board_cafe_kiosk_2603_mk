package org.example.board_cafe_kiosk_2603.admin.point.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.admin.point.dto.PointAdminDTO;
import org.example.board_cafe_kiosk_2603.admin.point.dto.PointHistoryDTO;
import org.example.board_cafe_kiosk_2603.common.pagination.PageRequestDTO;
import org.example.board_cafe_kiosk_2603.common.pagination.PageResponseDTO;
import org.example.board_cafe_kiosk_2603.admin.point.service.PointService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/*
 * 작성자 : 김민기
 * 기능 : 관리자 포인트 관리 컨트롤러
 * 날짜 : 2026-03-27
 */

@Log4j2
@Controller
@RequestMapping("/admin/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    /* 포인트 관리 페이지 */

    @GetMapping
    public String pointManagement() {
        return "redirect:/admin/points/list";
    }
    /**
     * 포인트 이력 조회 (AJAX) 작업을 수행합니다.
     *
     * @param pointId 전달받은 pointId 값
     * @return 처리 결과
     */

    @GetMapping("/{pointId}/history")
    @ResponseBody

    public List<PointHistoryDTO> pointHistory(@PathVariable int pointId) {
        return pointService.getHistoryByPointId(pointId);
    }

    /*  페이징 처리된 포인트 목록 페이지 */
    @GetMapping("/list")
    public String list(PageRequestDTO pageRequestDTO, Model model) {
        log.info("포인트 페이징 목록 요청: " + pageRequestDTO);

        PageResponseDTO<PointAdminDTO> responseDTO = pointService.getPagedPoints(pageRequestDTO);

        model.addAttribute("responseDTO", responseDTO);

        log.info("responseDTO: " + responseDTO);
        model.addAttribute("pageRequestDTO", pageRequestDTO);

        // 3. 상단 대시보드용 통계 데이터
        model.addAttribute("totalCustomers", pointService.getTotalCustomers());
        model.addAttribute("totalPoints",    pointService.getTotalPoints());
        model.addAttribute("avgPoints",      pointService.getAvgPoints());
        model.addAttribute("activePage",     "pointManagement");

        return "admin/point";
    }
}
