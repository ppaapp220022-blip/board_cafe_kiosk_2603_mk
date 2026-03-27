package org.example.board_cafe_kiosk_2603.dto.admin.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.board_cafe_kiosk_2603.domain.admin.product.GameItemStatus;

/**
 * 게임 아이템(재고) 등록·수정 요청 시 사용하는 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameItemRequestDTO {
    /** FK → game.id */
    private int gameId;

    /** 실물 시리얼 번호 */
    private String serialNumber;

    /** NORMAL / RENTED / DAMAGED / LOST */
    private GameItemStatus status;
}
