package org.example.board_cafe_kiosk_2603.kiosk.cart.model;

import lombok.*;

import java.time.LocalDateTime;

/*
 * 작성자 : 김민기
 * 기능 : Cart 클래스
 * 날짜 : 2026-03-27
 */

@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Cart {
    private int id;
    private int tableId;
    private LocalDateTime updatedAt;
}
