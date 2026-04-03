package org.example.board_cafe_kiosk_2603.service.admin.policy;

import org.example.board_cafe_kiosk_2603.dto.admin.policy.PolicyDTO;

import java.util.List;

public interface PolicyService {

    // 전체 패키지 조회 (페이징 + 필터)
    List<PolicyDTO> getAllPolicies(int page, int size, String filter);

    // 전체 개수
    int countAll(String filter);

    // ID로 단건 조회
    PolicyDTO getById(int id);

    // 패키지 등록
    void insert(PolicyDTO policyDTO);

    // 패키지 수정
    void update(PolicyDTO policyDTO);

    // 활성/비활성 토글
    void updateStatus(int id, boolean active);
}
