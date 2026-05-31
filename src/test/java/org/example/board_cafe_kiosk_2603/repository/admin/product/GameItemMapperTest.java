//package org.example.board_cafe_kiosk_2603.repository.admin.product;
//
//import lombok.extern.log4j.Log4j2;
//import org.example.board_cafe_kiosk_2603.admin.product.model.GameItem;
//import org.example.board_cafe_kiosk_2603.admin.product.model.GameItemStatus;
//import org.example.board_cafe_kiosk_2603.admin.product.dto.GameItemResponseDTO;
//import org.example.board_cafe_kiosk_2603.admin.product.mapper.GameItemMapper;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//@Log4j2
//@SpringBootTest
//@Transactional
//class GameItemMapperTest {
//
//    @Autowired
//    private GameItemMapper gameItemMapper;
//
//    @Test
//    void findAllTest() {
//        List<GameItemResponseDTO> list = gameItemMapper.findAll();
//        assertTrue(list.stream().allMatch(item -> item.getGameName() != null && !item.getGameName().isBlank()));
//        list.forEach(item -> log.info(item));
//    }
//
//    @Test
//    void findByGameIdTest() {
//        List<GameItemResponseDTO> list = gameItemMapper.findByGameId(1);
//        list.forEach(item -> log.info(item));
//    }
//
//    @Test
//    void findByStatusTest() {
//        List<GameItemResponseDTO> list = gameItemMapper.findByStatus(GameItemStatus.NORMAL);
//        assertTrue(list.stream().allMatch(item -> item.getStatus() == GameItemStatus.NORMAL));
//        list.forEach(item -> log.info(item));
//    }
//
//    @Test
//    void findByIdTest() {
//        Optional<GameItemResponseDTO> item = gameItemMapper.findById(1);
//        item.ifPresent(gameItem -> assertNotNull(gameItem.getGameName()));
//        log.info(item);
//    }
//
//    @Test
//    void insertTest() {
//        String serial = "SN-TEST-" + System.currentTimeMillis();
//        GameItem gameItem = GameItem.builder()
//                .gameId(1)
//                .serialNumber(serial)
//                .status(GameItemStatus.NORMAL)
//                .build();
//        int result = gameItemMapper.insert(gameItem);
//        log.info("insert 결과: " + result);
//    }
//
//    @Test
//    void updateTest() {
//        GameItem gameItem = GameItem.builder()
//                .id(1)
//                .gameId(1)
//                .serialNumber("SN-TEST-002")
//                .status(GameItemStatus.RENTED)
//                .build();
//        int result = gameItemMapper.update(gameItem);
//        log.info("update 결과: " + result);
//    }
//
//    @Test
//    void deleteTest() {
//        GameItem gameItem = GameItem.builder()
//                .gameId(1)
//                .serialNumber("SN-DEL-" + System.currentTimeMillis())
//                .status(GameItemStatus.DAMAGED)
//                .build();
//        gameItemMapper.insert(gameItem);
//        int result = gameItemMapper.delete(gameItem.getId());
//        log.info("delete 결과: " + result);
//    }
//
//    @Test
//    void updateStatusTest() {
//        int result = gameItemMapper.updateStatus(1, GameItemStatus.DAMAGED);
//        log.info("updateStatus 결과: " + result);
//    }
//}
