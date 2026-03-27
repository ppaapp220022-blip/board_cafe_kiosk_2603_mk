package org.example.board_cafe_kiosk_2603.service.kiosk;

import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.kiosk.*;
import org.example.board_cafe_kiosk_2603.dto.kiosk.PaymentDTO;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.CartItemMapper;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.CartMapper;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.OrdersMapper;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.TableSessionMapper;
import org.example.board_cafe_kiosk_2603.service.admin.PointService;
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
class PaymentServiceTest {

    // ===== Mock 선언 (PaymentService 생성자 주입 순서와 일치) =====
    @Mock private OrdersMapper       ordersMapper;
    @Mock private CartMapper         cartMapper;
    @Mock private CartItemMapper     cartItemMapper;
    @Mock private CartService        cartService;
    @Mock private PointService       pointService;
    @Mock private TableSessionMapper tableSessionMapper;

    @InjectMocks
    private PaymentService paymentService;

    // ===== 테스트 상수 =====
    private static final int    TABLE_NUMBER = 5;
    private static final int    TABLE_ID     = 10;
    private static final int    CART_ID      = 100;
    private static final long   SESSION_ID   = 6L;
    private static final String PHONE        = "010-1234-5678";

    // ===== 공통 Mock 객체 =====
    private Cart         mockCart;
    private TableSession mockSession;
    private CartItem     mockItem1;
    private CartItem     mockItem2;

    @BeforeEach
    void setUp() {
        mockCart    = Cart.builder().id(CART_ID).tableId(TABLE_ID).build();
        mockSession = TableSession.builder().id(SESSION_ID).tableId(TABLE_ID).isActive(true).build();
        mockItem1   = CartItem.builder().cartId(CART_ID).menuId(1)
                .menuName("아이스 아메리카노").menuPrice(4000).quantity(1).build();
        mockItem2   = CartItem.builder().cartId(CART_ID).menuId(2)
                .menuName("소금 버터 팝콘").menuPrice(3500).quantity(2).build();
    }

    // ===== 헬퍼: 카트 정상 상태 Stub =====
    private void givenCartReady() {
        given(cartMapper.findCafeTableIdByTableNumber(TABLE_NUMBER)).willReturn(TABLE_ID);
        given(tableSessionMapper.findActiveByTableId(TABLE_ID)).willReturn(mockSession);
        given(cartMapper.findByTableId(TABLE_ID)).willReturn(mockCart);
        given(cartItemMapper.findByCartId(CART_ID)).willReturn(List.of(mockItem1, mockItem2));

        willAnswer(inv -> {
            setField(inv.getArgument(0, Orders.class), "id", 99);
            return null;
        }).given(ordersMapper).insertOrder(any(Orders.class));

        willAnswer(inv -> {
            setField(inv.getArgument(0, Payment.class), "id", 200);
            return null;
        }).given(ordersMapper).insertPayment(any(Payment.class));
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    // ===================================================
    // 성공 케이스
    // ===================================================

    @Test
    void processPayment_success_noPoint() {
        givenCartReady();

        PaymentDTO result = paymentService.processPayment(TABLE_NUMBER, mockRequest("CARD", 0), null);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTotalAmount()).isEqualTo(11000);   // 4000*1 + 3500*2
        assertThat(result.getFinalAmount()).isEqualTo(11000);
        assertThat(result.getPointUsed()).isEqualTo(0);
        assertThat(result.getEarnedPoints()).isEqualTo(0);      // phone 없으므로 적립 없음
        then(ordersMapper).should().completePayment(SESSION_ID);
        then(cartItemMapper).should().deleteAllByCartId(CART_ID);
    }

    @Test
    void processPayment_success_withPoint() {
        givenCartReady();

        PaymentDTO result = paymentService.processPayment(TABLE_NUMBER, mockRequest("CARD", 1000), PHONE);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTotalAmount()).isEqualTo(11000);
        assertThat(result.getPointUsed()).isEqualTo(1000);
        assertThat(result.getFinalAmount()).isEqualTo(10000);
        assertThat(result.getEarnedPoints()).isEqualTo(500);   // 10000 * 5% = 500
        then(pointService).should().usePoint(PHONE, 1000, 99);
        then(pointService).should().earnPoint(PHONE, 500, 99);
    }

    @Test
    void processPayment_earnsPoints_noPointUsed() {
        givenCartReady();

        PaymentDTO result = paymentService.processPayment(TABLE_NUMBER, mockRequest("CARD", 0), PHONE);

        assertThat(result.getEarnedPoints()).isEqualTo(550);   // 11000 * 5% = 550
        then(pointService).should().earnPoint(PHONE, 550, 99);
        then(pointService).should(never()).usePoint(anyString(), anyInt(), anyInt());
    }

    // ===================================================
    // 실패 케이스
    // ===================================================

    @Test
    void processPayment_tableNotFound() {
        given(cartMapper.findCafeTableIdByTableNumber(TABLE_NUMBER)).willReturn(null);

        PaymentDTO result = paymentService.processPayment(TABLE_NUMBER, mockRequest("CARD", 0), null);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("존재하지 않는 테이블");
    }

    @Test
    void processPayment_noActiveSession() {
        given(cartMapper.findCafeTableIdByTableNumber(TABLE_NUMBER)).willReturn(TABLE_ID);
        given(tableSessionMapper.findActiveByTableId(TABLE_ID)).willReturn(null);

        PaymentDTO result = paymentService.processPayment(TABLE_NUMBER, mockRequest("CARD", 0), null);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("세션이 없습니다");
    }

    @Test
    void processPayment_noCart() {
        given(cartMapper.findCafeTableIdByTableNumber(TABLE_NUMBER)).willReturn(TABLE_ID);
        given(tableSessionMapper.findActiveByTableId(TABLE_ID)).willReturn(mockSession);
        given(cartMapper.findByTableId(TABLE_ID)).willReturn(null);

        PaymentDTO result = paymentService.processPayment(TABLE_NUMBER, mockRequest("CARD", 0), null);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("장바구니가 비어있습니다");
    }

    @Test
    void processPayment_emptyCart() {
        given(cartMapper.findCafeTableIdByTableNumber(TABLE_NUMBER)).willReturn(TABLE_ID);
        given(tableSessionMapper.findActiveByTableId(TABLE_ID)).willReturn(mockSession);
        given(cartMapper.findByTableId(TABLE_ID)).willReturn(mockCart);
        given(cartItemMapper.findByCartId(CART_ID)).willReturn(List.of());

        PaymentDTO result = paymentService.processPayment(TABLE_NUMBER, mockRequest("CARD", 0), null);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("상품이 없습니다");
    }

    @Test
    void processPayment_pointWithoutPhone() {
        given(cartMapper.findCafeTableIdByTableNumber(TABLE_NUMBER)).willReturn(TABLE_ID);
        given(tableSessionMapper.findActiveByTableId(TABLE_ID)).willReturn(mockSession);
        given(cartMapper.findByTableId(TABLE_ID)).willReturn(mockCart);
        given(cartItemMapper.findByCartId(CART_ID)).willReturn(List.of(mockItem1));

        PaymentDTO result = paymentService.processPayment(TABLE_NUMBER, mockRequest("CARD", 500), null);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("전화번호가 필요합니다");
    }

    @Test
    void processPayment_pointExceedsAmount() {
        given(cartMapper.findCafeTableIdByTableNumber(TABLE_NUMBER)).willReturn(TABLE_ID);
        given(tableSessionMapper.findActiveByTableId(TABLE_ID)).willReturn(mockSession);
        given(cartMapper.findByTableId(TABLE_ID)).willReturn(mockCart);
        given(cartItemMapper.findByCartId(CART_ID)).willReturn(List.of(mockItem1));  // 4000원

        PaymentDTO result = paymentService.processPayment(TABLE_NUMBER, mockRequest("CARD", 9999), PHONE);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("초과할 수 없습니다");
    }

    // ===== 헬퍼 =====
    private PaymentDTO mockRequest(String method, int pointUsed) {
        return PaymentDTO.builder()
                .paymentMethod(method)
                .pointUsed(pointUsed)
                .build();
    }
}
