package org.example.board_cafe_kiosk_2603.dto.admin;

import lombok.*;

@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PointHistoryResponseDTO {

    private String transactionDate; // 변동 발생 일시 (MM-dd HH:mm)
    private int changeAmount;       // 변동된 포인트 양 (+ 또는 -)
    private String description;     // 변동 사유 (예: "음료 결제 적립", "포인트 사용")
    private int remainingPoint;     // 해당 시점의 잔여 포인트 결과값
}
