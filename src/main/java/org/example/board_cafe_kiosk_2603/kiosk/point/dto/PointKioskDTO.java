package org.example.board_cafe_kiosk_2603.kiosk.point.dto;

import lombok.Builder;
import lombok.Getter;

/*
 * 작성자 : 김민기
 * 기능 : PointKiosk 데이터 전달 객체
 * 날짜 : 2026-03-27
 */
@Getter
@Builder
public class PointKioskDTO {

    private final boolean exists;
    private final int     balance;

    /**
     * of 동작을 수행합니다.
     *
     * @param exists 전달받은 exists 값
     * @param balance 전달받은 balance 값
     * @return 처리 결과
     */
    public static PointKioskDTO of(boolean exists, int balance) {
        return PointKioskDTO.builder()
                .exists(exists)
                .balance(balance)
                .build();
    }

    /**
     * found 동작을 수행합니다.
     *
     * @param balance 전달받은 balance 값
     * @return 처리 결과
     */
    public static PointKioskDTO found(int balance) {
        return PointKioskDTO.builder()
                .exists(true)
                .balance(balance)
                .build();
    }

    /**
     * notFound 동작을 수행합니다.
     *
     * @return 처리 결과
     */
    public static PointKioskDTO notFound() {
        return PointKioskDTO.builder()
                .exists(false)
                .balance(0)
                .build();
    }
}
