package org.example.board_cafe_kiosk_2603.admin.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.board_cafe_kiosk_2603.admin.product.model.CategoryType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequestDTO {
    /* 카테고리 등록, 수정 요청 DTO */
    private String name;
    private CategoryType type;  // Enum
}
