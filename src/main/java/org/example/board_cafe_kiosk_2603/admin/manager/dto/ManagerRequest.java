package org.example.board_cafe_kiosk_2603.admin.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.board_cafe_kiosk_2603.admin.manager.model.RoleType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManagerRequest {
    private String loginId;     // 등록 시 사용
    private String password;    // 등록 시 사용
    private String name;        // 등록 시 사용
    private String email;       // 등록 시 사용 (OTP, 필수값)
    private RoleType role;  // 등록 시 사용
    private Boolean isActive;   // 상태 토글 시 사용
}
