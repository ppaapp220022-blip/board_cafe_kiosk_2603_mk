package org.example.board_cafe_kiosk_2603.kiosk.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/*
 * 작성자 : 김민기
 * 기능 : 주문 생성 요청 DTO
 * 날짜 : 2026-04-06
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {
    private int tableNumber;
    private int totalAmount;
    private String customerPhone;  // optional
}
