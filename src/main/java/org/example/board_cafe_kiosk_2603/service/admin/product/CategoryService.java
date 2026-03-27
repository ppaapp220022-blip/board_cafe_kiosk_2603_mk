package org.example.board_cafe_kiosk_2603.service.admin.product;

import org.example.board_cafe_kiosk_2603.domain.admin.product.CategoryType;
import org.example.board_cafe_kiosk_2603.dto.admin.product.CategoryRequestDTO;
import org.example.board_cafe_kiosk_2603.dto.admin.product.CategoryResponseDTO;

import java.util.List;

public interface CategoryService {
    /** 카테고리 전체 목록 반환 */
    List<CategoryResponseDTO> getAll();

    /** type 기준 카테고리 목록 반환 */
    List<CategoryResponseDTO> getByType(CategoryType type);

    /** PK로 카테고리 단건 반환 */
    CategoryResponseDTO getById(int id);

    /** 카테고리 등록 */
    void register(CategoryRequestDTO categoryRequestDTO);

    /** 카테고리 수정 */
    void modify(int id, CategoryRequestDTO categoryRequestDTO);

    /** 카테고리 삭제 */
    void remove(int id);
}
