package org.example.board_cafe_kiosk_2603.service.admin.macro;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
@SpringBootTest
class MacroMessageServiceImplTest {
    @Autowired
    private MacroMessageService macroMessageService;

    @Test
    void getAllMacroMessagesTest() {
        macroMessageService.getAllActiveMessages()
                .forEach(macroMessage -> log.info(macroMessage));
    }
}