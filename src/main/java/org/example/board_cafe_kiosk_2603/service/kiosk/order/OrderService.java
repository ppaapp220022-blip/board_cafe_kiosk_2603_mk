package org.example.board_cafe_kiosk_2603.service.kiosk.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.kiosk.cart.Cart;
import org.example.board_cafe_kiosk_2603.domain.kiosk.cart.CartItem;
import org.example.board_cafe_kiosk_2603.domain.kiosk.order.OrderItem;
import org.example.board_cafe_kiosk_2603.domain.kiosk.order.OrderStatus;
import org.example.board_cafe_kiosk_2603.domain.kiosk.order.Orders;
import org.example.board_cafe_kiosk_2603.dto.kiosk.order.OrderItemDTO;
import org.example.board_cafe_kiosk_2603.dto.kiosk.order.OrdersDTO;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.cart.CartItemMapper;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.cart.CartMapper;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.order.OrdersMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 주문 생성 · 조회 · 상태 관리 서비스.
 *
 * [상태 전이]
 *   PENDING    → PAID       : 결제 완료
 *   PAID       → CONFIRMED  : 관리자 확인
 *   CONFIRMED  → COOKING    : 조리 시작
 *   COOKING    → DELIVERING : 서빙 시작
 *   DELIVERING → COMPLETED  : 서빙 완료
 *   (COMPLETED · CANCELLED 제외) → CANCELLED : 취소
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrdersMapper   ordersMapper;
    private final CartMapper     cartMapper;
    private final CartItemMapper cartItemMapper;

    // ===================================================
    // 카트 → 주문 생성
    // ===================================================

    @Transactional
    public OrdersDTO createOrderFromCart(int tableId, long sessionId,
                                         String customerPhone, int totalAmount) {
        Cart cart = cartMapper.findByTableId(tableId);
        if (cart == null) {
            return OrdersDTO.builder().success(false).message("장바구니가 존재하지 않습니다.").build();
        }

        List<CartItem> cartItems = cartItemMapper.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            return OrdersDTO.builder().success(false).message("장바구니가 비어있습니다.").build();
        }

        Orders order = Orders.builder()
                .sessionId(sessionId)
                .tableId(tableId)
                .customerPhone((customerPhone != null && !customerPhone.isBlank()) ? customerPhone : null)
                .status(OrderStatus.PENDING.name())
                .totalAmount(totalAmount)
                .build();
        ordersMapper.insertOrder(order);
        log.info("주문 생성 - orderId: {}, sessionId: {}, tableId: {}, 항목 수: {}",
                order.getId(), sessionId, tableId, cartItems.size());

        for (CartItem ci : cartItems) {
            ordersMapper.insertOrderItem(OrderItem.builder()
                    .orderId(order.getId())
                    .menuId(ci.getMenuId())
                    .menuName(ci.getMenuName())
                    .price(ci.getMenuPrice())
                    .quantity(ci.getQuantity())
                    .build());
        }

        cartItemMapper.deleteAllByCartId(cart.getId());
        log.info("장바구니 비우기 완료 - cartId: {}", cart.getId());

        return toDTO(order, fetchItemDTOs(order.getId()));
    }

    // ===================================================
    // 주문 단건 조회
    // ===================================================

    public OrdersDTO getOrder(int orderId) {
        Orders order = ordersMapper.findByOrderId(orderId);
        if (order == null) {
            return OrdersDTO.builder().success(false).message("주문을 찾을 수 없습니다: " + orderId).build();
        }
        return toDTO(order, fetchItemDTOs(orderId));
    }

    // ===================================================
    // 테이블 최근 주문 조회
    // ===================================================

    public OrdersDTO getLatestOrder(int tableNumber) {
        Integer tableId = cartMapper.findCafeTableIdByTableNumber(tableNumber);
        if (tableId == null) {
            return OrdersDTO.builder().success(false).message("존재하지 않는 테이블 번호입니다: " + tableNumber).build();
        }
        Orders order = ordersMapper.findLatestByTableId(tableId);
        if (order == null) {
            return OrdersDTO.builder().success(false).message("주문 내역이 없습니다.").build();
        }
        return toDTO(order, fetchItemDTOs(order.getId()));
    }

    // ===================================================
    // 세션 주문 목록 조회
    // ===================================================

    public List<OrdersDTO> getOrdersBySession(long sessionId) {
        return ordersMapper.findBySessionId(sessionId).stream()
                .map(order -> toDTO(order, fetchItemDTOs(order.getId())))
                .collect(Collectors.toList());
    }

    // ===================================================
    // 주문 상태 변경 (전이 규칙 검증 포함)
    // ===================================================

    @Transactional
    public OrdersDTO updateStatus(int orderId, String newStatus) {
        Orders order = ordersMapper.findByOrderId(orderId);
        if (order == null) {
            return OrdersDTO.builder().success(false).message("주문을 찾을 수 없습니다: " + orderId).build();
        }

        OrderStatus current;
        OrderStatus next;
        try {
            current = order.getStatusEnum();
            next    = OrderStatus.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            return OrdersDTO.builder().success(false).message("알 수 없는 주문 상태입니다: " + newStatus).build();
        }

        try {
            current.validateTransitionTo(next);
        } catch (IllegalStateException e) {
            return OrdersDTO.builder().success(false).message(e.getMessage()).build();
        }

        ordersMapper.updateOrderStatus(Orders.builder().id(orderId).status(next.name()).build());
        log.info("주문 상태 변경 - orderId: {}, {} → {}", orderId, current.name(), next.name());

        Orders updated = Orders.builder()
                .id(orderId)
                .sessionId(order.getSessionId())
                .tableId(order.getTableId())
                .customerPhone(order.getCustomerPhone())
                .status(next.name())
                .totalAmount(order.getTotalAmount())
                .orderedAt(order.getOrderedAt())
                .build();
        return toDTO(updated, fetchItemDTOs(orderId));
    }

    // ===================================================
    // 주문 취소
    // ===================================================

    @Transactional
    public OrdersDTO cancelOrder(int orderId) {
        return updateStatus(orderId, OrderStatus.CANCELLED.name());
    }

    // ===================================================
    // 헬퍼
    // ===================================================

    private List<OrderItemDTO> fetchItemDTOs(int orderId) {
        return ordersMapper.findItemsByOrderId(orderId).stream()
                .map(item -> OrderItemDTO.builder()
                        .id(item.getId())
                        .menuId(item.getMenuId())
                        .menuName(item.getMenuName())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());
    }

    private OrdersDTO toDTO(Orders order, List<OrderItemDTO> items) {
        return OrdersDTO.builder()
                .success(true)
                .id(order.getId())
                .tableId(order.getTableId())
                .customerPhone(order.getCustomerPhone())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .orderedAt(order.getOrderedAt())
                .items(items)
                .build();
    }
}

