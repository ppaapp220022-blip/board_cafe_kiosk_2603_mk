package org.example.board_cafe_kiosk_2603.dto.admin;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointHistoryDTO {
    private long id;
    private int pointId;
    private Integer orderId;
    private String type;         // EARN | USE
    private int amount;
    private int balanceAfter;
    private LocalDateTime createdAt;

    // === 정적 팩토리 ===
    public static PointHistoryDTO from(org.example.board_cafe_kiosk_2603.domain.admin.PointHistory history) {
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
