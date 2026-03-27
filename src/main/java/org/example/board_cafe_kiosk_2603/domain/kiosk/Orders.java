package org.example.board_cafe_kiosk_2603.domain.kiosk;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Orders {
    private int id;
    private long sessionId;   // table_session.id FK
    private int tableId;
    private String customerPhone;
    private String status;    // PENDING | PAID | CONFIRMED | COOKING | DELIVERING | COMPLETED | CANCELLED
    private int totalAmount;
    private LocalDateTime orderedAt;
}
