package org.example.board_cafe_kiosk_2603.config;

import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.admin.manager.Manager;
import org.example.board_cafe_kiosk_2603.domain.admin.manager.RoleType;
import org.example.board_cafe_kiosk_2603.mapper.admin.manager.ManagerMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Log4j2
class ManagerEncodingTest {

    @Autowired
    private ManagerMapper managerMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void passwordEncoderTest() {
        // 1. 준비
        String rawPassword = "1212";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // VO 생성 (생성자 또는 빌더 사용)
        Manager newManager = Manager.builder()
                .loginId("qwer")
                .password(encodedPassword)
                .name("테스터")
                .role(RoleType.ADMIN)
                .isActive(true)
                .build();

        // 2. 실행
        managerMapper.insert(newManager);

        // 3. 검증 (Optional 처리 중요!)
        // .orElseThrow()를 사용하면 Optional 안의 Manager를 바로 꺼낼 수 있습니다.
        Manager savedManager = managerMapper.findByLoginId("qwer")
                .orElseThrow(() -> new RuntimeException("관리자를 찾을 수 없습니다."));

        log.info("입력 평문: {}", rawPassword);
        log.info("DB에 저장된 암호문: {}", savedManager.getPassword());

        // 4. 단언
        // BCrypt 암호화 결과가 평문과 다른지 확인
        Assertions.assertNotEquals(rawPassword, savedManager.getPassword());

        // BCrypt 전용 매칭 확인
        boolean isMatch = passwordEncoder.matches(rawPassword, savedManager.getPassword());
        Assertions.assertTrue(isMatch, "암호화된 비밀번호가 일치해야 합니다.");
    }
}