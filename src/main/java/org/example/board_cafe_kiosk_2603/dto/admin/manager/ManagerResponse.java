package org.example.board_cafe_kiosk_2603.dto.admin.manager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.board_cafe_kiosk_2603.domain.admin.manager.RoleType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManagerResponse {
    private Integer id;
    private String loginId;
    private String name;
    private RoleType role;
    private Boolean isActive;
    private LocalDateTime createdAt;
    // password는 응답에 포함 안 함
}
