package org.example.board_cafe_kiosk_2603.admin.product.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.board_cafe_kiosk_2603.admin.product.model.Game;
import org.example.board_cafe_kiosk_2603.admin.product.dto.GameResponseDTO;
import org.example.board_cafe_kiosk_2603.common.pagination.PageRequestDTO;

import java.util.List;
import java.util.Optional;

@Mapper
public interface GameMapper {

    List<GameResponseDTO> findAll();

    /**
     * 카테고리 ID 기준 조회합니다.
     *
     * @param categoryId 전달받은 categoryId 값
     * @return 처리 결과
     */
    List<GameResponseDTO> findByCategoryId(int categoryId);

    List<GameResponseDTO> findByIsActive(boolean isActive);

    Optional<GameResponseDTO> findById(int id);

    List<GameResponseDTO> findByNames(@Param("names") List<String> names);

    int insert(Game game);

    int update(Game game);

    int delete(int id);

    int toggleActive(int id);

    List<GameResponseDTO> findAllPaged(PageRequestDTO pageRequestDTO);

    int countAll();

    List<GameResponseDTO> findByCategoryIdPaged(@Param("categoryId") int categoryId,
                                                @Param("pageRequestDTO") PageRequestDTO pageRequestDTO);

    List<GameResponseDTO> findByIsActivePaged(@Param("isActive") boolean isActive,
                                              @Param("categoryId") Integer categoryId,
                                              @Param("pageRequestDTO") PageRequestDTO pageRequestDTO);

    int countByIsActive(@Param("isActive") boolean isActive,
                        @Param("categoryId") Integer categoryId);

    int countByCategoryId(int categoryId);
}