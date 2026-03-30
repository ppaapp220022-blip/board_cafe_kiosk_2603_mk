package org.example.board_cafe_kiosk_2603.controller.kiosk.cart;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.kiosk.cart.Cart;
import org.example.board_cafe_kiosk_2603.domain.kiosk.cart.CartItem;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.cart.CartItemMapper;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.cart.CartMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 장바구니 페이지 + REST API (DB 연동).
 *
 * [페이지] GET    /kiosk/cart        → cart.html
 * [API]   GET    /kiosk/cart/items  → 장바구니 조회
 *         POST   /kiosk/cart/add    → 상품 추가
 *         PUT    /kiosk/cart/update → 수량 변경 / 삭제
 *         DELETE /kiosk/cart/clear  → 전체 비우기
 */
@Log4j2
@Controller
@RequestMapping("/kiosk/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartMapper     cartMapper;
    private final CartItemMapper cartItemMapper;

    // ===========================================================
    // 페이지
    // ===========================================================

    @GetMapping
    public String cartPage(
            @RequestParam(required = false, defaultValue = "1") Integer tableNumber,
            HttpSession session, Model model) {
        model.addAttribute("tableNumber", tableNumber);
        model.addAttribute("partySize",   getPartySize(session));
        return "kiosk/cart";
    }

    // ===========================================================
    // REST API
    // ===========================================================

    @GetMapping("/items")
    @ResponseBody
    public Map<String, Object> getCart(
            @RequestParam(required = false, defaultValue = "1") Integer tableNumber) {

        Integer tableId = cartMapper.findCafeTableIdByTableNumber(tableNumber);
        Map<String, Object> res = new LinkedHashMap<>();

        if (tableId == null) {
            res.put("success",    false);
            res.put("message",    "테이블을 찾을 수 없습니다.");
            res.put("cartItems",  List.of());
            res.put("cartCount",  0);
            res.put("totalPrice", 0);
            return res;
        }

        Cart cart = cartMapper.findByTableId(tableId);
        if (cart == null) {
            res.put("success",    true);
            res.put("cartItems",  List.of());
            res.put("cartCount",  0);
            res.put("totalPrice", 0);
            return res;
        }

        List<CartItem> items = cartItemMapper.findByCartId(cart.getId());
        int total = items.stream().mapToInt(i -> i.getMenuPrice() * i.getQuantity()).sum();

        res.put("success",    true);
        res.put("cartItems",  items);
        res.put("cartCount",  items.size());
        res.put("totalPrice", total);
        return res;
    }

    @PostMapping("/add")
    @ResponseBody
    public Map<String, Object> addToCart(
            @RequestBody Map<String, Object> req,
            @RequestParam(required = false, defaultValue = "1") Integer tableNumber) {

        String menuName  = (String) req.get("menuName");
        int    menuPrice = toInt(req.get("menuPrice"));
        int    quantity  = toInt(req.get("quantity"));

        log.info("장바구니 추가 - 테이블: {}, 메뉴: {} x{} (₩{})", tableNumber, menuName, quantity, menuPrice);

        Map<String, Object> res = new LinkedHashMap<>();

        // 1. tableId 조회
        Integer tableId = cartMapper.findCafeTableIdByTableNumber(tableNumber);
        if (tableId == null) {
            res.put("success", false);
            res.put("message", "테이블을 찾을 수 없습니다.");
            return res;
        }

        // 2. menuId 조회
        Integer menuId = cartItemMapper.findMenuIdByNameAndPrice(menuName, menuPrice);
        if (menuId == null) {
            res.put("success", false);
            res.put("message", "메뉴를 찾을 수 없습니다: " + menuName);
            return res;
        }

        // 3. cart 조회 or 생성
        Cart cart = cartMapper.findByTableId(tableId);
        if (cart == null) {
            cart = Cart.builder().tableId(tableId).build();
            cartMapper.insert(cart);
        }

        // 4. 기존 항목 확인 → 수량 누적 or 신규 추가
        CartItem existing = cartItemMapper.findByCartIdAndMenuId(cart.getId(), menuId);
        if (existing != null) {
            cartItemMapper.updateQuantity(cart.getId(), menuId, existing.getQuantity() + quantity);
        } else {
            cartItemMapper.insert(CartItem.builder()
                    .cartId(cart.getId())
                    .menuId(menuId)
                    .quantity(quantity)
                    .build());
        }
        cartMapper.updateTimestamp(cart.getId());

        // 5. 최신 cart 조회 후 응답
        List<CartItem> items = cartItemMapper.findByCartId(cart.getId());
        int total = items.stream().mapToInt(i -> i.getMenuPrice() * i.getQuantity()).sum();

        res.put("success",    true);
        res.put("message",    menuName + "이(가) 장바구니에 추가되었습니다.");
        res.put("cartCount",  items.size());
        res.put("totalPrice", total);
        return res;
    }

    @PutMapping("/update")
    @ResponseBody
    public Map<String, Object> updateCart(
            @RequestBody Map<String, Object> req,
            @RequestParam(required = false, defaultValue = "1") Integer tableNumber) {

        String menuName  = (String) req.get("menuName");
        int    menuPrice = toInt(req.get("menuPrice"));
        int    quantity  = toInt(req.get("quantity"));

        Map<String, Object> res = new LinkedHashMap<>();

        Integer tableId = cartMapper.findCafeTableIdByTableNumber(tableNumber);
        if (tableId == null) { res.put("success", false); return res; }

        Integer menuId = cartItemMapper.findMenuIdByNameAndPrice(menuName, menuPrice);
        if (menuId == null) { res.put("success", false); return res; }

        Cart cart = cartMapper.findByTableId(tableId);
        if (cart == null)    { res.put("success", false); return res; }

        if (quantity <= 0) {
            cartItemMapper.deleteByCartIdAndMenuId(cart.getId(), menuId);
        } else {
            cartItemMapper.updateQuantity(cart.getId(), menuId, quantity);
        }
        cartMapper.updateTimestamp(cart.getId());

        List<CartItem> items = cartItemMapper.findByCartId(cart.getId());
        int total = items.stream().mapToInt(i -> i.getMenuPrice() * i.getQuantity()).sum();

        res.put("success",    true);
        res.put("cartCount",  items.size());
        res.put("totalPrice", total);
        return res;
    }

    @DeleteMapping("/clear")
    @ResponseBody
    public Map<String, Object> clearCart(
            @RequestParam(required = false, defaultValue = "1") Integer tableNumber) {

        Map<String, Object> res = new LinkedHashMap<>();

        Integer tableId = cartMapper.findCafeTableIdByTableNumber(tableNumber);
        if (tableId == null) { res.put("success", false); return res; }

        Cart cart = cartMapper.findByTableId(tableId);
        if (cart != null) {
            cartItemMapper.deleteAllByCartId(cart.getId());
            cartMapper.updateTimestamp(cart.getId());
        }

        res.put("success",    true);
        res.put("cartCount",  0);
        res.put("totalPrice", 0);
        return res;
    }

    // ===========================================================
    // 헬퍼
    // ===========================================================

    private int getPartySize(HttpSession session) {
        Object val = session.getAttribute("partySize");
        return val instanceof Integer ? (Integer) val : 2;
    }

    private int toInt(Object val) {
        if (val == null) return 0;
        return ((Number) val).intValue();
    }
}
