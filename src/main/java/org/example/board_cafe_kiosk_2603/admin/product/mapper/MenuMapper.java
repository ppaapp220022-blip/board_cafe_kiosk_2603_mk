package org.example.board_cafe_kiosk_2603.admin.product.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.board_cafe_kiosk_2603.admin.product.model.Menu;
import org.example.board_cafe_kiosk_2603.admin.product.dto.MenuResponseDTO;
import org.example.board_cafe_kiosk_2603.common.pagination.PageRequestDTO;

import java.util.List;
import java.util.Optional;

@Mapper

/* 메뉴 상품 데이터 접근 인터페이스 */
public interface MenuMapper {

    /* 전체 목록 조회 */
    List<MenuResponseDTO> findAll();

    /* 카테고리 ID 기준 조회 */
    List<MenuResponseDTO> findByCategoryId(int categoryId);

    /* 유형 기준 조회 */
    List<MenuResponseDTO> findByType(String type);

    /* 판매 가능 여부 기준 조회 */
    List<MenuResponseDTO> findByIsAvailable(boolean isAvailable);

    /* 삭제 상태 기준 조회 */
    List<MenuResponseDTO> findByIsDeleted(boolean isDeleted);

    /* 삭제 포함 ID 단건 조회 */
    Optional<MenuResponseDTO> findByIdIncludeDeleted(int id);

    /* 메뉴 등록 */
    int insert(Menu menu);

    /* 메뉴 수정 */
    int update(Menu menu);

    /* 메뉴 소프트 삭제 처리 */
    int softDelete(int id);

    /**
     * 삭제 데이터 복구합니다.
     *
     * @param id 전달받은 id 값
     * @return 처리 결과
     */
    int restore(int id);

    /* 판매 가능 여부 전환 */
    int toggleAvailable(int id);

    /* 유형별 목록 페이징 조회 */
    List<MenuResponseDTO> findByTypePaged(@Param("type") String type,
                                          @Param("pageRequestDTO") PageRequestDTO pageRequestDTO);

    /* 유형별 건수 조회 */
    int countByType(String type);

    /* 유형 및 카테고리별 목록 페이징 조회 */
    List<MenuResponseDTO> findByTypeAndCategoryIdPaged(@Param("type") String type,
                                                       @Param("categoryId") int categoryId,
                                                       @Param("pageRequestDTO") PageRequestDTO pageRequestDTO);

    /* 유형 및 카테고리별 건수 조회 */
    int countByTypeAndCategoryId(@Param("type") String type, @Param("categoryId") int categoryId);

    /* 삭제 상태별 목록 페이징 조회 */
    List<MenuResponseDTO> findByIsDeletedPaged(@Param("isDeleted") boolean isDeleted,
                                               @Param("pageRequestDTO") PageRequestDTO pageRequestDTO);

    /* 삭제 상태별 건수 조회 */
    int countByIsDeleted(boolean isDeleted);

    /* 삭제 상태 및 카테고리별 목록 페이징 조회 */
    List<MenuResponseDTO> findByIsDeletedAndCategoryIdPaged(@Param("isDeleted") boolean isDeleted,
                                                            @Param("categoryId") int categoryId,
                                                            @Param("pageRequestDTO") PageRequestDTO pageRequestDTO);

    /* 삭제 상태 및 카테고리별 건수 조회 */
    int countByIsDeletedAndCategoryId(@Param("isDeleted") boolean isDeleted,
                                      @Param("categoryId") int categoryId);

    /**
     * 게임 메뉴가 없으면 등록합니다.
     *
     * @param categoryId 전달받은 categoryId 값
     * @param name 전달받은 name 값
     * @param description 전달받은 description 값
     * @return 처리 결과
     */
    int insertGameMenuIfNotExists(@Param("categoryId") Integer categoryId,
                                  @Param("name") String name,
                                  @Param("description") String description);

    /**
     * 게임 메뉴 설명 수정합니다.
     *
     * @param name 전달받은 name 값
     * @param description 전달받은 description 값
     * @return 처리 결과
     */
    int updateGameMenuDescriptionByName(@Param("name") String name,
                                        @Param("description") String description);

    /**
     * 게임 메뉴명 변경합니다.
     *
     * @param oldName 전달받은 oldName 값
     * @param newName 전달받은 newName 값
     * @return 처리 결과
     */
    int renameGameMenuName(@Param("oldName") String oldName,
                           @Param("newName") String newName);

    /* 게임명으로 메뉴 ID 조회 */
    Integer findMenuIdByGameName(String gameName);

    /* 게임 ID로 메뉴 ID 조회 */
    Integer findMenuIdByGameId(int gameId);
}
