package org.example.board_cafe_kiosk_2603.domain.kiosk;

import lombok.*;

@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Game {
    private int     id;
    private Integer categoryId;
    private String  name;
    private Integer minPlayers;
    private Integer maxPlayers;
    private Integer playTime;
    private boolean isActive;

    // game_item 중 NORMAL 상태 수 (재고, JOIN으로 계산)
    private int stock;
}
