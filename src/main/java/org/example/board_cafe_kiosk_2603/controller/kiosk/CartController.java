package org.example.board_cafe_kiosk_2603.controller.kiosk;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.dto.kiosk.CartDTO;
import org.example.board_cafe_kiosk_2603.dto.kiosk.CartItemDTO;
import org.example.board_cafe_kiosk_2603.service.kiosk.CartService;
import org.example.board_cafe_kiosk_2603.service.kiosk.KioskPageService;
import org.springframework.web.bind.annotation.*;

/**
 * 키오스크 장바구니 REST API 컨트롤러.
 * URL: /kiosk/cart/*
 */
@Log4j2
@RestController
@RequestMapping("/kiosk/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService      cartService;
    private final KioskPageService kioskPageService;

    @GetMapping("/items")
    public CartDTO getCart(
            @RequestParam(required = false, defaultValue = "1") Integer tableNumber,
            HttpSession session) {
        return cartService.getCart(kioskPageService.resolveTableNumber(tableNumber, session));
    }

    @PostMapping("/add")
    public CartDTO addToCart(
            @RequestBody @Valid CartItemDTO request,
            @RequestParam(required = false, defaultValue = "1") Integer tableNumber,
            HttpSession session) {
        tableNumber = kioskPageService.resolveTableNumber(tableNumber, session);
        log.info("장바구니 추가 - 테이블: {}, 메뉴: {} x{} (₩{})",
                tableNumber, request.getMenuName(), request.getQuantity(), request.getMenuPrice());
        return cartService.addItem(tableNumber, request);
    }

    @PutMapping("/update")
    public CartDTO updateCartItem(
            @RequestBody @Valid CartItemDTO request,
            @RequestParam(required = false, defaultValue = "1") Integer tableNumber,
            HttpSession session) {
        return cartService.updateItem(
                kioskPageService.resolveTableNumber(tableNumber, session), request);
    }

    @DeleteMapping("/clear")
    public CartDTO clearCart(
            @RequestParam(required = false, defaultValue = "1") Integer tableNumber,
            HttpSession session) {
        return cartService.clearCart(
                kioskPageService.resolveTableNumber(tableNumber, session));
    }
}
