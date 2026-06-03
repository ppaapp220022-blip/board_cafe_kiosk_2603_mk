package org.example.board_cafe_kiosk_2603.kiosk.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * 작성자 : 김민기
 * 기능 : OrderItem 데이터 전달 객체
 * 날짜 : 2026-03-27
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {

    private int     id;
    private Integer orderId;
    private Integer menuId;
    private String  menuName;
    private int     price;
    private int     quantity;
    private String  status;
}
