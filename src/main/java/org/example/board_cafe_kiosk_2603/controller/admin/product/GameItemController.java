package org.example.board_cafe_kiosk_2603.controller.admin.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.admin.product.GameItemStatus;
import org.example.board_cafe_kiosk_2603.dto.admin.product.GameItemRequestDTO;
import org.example.board_cafe_kiosk_2603.dto.admin.product.GameItemResponseDTO;
import org.example.board_cafe_kiosk_2603.dto.admin.product.GameResponseDTO;
import org.example.board_cafe_kiosk_2603.service.admin.product.GameItemService;
import org.example.board_cafe_kiosk_2603.service.admin.product.GameService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 게임 아이템(재고) CRUD 컨트롤러 (Admin 전용)
 * 기본 경로: /admin/product/game-items
 */
@Log4j2
@Controller
@RequestMapping("/admin/product/game-items")
@RequiredArgsConstructor
public class GameItemController {

    private final GameItemService gameItemService;
    private final GameService gameService;

    /**
     * 게임 아이템 전체 목록 조회 → 뷰 반환
     * GET /admin/product/game-items
     */
    @GetMapping
    public String getAll(Model model) {
        log.debug("GET /admin/product/game-items 요청");
        List<GameItemResponseDTO> gameItemList = gameItemService.getAll();
        model.addAttribute("gameItemList", gameItemList);
        model.addAttribute("activePage", "productReg");
        log.debug("게임 아이템 목록 조회 완료 - 건수: {}", gameItemList.size());
        return "admin/product_game_item";
    }

    /**
     * 게임 아이템 등록 폼 페이지
     * GET /admin/product/game-items/add
     */
    @GetMapping("/add")
    public String addForm(Model model) {
        log.debug("GET /admin/product/game-items/add 요청");
        List<GameResponseDTO> gameList = gameService.getAll();
        model.addAttribute("gameList", gameList);
        model.addAttribute("statusList", GameItemStatus.values());
        model.addAttribute("activePage", "productReg");
        return "admin/product_game_item_form";
    }

    /**
     * 게임 아이템 등록 처리
     * POST /admin/product/game-items/add
     */
    @PostMapping("/add")
    public String register(@ModelAttribute GameItemRequestDTO dto) {
        log.debug("POST /admin/product/game-items/add 요청 - dto: {}", dto);
        gameItemService.register(dto);
        log.debug("게임 아이템 등록 완료");
        return "redirect:/admin/product/game-items";
    }

    /**
     * 게임 아이템 수정 폼 페이지
     * GET /admin/product/game-items/edit/{id}
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable int id, Model model) {
        log.debug("GET /admin/product/game-items/edit/{} 요청", id);
        GameItemResponseDTO gameItem = gameItemService.getById(id);
        List<GameResponseDTO> gameList = gameService.getAll();
        model.addAttribute("gameItem", gameItem);
        model.addAttribute("gameList", gameList);
        model.addAttribute("statusList", GameItemStatus.values());
        model.addAttribute("activePage", "productReg");
        return "admin/product_game_item_form";
    }

    /**
     * 게임 아이템 수정 처리
     * POST /admin/product/game-items/edit/{id}
     */
    @PostMapping("/edit/{id}")
    public String modify(@PathVariable int id, @ModelAttribute GameItemRequestDTO dto) {
        log.debug("POST /admin/product/game-items/edit/{} 요청 - dto: {}", id, dto);
        gameItemService.modify(id, dto);
        log.debug("게임 아이템 수정 완료 - id: {}", id);
        return "redirect:/admin/product/game-items";
    }

    /**
     * 게임 아이템 삭제 처리
     * POST /admin/product/game-items/delete/{id}
     */
    @PostMapping("/delete/{id}")
    public String remove(@PathVariable int id) {
        log.debug("POST /admin/product/game-items/delete/{} 요청", id);
        gameItemService.remove(id);
        log.debug("게임 아이템 삭제 완료 - id: {}", id);
        return "redirect:/admin/product/game-items";
    }

    /**
     * 게임 아이템 상태 변경
     * POST /admin/product/game-items/{id}/status
     */
    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable int id,
                               @RequestParam GameItemStatus status) {
        log.debug("POST /admin/product/game-items/{}/status 요청 - status: {}", id, status);
        gameItemService.changeStatus(id, status);
        log.debug("게임 아이템 상태 변경 완료 - id: {}, status: {}", id, status);
        return "redirect:/admin/product/game-items";
    }
}
