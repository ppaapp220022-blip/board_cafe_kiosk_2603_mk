package org.example.board_cafe_kiosk_2603.domain.kiosk;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TossPayment {
    private int           id;
    private int           paymentId;    // payment.id FK
    private String        paymentKey;   // 토스 발급 결제 키
    private String        orderIdToss;  // 토스용 주문번호
    private String        method;       // 간편결제 | 계좌이체
    private String        rawResponse;  // 토스 응답 JSON (DB: JSON 타입)
    private LocalDateTime approvedAt;
}
