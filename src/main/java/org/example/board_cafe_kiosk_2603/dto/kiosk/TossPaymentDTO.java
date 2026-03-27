package org.example.board_cafe_kiosk_2603.dto.kiosk;

import jakarta.validation.constraints.Min;
import lombok.*;

@Data
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class TossPaymentDTO {

    // === 도메인 VO 필드 ===
    private final int    id;
    private final int    paymentId;
    private final String paymentKey;
    private final String orderIdToss;   // 토스 주문 ID
    private final String method;        // 결제 수단
    private final String rawResponse;

    // === prepare 단계 응답용 ===
    private final String orderName;
    private final int    amount;        // 토스에 전달할 최종 결제 금액

    // === 비즈니스 로직용 필드 ===
    private final boolean success;
    private final String  message;
    private final int     orderId;
    private final int     totalAmount;

    @Min(value = 0, message = "포인트 사용액은 0 이상이어야 합니다.")
    private final int pointUsed;

    private final int    finalAmount;
    private final int    earnedPoints;

    // === 정적 팩토리 ===
    public static TossPaymentDTO fail(String message) {
        return TossPaymentDTO.builder()
                .success(false)
                .message(message)
                .build();
    }

    public static TossPaymentDTO prepared(String orderIdToss, int amount, String orderName,
                                          int totalAmount, int pointUsed) {
        return TossPaymentDTO.builder()
                .success(true)
                .orderIdToss(orderIdToss)
                .amount(amount)
                .orderName(orderName)
                .totalAmount(totalAmount)
                .pointUsed(pointUsed)
                .build();
    }

    public static TossPaymentDTO confirmed(int orderId, int totalAmount, int pointUsed,
                                           int finalAmount, int earnedPoints,
                                           String paymentKey, String method) {
        return TossPaymentDTO.builder()
                .success(true)
                .orderId(orderId)
                .totalAmount(totalAmount)
                .pointUsed(pointUsed)
                .finalAmount(finalAmount)
                .earnedPoints(earnedPoints)
                .paymentKey(paymentKey)
                .method(method)
                .build();
    }
}
