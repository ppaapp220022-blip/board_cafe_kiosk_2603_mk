package org.example.board_cafe_kiosk_2603.dto.admin;

import lombok.*;

@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerResponseDTO {

    private int id;             // 고객 고유 번호 (내부 관리용)
    private String phone;       // 마스킹 처리된 전화번호 (예: 010-****-1234)
    private int totalPoint;     // 현재 보유 중인 총 포인트
}
