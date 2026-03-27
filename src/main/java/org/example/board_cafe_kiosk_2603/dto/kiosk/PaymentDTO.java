package org.example.board_cafe_kiosk_2603.dto.kiosk;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class PaymentDTO {

    // === 요청(Request)용 필드 ===
    @NotBlank(message = "결제 수단은 필수입니다.")
    private final String paymentMethod;

    @Min(value = 0, message = "포인트 사용액은 0 이상이어야 합니다.")
    private final int pointUsed;

    // === 응답(Response)용 필드 ===
    private final boolean success;
    private final String  message;
    private final int     orderId;
    private final int     totalAmount;
    private final int     finalAmount;
    private final int     earnedPoints;

    // === 정적 팩토리 ===
    public static PaymentDTO fail(String message) {
        return PaymentDTO.builder()
                .success(false)
                .message(message)
                .build();
    }

    public static PaymentDTO success(int orderId, int totalAmount, int pointUsed,
                                     int finalAmount, int earnedPoints) {
        return PaymentDTO.builder()
                .success(true)
                .message("결제가 완료되었습니다.")
                .orderId(orderId)
                .totalAmount(totalAmount)
                .pointUsed(pointUsed)
                .finalAmount(finalAmount)
                .earnedPoints(earnedPoints)
                .build();
    }
}
