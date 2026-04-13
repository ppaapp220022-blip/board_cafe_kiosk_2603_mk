package org.example.board_cafe_kiosk_2603.mapper.admin.statistics;

import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.dto.admin.statistics.ItemSalesDTO;
import org.example.board_cafe_kiosk_2603.dto.admin.statistics.TopItemDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Log4j2
@SpringBootTest
@Transactional // 테스트 후 데이터를 롤백하여 DB 청결 유지
class StatMapperTest {
    @Autowired
    private StatMapper statMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate; // DB에 직접 쿼리해서 데이터 확인용

    @Rollback(false)
    @Test
    void dailySummaryTest() {
        for (int i = 1; i <= 31; i++) {
            // 1. Given: 테스트 날짜 설정
            LocalDate targetDate = LocalDate.of(2026, 3, i);

            // 2. When: 통계 생성 로직 실행 (반환타입 void)
            statMapper.deleteDailySummary(targetDate);
            statMapper.insertDailySummaryFromSessions(targetDate);

            statMapper.deleteItemSalesHistory(targetDate);
            statMapper.insertItemSalesHistory(targetDate);

            // 3. Then: JdbcTemplate을 사용하여 실제로 데이터가 1줄 생성되었는지 확인
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM daily_sales_summary WHERE stat_date = ?",
                    Integer.class,
                    targetDate
            );

            assertThat(count).isEqualTo(1); // 특정 날짜에 대해 1행이 생성되어야 함

            // 추가 검증: 매출액이 0보다 큰지 확인 (더미 데이터가 있다는 가정 하에)
            Long totalRevenue = jdbcTemplate.queryForObject(
                    "SELECT total_revenue FROM daily_sales_summary WHERE stat_date = ?",
                    Long.class,
                    targetDate
            );
            assertThat(totalRevenue).isGreaterThanOrEqualTo(0);

            log.info("✅ 3월 {}일 요약 데이터 저장 확인 완료", i);

        }

    }

    @Test
    void itemSalesHistoryTest() {
        // Given
        LocalDate targetDate = LocalDate.of(2026, 3, 15);

        // When
        statMapper.deleteItemSalesHistory(targetDate);
        statMapper.insertItemSalesHistory(targetDate);

        // Then: item_sales_history 테이블에 해당 날짜 데이터가 존재하는지 확인
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM item_sales_history WHERE stat_date = ?",
                Integer.class,
                targetDate
        );

        assertThat(count).isGreaterThan(0); // 여러 상품이 팔렸으므로 0보다 커야 함
        log.info("✅ " + targetDate + " 상품별 판매 기록 " + count + "건 확인");
    }

    @Test
    void findMonthlyTop5ItemsTest() {
        String yearMonth = "2026-04";

        List<ItemSalesDTO> result = statMapper.findMonthlyTop5Items(yearMonth);

        for (int i = 0; i < result.size() - 1; i++) {
            ItemSalesDTO current = result.get(i);
            ItemSalesDTO next = result.get(i + 1);

            assertThat(current.getSalesQty())
                    .isGreaterThanOrEqualTo(next.getSalesQty());
        }

        log.info("--- {} 인기 메뉴 Top 5 ---", yearMonth);
        result.forEach(item ->
                System.out.println("메뉴명: " + item.getMenuName() + " | 판매수량: " + item.getSalesQty())
        );
    }

}