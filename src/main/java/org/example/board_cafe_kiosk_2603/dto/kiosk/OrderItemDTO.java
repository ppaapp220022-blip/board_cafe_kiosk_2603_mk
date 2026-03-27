package org.example.board_cafe_kiosk_2603.dto.kiosk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private int     id;
    private int     orderId;
    private Integer menuId;
    private String  menuName;
    private int     price;
    private int     quantity;

    // === 정적 팩토리 ===
    public static OrderItemDTO from(org.example.board_cafe_kiosk_2603.domain.kiosk.OrderItem item) {
        return OrderItemDTO.builder()
                .id(item.getId())
                .orderId(item.getOrderId())
                .menuId(item.getMenuId())
                .menuName(item.getMenuName())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .build();
    }
}
