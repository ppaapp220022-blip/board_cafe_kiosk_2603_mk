//package org.example.board_cafe_kiosk_2603.mapper.admin.manager;
//
//import lombok.extern.log4j.Log4j2;
//import org.example.board_cafe_kiosk_2603.domain.admin.manager.Manager;
//import org.example.board_cafe_kiosk_2603.domain.admin.manager.RoleType;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@Log4j2
//@SpringBootTest
//class ManagerMapperTest {
//
//    @Autowired
//    private ManagerMapper managerMapper;
//
//    @Test
//    void insertTest() {
//        String loginId = "test0002";
//        Manager manager = Manager.builder()
//                .loginId(loginId)
//                .password("2222")
//                .name("테스터_02")
//                .role(RoleType.STAFF)
//                .isActive(true)
//                .createdAt(LocalDateTime.now())
//                .build();
//        managerMapper.insert(manager);
////        Manager saveManager = managerMapper.findByLoginId("test0002");
//        // Optional로 반환되므로 .orElse(null) 또는 .get() 사용
//        Manager saveManager = managerMapper.findByLoginId(loginId).orElse(null);
//
//        log.info("--- 등록 결과 확인 ---");
//
//        // 객체가 null이 아닌지 확인 (일치->true)
//        Assertions.assertNotNull(saveManager);
//        log.info("조회 성공 여부: true");
//
//        // 이름 일치 여부 확인
//        boolean isNameMatch = "테스터_02".equals(saveManager.getName());
//        log.info("이름 일치 여부: {}", isNameMatch);
//        Assertions.assertEquals("테스터_02", saveManager.getName());
//
//        // 권한 일치 여부 확인 (Role.STAFF와 비교)
//        boolean isRoleMatch = RoleType.STAFF.equals(saveManager.getRole());
//        log.info("권한 일치 여부: {}", isRoleMatch);
//        Assertions.assertEquals(RoleType.STAFF, saveManager.getRole());
//    }
//
//    @Test
//    void findAllTest() {
//        List<Manager> result = managerMapper.findAll();
//
//        log.info("--- 전체 목록 조회 결과 확인 ---");
//
//        // 1. 개별 객체 정보를 하나씩 로그로 출력 (추천)
//        result.forEach(manager -> log.info("조회된 관리자: {}", manager));
//
//        // 2. 리스트 요약 정보 확인
//        boolean isListNotEmpty = !result.isEmpty();
//        log.info("목록이 비어있지 않음: {}", isListNotEmpty);
//        log.info("현재 총 관리자 수: {}", result.size());
//
//        Assertions.assertTrue(isListNotEmpty);
//    }
//
//    @Test
//    void findByLoginIdTest() {
//        String targetId = "test0002";
//
//        // 1. Optional 자체를 받아옴
//        Optional<Manager> foundOptional = managerMapper.findByLoginId(targetId);
//
//        // 2. 검증 (Optional이 값을 가지고 있는지 확인)
//        log.info("--- 단건 조회 결과 확인 ---");
//        boolean isPresent = foundOptional.isPresent();
//        log.info("데이터 존재 여부: {}", isPresent);
//
//        Assertions.assertTrue(isPresent, "데이터를 찾지 못했습니다.");
//
//        // 3. 값 꺼내서 비교
//        foundOptional.ifPresent(found -> {
//            log.info("조회된 이름: {}", found.getName());
//            Assertions.assertEquals(targetId, found.getLoginId());
//        });
//    }
//
//    @Test
//    void updateActiveTest() {
//        // 1. 기존 데이터 조회 (Optional에서 꺼내기)
//        Manager saved = managerMapper.findByLoginId("test0002")
//                .orElseThrow(() -> new RuntimeException("테스트 데이터를 찾을 수 없습니다."));
//
//        int targetId = saved.getId();
//        log.info("대상 ID: {}, 현재 상태: {}", targetId, saved.isActive());
//
//        // 2. 상태 변경 실행
//        managerMapper.updateActive(targetId, false);
//        log.info("--- 상태 업데이트(false) 실행 ---");
//
//        // 3. 재조회 및 검증
//        Manager updated = managerMapper.findByLoginId("test0002")
//                .orElseThrow(() -> new RuntimeException("업데이트 후 데이터를 찾을 수 없습니다."));
//
//        log.info("--- 변경 결과 확인 ---");
//        log.info("변경 후 isActive 값: {}", updated.isActive());
//
//        Assertions.assertFalse(updated.isActive(), "isActive 상태가 false여야 합니다.");
//    }
//}