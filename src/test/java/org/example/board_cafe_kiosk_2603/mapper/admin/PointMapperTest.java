package org.example.board_cafe_kiosk_2603.mapper.admin;

import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.admin.Point;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Log4j2
@SpringBootTest
class PointMapperTest {
    @Autowired
    private PointMapper pointMapper;

    @Test
    void insertPointTest() {
        Point point = Point.builder()
                .phone("010-1111-2222")
                .balance(0)
                .build();

        pointMapper.insertPoint(point);
        log.info("생성된 포인트 계좌: {}", point);
    }

    @Test
    void selectByPhoneTest() {
        Point found = pointMapper.selectByPhone("010-1111-2222");
        log.info("조회된 포인트 계좌: {}", found);
    }

    @Test
    void updateBalanceTest() {
        Point point = pointMapper.selectByPhone("010-1111-2222");

        Point updated = Point.builder()
                .id(point.getId())
                .phone(point.getPhone())
                .balance(1000)
                .build();

        pointMapper.updateBalance(updated);

        Point found = pointMapper.selectByPhone("010-1111-2222");
        log.info("업데이트 후 잔액: {}", found.getBalance());
    }
}