package org.example.board_cafe_kiosk_2603.mapper.kiosk;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.board_cafe_kiosk_2603.domain.kiosk.CartItem;

import java.util.List;

@Mapper
public interface CartItemMapper {

    // 장바구니 항목 목록 조회 (menu JOIN)
    List<CartItem> findByCartId(int cartId);

    // cart_id + menu_id로 기존 항목 확인
    CartItem findByCartIdAndMenuId(@Param("cartId") int cartId,
                                   @Param("menuId") int menuId);

    // name + price로 menu_id 조회 (HTML 방식 A 대응)
    Integer findMenuIdByNameAndPrice(@Param("name") String name,
                                     @Param("price") int price);

    // 항목 추가
    void insert(CartItem cartItem);

    // 수량 변경
    void updateQuantity(@Param("cartId") int cartId,
                        @Param("menuId") int menuId,
                        @Param("quantity") int quantity);

    // 단건 삭제
    void deleteByCartIdAndMenuId(@Param("cartId") int cartId,
                                 @Param("menuId") int menuId);

    // 전체 삭제 (장바구니 비우기)
    void deleteAllByCartId(int cartId);
}
