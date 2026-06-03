package org.example.board_cafe_kiosk_2603.kiosk.order.model;

import lombok.*;

import java.time.LocalDateTime;

/*
 * 작성자 : 김민기
 * 기능 : Orders 클래스
 * 날짜 : 2026-03-27
 */

@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Orders {
    private int           id;
    private long          sessionId;       // table_session.id FK
    private int           tableId;
    private String        customerPhone;
    private String        status;          // DB의 ENUM 값을 String으로 매핑
    private int           totalAmount;
    private LocalDateTime orderedAt;
    /**
     * status 문자열을 OrderStatus enum으로 변환합니다.
     *
     * @return 처리 결과
     */

    public OrderStatus getStatusEnum() {
        return OrderStatus.valueOf(this.status);
    }
}
