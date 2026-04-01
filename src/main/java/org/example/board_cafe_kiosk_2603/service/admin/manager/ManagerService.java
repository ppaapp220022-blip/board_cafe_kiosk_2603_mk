package org.example.board_cafe_kiosk_2603.service.admin.manager;

import org.example.board_cafe_kiosk_2603.dto.admin.manager.ManagerRequest;
import org.example.board_cafe_kiosk_2603.dto.admin.manager.ManagerResponse;

import java.util.List;
import java.util.Optional;

public interface ManagerService {

    // 전체 목록 조회
    List<ManagerResponse> findAll();

    // 직원 등록
    void createManager(ManagerRequest request);

    // 활성/비활성 토글
    void updateActive(int id, boolean isActive);

    // 아이디로 직원 조회 (중복 확인용 -> 중복 여부만 알수있음)
    boolean isLoginIdDuplicate(String loginId);

    // profile
    // 프로필 조회용
    Optional<ManagerResponse> findByLoginId(String loginId);
    // 프로필 수정용
    void updateProfile(String loginId, ManagerRequest request);
}
