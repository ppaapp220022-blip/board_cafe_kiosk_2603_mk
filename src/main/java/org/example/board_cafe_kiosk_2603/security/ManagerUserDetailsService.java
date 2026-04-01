package org.example.board_cafe_kiosk_2603.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.admin.manager.Manager;
import org.example.board_cafe_kiosk_2603.mapper.admin.manager.ManagerMapper;
import org.example.board_cafe_kiosk_2603.security.dto.ManagerDTO;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class ManagerUserDetailsService implements UserDetailsService {

    private final ManagerMapper managerMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("--- 로그인 시도 아이디: {} ---", username);

        // 1. DB에서 관리자 조회 (Optional 처리)
        Manager manager = managerMapper.findByLoginId(username)
                .orElseThrow(() -> new UsernameNotFoundException("해당 아이디의 관리자를 찾을 수 없습니다: " + username));

        log.info("--- 관리자 조회 성공: {} ---", manager);

//        // 2. 시큐리티 전용 User 객체 생성 (권한은 일단 ROLE_ADMIN으로 부여)
//        return new User(
//                manager.getLoginId(),
//                manager.getPassword(), // 반드시 BCrypt로 인코딩된 상태여야 함
//                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
//        );

        // DB의 name을 ManagerDTO에 넣어서 리턴!
        return new ManagerDTO(
                manager.getLoginId(),
                manager.getPassword(),
                manager.getName(), // 여기에 실명을 넣는 게 핵심입니다.
                List.of(new SimpleGrantedAuthority("ROLE_" + manager.getRole().name()))
        );
    }
}
