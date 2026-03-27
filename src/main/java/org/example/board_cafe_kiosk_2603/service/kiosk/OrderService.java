package org.example.board_cafe_kiosk_2603.service.kiosk;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.kiosk.OrderItem;
import org.example.board_cafe_kiosk_2603.domain.kiosk.Orders;
import org.example.board_cafe_kiosk_2603.dto.kiosk.OrdersDTO;
import org.example.board_cafe_kiosk_2603.dto.kiosk.OrderItemDTO;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.CartMapper;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.OrdersMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 주문 조회 및 상태 관리 서비스.
 * 주문 생성/결제는 PaymentService / TossPaymentService 에서 담당합니다.
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrdersMapper ordersMapper;
    private final CartMapper   cartMapper;

    // ===================================================
    // 주문 단건 조회 (orderId)
    // ===================================================

    public OrdersDTO getOrder(int orderId) {
        Orders order = ordersMapper.findByOrderId(orderId);
        if (order == null) {
            return OrdersDTO.fail("주문을 찾을 수 없습니다: " + orderId);
        }
        List<OrderItemDTO> items = findItems(orderId);
        return OrdersDTO.of(order, items);
    }

    // ===================================================
    // 테이블의 최근 주문 조회
    // ===================================================

    public OrdersDTO getLatestOrder(int tableNumber) {
        Integer tableId = cartMapper.findCafeTableIdByTableNumber(tableNumber);
        if (tableId == null) {
            return OrdersDTO.fail("존재하지 않는 테이블 번호입니다: " + tableNumber);
        }
        Orders order = ordersMapper.findLatestByTableId(tableId);
        if (order == null) {
            return OrdersDTO.fail("주문 내역이 없습니다.");
        }
        List<OrderItemDTO> items = findItems(order.getId());
        return OrdersDTO.of(order, items);
    }

    // ===================================================
    // 세션의 주문 목록 조회
    // ===================================================

    public List<OrdersDTO> getOrdersBySession(long sessionId) {
        return ordersMapper.findBySessionId(sessionId).stream()
                .map(order -> OrdersDTO.of(order, findItems(order.getId())))
                .collect(Collectors.toList());
    }

    // ===================================================
    // 주문 상태 변경
    // ===================================================

    public OrdersDTO updateStatus(int orderId, String status) {
        Orders order = ordersMapper.findByOrderId(orderId);
        if (order == null) {
            return OrdersDTO.fail("주문을 찾을 수 없습니다: " + orderId);
        }

        ordersMapper.updateOrderStatus(
                Orders.builder()
                        .id(orderId)
                        .status(status)
                        .build());

        log.info("주문 상태 변경 - orderId: {}, {} → {}", orderId, order.getStatus(), status);
        return OrdersDTO.of(
                Orders.builder()
                        .id(orderId)
                        .sessionId(order.getSessionId())
                        .tableId(order.getTableId())
                        .customerPhone(order.getCustomerPhone())
                        .status(status)
                        .totalAmount(order.getTotalAmount())
                        .orderedAt(order.getOrderedAt())
                        .build(),
                findItems(orderId));
    }

    // ===================================================
    // 헬퍼
    // ===================================================

    private List<OrderItemDTO> findItems(int orderId) {
        return ordersMapper.findItemsByOrderId(orderId).stream()
                .map(OrderItemDTO::from)
                .collect(Collectors.toList());
    }
}
