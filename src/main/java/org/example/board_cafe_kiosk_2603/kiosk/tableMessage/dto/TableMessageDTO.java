package org.example.board_cafe_kiosk_2603.kiosk.tableMessage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
/*
 * 작성자 : 김민기
 * 기능 : TableMessage 데이터 전달 객체
 * 날짜 : 2026-03-30
 */

@Data
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class TableMessageDTO {
    private int id;
    private int tableId;
    private int macroId;
    private String content;
    private boolean isRead;
    private LocalDateTime createdAt;
}
