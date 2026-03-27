package org.example.board_cafe_kiosk_2603.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.service.admin.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 관리자 대시보드 / 통계 / 가격 / 상품 컨트롤러
 * URL: /admin/*
 */
@Log4j2
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        dashboardService.buildDashboardModel(model);
        return "admin/dashboard";
    }

    @GetMapping("/status")
    public String stats(Model model) {
        dashboardService.buildStatsModel(model);
        return "admin/status";
    }

    @GetMapping("/price")
    public String prices(Model model) {
        dashboardService.buildPriceModel(model);
        return "admin/price";
    }

    @PostMapping("/price")
    public String savePrices(@RequestParam Map<String, String> params) {
        log.info("수정된 가격 데이터: {}", params);
        return "redirect:/admin/price";
    }

    @GetMapping("/product")
    public String products(Model model,
                           @RequestParam(defaultValue = "game") String tab) {
        dashboardService.buildProductModel(model, tab);
        return "admin/product";
    }

    @PostMapping("/product/add")
    public String addProduct(@RequestParam Map<String, String> params) {
        log.info("상품 등록 데이터: {}", params);
        return "redirect:/admin/product?tab=" + params.get("category");
    }

    @PostMapping("/product/{id}/soldout")
    public String toggleSoldOut(@PathVariable Long id) {
        log.info("상품 품절 상태 변경 ID: {}", id);
        return "redirect:/admin/product?tab=food";
    }

    @PostMapping("/product/{id}/delete")
    public String deleteProduct(@PathVariable Long id) {
        log.info("상품 삭제 ID: {}", id);
        return "redirect:/admin/product";
    }
}
