package org.example.board_cafe_kiosk_2603.kiosk.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.example.board_cafe_kiosk_2603.kiosk.cafePackage.dto.CafePackageDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/*
 * 작성자 : 김민기
 * 기능 : 결제 REST API 요청/응답 DTO
 * 날짜 : 2026-03-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentDTO {

    private Integer pointUsed;           // 사용할 포인트

    // === Package 정보 필드 ===
    private CafePackageDTO cafePackage;  // 선택한 패키지 정보 전체

    // === Prepare 단계 응답 필드 ===
    private String orderIdToss;          // 토스용 주문번호
    private Integer amount;              // 최종 결제 금액
    private String orderName;            // 주문명
    private Integer totalAmount;         // 상품 합계
    private String clientKey;            // 토스 클라이언트 키
    private String customerKey;          // 토스 고객 키 (결제 위젯 초기화용)

    // === Confirm 단계 응답 필드 ===
    private Long orderId;                // 생성된 주문 ID
    private Integer finalAmount;         // 최종 결제액
    private Integer earnedPoints;        // 적립된 포인트
    private String paymentKey;           // 토스 결제 키
    private String method;               // 결제 수단

    // === 공통 응답 필드 ===
    private boolean success;             // 성공 여부
    private String message;              // 오류 메시지
}
