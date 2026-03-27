package org.example.board_cafe_kiosk_2603.repository.admin.product;

import org.apache.ibatis.annotations.Mapper;
import org.example.board_cafe_kiosk_2603.domain.admin.product.GameItem;
import org.example.board_cafe_kiosk_2603.domain.admin.product.GameItemStatus;
import org.example.board_cafe_kiosk_2603.dto.admin.product.GameItemResponseDTO;

import java.util.List;
import java.util.Optional;

/**
 * game_item 테이블 CRUD MyBatis Mapper 인터페이스
 */
@Mapper
public interface GameItemMapper {

    /** 전체 게임 아이템 목록 조회 (game JOIN 포함) */
    List<GameItemResponseDTO> findAll();

    /** game_id 기준 게임 아이템 목록 조회 */
    List<GameItemResponseDTO> findByGameId(int gameId);

    /** status 기준 게임 아이템 목록 조회 */
    List<GameItemResponseDTO> findByStatus(GameItemStatus gameItemStatus);

    /** PK로 게임 아이템 단건 조회 (game JOIN 포함) */
    Optional<GameItemResponseDTO> findById(int id);

    /** 게임 아이템 등록 */
    int insert(GameItem gameItem);

    /** 게임 아이템 수정 (시리얼 번호·상태 변경) */
    int update(GameItem gameItem);

    /** 게임 아이템 삭제 */
    int delete(int id);

    /** 게임 아이템 상태 변경 */
    int updateStatus(int id, GameItemStatus gameItemStatus);
}
