package org.example.board_cafe_kiosk_2603.kiosk.tableMessage.model;

import lombok.*;

import java.time.LocalDateTime;

/*
 * 작성자 : 김민기
 * 기능 : TableMessage 클래스
 * 날짜 : 2026-03-30
 */

@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TableMessage {
    private long          id;
    private int           tableId;
    private Integer       macroId;     // nullable — 자유 입력 시 null
    private String        direction;   // STAFF_TO_TABLE | TABLE_TO_STAFF
    private String        content;
    private boolean       isRead;
    private LocalDateTime createdAt;
}
