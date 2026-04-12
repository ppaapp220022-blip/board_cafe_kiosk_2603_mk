package org.example.board_cafe_kiosk_2603.dto.admin.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopItemDTO {
    private String itemName;      // 상품명
    private int totalQuantity;    // 총 판매 수량
}
