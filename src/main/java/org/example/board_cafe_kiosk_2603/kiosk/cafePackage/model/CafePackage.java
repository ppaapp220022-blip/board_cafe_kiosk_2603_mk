package org.example.board_cafe_kiosk_2603.kiosk.cafePackage.model;

import lombok.*;

import java.time.LocalDateTime;

/*
 * 작성자 : 김민기
 * 기능 : CafePackage 클래스
 * 날짜 : 2026-03-27
 */

@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CafePackage {
    private int           id;
    private String        name;
    private String        type;             // HOURLY | FIXED_TIME | FREE
    private Integer       durationMinutes;  // nullable
    private int           basePrice;
    private Double        extraPricePerMin; // nullable
    private boolean       active;
    private Integer       updatedBy;        // nullable
    private LocalDateTime updatedAt;
}
