package org.example.board_cafe_kiosk_2603.service.admin.cafeTable;

import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.dto.admin.CafeTableDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
@SpringBootTest
class CafeTableServiceImplTest {
    @Autowired
    private CafeTableService cafeTableService;

    @Test
    void getAllTableStatusTest() {
        List<CafeTableDTO> cafeTableDTOList = cafeTableService.getAllTableStatus();
        cafeTableDTOList.forEach(cafeTableDTO -> log.info(cafeTableDTO));
    }

    // EMPTY, OCCUPIED, CLEANING
    @Test
    void changeTableStatusTest() {
        Integer id = 8;
        String status = "EMPTY";

        cafeTableService.changeTableStatus(id, status);
    }

    @Test
    void generateNewTokenTest() {
        Integer id = 3;
        cafeTableService.generateNewToken(id);
    }

    @Test
    void resetAllTablesForNewDayTest() {
        cafeTableService.resetAllTablesForNewDay();
    }

}