package org.example.board_cafe_kiosk_2603.global.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.kiosk.order.dto.OrdersDTO;
import org.example.board_cafe_kiosk_2603.kiosk.order.service.OrderService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;
/*
 * 작성자 : 김민기
 * 기능 : 주문 상태 WebSocket 핸들러
 * 날짜 : 2026-04-09
 */

@Log4j2
@Controller
@RequiredArgsConstructor
public class OrderWebSocketHandler {

    private final OrderService orderService;
    private final SimpMessagingTemplate messagingTemplate;
    @MessageMapping("/subscribe/{tableId}")
    public void subscribeTableOrders(@DestinationVariable int tableId) {
        log.debug("테이블 {} 주문 상태 구독 시작", tableId);
        
        try {
            // 현재 테이블의 주문 조회
            List<OrdersDTO> orders = orderService.getOrdersByTableId(tableId);
            
            // 구독 클라이언트에게 전송
            messagingTemplate.convertAndSend(
                    "/topic/orders/" + tableId,
                    orders
            );
            
            log.debug("테이블 {} 주문 {}건 전송", tableId, orders.size());
        } catch (Exception e) {
            log.error("주문 상태 전송 실패 - tableId: {}", tableId, e);
        }
    }
    public void broadcastOrderUpdate(int orderId, int tableId) {
        try {
            List<OrdersDTO> orders = orderService.getOrdersByTableId(tableId);
            
            // 해당 테이블을 구독 중인 모든 클라이언트에게 전송
            messagingTemplate.convertAndSend(
                    "/topic/orders/" + tableId,
                    orders
            );
            
            log.debug("주문 상태 변경 브로드캐스트 - orderId: {}, tableId: {}", orderId, tableId);
        } catch (Exception e) {
            log.error("브로드캐스트 실패", e);
        }
    }
    public void broadcastNewOrder(OrdersDTO newOrder) {
        try {
            // 관리자 대시보드에 신규 주문 알림
            messagingTemplate.convertAndSend("/topic/new-orders", newOrder);
            log.debug("신규 주문 브로드캐스트 - orderId: {}, tableId: {}",
                    newOrder.getId(), newOrder.getTableId());
        } catch (Exception e) {
            log.error("신규 주문 브로드캐스트 실패", e);
        }
    }
}
