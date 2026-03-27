package org.example.board_cafe_kiosk_2603.domain.kiosk;

import lombok.*;

import java.time.LocalDateTime;

/**
 * table_session 테이블의 도메인 클래스.
 * 테이블 이용 세션 및 방문 히스토리를 나타냅니다.
 * 입장~퇴장 한 번의 방문을 나타내며 orders, payment, rental_log 의 기준이 됩니다.
 */
@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TableSession {
    private long          id;
    private int           tableId;
    private int           packageId;
    private int           initialGuestCnt;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;   // nullable — 퇴장 전 null
    private boolean       isActive;
    private int           totalAmount;
}
