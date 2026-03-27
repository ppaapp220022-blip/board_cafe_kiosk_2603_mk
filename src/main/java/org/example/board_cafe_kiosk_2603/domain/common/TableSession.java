package org.example.board_cafe_kiosk_2603.domain.common;

import lombok.*;

import java.sql.Timestamp;

@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TableSession {

    private int id;                // 세션 고유 번호 (PK, AI)
    private int tableId;            // 이용 테이블 (FK → cafe_table.id)
    private int packageId;          // 선택 패키지 (FK → cafe_package.id)
    private int initialGuestCnt;    // 최초 입장 인원
    private Timestamp checkInTime;  // 입장 시간
    private Timestamp checkOutTime; // 퇴장 시간 (nullable)
    private Boolean isActive;       // 현재 세션 활성화 여부
    private int totalAmount;        // 최종 정산 금액
}
