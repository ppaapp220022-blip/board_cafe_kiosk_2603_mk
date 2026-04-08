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
import org.example.board_cafe_kiosk_2603.mapper.common.cafeTableSession.CafeTableSessionMapper;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.cart.CartItemMapper;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.cart.CartMapper;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.order.OrdersMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 주문 관리 서비스
 * 웹소켓 통합 - 상태 변경 시 /topic/orders/{tableId}로 실시간 전송
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrdersMapper ordersMapper;
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;
    private final CafeTableSessionMapper tableSessionMapper;
    private final SimpMessagingTemplate messagingTemplate;

    // ===================================================
    // 주문 생성
    // ===================================================

    @Transactional
    public OrdersDTO createOrderFromCart(int tableNumber, String customerPhone, int totalAmount) {
        // tableNumber → tableId 변환
        Integer tableId = cartMapper.findCafeTableIdByTableNumber(tableNumber);
        if (tableId == null) {
            return OrdersDTO.builder()
                    .success(false)
                    .message("테이블을 찾을 수 없습니다.")
                    .build();
        }

        // 활성 세션 조회
        var session = tableSessionMapper.findActiveByTableId(tableId);
        if (session == null) {
            return OrdersDTO.builder()
                    .success(false)
                    .message("활성 세션이 없습니다. 패키지를 먼저 선택해주세요.")
                    .build();
        }

        // 카트 조회
        Cart cart = cartMapper.findByTableId(tableId);
        if (cart == null) {
            return OrdersDTO.builder().success(false).message("장바구니가 없습니다.").build();
        }

        List<CartItem> cartItems = cartItemMapper.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            return OrdersDTO.builder().success(false).message("장바구니가 비어있습니다.").build();
        }

        // 주문 생성
        Orders order = Orders.builder()
                .sessionId(session.getId())
                .tableId(tableId)
                .customerPhone((customerPhone != null && !customerPhone.isBlank()) ? customerPhone : null)
                .status(OrderStatus.ORDERED.name())
                .totalAmount(totalAmount)
                .build();
        ordersMapper.insertOrder(order);
        log.info(" 주문 생성 - orderId: {}, tableId: {}, amount: {}", order.getId(), tableId, totalAmount);

        // 주문 항목 추가
        for (CartItem ci : cartItems) {
            ordersMapper.insertOrderItem(OrderItem.builder()
                    .orderId(order.getId())
                    .menuId(ci.getMenuId())
                    .menuName(ci.getMenuName())
                    .price(ci.getMenuPrice())
                    .quantity(ci.getQuantity())
                    .build());
        }

        // 장바구니 비우기
        cartItemMapper.deleteAllByCartId(cart.getId());

        OrdersDTO result = toDTO(order, fetchItemDTOs(order.getId()));

        // 웹소켓: 신규 주문 알림
        broadcastNewOrder(result, tableId);

        return result;
    }

    // ===================================================
    // 주문 조회
    // ===================================================

    public OrdersDTO getOrder(int orderId) {
        Orders order = ordersMapper.findByOrderId(orderId);
        if (order == null) {
            return OrdersDTO.builder().success(false).message("주문을 찾을 수 없습니다.").build();
        }
        return toDTO(order, fetchItemDTOs(orderId));
    }

    public List<OrdersDTO> getNewOrders() {
        return ordersMapper.findByStatus(OrderStatus.ORDERED.name()).stream()
                .map(order -> toDTO(order, fetchItemDTOs(order.getId())))
                .collect(Collectors.toList());
    }

    public List<OrdersDTO> getOrdersByTableId(int tableId) {
        var session = tableSessionMapper.findActiveByTableId(tableId);
        if (session == null) return List.of();
        return ordersMapper.findBySessionId(session.getId()).stream()
                .map(order -> toDTO(order, fetchItemDTOs(order.getId())))
                .collect(Collectors.toList());
    }

    public OrdersDTO getLatestOrderByTableNumber(int tableNumber) {
        Integer tableId = cartMapper.findCafeTableIdByTableNumber(tableNumber);
        if (tableId == null) {
            return OrdersDTO.builder().success(false).message("테이블을 찾을 수 없습니다.").build();
        }
        Orders order = ordersMapper.findLatestByTableId(tableId);
        if (order == null) {
            return OrdersDTO.builder().success(false).message("주문이 없습니다.").build();
        }
        return toDTO(order, fetchItemDTOs(order.getId()));
    }

    public List<OrdersDTO> getOrdersBySessionId(long sessionId) {
        return ordersMapper.findBySessionId(sessionId).stream()
                .map(order -> toDTO(order, fetchItemDTOs(order.getId())))
                .collect(Collectors.toList());
    }

    public List<OrdersDTO> getActiveSessionOrders(int tableNumber) {
        Integer tableId = cartMapper.findCafeTableIdByTableNumber(tableNumber);
        if (tableId == null) return List.of();
        return getOrdersByTableId(tableId);
    }

    // ===================================================
    // 주문 상태 변경
    // ===================================================

    @Transactional
    public OrdersDTO updateStatus(int orderId, String newStatus) {
        Orders order = ordersMapper.findByOrderId(orderId);
        if (order == null) {
            return OrdersDTO.builder().success(false).message("주문을 찾을 수 없습니다.").build();
        }

        OrderStatus current;
        OrderStatus next;
        try {
            current = order.getStatusEnum();
            next = OrderStatus.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            return OrdersDTO.builder().success(false).message("알 수 없는 상태입니다.").build();
        }

        // 상태 전이 검증
        try {
            current.validateTransitionTo(next);
        } catch (IllegalStateException e) {
            return OrdersDTO.builder().success(false).message(e.getMessage()).build();
        }

        // DB 업데이트
        ordersMapper.updateOrderStatus(Orders.builder().id(orderId).status(next.name()).build());
        log.info(" 주문 상태 변경 - orderId: {}, {} → {}", orderId, current.name(), next.name());

        Orders updated = ordersMapper.findByOrderId(orderId);
        OrdersDTO result = toDTO(updated, fetchItemDTOs(orderId));

        // 웹소켓: 상태 변경 실시간 전송
        broadcastOrderUpdate(result, order.getTableId());

        return result;
    }

    @Transactional
    public OrdersDTO cancelOrder(int orderId) {
        return updateStatus(orderId, OrderStatus.CANCELLED.name());
    }

    // ===================================================
    // 웹소켓 브로드캐스팅
    // ===================================================

    /**
     * 신규 주문 알림
     */
    private void broadcastNewOrder(OrdersDTO order, int tableId) {
        try {
            messagingTemplate.convertAndSend("/topic/new-orders", order);
            log.info("📡 신규 주문 브로드캐스트 - orderId: {}, tableId: {}", order.getId(), tableId);
        } catch (Exception e) {
            log.warn("웹소켓 전송 실패: {}", e.getMessage());
        }
    }

    /**
     * 주문 상태 변경 알림
     */
    private void broadcastOrderUpdate(OrdersDTO order, int tableId) {
        try {
            List<OrdersDTO> orders = getOrdersByTableId(tableId);
            messagingTemplate.convertAndSend("/topic/orders/" + tableId, orders);
            log.info("📡 주문 상태 업데이트 - tableId: {}, 주문 수: {}", tableId, orders.size());
        } catch (Exception e) {
            log.warn("웹소켓 전송 실패: {}", e.getMessage());
        }
    }

    // ===================================================
    // 헬퍼 메서드
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