package org.example.board_cafe_kiosk_2603.controller.admin.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.admin.product.CategoryType;
import org.example.board_cafe_kiosk_2603.dto.admin.product.CategoryResponseDTO;
import org.example.board_cafe_kiosk_2603.dto.admin.product.MenuRequestDTO;
import org.example.board_cafe_kiosk_2603.dto.admin.product.MenuResponseDTO;
import org.example.board_cafe_kiosk_2603.dto.common.pagenation.PageRequestDTO;
import org.example.board_cafe_kiosk_2603.dto.common.pagenation.PageResponseDTO;
import org.example.board_cafe_kiosk_2603.service.admin.product.CategoryService;
import org.example.board_cafe_kiosk_2603.service.admin.product.MenuService;
import org.example.board_cafe_kiosk_2603.util.FileUploadUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Log4j2
@Controller
@RequestMapping("/admin/product/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;
    private final CategoryService categoryService;
    private final FileUploadUtil fileUploadUtil;

    /* 메뉴 목록 조회 - 페이징 포함 */
    @GetMapping
    public String getList(@RequestParam(defaultValue = "food") String tab,
                          @RequestParam(required = false) Integer categoryId,
                          PageRequestDTO pageRequestDTO,
                          Model model) {
        log.info("--- 메뉴 목록 조회 요청 (Tab: {}, CategoryID: {}, page: {}) ---", tab, categoryId, pageRequestDTO.getPage());

        // 탭별 type 결정
        String type = switch (tab) {
            case "drink" -> "DRINK";
            case "guest" -> "GUEST";
            default -> "FOOD";
        };

        PageResponseDTO<MenuResponseDTO> pageResponse;

        if ("hidden".equals(tab)) {
            // 숨김 탭
            pageResponse = menuService.getByIsDeleted(true, pageRequestDTO);
        } else if (categoryId != null) {
            // 탭 + 카테고리 필터
            pageResponse = menuService.getByTypeAndCategoryId(type, categoryId, pageRequestDTO);
        } else {
            // 탭만
            pageResponse = menuService.getByType(type, pageRequestDTO);
        }

        // 탭에 맞는 카테고리 버튼 목록 (숨김 탭은 불필요)
        List<CategoryResponseDTO> categoryList = null;
        if (!"hidden".equals(tab)) {
            CategoryType categoryType = switch (tab) {
                case "drink" -> CategoryType.DRINK;
                case "guest" -> CategoryType.GUEST;
                default -> CategoryType.FOOD;
            };
            categoryList = categoryService.getByType(categoryType);
        }

        model.addAttribute("pageResponse", pageResponse);
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("pageRequestDTO", pageRequestDTO);
        model.addAttribute("activeTab", tab);
        model.addAttribute("activePage", "productReg");

        log.info("--- 메뉴 목록 조회 완료 - tab: {}, 건수: {}, 전체: {}", tab, pageResponse.getDtoList().size(), pageResponse.getTotal());
        return "admin/product_menu";
    }

    /* 메뉴 등록 페이지 이동 */
    @GetMapping("/add")
    public String addForm(@RequestParam(defaultValue = "food") String tab, Model model) {
        log.info("--- 메뉴 등록 폼 요청 (Tab: {}) ---", tab);
        List<CategoryType> menuTypes = List.of(CategoryType.FOOD, CategoryType.DRINK, CategoryType.GUEST);
        List<CategoryResponseDTO> categoryList = categoryService.getAll().stream()
                .filter(c -> menuTypes.contains(c.getType()))
                .toList();
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("activeTab", tab);
        model.addAttribute("activePage", "productReg");
        return "admin/product_menu_form";
    }

    /* 메뉴 등록 처리 */
    @PostMapping("/add")
    public String register(@ModelAttribute MenuRequestDTO menuRequestDTO,
                           @RequestParam(defaultValue = "imageFile", required = false) MultipartFile imageFile,
                           @RequestParam(defaultValue = "food") String tab) throws IOException {
        log.info("--- 메뉴 신규 등록 시작 (Tab: {}) ---", tab);
        String imageUrl = fileUploadUtil.save(imageFile);
        if (imageUrl != null) {
            menuRequestDTO.setImageUrl(imageUrl);
        }
        menuService.register(menuRequestDTO);
        log.info("--- 메뉴 등록 완료 ---");
        return "redirect:/admin/product/menu?tab=" + tab;
    }

    /* 메뉴 수정 페이지 이동 */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable int id,
                           @RequestParam(defaultValue = "food") String tab, Model model) {
        log.info("--- 메뉴 수정 폼 요청 (MenuID: {}, Tab: {}) ---", id, tab);
        MenuResponseDTO menu = menuService.getById(id);
        List<CategoryType> menuTypes = List.of(CategoryType.FOOD, CategoryType.DRINK, CategoryType.GUEST);
        List<CategoryResponseDTO> categoryList = categoryService.getAll().stream()
                .filter(c -> menuTypes.contains(c.getType()))
                .toList();
        model.addAttribute("menu", menu);
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("activeTab", tab);
        model.addAttribute("activePage", "productReg");
        return "admin/product_menu_form";
    }

    /* 메뉴 수정 처리 */
    @PostMapping("/edit/{id}")
    public String modify(@PathVariable int id,
                         @ModelAttribute MenuRequestDTO menuRequestDTO,
                         @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                         @RequestParam(defaultValue = "food") String tab) throws IOException {
        log.info("--- 메뉴 수정 요청 (MenuID: {}, Tab: {}) ---", id, tab);
        if (imageFile != null && !imageFile.isEmpty()) {
            fileUploadUtil.delete(menuService.getById(id).getImageUrl());
            menuRequestDTO.setImageUrl(fileUploadUtil.save(imageFile));
        }
        menuService.modify(id, menuRequestDTO);
        return "redirect:/admin/product/menu?tab=" + tab;
    }

    /* 메뉴 숨김 처리 */
    @PostMapping("/{id}/toggle-hide")
    public String toggleHide(@PathVariable int id,
                             @RequestParam(defaultValue = "food") String tab,
                             @RequestParam(required = false) Integer categoryId,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "10") int size) {
        log.info("--- 메뉴 숨김 요청 (MenuID: {}, Tab: {}) ---", id, tab);
        menuService.remove(id);
        String redirect = "redirect:/admin/product/menu?tab=" + tab + "&page=" + page + "&size=" + size;
        if (categoryId != null) redirect += "&categoryId=" + categoryId;
        return redirect;
    }

    /* 메뉴 품절 상태 토글 */
    @PostMapping("/{id}/toggle-soldout")
    public String toggleSoldout(@PathVariable int id,
                                @RequestParam(defaultValue = "food") String tab,
                                @RequestParam(required = false) Integer categoryId,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "10") int size) {
        log.info("--- 메뉴 품절 상태 토글 요청 (MenuID: {}) ---", id);
        menuService.toggleAvailable(id);
        String redirect = "redirect:/admin/product/menu?tab=" + tab + "&page=" + page + "&size=" + size;
        if (categoryId != null) redirect += "&categoryId=" + categoryId;
        return redirect;
    }

    /* 숨김 메뉴 복원 */
    @PostMapping("/{id}/restore")
    public String restore(@PathVariable int id,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size) {
        log.info("--- 숨김 메뉴 복원 요청 (MenuID: {}) ---", id);
        menuService.restore(id);
        return "redirect:/admin/product/menu?tab=hidden&page=" + page + "&size=" + size;
    }
}