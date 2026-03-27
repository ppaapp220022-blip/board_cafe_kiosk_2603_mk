package org.example.board_cafe_kiosk_2603.dto.kiosk;

import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CafePackageDTO {

    // === 도메인 VO 필드 ===
    private int           id;
    private String        name;
    private String        type;
    private Integer       durationMinutes;
    private int           basePrice;
    private Double        extraPricePerMin;
    private boolean       active;
    private LocalDateTime updatedAt;

    // === 요청(Request)용 필드 ===
    @Positive(message = "packageId는 1 이상이어야 합니다.")
    private int packageId;

    // === 응답(Response)용 필드 ===
    private boolean success;
    private String  message;
    private Integer tableNumber;

    // === 정적 팩토리 ===
    public static CafePackageDTO fail(String message) {
        return CafePackageDTO.builder()
                .success(false)
                .message(message)
                .build();
    }

    public static CafePackageDTO selected(CafePackageDTO pkg, Integer tableNumber) {
        return CafePackageDTO.builder()
                .success(true)
                .name(pkg.getName())
                .basePrice(pkg.getBasePrice())
                .tableNumber(tableNumber)
                .build();
    }
}
