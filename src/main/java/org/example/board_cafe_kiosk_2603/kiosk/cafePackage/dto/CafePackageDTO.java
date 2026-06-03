package org.example.board_cafe_kiosk_2603.kiosk.cafePackage.dto;

import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;

/*
 * 작성자 : 김민기
 * 기능 : 도메인 VO 필드 정의
 * 날짜 : 2026-03-27
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CafePackageDTO {

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

    /**
     * fail 결과를 생성해 반환합니다.
     *
     * @param message 전달받은 message 값
     * @return 처리 결과
     */
    public static CafePackageDTO fail(String message) {
        return CafePackageDTO.builder()
                .success(false)
                .message(message)
                .build();
    }

    /**
     * selected 동작을 수행합니다.
     *
     * @param pkg 전달받은 pkg 값
     * @param tableNumber 전달받은 tableNumber 값
     * @return 처리 결과
     */
    public static CafePackageDTO selected(CafePackageDTO pkg, Integer tableNumber) {
        return CafePackageDTO.builder()
                .success(true)
                .name(pkg.getName())
                .basePrice(pkg.getBasePrice())
                .tableNumber(tableNumber)
                .build();
    }

    public String getDisplayTime() { // package_selection.html에서 pkg.displayTime을 사용하고 있어서 추가함
        if (durationMinutes == null) return "Free";
        if (durationMinutes < 60) return durationMinutes + "분";
        return (durationMinutes / 60) + "시간";
    }
}
