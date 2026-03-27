package org.example.board_cafe_kiosk_2603.dto.kiosk;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdersDTO {

    // === 도메인 VO 필드 ===
    private int           id;
    private long          sessionId;
    private int           tableId;
    private String        customerPhone;
    private String        status;
    private int           totalAmount;
    private LocalDateTime orderedAt;
    private List<OrderItemDTO> items;

    // === 비즈니스 로직용 필드 ===
    private boolean success;
    private String  message;

    // === 정적 팩토리 ===
    public static OrdersDTO fail(String message) {
        return OrdersDTO.builder()
                .success(false)
                .message(message)
                .build();
    }

    public static OrdersDTO of(org.example.board_cafe_kiosk_2603.domain.kiosk.Orders order,
                               List<OrderItemDTO> items) {
        return OrdersDTO.builder()
                .success(true)
                .id(order.getId())
                .sessionId(order.getSessionId())
                .tableId(order.getTableId())
                .customerPhone(order.getCustomerPhone())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .orderedAt(order.getOrderedAt())
                .items(items)
                .build();
    }
}
