package org.example.board_cafe_kiosk_2603.service.kiosk;

import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.kiosk.Cart;
import org.example.board_cafe_kiosk_2603.domain.kiosk.CartItem;
import org.example.board_cafe_kiosk_2603.dto.kiosk.CartDTO;
import org.example.board_cafe_kiosk_2603.dto.kiosk.CartItemDTO;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.CartItemMapper;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.CartMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@Log4j2
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartMapper     cartMapper;
    @Mock private CartItemMapper cartItemMapper;

    @InjectMocks
    private CartService cartService;

    private static final int TABLE_NUMBER = 5;
    private static final int TABLE_ID     = 10;
    private static final int CART_ID      = 100;
    private static final int MENU_ID      = 1;

    private Cart     mockCart;
    private CartItem mockCartItem;

    @BeforeEach
    void setUp() {
        mockCart     = Cart.builder().id(CART_ID).tableId(TABLE_ID).build();
        mockCartItem = CartItem.builder()
                .id(1).cartId(CART_ID).menuId(MENU_ID)
                .menuName("아이스 아메리카노").menuPrice(4000).quantity(2)
                .build();
    }

    // ===================================================
    // getCart
    // ===================================================

    @Test
    void getCart_withItems() {
        given(cartMapper.findCafeTableIdByTableNumber(TABLE_NUMBER)).willReturn(TABLE_ID);
        given(cartMapper.findByTableId(TABLE_ID)).willReturn(mockCart);
        given(cartItemMapper.findByCartId(CART_ID)).willReturn(List.of(mockCartItem));

        CartDTO result = cartService.getCart(TABLE_NUMBER);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getCartItems()).hasSize(1);
        assertThat(result.getTotalPrice()).isEqualTo(8000);   // 4000 * 2
        assertThat(result.getCartCount()).isEqualTo(1);
        assertThat(result.getCartItems().get(0).getMenuName()).isEqualTo("아이스 아메리카노");
    }

    @Test
    void getCart_noCart() {
        given(cartMapper.findCafeTableIdByTableNumber(TABLE_NUMBER)).willReturn(TABLE_ID);
        given(cartMapper.findByTableId(TABLE_ID)).willReturn(null);

        CartDTO result = cartService.getCart(TABLE_NUMBER);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getCartItems()).isEmpty();
        assertThat(result.getTotalPrice()).isEqualTo(0);
    }

    @Test
    void getCart_exception_returnsFail() {
        given(cartMapper.findCafeTableIdByTableNumber(TABLE_NUMBER))
                .willThrow(new RuntimeException("DB error"));

        CartDTO result = cartService.getCart(TABLE_NUMBER);

        assertThat(result.isSuccess()).isFalse();
    }

    // ===================================================
    // addItem
    // ===================================================

    @Test
    void addItem_newItem_success() {
        CartItemDTO request = mockItemDTO("아이스 아메리카노", 4000, 2);

        given(cartItemMapper.findMenuIdByNameAndPrice("아이스 아메리카노", 4000)).willReturn(MENU_ID);
        given(cartMapper.findCafeTableIdByTableNumber(TABLE_NUMBER)).willReturn(TABLE_ID);
        given(cartMapper.findByTableId(TABLE_ID)).willReturn(mockCart);
        given(cartItemMapper.findByCartIdAndMenuId(CART_ID, MENU_ID)).willReturn(null);
        given(cartItemMapper.findByCartId(CART_ID)).willReturn(List.of(mockCartItem));

        CartDTO result = cartService.addItem(TABLE_NUMBER, request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).contains("아이스 아메리카노");
        assertThat(result.getCartCount()).isEqualTo(1);
        then(cartItemMapper).should().insert(any(CartItem.class));
    }

    @Test
    void addItem_existingItem_accumulates() {
        CartItemDTO request  = mockItemDTO("아이스 아메리카노", 4000, 2);
        CartItem    existing = CartItem.builder().cartId(CART_ID).menuId(MENU_ID).quantity(1).build();

        given(cartItemMapper.findMenuIdByNameAndPrice("아이스 아메리카노", 4000)).willReturn(MENU_ID);
        given(cartMapper.findCafeTableIdByTableNumber(TABLE_NUMBER)).willReturn(TABLE_ID);
        given(cartMapper.findByTableId(TABLE_ID)).willReturn(mockCart);
        given(cartItemMapper.findByCartIdAndMenuId(CART_ID, MENU_ID)).willReturn(existing);
        given(cartItemMapper.findByCartId(CART_ID)).willReturn(List.of(mockCartItem));

        CartDTO result = cartService.addItem(TABLE_NUMBER, request);

        assertThat(result.isSuccess()).isTrue();
        then(cartItemMapper).should().updateQuantity(CART_ID, MENU_ID, 3); // 1+2=3
    }

    @Test
    void addItem_menuNotFound_returnsFail() {
        CartItemDTO request = mockItemDTO("없는메뉴", 9999, 1);
        given(cartItemMapper.findMenuIdByNameAndPrice("없는메뉴", 9999)).willReturn(null);

        CartDTO result = cartService.addItem(TABLE_NUMBER, request);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("없는메뉴");
        then(cartItemMapper).should(never()).insert(any());
    }

    @Test
    void addItem_cartAutoCreated() {
        CartItemDTO request = mockItemDTO("아이스 아메리카노", 4000, 1);

        given(cartItemMapper.findMenuIdByNameAndPrice("아이스 아메리카노", 4000)).willReturn(MENU_ID);
        given(cartMapper.findCafeTableIdByTableNumber(TABLE_NUMBER)).willReturn(TABLE_ID);
        // 첫 번째 호출: null(카트 없음), 두 번째 호출: mockCart(생성 후 조회)
        given(cartMapper.findByTableId(TABLE_ID)).willReturn(null).willReturn(mockCart);
        given(cartItemMapper.findByCartIdAndMenuId(CART_ID, MENU_ID)).willReturn(null);
        given(cartItemMapper.findByCartId(CART_ID)).willReturn(List.of());

        cartService.addItem(TABLE_NUMBER, request);

        then(cartMapper).should().insert(any(Cart.class));
    }

    // ===================================================
    // updateItem
    // ===================================================

    @Test
    void updateItem_changeQuantity() {
        CartItemDTO request = mockItemDTO("아이스 아메리카노", 4000, 3);

        given(cartItemMapper.findMenuIdByNameAndPrice("아이스 아메리카노", 4000)).willReturn(MENU_ID);
        given(cartMapper.findCafeTableIdByTableNumber(TABLE_NUMBER)).willReturn(TABLE_ID);
        given(cartMapper.findByTableId(TABLE_ID)).willReturn(mockCart);
        given(cartItemMapper.findByCartId(CART_ID)).willReturn(List.of(mockCartItem));

        CartDTO result = cartService.updateItem(TABLE_NUMBER, request);

        assertThat(result.isSuccess()).isTrue();
        then(cartItemMapper).should().updateQuantity(CART_ID, MENU_ID, 3);
    }

    @Test
    void updateItem_zeroQuantity_deletesItem() {
        CartItemDTO request = mockItemDTO("아이스 아메리카노", 4000, 0);

        given(cartItemMapper.findMenuIdByNameAndPrice("아이스 아메리카노", 4000)).willReturn(MENU_ID);
        given(cartMapper.findCafeTableIdByTableNumber(TABLE_NUMBER)).willReturn(TABLE_ID);
        given(cartMapper.findByTableId(TABLE_ID)).willReturn(mockCart);
        given(cartItemMapper.findByCartId(CART_ID)).willReturn(List.of());

        CartDTO result = cartService.updateItem(TABLE_NUMBER, request);

        assertThat(result.isSuccess()).isTrue();
        then(cartItemMapper).should().deleteByCartIdAndMenuId(CART_ID, MENU_ID);
    }

    @Test
    void updateItem_menuNotFound_returnsFail() {
        CartItemDTO request = mockItemDTO("없는메뉴", 9999, 2);
        given(cartItemMapper.findMenuIdByNameAndPrice("없는메뉴", 9999)).willReturn(null);

        CartDTO result = cartService.updateItem(TABLE_NUMBER, request);

        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    void updateItem_noCart_returnsFail() {
        CartItemDTO request = mockItemDTO("아이스 아메리카노", 4000, 2);

        given(cartItemMapper.findMenuIdByNameAndPrice("아이스 아메리카노", 4000)).willReturn(MENU_ID);
        given(cartMapper.findCafeTableIdByTableNumber(TABLE_NUMBER)).willReturn(TABLE_ID);
        given(cartMapper.findByTableId(TABLE_ID)).willReturn(null);

        CartDTO result = cartService.updateItem(TABLE_NUMBER, request);

        assertThat(result.isSuccess()).isFalse();
    }

    // ===================================================
    // clearCart
    // ===================================================

    @Test
    void clearCart_success() {
        given(cartMapper.findCafeTableIdByTableNumber(TABLE_NUMBER)).willReturn(TABLE_ID);
        given(cartMapper.findByTableId(TABLE_ID)).willReturn(mockCart);

        CartDTO result = cartService.clearCart(TABLE_NUMBER);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getCartCount()).isEqualTo(0);
        then(cartItemMapper).should().deleteAllByCartId(CART_ID);
    }

    @Test
    void clearCart_noCart_stillSuccess() {
        given(cartMapper.findCafeTableIdByTableNumber(TABLE_NUMBER)).willReturn(TABLE_ID);
        given(cartMapper.findByTableId(TABLE_ID)).willReturn(null);

        CartDTO result = cartService.clearCart(TABLE_NUMBER);

        assertThat(result.isSuccess()).isTrue();
        then(cartItemMapper).should(never()).deleteAllByCartId(anyInt());
    }

    // ===== 헬퍼 =====
    private CartItemDTO mockItemDTO(String menuName, int menuPrice, int quantity) {
        return CartItemDTO.builder()
                .menuName(menuName)
                .menuPrice(menuPrice)
                .quantity(quantity)
                .build();
    }
}
