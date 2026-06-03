package org.example.board_cafe_kiosk_2603.kiosk.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * 작성자 : 김민기
 * 기능 : CartItem 데이터 전달 객체
 * 날짜 : 2026-03-27
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {

    private int    id;
    private int    cartId;
    private int    menuId;
    private int    quantity;
    private String menuName;   // cart_item JOIN menu 결과 (DB 저장 컬럼 아님)
    private int    menuPrice;  // cart_item JOIN menu 결과 (DB 저장 컬럼 아님)
}
