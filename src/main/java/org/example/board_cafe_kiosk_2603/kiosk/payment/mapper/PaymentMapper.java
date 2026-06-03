package org.example.board_cafe_kiosk_2603.kiosk.payment.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.board_cafe_kiosk_2603.kiosk.payment.model.Payment;

@Mapper

/*
 * 작성자 : 김민기
 * 기능 : 결제 데이터 접근 인터페이스
 * 날짜 : 2026-04-06
 */
public interface PaymentMapper {

    /**
     * 데이터 등록합니다.
     *
     * @param payment 전달받은 payment 값
     */
    void insert(Payment payment);

    /**
     * 결제 키로 결제 조회합니다.
     *
     * @param paymentKey 전달받은 paymentKey 값
     * @return 처리 결과
     */
    Payment findByPaymentKey(String paymentKey);

    /**
     * 주문 ID로 결제 조회합니다.
     *
     * @param orderIdToss 전달받은 orderIdToss 값
     * @return 처리 결과
     */
    Payment findByOrderIdToss(String orderIdToss);

    /**
     * 세션 ID 기준 조회합니다.
     *
     * @param sessionId 전달받은 sessionId 값
     * @return 처리 결과
     */
    Payment findBySessionId(long sessionId);

    /**
     * 상태 변경합니다.
     *
     * @param payment 전달받은 payment 값
     */
    void updateStatus(Payment payment);

    /**
     * ID로 단건 조회합니다.
     *
     * @param id 전달받은 id 값
     * @return 처리 결과
     */
    Payment findById(int id);
}
