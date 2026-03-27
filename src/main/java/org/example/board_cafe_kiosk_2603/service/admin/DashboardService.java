package org.example.board_cafe_kiosk_2603.service.admin;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 관리자 페이지 렌더링에 필요한 Model 구성 로직을 담당.
 * Controller 는 서비스를 호출하고 뷰 이름만 반환합니다.
 */
@Service
public class DashboardService {

    public void buildDashboardModel(Model model) {
        model.addAttribute("tableList",      new ArrayList<>());
        model.addAttribute("emptyCount",     12);
        model.addAttribute("occupiedCount",  0);
        model.addAttribute("waitingCount",   0);
        model.addAttribute("activePage",     "tableStatus");
    }

    public void buildStatsModel(Model model) {
        model.addAttribute("activePage",       "salesStats");
        model.addAttribute("totalRevenue",     4350000L);
        model.addAttribute("dailyAvgRevenue",  621400L);
        model.addAttribute("totalOrders",      158);
        model.addAttribute("totalVisitors",    342);
        model.addAttribute("avgUsageTime",     85);
        model.addAttribute("dailyLabels",      "[\"3/13\",\"3/14\",\"3/15\",\"3/16\",\"3/17\",\"3/18\",\"3/19\"]");
        model.addAttribute("dailyData",        "[450000,520000,380000,620000,580000,680000,720000]");
        model.addAttribute("categoryLabels",   "[\"음료\",\"스낵\",\"게임대여\",\"식사\"]");
        model.addAttribute("categoryData",     "[1200000,850000,1500000,800000]");
        model.addAttribute("avgTimeData",      "[65,72,58,85,78,92,88]");
        model.addAttribute("bestSellers",      getBestSellers());
    }

    public void buildPriceModel(Model model) {
        model.addAttribute("activePage",    "salesAnalysis");
        model.addAttribute("pendingPrices", getPendingPrices());
        model.addAttribute("priceHistory",  getPriceHistory());
    }

    public void buildProductModel(Model model, String tab) {
        model.addAttribute("activePage", "productReg");
        model.addAttribute("activeTab",  tab);
        model.addAttribute("gameList",   getGameList());
        model.addAttribute("foodList",   getFoodList());
    }

    // ===================================================
    // 더미 데이터 (추후 DB 연동 시 Mapper 호출로 교체)
    // ===================================================

    private List<Map<String, Object>> getBestSellers() {
        return List.of(
                Map.of("rank", 1, "productName", "아이스 아메리카노", "soldCount", 45, "totalAmount", 202500L),
                Map.of("rank", 2, "productName", "나초 & 치즈딥",    "soldCount", 32, "totalAmount", 192000L),
                Map.of("rank", 3, "productName", "불닭볶음면",        "soldCount", 28, "totalAmount", 112000L),
                Map.of("rank", 4, "productName", "초코츄러스",        "soldCount", 15, "totalAmount",  67500L)
        );
    }

    private List<Map<String, Object>> getPendingPrices() {
        return List.of(
                Map.of("id", 1L, "productName", "아이스 아메리카노 (L)", "currentPrice", 4500, "newPrice", 4800, "applyDate", "2026-04-01"),
                Map.of("id", 2L, "productName", "나초 & 치즈 세트",      "currentPrice", 8000, "newPrice", 7500, "applyDate", "2026-04-05")
        );
    }

    private List<Map<String, Object>> getPriceHistory() {
        return List.of(
                Map.of("productName", "보드게임 1시간 이용권", "changedAt", "2026-01-10", "beforePrice", 3000, "afterPrice", 3500, "diff",  500),
                Map.of("productName", "캔콜라 355ml",         "changedAt", "2025-12-20", "beforePrice", 2500, "afterPrice", 2000, "diff", -500)
        );
    }

    private List<Map<String, Object>> getGameList() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(Map.of("id", 1L, "name", "루미큐브", "price", 0, "stock", 5, "maxStock", 5));
        list.add(Map.of("id", 2L, "name", "스플렌더",  "price", 0, "stock", 2, "maxStock", 3));
        return list;
    }

    private List<Map<String, Object>> getFoodList() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(Map.of("id", 3L, "name", "아이스 아메리카노", "price", 4500, "soldOut", false));
        list.add(Map.of("id", 4L, "name", "초코 츄러스",       "price", 3500, "soldOut", true));
        return list;
    }
}
