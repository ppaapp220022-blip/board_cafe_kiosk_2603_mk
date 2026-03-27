package org.example.board_cafe_kiosk_2603.dto.admin.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameRequestDTO {
    /** FK → category.id */
    private Integer categoryId;

    private String name;
    private Integer minPlayers;
    private Integer maxPlayers;

    /** 평균 플레이 시간 (분) */
    private Integer playTime;

    /** 활성 여부 */
    private boolean isActive;
}
