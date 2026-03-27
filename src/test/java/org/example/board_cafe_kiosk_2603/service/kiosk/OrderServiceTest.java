package org.example.board_cafe_kiosk_2603.service.kiosk;

import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.kiosk.OrderItem;
import org.example.board_cafe_kiosk_2603.domain.kiosk.Orders;
import org.example.board_cafe_kiosk_2603.dto.kiosk.OrdersDTO;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.CartMapper;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.OrdersMapper;
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
class OrderServiceTest {

    @Mock private OrdersMapper ordersMapper;
    @Mock private CartMapper   cartMapper;

    @InjectMocks
    private OrderService orderService;

    private static final int    ORDER_ID   = 1;
    private static final long   SESSION_ID = 6L;
    private static final int    TABLE_ID   = 1;
    private static final int    TABLE_NUM  = 1;

    private Orders    mockOrder;
    private OrderItem mockItem;

    @BeforeEach
    void setUp() {
        mockOrder = Orders.builder()
                .id(ORDER_ID).sessionId(SESSION_ID).tableId(TABLE_ID)
                .customerPhone("010-1234-5678").status("PAID").totalAmount(8500)
                .build();
        mockItem = OrderItem.builder()
                .id(1).orderId(ORDER_ID).menuId(1)
                .menuName("아이스 아메리카노").price(4000).quantity(2)
                .build();
    }

    // ===================================================
    // getOrder
    // ===================================================

    @Test
    void getOrder_success() {
        given(ordersMapper.findByOrderId(ORDER_ID)).willReturn(mockOrder);
        given(ordersMapper.findItemsByOrderId(ORDER_ID)).willReturn(List.of(mockItem));

        OrdersDTO result = orderService.getOrder(ORDER_ID);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getId()).isEqualTo(ORDER_ID);
        assertThat(result.getSessionId()).isEqualTo(SESSION_ID);
        assertThat(result.getStatus()).isEqualTo("PAID");
        assertThat(result.getTotalAmount()).isEqualTo(8500);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getMenuName()).isEqualTo("아이스 아메리카노");
    }

    @Test
    void getOrder_notFound() {
        given(ordersMapper.findByOrderId(99999)).willReturn(null);

        OrdersDTO result = orderService.getOrder(99999);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("찾을 수 없습니다");
    }

    // ===================================================
    // getLatestOrder
    // ===================================================

    @Test
    void getLatestOrder_success() {
        given(cartMapper.findCafeTableIdByTableNumber(TABLE_NUM)).willReturn(TABLE_ID);
        given(ordersMapper.findLatestByTableId(TABLE_ID)).willReturn(mockOrder);
        given(ordersMapper.findItemsByOrderId(ORDER_ID)).willReturn(List.of(mockItem));

        OrdersDTO result = orderService.getLatestOrder(TABLE_NUM);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTableId()).isEqualTo(TABLE_ID);
        assertThat(result.getItems()).hasSize(1);
    }

    @Test
    void getLatestOrder_tableNotFound() {
        given(cartMapper.findCafeTableIdByTableNumber(99)).willReturn(null);

        OrdersDTO result = orderService.getLatestOrder(99);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("존재하지 않는 테이블");
    }

    @Test
    void getLatestOrder_noOrders() {
        given(cartMapper.findCafeTableIdByTableNumber(TABLE_NUM)).willReturn(TABLE_ID);
        given(ordersMapper.findLatestByTableId(TABLE_ID)).willReturn(null);

        OrdersDTO result = orderService.getLatestOrder(TABLE_NUM);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("주문 내역이 없습니다");
    }

    // ===================================================
    // getOrdersBySession
    // ===================================================

    @Test
    void getOrdersBySession_success() {
        given(ordersMapper.findBySessionId(SESSION_ID)).willReturn(List.of(mockOrder));
        given(ordersMapper.findItemsByOrderId(ORDER_ID)).willReturn(List.of(mockItem));

        List<OrdersDTO> result = orderService.getOrdersBySession(SESSION_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isSuccess()).isTrue();
        assertThat(result.get(0).getItems()).hasSize(1);
    }

    @Test
    void getOrdersBySession_empty() {
        given(ordersMapper.findBySessionId(99L)).willReturn(List.of());

        List<OrdersDTO> result = orderService.getOrdersBySession(99L);

        assertThat(result).isEmpty();
    }

    // ===================================================
    // updateStatus
    // ===================================================

    @Test
    void updateStatus_success() {
        given(ordersMapper.findByOrderId(ORDER_ID)).willReturn(mockOrder);
        given(ordersMapper.findItemsByOrderId(ORDER_ID)).willReturn(List.of());

        OrdersDTO result = orderService.updateStatus(ORDER_ID, "COMPLETED");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        then(ordersMapper).should().updateOrderStatus(
                argThat(o -> o.getId() == ORDER_ID && "COMPLETED".equals(o.getStatus())));
    }

    @Test
    void updateStatus_orderNotFound() {
        given(ordersMapper.findByOrderId(99999)).willReturn(null);

        OrdersDTO result = orderService.updateStatus(99999, "COMPLETED");

        assertThat(result.isSuccess()).isFalse();
        then(ordersMapper).should(never()).updateOrderStatus(any());
    }
}
