package org.example.board_cafe_kiosk_2603.dto.kiosk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {

    // === 도메인 VO 필드 ===
    private int id;
    private int cartId;
    private int menuId;
    private int quantity;
    // cart_item JOIN menu 결과 수신용 (DB 저장 컬럼 아님)
    private String menuName;
    private int menuPrice;

    // === 요청(Request)으로 사용 시 validation ===
    // menuName, menuPrice, quantity 는 위 필드를 그대로 활용

    // === 정적 팩토리 ===
    public static CartItemDTO from(org.example.board_cafe_kiosk_2603.domain.kiosk.CartItem item) {
        return CartItemDTO.builder()
                .id(item.getId())
                .cartId(item.getCartId())
                .menuId(item.getMenuId())
                .menuName(item.getMenuName())
                .menuPrice(item.getMenuPrice())
                .quantity(item.getQuantity())
                .build();
    }
}
