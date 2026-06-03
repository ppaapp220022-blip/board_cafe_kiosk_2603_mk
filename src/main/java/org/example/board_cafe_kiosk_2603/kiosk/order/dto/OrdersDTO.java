package org.example.board_cafe_kiosk_2603.kiosk.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/*
 * 작성자 : 김민기
 * 기능 : Orders 데이터 전달 객체
 * 날짜 : 2026-03-27
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdersDTO {

    private boolean            success;
    private String             message;
    private int                id;
    private int                tableId;
    private String             customerPhone;
    private String             status;
    private int                totalAmount;
    private LocalDateTime      orderedAt;
    private List<OrderItemDTO> items;
}
