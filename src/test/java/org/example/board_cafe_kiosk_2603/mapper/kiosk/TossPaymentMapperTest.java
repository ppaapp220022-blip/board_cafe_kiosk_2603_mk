package org.example.board_cafe_kiosk_2603.mapper.kiosk;

import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.kiosk.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@Log4j2
@SpringBootTest
class TossPaymentMapperTest {

    @Autowired private TossPaymentMapper tossPaymentMapper;
    @Autowired private OrdersMapper      ordersMapper;
    @Autowired private CartMapper        cartMapper;

    /**
     * payment 테이블은 session_id UNIQUE 제약이 있어 직접 insert 시 충돌 가능.
     * init.sql 더미 데이터에 이미 payment 레코드가 있는 session을 활용하거나,
     * 기존 payment id를 조회해서 TossPayment 연결에 활용한다.
     *
     * 여기서는 findByPaymentKey / findByOrderIdToss의 조회 동작만 단위 검증하고,
     * insert는 실제 payment id가 존재하는 경우에만 시도한다.
     */

    @Test
    void findByPaymentKey_notFound() {
        // 존재하지 않는 key는 null 반환
        assertThat(tossPaymentMapper.findByPaymentKey("not-exist-key-xyz")).isNull();
    }

    @Test
    void findByOrderIdToss_notFound() {
        // 존재하지 않는 orderIdToss는 null 반환
        assertThat(tossPaymentMapper.findByOrderIdToss("NOT-EXIST-ORDER")).isNull();
    }

    @Test
    void insert_and_findByPaymentKey() {
        // init.sql 더미데이터: session_id=1 (is_active=FALSE)로 주문+결제 생성
        // payment 테이블 session_id UNIQUE → 아직 등록 안 된 세션 필요
        // 완료된 세션 중 payment가 없는 session_id=3 사용
        int tableId = cartMapper.findCafeTableIdByTableNumber(3);
        if (tableId <= 0) {
            // table_number=3이 없으면 스킵
            return;
        }

        Orders order = Orders.builder()
                .sessionId(3L)
                .tableId(tableId)
                .status("PAID")
                .totalAmount(5000)
                .build();
        ordersMapper.insertOrder(order);

        Payment payment = Payment.builder()
                .sessionId(3L)
                .finalAmount(5000)
                .build();
        ordersMapper.insertPayment(payment);

        if (payment.getId() <= 0) return; // payment insert 실패 시 스킵

        String uniqueKey = "test-toss-key-" + System.currentTimeMillis();
        String uniqueOrderId = "KIOSK-TEST-" + System.currentTimeMillis();

        TossPayment tp = TossPayment.builder()
                .paymentId(payment.getId())
                .paymentKey(uniqueKey)
                .orderIdToss(uniqueOrderId)
                .method("카드")
                .rawResponse("{\"status\":\"DONE\"}")
                .approvedAt(LocalDateTime.now())
                .build();
        tossPaymentMapper.insert(tp);
        assertThat(tp.getId()).isPositive();

        TossPayment found = tossPaymentMapper.findByPaymentKey(uniqueKey);
        assertThat(found).isNotNull();
        assertThat(found.getPaymentKey()).isEqualTo(uniqueKey);
        assertThat(found.getMethod()).isEqualTo("카드");

        TossPayment foundByOrder = tossPaymentMapper.findByOrderIdToss(uniqueOrderId);
        assertThat(foundByOrder).isNotNull();
        assertThat(foundByOrder.getOrderIdToss()).isEqualTo(uniqueOrderId);
    }
}
