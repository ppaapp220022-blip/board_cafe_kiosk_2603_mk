package org.example.board_cafe_kiosk_2603.dto.kiosk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {

    // === 도메인 VO 필드 ===
    private int id;
    private int tableId;
    private LocalDateTime updatedAt;

    // === 비즈니스 로직용 필드 ===
    private boolean success;
    private String message;
    private List<CartItemDTO> cartItems;
    private int totalPrice;
    private int cartCount;

    // === 정적 팩토리 ===
    public static CartDTO empty() {
        return CartDTO.builder()
                .success(true)
                .cartItems(Collections.emptyList())
                .totalPrice(0)
                .cartCount(0)
                .build();
    }

    public static CartDTO fail() {
        return CartDTO.builder()
                .success(false)
                .cartItems(Collections.emptyList())
                .build();
    }

    public static CartDTO fail(String message) {
        return CartDTO.builder()
                .success(false)
                .message(message)
                .cartItems(Collections.emptyList())
                .build();
    }

    public static CartDTO added(String menuName, int cartCount) {
        return CartDTO.builder()
                .success(true)
                .message(menuName + "이(가) 장바구니에 추가되었습니다.")
                .cartCount(cartCount)
                .build();
    }

    public static CartDTO updated(int cartCount, int totalPrice) {
        return CartDTO.builder()
                .success(true)
                .cartCount(cartCount)
                .totalPrice(totalPrice)
                .build();
    }

    public static CartDTO cleared() {
        return CartDTO.builder()
                .success(true)
                .cartCount(0)
                .totalPrice(0)
                .cartItems(Collections.emptyList())
                .build();
    }

    public static CartDTO fetched(List<CartItemDTO> cartItems, int totalPrice) {
        return CartDTO.builder()
                .success(true)
                .cartItems(cartItems)
                .totalPrice(totalPrice)
                .cartCount(cartItems.size())
                .build();
    }
}
