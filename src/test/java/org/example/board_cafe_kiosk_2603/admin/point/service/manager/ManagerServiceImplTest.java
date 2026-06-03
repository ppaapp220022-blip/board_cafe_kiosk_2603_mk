//package org.example.board_cafe_kiosk_2603.admin.manager.service;
//
//import lombok.extern.log4j.Log4j2;
//import org.example.board_cafe_kiosk_2603.admin.manager.model.RoleType;
//import org.example.board_cafe_kiosk_2603.admin.manager.dto.ManagerRequest;
//import org.example.board_cafe_kiosk_2603.admin.manager.dto.ManagerResponse;
//import org.example.board_cafe_kiosk_2603.common.pagination.PageRequestDTO;
//import org.example.board_cafe_kiosk_2603.common.pagination.PageResponseDTO;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.transaction.annotation.Transactional;
//
//@Log4j2
//@SpringBootTest
//@Transactional
//class ManagerServiceImplTest {
//
//    @Autowired
//    private ManagerService managerService;
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @Test
//    void CreateAndFindAll() {
//        String loginId = "svc_" + System.currentTimeMillis();
//        // 1. 준비: 등록할 정보 생성
//        ManagerRequest request = ManagerRequest.builder()
//                .loginId(loginId)
//                .password(passwordEncoder.encode("1234"))
//                .name("서비스테스터")
//                .email(loginId + "@test.com")
//                .role(RoleType.ADMIN)
//                .build();
//
//        // 2. 실행: 서비스의 등록 메서드 호출
//        managerService.createManager(request);
//        log.info("--- 매니저 등록 서비스 실행 완료 ---");
//
//        // 3. 페이징 목록 조회
//        PageRequestDTO pageRequestDTO = PageRequestDTO.builder()
//                .page(1)
//                .size(10)
//                .build();
//
//        PageResponseDTO<ManagerResponse> responseDTO = managerService.getPagedManagers(pageRequestDTO);
//        log.info("--- 페이징 목록 조회 완료 (전체: {}) ---", responseDTO.getTotal());
//
//        // 4. 검증: 방금 넣은 데이터가 리스트에 있는지 확인
//        boolean exists = responseDTO.getDtoList().stream()
//                .anyMatch(m -> m.getLoginId().equals(loginId));
//
//        log.info("등록된 아이디 존재 여부: {}", exists);
//        Assertions.assertTrue(exists, "등록한 매니저가 목록에 존재해야 합니다.");
//    }
//
//    @Test
//    void updateActive() {
//        String loginId = "active_test_" + System.currentTimeMillis();
//        // 1. 우선 하나 등록 (ID를 알기 위해 목록 조회를 활용)
//        managerService.createManager(ManagerRequest.builder()
//                .loginId(loginId)
//                .password("1111")
//                .name("상태변경자")
//                .email(loginId + "@test.com")
//                .role(RoleType.ADMIN)
//                .build());
//
//        // 2. 목록 조회해서 대상 ID 가져오기
//        PageRequestDTO pageRequestDTO = PageRequestDTO.builder()
//                .page(1)
//                .size(50)
//                .build();
//
//        PageResponseDTO<ManagerResponse> responseDTO = managerService.getPagedManagers(pageRequestDTO);
//        ManagerResponse target = responseDTO.getDtoList().stream()
//                .filter(m -> loginId.equals(m.getLoginId()))
//                .findFirst()
//                .orElseThrow(() -> new RuntimeException("등록한 직원을 찾을 수 없습니다."));
//        int targetId = target.getId();
//        log.info("변경 대상 ID: {}, 현재 상태: {}", targetId, target.getIsActive());
//
//        // 3. 실행: 상태를 비활성(false)으로 변경
//        managerService.updateActive(targetId, false);
//        log.info("--- 상태 변경(false) 서비스 호출 ---");
//
//        // 4. 검증: 다시 조회해서 상태 확인
//        PageResponseDTO<ManagerResponse> updatedResponseDTO = managerService.getPagedManagers(pageRequestDTO);
//        ManagerResponse updated = updatedResponseDTO.getDtoList().stream()
//                .filter(m -> m.getId() == targetId)
//                .findFirst()
//                .orElseThrow(() -> new RuntimeException("변경된 직원을 찾을 수 없습니다."));
//
//        log.info("변경 후 상태: {}", updated.getIsActive());
//        Assertions.assertFalse(updated.getIsActive());
//    }
//}
