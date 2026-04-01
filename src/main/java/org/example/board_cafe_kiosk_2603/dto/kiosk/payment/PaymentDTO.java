package org.example.board_cafe_kiosk_2603.dto.kiosk.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {

    // 요청 필드
    private String  paymentMethod;
    private int     pointUsed;

    // 응답 필드
    private boolean success;
    private String  message;
    private int     orderId;
    private int     totalAmount;
    private int     finalAmount;
    private int     earnedPoints;
}

