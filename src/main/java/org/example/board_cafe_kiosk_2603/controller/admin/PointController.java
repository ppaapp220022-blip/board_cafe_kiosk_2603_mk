package org.example.board_cafe_kiosk_2603.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.dto.admin.PointHistoryDTO;
import org.example.board_cafe_kiosk_2603.service.admin.PointService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 관리자 포인트 관리 컨트롤러
 * URL: /admin/points/*
 */
@Log4j2
@Controller
@RequestMapping("/admin/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    /** 포인트 관리 페이지 */
    @GetMapping
    public String pointManagement(Model model) {
        model.addAttribute("pointList",      pointService.getAllPoints());
        model.addAttribute("totalCustomers", pointService.getTotalCustomers());
        model.addAttribute("totalPoints",    pointService.getTotalPoints());
        model.addAttribute("avgPoints",      pointService.getAvgPoints());
        model.addAttribute("activePage",     "pointManagement");
        return "admin/point";
    }

    /** 포인트 이력 조회 (AJAX) */
    @GetMapping("/{pointId}/history")
    @ResponseBody
    public List<PointHistoryDTO> pointHistory(@PathVariable int pointId) {
        return pointService.getHistoryByPointId(pointId);
    }
}
