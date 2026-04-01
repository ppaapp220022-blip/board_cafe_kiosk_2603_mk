package org.example.board_cafe_kiosk_2603.controller.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.service.admin.point.PointService;
import org.example.board_cafe_kiosk_2603.service.admin.macro.MacroMessageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Admin 관리자 페이지 컨트롤러
 * 모든 관리자 페이지의 라우팅을 담당합니다.
 *
 * URL 패턴: /admin/*
 * 뷰 경로: templates/admin/*
 */
@Log4j2
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MacroMessageService macroMessageService;
    private final PointService pointService;

    // 포인트 관리 페이지 이동 로직
    @GetMapping("/points")
    public String pointManagement(Model model) {
//        // 화면 확인을 위한 더미 데이터 (추후 DB 연동)
//        List<Map<String, Object>> pointList = new ArrayList<>();
//        pointList.add(Map.of("phone", "010-1234-5678", "balance", 12500, "updatedAt", LocalDateTime.now()));
//        pointList.add(Map.of("phone", "010-9876-5432", "balance", 5000, "updatedAt", LocalDateTime.now()));
//        pointList.add(Map.of("phone", "010-2222-2222", "balance", 2222, "updatedAt", LocalDateTime.now()));
//
//        model.addAttribute("pointList", pointList);
//        model.addAttribute("totalCustomers", 2);
//        model.addAttribute("totalPoints", 17500);
//        model.addAttribute("avgPoints", 8750);
//
//        // 중요: 사이드바 하이라이트를 위해 'pointManagement' 전달
//        model.addAttribute("activePage", "pointManagement");
//
//        return "admin/point"; // templates/admin/point.html 호출
        model.addAttribute("pointList",      pointService.getAllPoints());
        model.addAttribute("totalCustomers", pointService.getTotalCustomers());
        model.addAttribute("totalPoints",    pointService.getTotalPoints());
        model.addAttribute("avgPoints",      pointService.getAvgPoints());
        model.addAttribute("activePage",     "pointManagement");
        return "admin/point";
    }

    @GetMapping("/status")
    public String stats(Model model) {
        // 1. 사이드바 하이라이트 설정
        model.addAttribute("activePage", "salesStats");

        // 2. 상단 요약 통계 (더미 데이터)
        model.addAttribute("totalRevenue", 4350000L);      // 7일 총 매출
        model.addAttribute("dailyAvgRevenue", 621400L);    // 일평균
        model.addAttribute("totalOrders", 158);            // 총 주문 건수
        model.addAttribute("totalVisitors", 342);          // 총 방문자
        model.addAttribute("avgUsageTime", 85);            // 평균 이용시간(분)

        // 3. 차트용 데이터 (JSON 문자열 형태로 전달)
        // 실제 서비스에서는 ObjectMapper를 사용하거나 직접 포맷팅합니다.
        model.addAttribute("dailyLabels", "[\"3/13\", \"3/14\", \"3/15\", \"3/16\", \"3/17\", \"3/18\", \"3/19\"]");
        model.addAttribute("dailyData", "[450000, 520000, 380000, 620000, 580000, 680000, 720000]");

        model.addAttribute("categoryLabels", "[\"음료\", \"스낵\", \"게임대여\", \"식사\"]");
        model.addAttribute("categoryData", "[1200000, 850000, 1500000, 800000]");

        model.addAttribute("avgTimeData", "[65, 72, 58, 85, 78, 92, 88]");

        // 4. 베스트 셀러 리스트 (더미 데이터)
        List<Map<String, Object>> bestSellers = new ArrayList<>();
        bestSellers.add(Map.of("rank", 1, "productName", "아이스 아메리카노", "soldCount", 45, "totalAmount", 202500L));
        bestSellers.add(Map.of("rank", 2, "productName", "나초 & 치즈딥", "soldCount", 32, "totalAmount", 192000L));
        bestSellers.add(Map.of("rank", 3, "productName", "불닭볶음면", "soldCount", 28, "totalAmount", 112000L));
        bestSellers.add(Map.of("rank", 4, "productName", "초코츄러스", "soldCount", 15, "totalAmount", 67500L));

        model.addAttribute("bestSellers", bestSellers);

        return "admin/status"; // 파일 위치: src/main/resources/templates/admin/status.html
    }


//    // 상품 등록 처리
//    @PostMapping("/product/add")
//    public String addProduct(@RequestParam Map<String, String> params) {
//        log.info("상품 등록 데이터: {}", params);
//        return "redirect:/admin/product?tab=" + params.get("category");
//    }
//
//    // 품절 상태 변경
//    @PostMapping("/product/{id}/soldout")
//    public String toggleSoldOut(@PathVariable Long id) {
//        log.info("상품 품절 상태 변경 ID: {}", id);
//        return "redirect:/admin/product?tab=food";
//    }
//
//    // 상품 삭제
//    @PostMapping("/product/{id}/delete")
//    public String deleteProduct(@PathVariable Long id) {
//        log.info("상품 삭제 ID: {}", id);
//        return "redirect:/admin/product";
//    }

    // 패키지 요금 정책
    @GetMapping("/package")
    public String addPackage(Model model) {
        log.info("--- AdminController addPackage post ---");

        // 중요: 사이드바 하이라이트를 위해 'pointManagement' 전달
        model.addAttribute("activePage", "packageManagement");
        return "admin/package";
    }

    // 패키지 요금 정책
//    @GetMapping("/staff")
//    public String getAllStaff(Model model) {
//        log.info("--- AdminController getAllStaff post ---");
//
//        // 중요: 사이드바 하이라이트를 위해 'pointManagement' 전달
//        model.addAttribute("activePage", "staffManagement");
//        return "admin/staff";
//    }

}
