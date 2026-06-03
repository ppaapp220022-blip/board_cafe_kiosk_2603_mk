package org.example.board_cafe_kiosk_2603.kiosk.cart.model;

import lombok.*;

/*
 * 작성자 : 김민기
 * 기능 : CartItem 클래스
 * 날짜 : 2026-03-27
 */

@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {
    private int id;
    private int cartId;
    private int menuId;
    private int quantity;
    // cart_item JOIN menu 결과 수신용 (DB 저장 컬럼 아님)
    private String menuName;
    private int menuPrice;
}
