package org.example.board_cafe_kiosk_2603.domain.admin;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TableSession {
    private Long id;                // 세션 고유 번호 (PK, BIGINT)
    private Integer tableId;        // 이용 중인 테이블 번호 (FK)
    private Integer packageId;      // 선택한 요금제 패키지 ID (FK)
    private Integer initialGuestCnt;// 최초 입장 인원
    private LocalDateTime checkInTime;  // 입장 시간 (DEFAULT CURRENT_TIMESTAMP)
    private LocalDateTime checkOutTime; // 퇴장 시간 (NULL 가능)
    private boolean isActive;       // 현재 세션 활성화 여부 (T/F)
    private Integer totalAmount;    // 최종 정산 금액 (퇴실 시 합산)
}
