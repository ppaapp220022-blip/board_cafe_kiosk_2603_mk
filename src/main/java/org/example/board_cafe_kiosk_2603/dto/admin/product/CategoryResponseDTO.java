package org.example.board_cafe_kiosk_2603.dto.admin.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.board_cafe_kiosk_2603.domain.admin.product.CategoryType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponseDTO {
    /* 카테고리 조회 응답 DTO */
    private int id;
    private String name;
    private CategoryType type;
}
