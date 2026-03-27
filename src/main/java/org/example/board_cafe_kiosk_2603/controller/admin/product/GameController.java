package org.example.board_cafe_kiosk_2603.controller.admin.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.dto.admin.product.CategoryResponseDTO;
import org.example.board_cafe_kiosk_2603.dto.admin.product.GameRequestDTO;
import org.example.board_cafe_kiosk_2603.dto.admin.product.GameResponseDTO;
import org.example.board_cafe_kiosk_2603.service.admin.product.CategoryService;
import org.example.board_cafe_kiosk_2603.service.admin.product.GameService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 게임 CRUD 컨트롤러 (Admin 전용)
 * 기본 경로: /admin/product/game
 */
@Log4j2
@Controller
@RequestMapping("/admin/product/game")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final CategoryService categoryService;

    /**
     * 게임 전체 목록 조회 → 뷰 반환
     * GET /admin/product/game
     */
    @GetMapping
    public String getAll(Model model) {
        log.debug("GET /admin/product/game 요청");
        List<GameResponseDTO> gameList = gameService.getAll();
        model.addAttribute("gameList", gameList);
        model.addAttribute("activePage", "productReg");
        model.addAttribute("activeTab", "game");
        log.debug("게임 목록 조회 완료 - 건수: {}", gameList.size());
        return "admin/product_game";
    }

    /**
     * 게임 등록 폼 페이지
     * GET /admin/product/game/add
     */
    @GetMapping("/add")
    public String addForm(Model model) {
        log.debug("GET /admin/product/game/add 요청");
        List<CategoryResponseDTO> categoryList = categoryService.getAll();
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("activePage", "productReg");
        return "admin/product_game_form";
    }

    /**
     * 게임 등록 처리
     * POST /admin/product/game/add
     */
    @PostMapping("/add")
    public String register(@ModelAttribute GameRequestDTO dto) {
        log.debug("POST /admin/product/game/add 요청 - dto: {}", dto);
        gameService.register(dto);
        log.debug("게임 등록 완료");
        return "redirect:/admin/product/game";
    }

    /**
     * 게임 수정 폼 페이지
     * GET /admin/product/game/edit/{id}
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable int id, Model model) {
        log.debug("GET /admin/product/game/edit/{} 요청", id);
        GameResponseDTO game = gameService.getById(id);
        List<CategoryResponseDTO> categoryList = categoryService.getAll();
        model.addAttribute("game", game);
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("activePage", "productReg");
        return "admin/product_game_form";
    }

    /**
     * 게임 수정 처리
     * POST /admin/product/game/edit/{id}
     */
    @PostMapping("/edit/{id}")
    public String modify(@PathVariable int id, @ModelAttribute GameRequestDTO dto) {
        log.debug("POST /admin/product/game/edit/{} 요청 - dto: {}", id, dto);
        gameService.modify(id, dto);
        log.debug("게임 수정 완료 - id: {}", id);
        return "redirect:/admin/product/game";
    }

    /**
     * 게임 삭제 처리
     * POST /admin/product/game/delete/{id}
     */
    @PostMapping("/delete/{id}")
    public String remove(@PathVariable int id) {
        log.debug("POST /admin/product/game/delete/{} 요청", id);
        gameService.remove(id);
        log.debug("게임 삭제 완료 - id: {}", id);
        return "redirect:/admin/product/game";
    }

    /**
     * 게임 활성 상태 토글
     * POST /admin/product/game/{id}/toggle-active
     */
    @PostMapping("/{id}/toggle-active")
    public String toggleActive(@PathVariable int id) {
        log.debug("POST /admin/product/game/{}/toggle-active 요청", id);
        gameService.toggleActive(id);
        log.debug("게임 활성 상태 토글 완료 - id: {}", id);
        return "redirect:/admin/product/game";
    }
}