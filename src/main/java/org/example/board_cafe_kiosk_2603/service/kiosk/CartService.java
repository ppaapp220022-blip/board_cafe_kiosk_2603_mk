package org.example.board_cafe_kiosk_2603.service.kiosk;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.kiosk.Cart;
import org.example.board_cafe_kiosk_2603.domain.kiosk.CartItem;
import org.example.board_cafe_kiosk_2603.dto.kiosk.CartDTO;
import org.example.board_cafe_kiosk_2603.dto.kiosk.CartItemDTO;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.CartItemMapper;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.CartMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartMapper     cartMapper;
    private final CartItemMapper cartItemMapper;

    // ===================================================
    // tableNumber → cafe_table.id(PK) 변환
    // ===================================================

    private int resolveTableId(int tableNumber) {
        Integer cafeTableId = cartMapper.findCafeTableIdByTableNumber(tableNumber);
        if (cafeTableId == null) {
            throw new IllegalArgumentException("존재하지 않는 테이블 번호입니다: " + tableNumber);
        }
        return cafeTableId;
    }

    // ===================================================
    // 장바구니 조회
    // ===================================================

    public CartDTO getCart(int tableNumber) {
        try {
            int tableId = resolveTableId(tableNumber);
            Cart cart = cartMapper.findByTableId(tableId);
            if (cart == null) return CartDTO.empty();

            List<CartItem> items = cartItemMapper.findByCartId(cart.getId());
            List<CartItemDTO> itemDTOs = items.stream()
                    .map(CartItemDTO::from)
                    .collect(Collectors.toList());

            int total = itemDTOs.stream()
                    .mapToInt(r -> r.getMenuPrice() * r.getQuantity())
                    .sum();

            return CartDTO.fetched(itemDTOs, total);
        } catch (Exception e) {
            log.warn("장바구니 조회 실패 - tableNumber: {}, 원인: {}", tableNumber, e.getMessage());
            return CartDTO.fail();
        }
    }

    // ===================================================
    // 장바구니에 상품 추가
    // ===================================================

    @Transactional
    public CartDTO addItem(int tableNumber, CartItemDTO request) {
        Integer menuId = cartItemMapper.findMenuIdByNameAndPrice(request.getMenuName(), request.getMenuPrice());
        if (menuId == null) {
            return CartDTO.fail("메뉴를 찾을 수 없습니다: " + request.getMenuName());
        }

        int tableId = resolveTableId(tableNumber);
        Cart cart = getOrCreateCart(tableId);

        CartItem existing = cartItemMapper.findByCartIdAndMenuId(cart.getId(), menuId);
        if (existing != null) {
            int newQty = existing.getQuantity() + request.getQuantity();
            cartItemMapper.updateQuantity(cart.getId(), menuId, newQty);
            log.info("수량 누적 - 메뉴: {}, {}→{}", request.getMenuName(), existing.getQuantity(), newQty);
        } else {
            CartItem newItem = CartItem.builder()
                    .cartId(cart.getId())
                    .menuId(menuId)
                    .quantity(request.getQuantity())
                    .build();
            cartItemMapper.insert(newItem);
            log.info("신규 추가 - 메뉴: {}, 수량: {}", request.getMenuName(), request.getQuantity());
        }

        cartMapper.updateTimestamp(cart.getId());
        int cartCount = cartItemMapper.findByCartId(cart.getId()).size();

        return CartDTO.added(request.getMenuName(), cartCount);
    }

    // ===================================================
    // 수량 변경 (quantity <= 0 이면 삭제)
    // ===================================================

    @Transactional
    public CartDTO updateItem(int tableNumber, CartItemDTO request) {
        Integer menuId = cartItemMapper.findMenuIdByNameAndPrice(request.getMenuName(), request.getMenuPrice());
        if (menuId == null) {
            return CartDTO.fail("메뉴를 찾을 수 없습니다.");
        }

        int tableId = resolveTableId(tableNumber);
        Cart cart = cartMapper.findByTableId(tableId);
        if (cart == null) {
            return CartDTO.fail("장바구니가 없습니다.");
        }

        if (request.getQuantity() <= 0) {
            cartItemMapper.deleteByCartIdAndMenuId(cart.getId(), menuId);
            log.info("항목 삭제 - 메뉴: {}", request.getMenuName());
        } else {
            cartItemMapper.updateQuantity(cart.getId(), menuId, request.getQuantity());
            log.info("수량 변경 - 메뉴: {}, 수량: {}", request.getMenuName(), request.getQuantity());
        }

        cartMapper.updateTimestamp(cart.getId());

        List<CartItem> items = cartItemMapper.findByCartId(cart.getId());
        int total = items.stream().mapToInt(i -> i.getMenuPrice() * i.getQuantity()).sum();

        return CartDTO.updated(items.size(), total);
    }

    // ===================================================
    // 장바구니 전체 비우기
    // ===================================================

    @Transactional
    public CartDTO clearCart(int tableNumber) {
        try {
            int tableId = resolveTableId(tableNumber);
            Cart cart = cartMapper.findByTableId(tableId);
            if (cart != null) {
                cartItemMapper.deleteAllByCartId(cart.getId());
                log.info("장바구니 비우기 - tableNumber: {}", tableNumber);
            }
        } catch (Exception e) {
            log.warn("장바구니 비우기 실패: {}", e.getMessage());
        }
        return CartDTO.cleared();
    }

    // ===================================================
    // 헬퍼
    // ===================================================

    private Cart getOrCreateCart(int tableId) {
        Cart cart = cartMapper.findByTableId(tableId);
        if (cart == null) {
            Cart newCart = Cart.builder().tableId(tableId).build();
            cartMapper.insert(newCart);
            cart = cartMapper.findByTableId(tableId);
            log.info("장바구니 신규 생성 - tableId(PK): {}", tableId);
        }
        return cart;
    }
}
