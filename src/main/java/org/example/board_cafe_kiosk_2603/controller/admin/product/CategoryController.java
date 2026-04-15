package org.example.board_cafe_kiosk_2603.controller.admin.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.dto.admin.product.CategoryRequestDTO;
import org.example.board_cafe_kiosk_2603.dto.admin.product.CategoryResponseDTO;
import org.example.board_cafe_kiosk_2603.service.admin.product.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * 카테고리 CRUD 컨트롤러
 * <p>
 * ■ 리팩토링 사항
 * 1. URL 통일: /admin/category (기존 /admin/categories 혼용 제거)
 * 2. addForm GET 제거: 목록 페이지(GET /)에서 모달로 등록 처리하므로 별도 폼 페이지 불필요
 * 3. save → add URL 변경 (기존 HTML과 일치)
 * 4. redirect URL 오류 수정: /admin/categories → /admin/category
 * 5. 삭제 시 IllegalStateException 처리 → RedirectAttributes로 에러 메시지 전달
 * 6. log.info 추가: 요청/응답 단계별 트러블슈팅 로그
 */

@Log4j2
@Controller
@RequestMapping("/admin/category")
@RequiredArgsConstructor
public class CategoryController {
    /**
     * ⚠️ 카테고리 CRUD 확장 대비용 controller ⚠️
     */

    /* 카테고리 CRUD 컨트롤러 */
    private final CategoryService categoryService;

    /* 카테고리 전체 목록 조회 → 뷰 반환 */
    @GetMapping
    public String getAll(Model model) {
        log.info("--- 카테고리 목록 조회 요청 ---");

        List<CategoryResponseDTO> list = categoryService.getAll();
        model.addAttribute("categoryList", list);
        model.addAttribute("activePage", "category");

        log.debug("카테고리 목록 조회 완료 - 건수: {}", list.size());
        return "admin/category_list";
    }

    /* 카테고리 등록 처리 */
    // 모달 폼에서 POST 요청 수신
    // 등록 후 목록으로 redirect
    @PostMapping("/add")
    public String register(@ModelAttribute CategoryRequestDTO categoryRequestDTO,
                           RedirectAttributes redirectAttributes) {
        log.info("--- 카테고리 등록 요청 (name: {}, type: {})",
                categoryRequestDTO.getName(), categoryRequestDTO.getType());

        try {
            categoryService.register(categoryRequestDTO);
            log.info("--- 카테고리 등록 성공: name={}", categoryRequestDTO.getName());
            redirectAttributes.addFlashAttribute("successMsg", "카테고리 등록되었습니다.");
        } catch (Exception e) {
            log.warn("--- 카테고리 등록 실패: {} ---", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMsg", "카테고리 등록에 실패했습니다: " + e.getMessage());
        }
        return "redirect:/admin/category";
    }

    /* 카테고리 수정 처리 */
    // 수정 모달 폼에서 POST 요청 수신
    // 수정 후 목록으로 redirect
    @PostMapping("/edit/{id}")
    public String modify(@PathVariable int id,
                         @ModelAttribute CategoryRequestDTO categoryRequestDTO,
                         RedirectAttributes redirectAttributes) {
        log.info("--- 카테고리 수정 요청 (id={}, name={}, type={}) ---",
                id, categoryRequestDTO.getName(), categoryRequestDTO.getType());

//        categoryService.modify(id, categoryRequestDTO);
        try {
            categoryService.modify(id, categoryRequestDTO);
            log.info("--- 카테고리 수정 성공: {} ---", id);
            redirectAttributes.addFlashAttribute("successMsg", "카테고리가 수정되었습니다.");
        } catch (NoSuchElementException e) {
            log.warn("--- 수정 대상 카테고리 없음: (id:{}, Msg:{}) ---", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMsg", "수정할 카테고리를 찾을 수 없습니다.");
        } catch (Exception e) {
            log.warn("--- 카테고리 수정 실패: (id:{}, Msg:{}) ---", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMsg", "카테고리 수정에 실패했습니다: " + e.getMessage());
        }
        log.debug("--- 카테고리 수정 완료, id: {} ---", id);
        return "redirect:/admin/category";
    }

    /* 카테고리 삭제 처리 */
    // 연결 상품 존재 시 IllegalStateException → errorMsg로 클라이언트에 전달
    // 뷰에서 errorMsg를 감지해 삭제 불가 모달 표시
    @PostMapping("/delete/{id}")
    public String remove(@PathVariable int id,
                         RedirectAttributes redirectAttributes) {
        log.info("--- 카테고리 삭제 요청: {} ---", id);

        try {
            categoryService.remove(id);
            log.info("--- 카테고리 삭제 성공: {} ---", id);
            redirectAttributes.addFlashAttribute("successMsg", "카테고리가 삭제되었습니다.");
        } catch (IllegalStateException e) {
            // 연결 상품 존재 → 삭제 불가
            log.warn("--- 삭제 불가, (id: {}, Msg: {}) ---", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        } catch (NoSuchElementException e) {
            log.warn("--- 삭제 대상 없음 (id: {}, Msg: {}) ---", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMsg", "삭제할 카테고리를 찾을 수 없습니다.");
        } catch (Exception e) {
            log.warn("--- 카테고리 삭제 실패 (id: {}, Msg: {}) ---", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMsg", "카테고리 삭제에 실패했습니다: " + e.getMessage());
        }

        return "redirect:/admin/category";
    }
}
