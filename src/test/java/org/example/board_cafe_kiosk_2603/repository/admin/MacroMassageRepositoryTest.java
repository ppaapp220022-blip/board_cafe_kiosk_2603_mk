package org.example.board_cafe_kiosk_2603.repository.admin;

import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.admin.macro.MacroMessage;
import org.example.board_cafe_kiosk_2603.repository.admin.macro.MacroMessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Log4j2
@SpringBootTest
class MacroMassageRepositoryTest {
    @Autowired
    private MacroMessageRepository macroMessageRepository;

    @Test
    void findAllActive() {
        List<MacroMessage> macroMassages = macroMessageRepository.findAllActive();
        macroMassages.forEach(macroMassage -> log.info(macroMassage));
    }

}