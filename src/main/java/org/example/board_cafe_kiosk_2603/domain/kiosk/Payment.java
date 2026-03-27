package org.example.board_cafe_kiosk_2603.domain.kiosk;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    private int id;
    private long sessionId;  // table_session.id FK (세션당 최종 1회 결제)
    private String status;   // READY | DONE
    private int finalAmount;
    private LocalDateTime paidAt;
}
