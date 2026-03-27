package org.example.board_cafe_kiosk_2603.domain.admin.product;

import lombok.*;

/**
 * game_item 테이블과 1:1 매핑되는 도메인(VO) 클래스
 * 보드게임의 실물 재고 단위를 관리하며, game 테이블을 FK로 참조
 */
@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameItem {
    private int id;

    /** FK → game.id (ON DELETE CASCADE) */
    private int gameId;

    /** 실물 시리얼 번호 (UNIQUE) */
    private String serialNumber;

    /** NORMAL / RENTED / DAMAGED / LOST */
    private GameItemStatus status;

}
