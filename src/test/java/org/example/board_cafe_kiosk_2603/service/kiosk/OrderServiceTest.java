package org.example.board_cafe_kiosk_2603.service.kiosk;

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
import org.example.board_cafe_kiosk_2603.service.kiosk.order.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@Log4j2
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrdersMapper   ordersMapper;
    @Mock private CartMapper     cartMapper;
    @Mock private CartItemMapper cartItemMapper;

    @InjectMocks
    private OrderService orderService;

    private static final int  ORDER_ID    = 1;
    private static final int  TABLE_ID    = 7;
    private static final int  TABLE_NUM   = 3;
    private static final long SESSION_ID  = 100L;
    private static final int  CART_ID     = 50;
    private static final int  MENU_ID     = 10;

    private Orders    mockOrder;
    private OrderItem mockOrderItem;
    private Cart      mockCart;
    private CartItem  mockCartItem;

    @BeforeEach
    void setUp() {
        mockOrder = Orders.builder()
                .id(ORDER_ID).sessionId(SESSION_ID).tableId(TABLE_ID)
                .customerPhone("010-1234-5678").status(OrderStatus.PAID.name())
                .totalAmount(12000).orderedAt(LocalDateTime.now())
                .build();

        mockOrderItem = OrderItem.builder()
                .id(1).orderId(ORDER_ID).menuId(MENU_ID)
                .menuName("아이스 아메리카노").price(4000).quantity(3)
                .build();

        mockCart = Cart.builder().id(CART_ID).tableId(TABLE_ID).build();

        mockCartItem = CartItem.builder()
                .id(1).cartId(CART_ID).menuId(MENU_ID)
                .menuName("아이스 아메리카노").menuPrice(4000).quantity(3)
                .build();
    }

    // ===================================================
    // createOrderFromCart
    // ===================================================

    @Test
    @DisplayName("카트 → 주문 생성 성공, 카트가 비워져야 함")
    void createOrderFromCart_success() {
        given(cartMapper.findByTableId(TABLE_ID)).willReturn(mockCart);
        given(cartItemMapper.findByCartId(CART_ID)).willReturn(List.of(mockCartItem));
        given(ordersMapper.findItemsByOrderId(anyInt())).willReturn(List.of(mockOrderItem));

        OrdersDTO result = orderService.createOrderFromCart(TABLE_ID, SESSION_ID, "010-1234-5678", 12000);

        assertThat(result.isSuccess()).isTrue();
        then(ordersMapper).should().insertOrder(any(Orders.class));
        then(ordersMapper).should().insertOrderItem(any(OrderItem.class));
        // 카트 비우기 검증
        then(cartItemMapper).should().deleteAllByCartId(CART_ID);
    }

    @Test
    @DisplayName("카트 없으면 주문 생성 실패")
    void createOrderFromCart_noCart() {
        given(cartMapper.findByTableId(TABLE_ID)).willReturn(null);

        OrdersDTO result = orderService.createOrderFromCart(TABLE_ID, SESSION_ID, null, 0);

        assertThat(result.isSuccess()).isFalse();
        then(ordersMapper).should(never()).insertOrder(any());
    }

    @Test
    @DisplayName("카트 아이템 없으면 주문 생성 실패")
    void createOrderFromCart_emptyCart() {
        given(cartMapper.findByTableId(TABLE_ID)).willReturn(mockCart);
        given(cartItemMapper.findByCartId(CART_ID)).willReturn(List.of());

        OrdersDTO result = orderService.createOrderFromCart(TABLE_ID, SESSION_ID, null, 0);

        assertThat(result.isSuccess()).isFalse();
        then(ordersMapper).should(never()).insertOrder(any());
    }

    // ===================================================
    // getOrder
    // ===================================================

    @Test
    @DisplayName("주문 단건 조회 성공")
    void getOrder_success() {
        given(ordersMapper.findByOrderId(ORDER_ID)).willReturn(mockOrder);
        given(ordersMapper.findItemsByOrderId(ORDER_ID)).willReturn(List.of(mockOrderItem));

        OrdersDTO result = orderService.getOrder(ORDER_ID);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getId()).isEqualTo(ORDER_ID);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PAID.name());
        assertThat(result.getItems()).hasSize(1);
    }

    @Test
    @DisplayName("존재하지 않는 주문 조회 시 fail 반환")
    void getOrder_notFound() {
        given(ordersMapper.findByOrderId(ORDER_ID)).willReturn(null);

        OrdersDTO result = orderService.getOrder(ORDER_ID);

        assertThat(result.isSuccess()).isFalse();
        then(ordersMapper).should(never()).findItemsByOrderId(anyInt());
    }

    // ===================================================
    // getLatestOrder
    // ===================================================

    @Test
    @DisplayName("테이블 최근 주문 조회 성공")
    void getLatestOrder_success() {
        given(cartMapper.findCafeTableIdByTableNumber(TABLE_NUM)).willReturn(TABLE_ID);
        given(ordersMapper.findLatestByTableId(TABLE_ID)).willReturn(mockOrder);
        given(ordersMapper.findItemsByOrderId(ORDER_ID)).willReturn(List.of(mockOrderItem));

        OrdersDTO result = orderService.getLatestOrder(TABLE_NUM);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTableId()).isEqualTo(TABLE_ID);
    }

    @Test
    @DisplayName("존재하지 않는 테이블 번호로 최근 주문 조회 시 fail")
    void getLatestOrder_tableNotFound() {
        given(cartMapper.findCafeTableIdByTableNumber(TABLE_NUM)).willReturn(null);

        OrdersDTO result = orderService.getLatestOrder(TABLE_NUM);

        assertThat(result.isSuccess()).isFalse();
    }

    // ===================================================
    // updateStatus — 정상 전이
    // ===================================================

    @Test
    @DisplayName("상태 전이: PAID → CONFIRMED 성공")
    void updateStatus_paidToConfirmed() {
        given(ordersMapper.findByOrderId(ORDER_ID)).willReturn(mockOrder); // status=PAID
        given(ordersMapper.findItemsByOrderId(ORDER_ID)).willReturn(List.of());

        OrdersDTO result = orderService.updateStatus(ORDER_ID, "CONFIRMED");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        then(ordersMapper).should().updateOrderStatus(
                argThat(o -> o.getId() == ORDER_ID && "CONFIRMED".equals(o.getStatus())));
    }

    @Test
    @DisplayName("상태 전이: PAID → CANCELLED 성공")
    void updateStatus_cancelFromPaid() {
        given(ordersMapper.findByOrderId(ORDER_ID)).willReturn(mockOrder); // status=PAID
        given(ordersMapper.findItemsByOrderId(ORDER_ID)).willReturn(List.of());

        OrdersDTO result = orderService.cancelOrder(ORDER_ID);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getStatus()).isEqualTo("CANCELLED");
    }

    // ===================================================
    // updateStatus — 허용되지 않는 전이
    // ===================================================

    @Test
    @DisplayName("상태 전이: PAID → COOKING 불허 → fail 반환")
    void updateStatus_illegalTransition_paidToCooking() {
        given(ordersMapper.findByOrderId(ORDER_ID)).willReturn(mockOrder); // status=PAID

        OrdersDTO result = orderService.updateStatus(ORDER_ID, "COOKING");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("PAID").contains("COOKING");
        then(ordersMapper).should(never()).updateOrderStatus(any());
    }

    @Test
    @DisplayName("상태 전이: COMPLETED 에서 어떤 상태로도 변경 불허")
    void updateStatus_fromCompleted_alwaysFail() {
        Orders completed = Orders.builder()
                .id(ORDER_ID).sessionId(SESSION_ID).tableId(TABLE_ID)
                .status(OrderStatus.COMPLETED.name()).totalAmount(12000)
                .orderedAt(LocalDateTime.now()).build();
        given(ordersMapper.findByOrderId(ORDER_ID)).willReturn(completed);

        for (OrderStatus next : OrderStatus.values()) {
            OrdersDTO result = orderService.updateStatus(ORDER_ID, next.name());
            assertThat(result.isSuccess())
                    .as("COMPLETED → %s 는 항상 실패여야 함", next)
                    .isFalse();
        }
    }

    @Test
    @DisplayName("상태 전이: CANCELLED 에서 어떤 상태로도 변경 불허")
    void updateStatus_fromCancelled_alwaysFail() {
        Orders cancelled = Orders.builder()
                .id(ORDER_ID).sessionId(SESSION_ID).tableId(TABLE_ID)
                .status(OrderStatus.CANCELLED.name()).totalAmount(12000)
                .orderedAt(LocalDateTime.now()).build();
        given(ordersMapper.findByOrderId(ORDER_ID)).willReturn(cancelled);

        OrdersDTO result = orderService.updateStatus(ORDER_ID, "PAID");

        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    @DisplayName("알 수 없는 상태 문자열 입력 시 fail")
    void updateStatus_unknownStatus() {
        given(ordersMapper.findByOrderId(ORDER_ID)).willReturn(mockOrder);

        OrdersDTO result = orderService.updateStatus(ORDER_ID, "INVALID_STATUS");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("알 수 없는");
    }

    @Test
    @DisplayName("존재하지 않는 주문 상태 변경 시 fail")
    void updateStatus_orderNotFound() {
        given(ordersMapper.findByOrderId(ORDER_ID)).willReturn(null);

        OrdersDTO result = orderService.updateStatus(ORDER_ID, "CONFIRMED");

        assertThat(result.isSuccess()).isFalse();
        then(ordersMapper).should(never()).updateOrderStatus(any());
    }

    // ===================================================
    // OrderStatus 전이 규칙 단위 테스트
    // ===================================================

    @Test
    @DisplayName("OrderStatus 전체 정상 전이 경로 검증")
    void orderStatus_validTransitions() {
        assertThatNoException().isThrownBy(() -> OrderStatus.PENDING.validateTransitionTo(OrderStatus.PAID));
        assertThatNoException().isThrownBy(() -> OrderStatus.PAID.validateTransitionTo(OrderStatus.CONFIRMED));
        assertThatNoException().isThrownBy(() -> OrderStatus.CONFIRMED.validateTransitionTo(OrderStatus.COOKING));
        assertThatNoException().isThrownBy(() -> OrderStatus.COOKING.validateTransitionTo(OrderStatus.DELIVERING));
        assertThatNoException().isThrownBy(() -> OrderStatus.DELIVERING.validateTransitionTo(OrderStatus.COMPLETED));
        // 취소
        assertThatNoException().isThrownBy(() -> OrderStatus.PENDING.validateTransitionTo(OrderStatus.CANCELLED));
        assertThatNoException().isThrownBy(() -> OrderStatus.PAID.validateTransitionTo(OrderStatus.CANCELLED));
        assertThatNoException().isThrownBy(() -> OrderStatus.CONFIRMED.validateTransitionTo(OrderStatus.CANCELLED));
        assertThatNoException().isThrownBy(() -> OrderStatus.COOKING.validateTransitionTo(OrderStatus.CANCELLED));
        assertThatNoException().isThrownBy(() -> OrderStatus.DELIVERING.validateTransitionTo(OrderStatus.CANCELLED));
    }

    @Test
    @DisplayName("OrderStatus 역방향 전이 불허 검증")
    void orderStatus_invalidTransitions() {
        assertThatThrownBy(() -> OrderStatus.PAID.validateTransitionTo(OrderStatus.PENDING))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> OrderStatus.COOKING.validateTransitionTo(OrderStatus.CONFIRMED))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> OrderStatus.COMPLETED.validateTransitionTo(OrderStatus.CANCELLED))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> OrderStatus.CANCELLED.validateTransitionTo(OrderStatus.PENDING))
                .isInstanceOf(IllegalStateException.class);
    }
}
