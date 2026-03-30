package org.example.board_cafe_kiosk_2603.repository.admin;

import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.admin.table.CafeTable;
import org.example.board_cafe_kiosk_2603.mapper.admin.table.CafeTableMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Log4j2
@SpringBootTest
class CafeTableMapperTest {
    @Autowired
    private CafeTableMapper cafeTableMapper;

    @Test
    void selectAllTablesTest() {
        List<CafeTable> allTables = cafeTableMapper.selectAllTables();
        allTables.forEach(table -> log.info(table));
    }

    @Test
    void updateTableStatus() {
        Integer id = 1;
        String status = "EMPTY";
        cafeTableMapper.updateTableStatusAndSession(id, status, 6L);
    }

    @Test
    void resetTable() {
        Integer id = 1;
        String accessToken = "제발 되줘 제발";
        cafeTableMapper.updateAccessToken(id,accessToken);
    }

    @Test
    void resetAllTablesForNewDayTest() {
        cafeTableMapper.updateAllActiveSessions();
    }

    @Test
    void selectActiveOrderItemsTest() {
        cafeTableMapper.selectActiveOrderItems(1L);
    }

}