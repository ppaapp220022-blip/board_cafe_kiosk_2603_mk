package org.example.board_cafe_kiosk_2603.mapper.admin.policy;

import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.admin.policy.Policy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
@SpringBootTest
class PolicyMapperTest {
    @Autowired
    private PolicyMapper policyMapper;

    // ===== 전체 조회 (페이징 + 필터) =====
    @Test
    void testFindAll() {
        List<Policy> list = policyMapper.findAll(0, 8, "all"); // 1페이지, 8개
        log.info("전체 패키지 수: {}", list.size());
        list.forEach(p -> log.info("패키지: {}", p));
        assertNotNull(list);
    }

    // ===== 활성 필터 조회 =====
    @Test
    void testFindAllActive() {
        List<Policy> list = policyMapper.findAll(0, 8, "active");
        log.info("활성 패키지 수: {}", list.size());
        list.forEach(p -> log.info("활성 패키지: {}", p));
        assertTrue(list.stream().allMatch(Policy::isActive));
    }

    // ===== 비활성 필터 조회 =====
    @Test
    void testFindAllInactive() {
        List<Policy> list = policyMapper.findAll(0, 8, "inactive");
        log.info("비활성 패키지 수: {}", list.size());
        list.forEach(p -> log.info("비활성 패키지: {}", p));
        assertTrue(list.stream().noneMatch(Policy::isActive));
    }

    // ===== 전체 개수 조회 =====
    @Test
    void testCountAll() {
        int count = policyMapper.countAll("all");
        log.info("전체 개수: {}", count);
        assertTrue(count >= 0);
    }

    // ===== 단건 조회 =====
    @Test
    void testFindById() {
        Policy policy = policyMapper.findById(1);
        log.info("단건 조회: {}", policy);
        assertNotNull(policy);
    }

    // ===== 등록 =====
    @Test
    void testInsert() {
        Policy policy = Policy.builder()
                .name("테스트 패키지")
                .type("HOURLY")
                .durationMinutes(60)
                .basePrice(5000)
                .extraPricePerMin(50.0)
                .active(true)
                .build();

        policyMapper.insert(policy);
        log.info("등록 완료 - id: {}", policy.getId());
        assertTrue(policy.getId() > 0);
    }

    // ===== 수정 =====
    @Test
    void testUpdate() {
        Policy policy = Policy.builder()
                .id(6)
                .name("수정된 패키지")
                .type("HOURLY")
                .durationMinutes(90)
                .basePrice(7000)
                .extraPricePerMin(60.0)
                .build();

        policyMapper.update(policy);
        Policy updated = policyMapper.findById(6);
        log.info("수정 결과: {}", updated);
        assertEquals("수정된 패키지", updated.getName());
    }

    // ===== 활성/비활성 토글 =====
    @Test
    void testUpdateStatus() {
        policyMapper.updateStatus(6, false);
        Policy policy = policyMapper.findById(6);
        log.info("비활성화 결과: {}", policy);
        assertFalse(policy.isActive());

        policyMapper.updateStatus(6, true);
        policy = policyMapper.findById(6);
        log.info("활성화 결과: {}", policy);
        assertTrue(policy.isActive());
    }
}