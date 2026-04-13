package org.example.board_cafe_kiosk_2603.mapper.admin.product;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.board_cafe_kiosk_2603.domain.admin.product.Menu;
import org.example.board_cafe_kiosk_2603.dto.admin.product.MenuResponseDTO;
import org.example.board_cafe_kiosk_2603.dto.common.pagenation.PageRequestDTO;

import java.util.List;
import java.util.Optional;

/**
 * menu 테이블 CRUD MyBatis Mapper 인터페이스
 */
@Mapper
public interface MenuMapper {
    /** 전체 메뉴 목록 조회 (소프트 삭제 제외, category JOIN 포함) */
    List<MenuResponseDTO> findAll();

    /** category_id 기준 메뉴 목록 조회 */
    List<MenuResponseDTO> findByCategoryId(int categoryId);

    /** category type 기준 메뉴 목록 조회 (FOOD / DRINK / GUEST) */
    List<MenuResponseDTO> findByType(String type);

    /** 판매 가능 여부 기준 메뉴 목록 조회 */
    List<MenuResponseDTO> findByIsAvailable(boolean isAvailable);

    /** 소프트 삭제 여부 기준 메뉴 목록 조회 (숨김 탭용) */
    List<MenuResponseDTO> findByIsDeleted(boolean isDeleted);

    /** PK로 메뉴 단건 조회 (category JOIN 포함) */
//    Optional<MenuResponseDTO> findById(int id);
    /** PK로 메뉴 단건 조회 (삭제 여부 무관) */
    Optional<MenuResponseDTO> findByIdIncludeDeleted(int id);

    /** 메뉴 등록 */
    int insert(Menu menu);

    /** 메뉴 수정 */
    int update(Menu menu);

    /** 메뉴 소프트 삭제 (is_deleted = true) */
    int softDelete(int id);

    /** 메뉴 복원 (is_deleted = false) */
    int restore(int id);

    /** 메뉴 판매 상태 토글 (is_available 반전) */
    int toggleAvailable(int id);

    /* =======페이지========= */
    /** category type 기준 메뉴 목록 조회 - 페이징 */
    List<MenuResponseDTO> findByTypePaged(@Param("type") String type,
                                          @Param("pageRequestDTO") PageRequestDTO pageRequestDTO);

    /** category type 기준 메뉴 수 */
    int countByType(String type);

    /** category type + category_id 기준 메뉴 목록 조회 - 페이징 */
    List<MenuResponseDTO> findByTypeAndCategoryIdPaged(@Param("type") String type,
                                                       @Param("categoryId") int categoryId,
                                                       @Param("pageRequestDTO") PageRequestDTO pageRequestDTO);

    /** category type + category_id 기준 메뉴 수 */
    int countByTypeAndCategoryId(@Param("type") String type, @Param("categoryId") int categoryId);

    /** 소프트 삭제 여부 기준 메뉴 목록 조회 - 페이징 (숨김 탭용) */
    List<MenuResponseDTO> findByIsDeletedPaged(@Param("isDeleted") boolean isDeleted,
                                               @Param("pageRequestDTO") PageRequestDTO pageRequestDTO);

    /** 소프트 삭제 여부 기준 메뉴 수 */
    int countByIsDeleted(boolean isDeleted);
}
