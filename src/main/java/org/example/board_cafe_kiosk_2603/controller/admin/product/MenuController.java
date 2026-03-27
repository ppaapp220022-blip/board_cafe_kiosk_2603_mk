package org.example.board_cafe_kiosk_2603.controller.admin.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.dto.admin.product.CategoryResponseDTO;
import org.example.board_cafe_kiosk_2603.dto.admin.product.MenuRequestDTO;
import org.example.board_cafe_kiosk_2603.dto.admin.product.MenuResponseDTO;
import org.example.board_cafe_kiosk_2603.service.admin.product.CategoryService;
import org.example.board_cafe_kiosk_2603.service.admin.product.MenuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 메뉴 CRUD 컨트롤러 (Admin 전용)
 * 기본 경로: /admin/product/menus
 */
@Log4j2
@Controller
@RequestMapping("/admin/product/menu")
@RequiredArgsConstructor
public class MenuController {  /** ⛔️ 수정 필요함 */

    private final MenuService menuService;
    private final CategoryService categoryService;

    /**
     * 메뉴 목록 조회 → 탭(tab) 파라미터로 food/drink/hidden 분기
     * GET /admin/product/menu?tab=food
     */
    @GetMapping
    public String getList(@RequestParam(defaultValue = "food") String tab, Model model) {
        log.debug("GET /admin/product/menu 요청 - tab: {}", tab);

        List<MenuResponseDTO> menuList;
        if ("hidden".equals(tab)) {
            menuList = menuService.getByIsAvailable(false);
            log.debug("숨김 메뉴 조회 - 건수: {}", menuList.size());
        } else if ("drink".equals(tab)) {
            menuList = menuService.getByType("DRINK");
            log.debug("음료 메뉴 조회 - 건수: {}", menuList.size());
        } else {
            menuList = menuService.getByType("FOOD");
            log.debug("음식 메뉴 조회 - 건수: {}", menuList.size());
        }

        model.addAttribute("menuList", menuList);
        model.addAttribute("activeTab", tab);
        model.addAttribute("activePage", "productReg");
        return "admin/product_menu";
    }

    /**
     * 메뉴 등록 폼 페이지
     * GET /admin/product/menu/add?tab=food
     */
    @GetMapping("/add")
    public String addForm(@RequestParam(defaultValue = "food") String tab, Model model) {
        log.debug("GET /admin/product/menu/add 요청 - tab: {}", tab);
        List<CategoryResponseDTO> categoryList = categoryService.getAll();
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("activeTab", tab);
        model.addAttribute("activePage", "productReg");
        return "admin/product_menu_form";
    }

    /**
     * 메뉴 등록 처리
     * POST /admin/product/menu/add
     */
    @PostMapping("/add")
    public String register(@ModelAttribute MenuRequestDTO dto,
                           @RequestParam(defaultValue = "food") String tab) {
        log.debug("POST /admin/product/menu/add 요청 - dto: {}", dto);
        menuService.register(dto);
        log.debug("메뉴 등록 완료");
        return "redirect:/admin/product/menu?tab=" + tab;
    }

    /**
     * 메뉴 수정 폼 페이지
     * GET /admin/product/menu/edit/{id}?tab=food
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable int id,
                           @RequestParam(defaultValue = "food") String tab, Model model) {
        log.debug("GET /admin/product/menu/edit/{} 요청", id);
        MenuResponseDTO menu = menuService.getById(id);
        List<CategoryResponseDTO> categoryList = categoryService.getAll();
        model.addAttribute("menu", menu);
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("activeTab", tab);
        model.addAttribute("activePage", "productReg");
        return "admin/product_menu_form";
    }

    /**
     * 메뉴 수정 처리
     * POST /admin/product/menu/edit/{id}
     */
    @PostMapping("/edit/{id}")
    public String modify(@PathVariable int id,
                         @ModelAttribute MenuRequestDTO dto,
                         @RequestParam(defaultValue = "food") String tab) {
        log.debug("POST /admin/product/menu/edit/{} 요청 - dto: {}", id, dto);
        menuService.modify(id, dto);
        log.debug("메뉴 수정 완료 - id: {}", id);
        return "redirect:/admin/product/menu?tab=" + tab;
    }

    /**
     * 메뉴 소프트 삭제 처리
     * POST /admin/product/menu/delete/{id}
     */
    @PostMapping("/delete/{id}")
    public String remove(@PathVariable int id,
                         @RequestParam(defaultValue = "food") String tab) {
        log.debug("POST /admin/product/menu/delete/{} 요청", id);
        menuService.remove(id);
        log.debug("메뉴 삭제 완료 - id: {}", id);
        return "redirect:/admin/product/menu?tab=" + tab;
    }

    /**
     * 메뉴 품절/입고 토글
     * POST /admin/product/menu/{id}/toggle-soldout
     */
    @PostMapping("/{id}/toggle-soldout")
    public String toggleSoldout(@PathVariable int id,
                                @RequestParam(defaultValue = "food") String tab) {
        log.debug("POST /admin/product/menu/{}/toggle-soldout 요청", id);
        menuService.toggleAvailable(id);
        log.debug("메뉴 판매 상태 토글 완료 - id: {}", id);
        return "redirect:/admin/product/menu?tab=" + tab;
    }

    /**
     * 메뉴 숨김 처리 (소프트 삭제)
     * POST /admin/product/menu/{id}/toggle-hide
     */
    @PostMapping("/{id}/toggle-hide")
    public String toggleHide(@PathVariable int id,
                             @RequestParam(defaultValue = "food") String tab) {
        log.debug("POST /admin/product/menu/{}/toggle-hide 요청", id);
        menuService.remove(id);
        log.debug("메뉴 숨김 완료 - id: {}", id);
        return "redirect:/admin/product/menu?tab=" + tab;
    }

    /**
     * 숨김 메뉴 다시 표시 (소프트 삭제 복원)
     * POST /admin/product/menu/{id}/restore
     */
    @PostMapping("/{id}/restore")
    public String restore(@PathVariable int id) {
        log.debug("POST /admin/product/menu/{}/restore 요청", id);
        menuService.toggleAvailable(id);
        log.debug("메뉴 복원 완료 - id: {}", id);
        return "redirect:/admin/product/menu?tab=hidden";
    }

}