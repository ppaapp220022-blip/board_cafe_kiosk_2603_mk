package org.example.board_cafe_kiosk_2603.kiosk.order.model;

import lombok.*;

/*
 * 작성자 : 김민기
 * 기능 : OrderItem 클래스
 * 날짜 : 2026-03-27
 */

@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
    private int id;
    private int orderId;
    private Integer menuId;
    private String menuName;
    private int price;
    private int quantity;
    private String  status;
}
