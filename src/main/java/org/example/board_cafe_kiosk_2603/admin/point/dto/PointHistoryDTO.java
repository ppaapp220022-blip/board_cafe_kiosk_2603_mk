package org.example.board_cafe_kiosk_2603.admin.point.dto;

import lombok.*;
import org.example.board_cafe_kiosk_2603.admin.point.model.PointHistory;

import java.time.LocalDateTime;

/*
 * 작성자 : 김민기
 * 기능 : PointHistory 데이터 전달 객체
 * 날짜 : 2026-03-27
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointHistoryDTO {
    private long id;
    private int pointId;
    private Long orderId;
    private String type;         // EARN | USE
    private int amount;
    private int balanceAfter;
    private LocalDateTime createdAt;

    /**
     * from 결과를 생성해 반환합니다.
     *
     * @param history 전달받은 history 값
     * @return 처리 결과
     */
    public static PointHistoryDTO from(PointHistory history) {
        return PointHistoryDTO.builder()
                .id(history.getId())
                .pointId(history.getPointId())
                .orderId(history.getOrderId())
                .type(history.getType())
                .amount(history.getAmount())
                .balanceAfter(history.getBalanceAfter())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
