package org.example.board_cafe_kiosk_2603.controller.admin.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.admin.product.CategoryType;
import org.example.board_cafe_kiosk_2603.dto.admin.product.CategoryResponseDTO;
import org.example.board_cafe_kiosk_2603.dto.admin.product.MenuRequestDTO;
import org.example.board_cafe_kiosk_2603.dto.admin.product.MenuResponseDTO;
import org.example.board_cafe_kiosk_2603.service.admin.product.CategoryService;
import org.example.board_cafe_kiosk_2603.service.admin.product.MenuService;
import org.example.board_cafe_kiosk_2603.util.FileUploadUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 메뉴 CRUD 컨트롤러 (Admin 전용)
 * 기본 경로: /admin/product/menus
 */
@Log4j2
@Controller
@RequestMapping("/admin/product/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;
    private final CategoryService categoryService;
    private final FileUploadUtil fileUploadUtil;

    /**
     * 메뉴 목록 조회 → tab 파라미터로 food / drink / guest / hidden 분기
     * GET /admin/product/menu?tab=food
     */
    // 카테고리 필터
    // GET /admin/product/menu?tab=food&categoryId=2
    @GetMapping
    public String getList(@RequestParam(defaultValue = "food") String tab,
                          @RequestParam(required = false) Integer categoryId,
                          Model model) {
        log.debug("GET /admin/product/menu 요청 - tab: {}, categoryId: {}", tab, categoryId);

        // 탭 기준으로 기본 목록 조회
        List<MenuResponseDTO> menuList = switch (tab) {
            case "drink"  -> menuService.getByType("DRINK");
            case "guest"  -> menuService.getByType("GUEST");
            case "hidden" -> menuService.getByIsDeleted(true);
            default       -> menuService.getByType("FOOD");
        };

        // 숨김 탭 외에서 categoryId 필터 적용
        if (categoryId != null && !"hidden".equals(tab)) {
            menuList = menuList.stream()
                    .filter(m -> m.getCategoryId() == categoryId)
                    .toList();
        }

        // 탭에 맞는 카테고리 목록 (숨김 탭은 불필요하므로 null)
        List<CategoryResponseDTO> categoryList = null;
        if (!"hidden".equals(tab)) {
            CategoryType type = switch (tab) {
                case "drink" -> CategoryType.DRINK;
                case "guest" -> CategoryType.GUEST;
                default      -> CategoryType.FOOD;
            };
            categoryList = categoryService.getByType(type);
        }

        log.debug("메뉴 목록 조회 완료 - tab: {}, categoryId: {}, 건수: {}", tab, categoryId, menuList.size());

        model.addAttribute("menuList", menuList);
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("selectedCategoryId", categoryId);   // 뷰에서 활성 버튼 판단에 사용
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
    // 이미지 파일 업로드 추가
    @PostMapping("/add")
    public String register(@ModelAttribute MenuRequestDTO menuRequestDTO,
                           @RequestParam(defaultValue = "imageFile", required = false) MultipartFile imageFile,
                           @RequestParam(defaultValue = "food") String tab) throws IOException {
        log.debug("POST /admin/product/menu/add 요청 - menuRequestDTO: {}", menuRequestDTO);
        String imageUrl = fileUploadUtil.save(imageFile);
        if (imageUrl != null) {
            menuRequestDTO.setImageUrl(imageUrl);
            log.debug("메뉴 이미지 저장 완료: {}", imageUrl);
        }
        menuService.register(menuRequestDTO);
        log.debug("메뉴 등록 완료");
        return "redirect:/admin/product/menu?tab=" + tab;
    }

    /**
     * 메뉴 수정 폼 페이지
     * GET /admin/product/menu/edit/{id}?tab=food
     */
    // 기존 이미지 표시
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable int id,
                           @RequestParam(defaultValue = "food") String tab, Model model) {
        log.debug("GET /admin/product/menu/edit/{} 요청 - tab: {}", id, tab);
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
    // 새 이미지 업로드 시 기존 이미지 교체
    @PostMapping("/edit/{id}")
    public String modify(@PathVariable int id,
                         @ModelAttribute MenuRequestDTO menuRequestDTO,
                         @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                         @RequestParam(defaultValue = "food") String tab) throws IOException {
        log.debug("POST /admin/product/menu/edit/{} 요청 - tab: {}", id, tab);
        if (imageFile != null && !imageFile.isEmpty()) {
            // 변수 선언 없이 메서드 체이닝으로 기존 imageUrl 사용
            fileUploadUtil.delete(menuService.getById(id).getImageUrl());
            menuRequestDTO.setImageUrl(fileUploadUtil.save(imageFile));
            log.debug("메뉴 이미지 교체 완료: {}", menuRequestDTO.getImageUrl());
        }
        menuService.modify(id, menuRequestDTO);
        log.debug("메뉴 수정 완료 - id: {}", id);
        return "redirect:/admin/product/menu?tab=" + tab;
    }

    /**
     * 메뉴 소프트 삭제 (숨김 처리)
     * POST /admin/product/menu/{id}/toggle-hide
     */
    @PostMapping("/{id}/toggle-hide")
    public String toggleHide(@PathVariable int id,
                             @RequestParam(defaultValue = "food") String tab) {
        log.debug("POST /admin/product/menu/{}/toggle-hide 요청 - tab: {}", id, tab);
        menuService.remove(id);
        log.debug("메뉴 숨김 완료 - id: {}", id);
        return "redirect:/admin/product/menu?tab=" + tab;
    }

    /**
     * 메뉴 품절/입고 토글
     * POST /admin/product/menu/{id}/toggle-soldout
     */
    @PostMapping("/{id}/toggle-soldout")
    public String toggleSoldout(@PathVariable int id,
                                @RequestParam(defaultValue = "food") String tab) {
        log.debug("POST /admin/product/menu/{}/toggle-soldout 요청 - tab: {}", id, tab);
        menuService.toggleAvailable(id);
        log.debug("메뉴 판매 상태 토글 완료 - id: {}", id);
        return "redirect:/admin/product/menu?tab=" + tab;
    }

    /**
     * 숨김 메뉴 다시 표시 (소프트 삭제 복원)
     * POST /admin/product/menu/{id}/restore
     */
    @PostMapping("/{id}/restore")
    public String restore(@PathVariable int id) {
        log.debug("POST /admin/product/menu/{}/restore 요청", id);
        menuService.restore(id);
        log.debug("메뉴 복원 완료 - id: {}", id);
        return "redirect:/admin/product/menu?tab=hidden";
    }
}