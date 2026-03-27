package org.example.board_cafe_kiosk_2603.service.admin;

import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.common.TableSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
@SpringBootTest
class CafeTableSessionAdminServiceTest {
    @Autowired
    private TableSessionAdminService tableSessionAdminService;

    @Test
    void getActiveSessionTest() {
        TableSession tableSession = tableSessionAdminService.getActiveSession(1);
        log.info("활성 세션 조회 결과... {}", tableSession);
    }

    @Test
    void updateTotalAmountTest() {
        // 활성 세션 조회 후 정산 금액 업데이트
        TableSession tableSession = tableSessionAdminService.getActiveSession(1);

        tableSessionAdminService.updateTotalAmount(tableSession.getId(), 25000);
        log.info("정산 금액 업데이트... sessionId: {}, totalAmount: 25000", tableSession.getId());
    }


}