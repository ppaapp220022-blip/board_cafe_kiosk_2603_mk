package org.example.board_cafe_kiosk_2603.security.dto;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class ManagerDTO extends User {
    /* 시큐리티 전용 DTO */
    // admin_layout에 현재 로그인되어있는 이름을 출력하기 위함

    private final String name;  // 실명 추가

    public ManagerDTO(String username,
                      String password,
                      String name,
                      Collection<? extends GrantedAuthority> authorities) {
        // 부모 클래스(User)의 생성자를 반드시 호출해야 합니다.
        super(username, password, authorities);
        this.name = name;
    }
}
