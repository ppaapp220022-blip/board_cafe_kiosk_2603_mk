package org.example.board_cafe_kiosk_2603.mapper.admin.manager;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.board_cafe_kiosk_2603.domain.admin.manager.Manager;
import org.example.board_cafe_kiosk_2603.dto.common.pagenation.PageRequestDTO;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ManagerMapper {

    // 로그인 ID로 단건 조회 (Security용)z
    Optional<Manager> findByLoginId(String loginId);

    // 직원 등록
    void insert(Manager manager);

    // 활성/비활성 토글
    void updateActive(@Param("id") int id, @Param("isActive") boolean isActive);

    // 내 정보 수정 (이름, 비밀번호 업데이트)
    void updateProfileInfo(@Param("loginId") String loginId,
                           @Param("name") String name,
                           @Param("password") String password);

    /*============= 페이징 =============== */
    // 페이징 목록 조회
    List<Manager> selectList(PageRequestDTO pageRequestDTO);

    // 페이징 전체 개수 조회
    int selectCount(PageRequestDTO pageRequestDTO);
}
