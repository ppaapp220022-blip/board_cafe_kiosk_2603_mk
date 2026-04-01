package org.example.board_cafe_kiosk_2603.controller.kiosk.cart;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.dto.kiosk.cart.CartDTO;
import org.example.board_cafe_kiosk_2603.dto.kiosk.cart.CartItemDTO;
import org.example.board_cafe_kiosk_2603.service.kiosk.KioskPageService;
import org.example.board_cafe_kiosk_2603.service.kiosk.cart.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * 장바구니 페이지 + REST API.
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

    private final CartService     cartService;
    private final KioskPageService kioskPageService;

    // ===========================================================
    // 페이지
    // ===========================================================

    @GetMapping
    public String cartPage(HttpSession session, Model model) {
        Integer tableNumber = tableNumber(session);
        if (tableNumber == null) return "redirect:/kiosk";
        kioskPageService.buildCartModel(model, tableNumber, session);
        return "kiosk/cart";
    }

    // ===========================================================
    // REST API
    // ===========================================================

    @GetMapping("/items")
    @ResponseBody
    public ResponseEntity<CartDTO> getCart(HttpSession session) {
        Integer tableNumber = tableNumber(session);
        if (tableNumber == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(cartService.getCart(tableNumber));
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<CartDTO> addToCart(@RequestBody CartItemDTO item,
                                             HttpSession session) {
        Integer tableNumber = tableNumber(session);
        if (tableNumber == null) return ResponseEntity.badRequest().build();
        log.info("장바구니 추가 - 테이블: {}, 메뉴: {} x{}", tableNumber, item.getMenuName(), item.getQuantity());
        return ResponseEntity.ok(cartService.addItem(tableNumber, item));
    }

    @PutMapping("/update")
    @ResponseBody
    public ResponseEntity<CartDTO> updateCart(@RequestBody CartItemDTO item,
                                              HttpSession session) {
        Integer tableNumber = tableNumber(session);
        if (tableNumber == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(cartService.updateItem(tableNumber, item));
    }

    @DeleteMapping("/clear")
    @ResponseBody
    public ResponseEntity<CartDTO> clearCart(HttpSession session) {
        Integer tableNumber = tableNumber(session);
        if (tableNumber == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(cartService.clearCart(tableNumber));
    }

    // ===========================================================
    // 헬퍼
    // ===========================================================

    private Integer tableNumber(HttpSession session) {
        return (Integer) session.getAttribute("tableNumber");
    }
}
