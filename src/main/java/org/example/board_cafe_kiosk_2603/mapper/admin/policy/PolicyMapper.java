package org.example.board_cafe_kiosk_2603.mapper.admin.policy;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.board_cafe_kiosk_2603.domain.admin.policy.Policy;

import java.util.List;

@Mapper
public interface PolicyMapper {
    // 전체 패키지 조회 (페이징 + 필터)
    List<Policy> findAll(@Param("offset") int offset,
                         @Param("limit") int limit,
                         @Param("filter") String filter);

    // 전체 개수 (필터)
    int countAll(@Param("filter") String filter);

    // ID로 단건 조회
    Policy findById(int id);

    // 패키지 등록
    void insert(Policy policy);

    // 패키지 수정
    void update(Policy policy);

    // 활성/비활성 토글
    void updateStatus(@Param("id") int id, @Param("active") boolean active);
}
