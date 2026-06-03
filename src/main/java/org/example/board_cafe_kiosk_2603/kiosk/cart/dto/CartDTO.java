package org.example.board_cafe_kiosk_2603.kiosk.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/*
 * 작성자 : 김민기
 * 기능 : Cart 데이터 전달 객체
 * 날짜 : 2026-03-27
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {

    private boolean           success;
    private String            message;
    private List<CartItemDTO> cartItems;
    private int               totalPrice;
    private int               cartCount;
}
