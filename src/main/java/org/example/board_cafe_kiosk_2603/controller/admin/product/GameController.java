package org.example.board_cafe_kiosk_2603.controller.admin.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.admin.product.CategoryType;
import org.example.board_cafe_kiosk_2603.dto.admin.product.*;
import org.example.board_cafe_kiosk_2603.service.admin.product.CategoryService;
import org.example.board_cafe_kiosk_2603.service.admin.product.GameItemService;
import org.example.board_cafe_kiosk_2603.service.admin.product.GameService;
import org.example.board_cafe_kiosk_2603.util.FileUploadUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final GameItemService gameItemService;
    private final FileUploadUtil fileUploadUtil;

    /**
     * 게임 전체 목록 조회 → 뷰 반환
     * GET /admin/product/game
     */
    // 카테고리 필터
    // GET /admin/product/game?categoryId=5
    @GetMapping
    public String getAll(@RequestParam(required = false) Integer categoryId, Model model) {
        log.debug("GET /admin/product/game 요청 - categoryId: {}", categoryId);

        // 카테고리 필터 적용: categoryId 있으면 해당 카테고리만, 없으면 전체
        List<GameResponseDTO> gameList = (categoryId != null)
                ? gameService.getByCategoryId(categoryId)
                : gameService.getAll();

        // 게임 탭에서 사용할 카테고리 목록 (GAME 타입만)
        List<CategoryResponseDTO> categoryList = categoryService.getByType(CategoryType.GAME);

        model.addAttribute("gameList", gameList);
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("selectedCategoryId", categoryId);   // 뷰에서 활성 버튼 판단에 사용
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
        List<CategoryResponseDTO> categoryList = categoryService.getByType(CategoryType.GAME);
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("activePage", "productReg");
        return "admin/product_game_form";
    }

    /**
     * 게임 등록 처리
     * POST /admin/product/game/add
     */
    // 이미지 업로드 + 신규 game_item 등록
    @PostMapping("/add")
    public String register(@ModelAttribute GameRequestDTO gameRequestDTO,
                           @RequestParam(value = "imageFile", required = false) MultipartFile imageFile)
            throws IOException {
        log.debug("POST /admin/product/game/add 요청 - gameRequestDTO: {}", gameRequestDTO);

        // 이미지 저장
        // 새 이미지 파일이 있으면 저장 후 imageUrl 세팅
        // 없으면 hidden name="imageUrl"로 넘어온 기존 값 유지
        if (imageFile != null && !imageFile.isEmpty()) {
            gameRequestDTO.setImageUrl(fileUploadUtil.save(imageFile));
            log.debug("게임 이미지 저장 완료: {}", gameRequestDTO.getImageUrl());
        }

        // 게임 등록 (insert 후 gameRequestDTO.getId()에 생성된 PK 세팅됨)
        // 생성된 PK 반환
        int gameId = gameService.register(gameRequestDTO);  // game 등록 후 PK 반환
        log.debug("게임 등록 완료 - gameId: {}", gameId);

        // 신규 game_item 등록
        if (gameRequestDTO.getNewItems() != null) {
            for (GameItemRequestDTO item : gameRequestDTO.getNewItems()) {
                if (item.getSerialNumber() != null && !item.getSerialNumber().isEmpty()) {
                    item.setGameId(gameId);  // gameId 세팅
                    gameItemService.register(item);
                    log.debug("game_item 등록 완료 - serialNumber: {}", item.getSerialNumber());
                }
            }
        }

        return "redirect:/admin/product/game";
    }

    /**
     * 게임 수정 폼 페이지
     * GET /admin/product/game/edit/{id}
     */
    // 기존 이미지 및 game_item 목록 포함
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable int id, Model model) {
        log.debug("GET /admin/product/game/edit/{} 요청", id);

        GameResponseDTO game = gameService.getById(id);
        List<CategoryResponseDTO> categoryList = categoryService.getByType(CategoryType.GAME);
        List<GameItemResponseDTO> gameItemList = gameItemService.getByGameId(id);

        model.addAttribute("game", game);
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("gameItemList", gameItemList);
        model.addAttribute("activePage", "productReg");

        log.info("게임 수정 폼 조회 완료 - gameId: {}, 재고수: {}", id, gameItemList.size());
        return "admin/product_game_form";
    }

    /**
     * 게임 수정 처리
     * POST /admin/product/game/edit/{id}
     */
    // 이미지 교체 + game_item 수정/추가/삭제
    @PostMapping("/edit/{id}")
    public String modify(@PathVariable int id,
                         @ModelAttribute GameRequestDTO gameRequestDTO,
                         @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                         @RequestParam(value = "deletedItemIds", required = false, defaultValue = "") String deletedItemIds)
            throws IOException {
        log.debug("POST /admin/product/game/edit/{} 요청 - gameRequestDTO: {}", id, gameRequestDTO);

        // 새 이미지 업로드 시 기존 이미지 삭제 후 교체
        // 없으면 hidden name="imageUrl"로 넘어온 기존 값이 dto에 자동 바인딩
        if (imageFile != null && !imageFile.isEmpty()) {
            // 기존 이미지 삭제 후 새 이미지 저장
            fileUploadUtil.delete(gameService.getById(id).getImageUrl());
            gameRequestDTO.setImageUrl(fileUploadUtil.save(imageFile));
            log.debug("게임 이미지 교체 완료: {}", gameRequestDTO.getImageUrl());
        }  // 새파일 없음 → hidden name="imageUrl" 값이 DTO에 이미 바인딩되어 있으므로 그대로 사용

//        String imageUrl = fileUploadUtil.save(imageFile);
//        if (imageUrl != null) {
//            gameRequestDTO.setImageUrl(imageUrl);
//        }

        // 게임 기본 정보 수정
        gameService.modify(id, gameRequestDTO);
        log.debug("게임 수정 완료 - id: {}", id);

        // 기존 game_item 수정
        if (gameRequestDTO.getItems() != null) {
            for (GameItemRequestDTO item : gameRequestDTO.getItems()) {
                if (item.getId() != 0) {
                    gameItemService.modify(item.getId(), item);
                    log.debug("game_item 수정 완료 - itemId: {}", item.getId());
                }
            }
        }

        // 신규 game_item 등록
        if (gameRequestDTO.getNewItems() != null) {
            for (GameItemRequestDTO newItem : gameRequestDTO.getNewItems()) {
                if (newItem.getSerialNumber() != null && !newItem.getSerialNumber().isEmpty()) {
                    newItem.setGameId(id);
                    gameItemService.register(newItem);
                    log.debug("game_item 신규 등록 완료 - serialNumber: {}", newItem.getSerialNumber());
                }
            }
        }

        // game_item 삭제 (콤마 구분 id 목록)
        if (!deletedItemIds.isEmpty()) {
            for (String itemIdStr : deletedItemIds.split(",")) {
                int itemId = Integer.parseInt(itemIdStr.trim());
                gameItemService.remove(itemId);
                log.debug("game_item 삭제 완료 - itemId: {}", itemId);
            }
        }

        return "redirect:/admin/product/game";
    }

    /**
     * 게임 삭제 처리
     * POST /admin/product/game/delete/{id}
     */
    // 이미지 파일도 함께 삭제
    @PostMapping("/delete/{id}")
    public String remove(@PathVariable int id) {
        log.debug("POST /admin/product/game/delete/{} 요청", id);
        fileUploadUtil.delete(gameService.getById(id).getImageUrl());
        gameService.remove(id);
        log.debug("게임 삭제 완료 - id: {}", id);
        return "redirect:/admin/product/game";
    }

    /**
     * 게임 활성 상태 토글
     * POST /admin/product/game/{id}/toggle-active
     */
    // 카테고리 필터 유지하며 리다리엑트
    @PostMapping("/{id}/toggle-active")
    public String toggleActive(@PathVariable int id,
                               @RequestParam(required = false) Integer categoryId) {
        log.debug("POST /admin/product/game/{}/toggle-active 요청", id);
        gameService.toggleActive(id);
        log.debug("게임 활성 상태 토글 완료 - id: {}", id);
//        return "redirect:/admin/product/game";
        return categoryId != null
                ? "redirect:/admin/product/game?categoryId=" + categoryId
                : "redirect:/admin/product/game";
    }
}