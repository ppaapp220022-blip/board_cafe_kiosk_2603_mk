package org.example.board_cafe_kiosk_2603.service.admin.product;

import org.example.board_cafe_kiosk_2603.domain.admin.product.GameItemStatus;
import org.example.board_cafe_kiosk_2603.dto.admin.product.GameItemRequestDTO;
import org.example.board_cafe_kiosk_2603.dto.admin.product.GameItemResponseDTO;

import java.util.List;

/**
 * GameItem 비즈니스 로직 인터페이스
 */
public interface GameItemService {
    /** 전체 게임 아이템 목록 반환 */
    List<GameItemResponseDTO> getAll();

    /** game_id 기준 게임 아이템 목록 반환 */
    List<GameItemResponseDTO> getByGameId(int gameId);

    /** status 기준 게임 아이템 목록 반환 */
    List<GameItemResponseDTO> getByStatus(GameItemStatus gameItemStatus);

    /** PK로 게임 아이템 단건 반환 */
    GameItemResponseDTO getById(int id);

    /** 게임 아이템 등록 */
    void register(GameItemRequestDTO gameItemRequestDTO);

    /** 게임 아이템 수정 */
    void modify(int id, GameItemRequestDTO gameItemRequestDTO);

    /** 게임 아이템 삭제 */
    void remove(int id);

    /** 게임 아이템 상태 변경 */
    void changeStatus(int id, GameItemStatus gameItemStatus);
}
