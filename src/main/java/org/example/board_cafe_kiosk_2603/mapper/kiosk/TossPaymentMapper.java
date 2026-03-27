package org.example.board_cafe_kiosk_2603.mapper.kiosk;

import org.apache.ibatis.annotations.Mapper;
import org.example.board_cafe_kiosk_2603.domain.kiosk.TossPayment;

@Mapper
public interface TossPaymentMapper {

    // 토스 결제 정보 저장
    void insert(TossPayment tossPayment);

    // paymentKey로 조회 (중복 승인 방지)
    TossPayment findByPaymentKey(String paymentKey);

    // orderId(toss)로 조회
    TossPayment findByOrderIdToss(String orderIdToss);
}
