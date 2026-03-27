package org.example.board_cafe_kiosk_2603.repository.admin.product;

import org.apache.ibatis.annotations.Mapper;
import org.example.board_cafe_kiosk_2603.domain.admin.product.Category;
import org.example.board_cafe_kiosk_2603.domain.admin.product.CategoryType;

import java.util.List;
import java.util.Optional;

/**
 * category 테이블 CRUD MyBatis Mapper 인터페이스
 */
@Mapper
public interface CategoryMapper {

    /** 카테고리 전체 목록 조회 */
    List<Category> findAll();

    /** type 기준 카테고리 목록 조회 (DRINK / FOOD / GAME / GUEST) */
    List<Category> findByType(CategoryType type);

    /** PK로 카테고리 단건 조회 */
    Optional<Category> findById(int id);

    /** 카테고리 등록 */
    int insert(Category category);

    /** 카테고리 수정 */
    int update(Category category);

    /** 카테고리 삭제 */
    int delete(int id);
}
