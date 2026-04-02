package org.example.board_cafe_kiosk_2603.dto.kiosk.payment;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * 토스페이먼츠 결제 REST API 응답 DTO.
 * 클라이언트로 반환되는 최종 응답 메시지만 포함.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TossPaymentDTO {

    // === prepare 단계 응답 ===
    private String orderIdToss;          // 토스용 주문번호
    private Integer amount;              // 최종 결제 금액
    private String orderName;            // 주문명
    private Integer totalAmount;         // 상품 합계
    private Integer pointUsed;           // 사용한 포인트
    private String clientKey;            // 토스 클라이언트 키

    // === confirmed 단계 응답 ===
    private Long orderId;                // 생성된 주문 ID
    private Integer finalAmount;         // 최종 결제액
    private Integer earnedPoints;        // 적립된 포인트
    private String paymentKey;           // 토스 결제 키
    private String method;               // 결제 수단

    // === 공통 응답 ===
    private boolean success;             // 성공 여부
    private String message;              // 오류 메시지
}
