package org.example.board_cafe_kiosk_2603.domain.admin;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CafeTable {
    /* 테이블 고유 ID (PK) */
    private Integer id;

    /* 테이블 번호 (1번, 2번 등) */
    private Integer tableNumber;

    /* 테이블 관리 비밀번호 (필요 시) */
    private String password;

    /* 테이블 현재 상태 (EMPTY, OCCUPIED, CLEANING) */
    private String status;

    /* 키오스크/태블릿 접속용 토큰 */
    private String accessToken;

    /* JOIN을 통해 가져온 세션 시작 시간 */
    private LocalDateTime checkInTime;
}
