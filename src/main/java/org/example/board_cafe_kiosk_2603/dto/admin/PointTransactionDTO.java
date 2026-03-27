package org.example.board_cafe_kiosk_2603.dto.admin;

import lombok.*;

@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PointTransactionDTO {

    private String phone;      // 적립/사용 대상 고객 전화번호
    private int amount;        // 결제 총 금액 (적립 계산용)
    private int usePoint;      // 이번 결제 시 사용하려는 포인트 액수 (default: 0)
    private boolean isEarning; // 적립 모드 여부 (false일 경우 사용 처리, default: true)
}
