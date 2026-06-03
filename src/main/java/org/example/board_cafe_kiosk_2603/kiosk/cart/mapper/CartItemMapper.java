package org.example.board_cafe_kiosk_2603.kiosk.cart.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.board_cafe_kiosk_2603.kiosk.cart.model.CartItem;

import java.util.List;

@Mapper

/*
 * 작성자 : 김민기
 * 기능 : 장바구니 항목 데이터 접근 인터페이스
 * 날짜 : 2026-03-27
 */
public interface CartItemMapper {

    /**
     * 장바구니 ID 기준 조회합니다.
     *
     * @param cartId 전달받은 cartId 값
     * @return 처리 결과
     */
    List<CartItem> findByCartId(int cartId);

    /**
     * 장바구니와 메뉴 기준 항목 조회합니다.
     *
     * @param cartId 전달받은 cartId 값
     * @param menuId 전달받은 menuId 값
     * @return 처리 결과
     */
    CartItem findByCartIdAndMenuId(@Param("cartId") int cartId,
                                   @Param("menuId") int menuId);

    /**
     * 메뉴명과 가격으로 메뉴 ID 조회합니다.
     *
     * @param name 전달받은 name 값
     * @param price 전달받은 price 값
     * @return 처리 결과
     */
    Integer findMenuIdByNameAndPrice(@Param("name") String name,
                                     @Param("price") int price);

    /**
     * 게임 메뉴 여부 건수 조회합니다.
     *
     * @param menuId 전달받은 menuId 값
     * @return 처리 결과
     */
    int countGameMenuByMenuId(@Param("menuId") int menuId);

    /**
     * 대여 가능한 게임 재고 건수 조회합니다.
     *
     * @param menuId 전달받은 menuId 값
     * @return 처리 결과
     */
    int countAvailableGameStockByMenuId(@Param("menuId") int menuId);

    /**
     * 데이터 등록합니다.
     *
     * @param cartItem 전달받은 cartItem 값
     */
    void insert(CartItem cartItem);

    /**
     * 장바구니 수량 수정합니다.
     *
     * @param cartId 전달받은 cartId 값
     * @param menuId 전달받은 menuId 값
     * @param quantity 전달받은 quantity 값
     */
    void updateQuantity(@Param("cartId") int cartId,
                        @Param("menuId") int menuId,
                        @Param("quantity") int quantity);

    /**
     * 장바구니 항목 삭제합니다.
     *
     * @param cartId 전달받은 cartId 값
     * @param menuId 전달받은 menuId 값
     */
    void deleteByCartIdAndMenuId(@Param("cartId") int cartId,
                                 @Param("menuId") int menuId);

    /**
     * 장바구니 전체 항목 삭제합니다.
     *
     * @param cartId 전달받은 cartId 값
     */
    void deleteAllByCartId(int cartId);
}
