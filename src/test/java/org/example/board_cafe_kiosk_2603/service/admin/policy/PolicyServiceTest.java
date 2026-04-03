package org.example.board_cafe_kiosk_2603.service.admin.policy;

import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.dto.admin.policy.PolicyDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
@SpringBootTest
class PolicyServiceTest {
    @Autowired
    private PolicyService policyService;

    // ===== 전체 조회 (페이징 + 필터) =====
    @Test
    void testGetAllPolicies() {
        List<PolicyDTO> list = policyService.getAllPolicies(1, 8, "all"); // 1페이지, 8개
        log.info("전체 패키지 수: {}", list.size());
        list.forEach(p -> log.info("패키지: {} | {} | {}원 | 활성: {}",
                p.getName(), p.getDisplayTime(), p.getBasePrice(), p.isActive()));
        assertNotNull(list);
    }

    // ===== 활성 필터 조회 =====
    @Test
    void testGetAllPoliciesActive() {
        List<PolicyDTO> list = policyService.getAllPolicies(1, 8, "active");
        log.info("활성 패키지 수: {}", list.size());
        assertTrue(list.stream().allMatch(PolicyDTO::isActive));
    }

    // ===== 비활성 필터 조회 =====
    @Test
    void testGetAllPoliciesInactive() {
        List<PolicyDTO> list = policyService.getAllPolicies(1, 8, "inactive");
        log.info("비활성 패키지 수: {}", list.size());
        assertTrue(list.stream().noneMatch(PolicyDTO::isActive));
    }

    // ===== 전체 개수 조회 =====
    @Test
    void testCountAll() {
        int count = policyService.countAll("all");
        log.info("전체 개수: {}", count);
        assertTrue(count >= 0);
    }

    // ===== 단건 조회 =====
    @Test
    void testGetById() {
        PolicyDTO dto = policyService.getById(6);
        log.info("단건 조회: {}", dto);
        assertNotNull(dto);
    }

    // ===== 등록 =====
    @Test
    void testInsert() {
        PolicyDTO dto = PolicyDTO.builder()
                .name("서비스 테스트 패키지")
                .type("HOURLY")
                .durationMinutes(120)
                .basePrice(10000)
                .extraPricePerMin(100.0)
                .active(true)
                .build();

        policyService.insert(dto);
        log.info("등록 완료");

        // 전체 개수로 확인
        int totalCount = policyService.countAll("all");
        List<PolicyDTO> list = policyService.getAllPolicies(1, totalCount, "all");
        assertTrue(list.stream().anyMatch(p -> p.getName().equals("서비스 테스트 패키지")));
    }

    // ===== 수정 =====
    @Test
    void testUpdate() {
        PolicyDTO dto = PolicyDTO.builder()
                .id(7)
                .name("수정된 서비스 패키지")
                .type("HOURLY")
                .durationMinutes(90)
                .basePrice(7000)
                .extraPricePerMin(60.0)
                .build();

        policyService.update(dto);
        PolicyDTO updated = policyService.getById(7);
        log.info("수정 결과: {}", updated);
        assertEquals("수정된 서비스 패키지", updated.getName());
    }

    // ===== 활성/비활성 토글 =====
    @Test
    void testUpdateStatus() {
        policyService.updateStatus(7, false);
        PolicyDTO dto = policyService.getById(7);
        log.info("비활성화 결과: active={}", dto.isActive());
        assertFalse(dto.isActive());

        policyService.updateStatus(7, true);
        dto = policyService.getById(7);
        log.info("활성화 결과: active={}", dto.isActive());
        assertTrue(dto.isActive());
    }

}