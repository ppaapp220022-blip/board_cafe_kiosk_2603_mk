//package org.example.board_cafe_kiosk_2603.mapper.admin;
//
//import lombok.extern.log4j.Log4j2;
//import org.example.board_cafe_kiosk_2603.admin.point.model.Point;
//import org.example.board_cafe_kiosk_2603.admin.point.model.PointHistory;
//import org.example.board_cafe_kiosk_2603.common.pagination.PageRequestDTO;
//import org.example.board_cafe_kiosk_2603.admin.point.mapper.PointMapper;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.*;
//
//@Log4j2
//@SpringBootTest
//@Transactional
//class PointMapperTest {
//
//    @Autowired
//    private PointMapper pointMapper;
//
//    private String uniquePhone(String suffix) {
//        return "010" + String.format("%08d", (System.currentTimeMillis() % 100_000_000)) + suffix;
//    }
//
//    @Test
//    @DisplayName("포인트 계좌 생성 후 전화번호로 조회 성공")
//    void insert_and_findByPhone() {
//        String phone = uniquePhone("01");
//        Point point = Point.builder().phone(phone).balance(0).build();
//        pointMapper.insert(point);
//
//        Point found = pointMapper.findByPhone(phone);
//        assertThat(found).isNotNull();
//        assertThat(found.getId()).isPositive();
//        assertThat(found.getPhone()).isEqualTo(phone);
//        assertThat(found.getBalance()).isEqualTo(0);
//    }
//
//    @Test
//    @DisplayName("존재하지 않는 전화번호 조회 시 null 반환")
//    void findByPhone_notFound() {
//        Point found = pointMapper.findByPhone("000-0000-0000");
//        assertThat(found).isNull();
//    }
//
//    @Test
//    @DisplayName("잔액 업데이트 성공")
//    void updateBalance() {
//        String phone = uniquePhone("02");
//        Point point = Point.builder().phone(phone).balance(0).build();
//        pointMapper.insert(point);
//        Point saved = pointMapper.findByPhone(phone);
//        assertThat(saved).isNotNull();
//
//        Point updated = Point.builder()
//                .id(saved.getId())
//                .phone(saved.getPhone())
//                .balance(5000)
//                .build();
//        pointMapper.updateBalance(updated);
//
//        Point found = pointMapper.findByPhone(phone);
//        assertThat(found.getBalance()).isEqualTo(5000);
//    }
//
//    @Test
//    @DisplayName("전체 고객 수 및 총 잔액 집계 성공")
//    void countAll_and_sumTotalBalance() {
//        int before = pointMapper.countAll();
//        pointMapper.insert(Point.builder().phone(uniquePhone("05")).balance(3000).build());
//
//        assertThat(pointMapper.countAll()).isEqualTo(before + 1);
//        assertThat(pointMapper.sumTotalBalance()).isGreaterThanOrEqualTo(3000);
//    }
//
//
//    @Test
//    @Disabled("현재 point_history 스키마가 주문 이력과 강하게 결합되어 있어 독립 실행 테스트가 불안정합니다.")
//    @DisplayName("포인트 이력 추가 후 조회 성공")
//    void insertHistory_and_findHistory() {
//        Point point = Point.builder().phone(uniquePhone("06")).balance(1000).build();
//        pointMapper.insert(point);
//
//        PointHistory history = PointHistory.builder()
//                .pointId(point.getId())
//                .type("EARN")
//                .amount(1000)
//                .balanceAfter(1000)
//                .build();
//        pointMapper.insertHistory(history);
//        assertThat(history.getId()).isPositive();
//
//        List<PointHistory> list = pointMapper.findHistoryByPointId(point.getId());
//        assertThat(list).hasSize(1);
//        assertThat(list.get(0).getType()).isEqualTo("EARN");
//        assertThat(list.get(0).getAmount()).isEqualTo(1000);
//        assertThat(list.get(0).getBalanceAfter()).isEqualTo(1000);
//    }
//
//    @Test
//    @DisplayName("페이징 포인트 목록 조회 성공")
//    void selectList() {
//        pointMapper.insert(Point.builder().phone(uniquePhone("03")).balance(0).build());
//        pointMapper.insert(Point.builder().phone(uniquePhone("04")).balance(0).build());
//
//        PageRequestDTO pageRequestDTO = PageRequestDTO.builder()
//                .page(1)
//                .size(10)
//                .build();
//
//        List<Point> list = pointMapper.selectList(pageRequestDTO);
//        assertThat(list).hasSizeGreaterThanOrEqualTo(2);
//    }
//
//    @Test
//    @DisplayName("keyword로 전화번호 검색 조회 성공")
//    void selectList_withKeyword() {
//        String phone = uniquePhone("07");
//        pointMapper.insert(Point.builder().phone(phone).balance(0).build());
//
//        PageRequestDTO pageRequestDTO = PageRequestDTO.builder()
//                .page(1)
//                .size(10)
//                .keyword(phone)
//                .build();
//
//        List<Point> list = pointMapper.selectList(pageRequestDTO);
//        assertThat(list).hasSizeGreaterThanOrEqualTo(1);
//        assertThat(list.stream().anyMatch(point -> phone.equals(point.getPhone()))).isTrue();
//    }
//
//    @Test
//    @DisplayName("전체 개수 조회 성공")
//    void selectCount() {
//        int before = pointMapper.selectCount(PageRequestDTO.builder().page(1).size(10).build());
//        pointMapper.insert(Point.builder().phone(uniquePhone("08")).balance(0).build());
//
//        int after = pointMapper.selectCount(PageRequestDTO.builder().page(1).size(10).build());
//        assertThat(after).isEqualTo(before + 1);
//    }
//}
