package org.example.board_cafe_kiosk_2603.controller.admin.statistics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.dto.admin.statistics.DailySalesDTO;
import org.example.board_cafe_kiosk_2603.dto.admin.statistics.ItemSalesDTO;
import org.example.board_cafe_kiosk_2603.service.admin.statistics.StatService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class StatController {
    private final StatService statService;

    @GetMapping("/status")
    public String adminStatusPage(Model model) {
        model.addAttribute("activePage", "salesStats");
        // 초기 날짜 설정
        model.addAttribute("selectedDate", LocalDate.now());
        return "admin/status";
    }

    @GetMapping("/api/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStatData(
            @RequestParam(value = "targetDate", required = false) String targetDateStr) {

        LocalDate targetDate = (targetDateStr == null || targetDateStr.isEmpty())
                ? LocalDate.now() : LocalDate.parse(targetDateStr);

        Map<String, Object> response = new HashMap<>();

        // 1. 주간 매출 (차트용)
        List<DailySalesDTO> weeklySales = statService.getWeeklyStats(targetDate);
        response.put("weeklySales", weeklySales);

        // 📈 주간 일평균 매출 직접 계산
        long weeklyTotal = weeklySales.stream().mapToLong(DailySalesDTO::getTotalRevenue).sum();
        long weeklyAvg = weeklySales.isEmpty() ? 0 : weeklyTotal / weeklySales.size();

        // 2. 선택일 요약 데이터
        DailySalesDTO summary = weeklySales.stream()
                .filter(s -> s.getStatDate().equals(targetDate.toString()))
                .findFirst()
                .orElse(DailySalesDTO.builder().statDate(targetDate.toString())
                        .totalRevenue(0L).orderCount(0).visitCount(0).build());
        response.put("summary", summary);

        // 3. 베스트 셀러
        response.put("topMenus", statService.getTopSellingMenuByDate(targetDate, 5));

        // 4. 카테고리 통계
        response.put("categoryStats", statService.getCategoryStats(targetDate));

        response.put("weeklySales", weeklySales);
        response.put("weeklyAvgRevenue", weeklyAvg);

        return ResponseEntity.ok(response);
    }
}
