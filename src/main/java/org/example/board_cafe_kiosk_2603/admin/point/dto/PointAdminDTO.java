package org.example.board_cafe_kiosk_2603.admin.point.dto;

import lombok.*;
import org.example.board_cafe_kiosk_2603.admin.point.model.Point;

import java.time.LocalDateTime;

/*
 * 작성자 : 김민기
 * 기능 : PointAdmin 데이터 전달 객체
 * 날짜 : 2026-03-27
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointAdminDTO {
    private int id;
    private String phone;
    private int balance;
    private LocalDateTime updatedAt;

    /**
     * from 결과를 생성해 반환합니다.
     *
     * @param point 전달받은 point 값
     * @return 처리 결과
     */
    public static PointAdminDTO from(Point point) {
        return PointAdminDTO.builder()
                .id(point.getId())
                .phone(point.getPhone())
                .balance(point.getBalance())
                .updatedAt(point.getUpdatedAt())
                .build();
    }
}
