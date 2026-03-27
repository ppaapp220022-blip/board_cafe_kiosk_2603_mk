package org.example.board_cafe_kiosk_2603.repository.admin;

import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.admin.CafeTable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
@SpringBootTest
class CafeTableRepositoryTest {
    @Autowired
    private CafeTableRepository cafeTableRepository;

    @Test
    void selectAllTablesTest() {
        List<CafeTable> allTables = cafeTableRepository.selectAllTables();
        allTables.forEach(table -> log.info(table));
    }

    @Test
    void updateTableStatus() {
        Integer id = 1;
        String status = "EMPTY";
        cafeTableRepository.updateTableStatus(id, status);
    }

    @Test
    void resetTable() {
        Integer id = 1;
        String accessToken = "제발 되줘 제발";
        cafeTableRepository.updateAccessToken(id,accessToken);
    }

    @Test
    void resetAllTablesForNewDayTest() {
        cafeTableRepository.updateAllTablesForNewDay();
    }

}