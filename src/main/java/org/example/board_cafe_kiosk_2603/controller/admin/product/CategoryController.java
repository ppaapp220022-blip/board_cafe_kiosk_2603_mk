package org.example.board_cafe_kiosk_2603.controller.admin.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.dto.admin.product.CategoryRequestDTO;
import org.example.board_cafe_kiosk_2603.dto.admin.product.CategoryResponseDTO;
import org.example.board_cafe_kiosk_2603.service.admin.product.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 카테고리 CRUD 컨트롤러 (Admin 전용)
 * 기본 경로: /admin/categories
 */
@Log4j2
@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class CategoryController {

    /** ⚠️ 카테고리 CRUD 확장 대비용 controller ⚠️ */

    private final CategoryService categoryService;

    /**
     * 카테고리 전체 목록 조회 → 뷰 반환
     * GET /admin/categories
     */
    @GetMapping
    public String getAll(Model model) {
        log.debug("GET /admin/categories 요청");
        List<CategoryResponseDTO> list = categoryService.getAll();
        model.addAttribute("categoryList", list);
        model.addAttribute("activePage", "productReg");
        log.debug("카테고리 목록 조회 완료 - 건수: {}", list.size());
        return "admin/category_list";
    }

    /**
     * 카테고리 등록 폼 페이지
     * GET /admin/categories/add
     */
    @GetMapping("/add")
    public String addForm(Model model) {
        log.debug("GET /admin/categories/add 요청");
        model.addAttribute("activePage", "productReg");
        return "admin/category_form";
    }

    /**
     * 카테고리 등록 처리
     * POST /admin/categories/add
     */
    @PostMapping("/add")
    public String register(@ModelAttribute CategoryRequestDTO categoryRequestDTO) {
        log.debug("POST /admin/categories/add 요청 - categoryRequestDTO: {}", categoryRequestDTO);
        categoryService.register(categoryRequestDTO);
        log.debug("카테고리 등록 완료");
        return "redirect:/admin/categories";
    }

    /**
     * 카테고리 수정 폼 페이지
     * GET /admin/categories/edit/{id}
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable int id, Model model) {
        log.debug("GET /admin/categories/edit/{} 요청", id);
        CategoryResponseDTO category = categoryService.getById(id);
        model.addAttribute("category", category);
        model.addAttribute("activePage", "productReg");
        return "admin/category_form";
    }

    /**
     * 카테고리 수정 처리
     * POST /admin/categories/edit/{id}
     */
    @PostMapping("/edit/{id}")
    public String modify(@PathVariable int id, @ModelAttribute CategoryRequestDTO categoryRequestDTO) {
        log.debug("POST /admin/categories/edit/{} 요청 - categoryRequestDTO: {}", id, categoryRequestDTO);
        categoryService.modify(id, categoryRequestDTO);
        log.debug("카테고리 수정 완료 - id: {}", id);
        return "redirect:/admin/categories";
    }

    /**
     * 카테고리 삭제 처리
     * POST /admin/categories/delete/{id}
     */
    @PostMapping("/delete/{id}")
    public String remove(@PathVariable int id) {
        log.debug("POST /admin/categories/delete/{} 요청", id);
        categoryService.remove(id);
        log.debug("카테고리 삭제 완료 - id: {}", id);
        return "redirect:/admin/categories";
    }
}
