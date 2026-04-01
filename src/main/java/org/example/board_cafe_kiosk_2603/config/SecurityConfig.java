package org.example.board_cafe_kiosk_2603.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.security.KioskAuthorizationManager;
import org.example.board_cafe_kiosk_2603.security.handler.Handler403;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.sql.DataSource;

@Log4j2
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final DataSource dataSource;  // 데이터베이스 이용

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        /* spring security의 설정을 담당 */
        log.info("-----Security Config-----");

        // 1. 권한 설정 (요청 경로별 접근 제한)
        httpSecurity.authorizeHttpRequests(auth -> auth
                // [정적 리소스] CSS, JS, Images 등은 인증 없이 허용
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()

                // [공통/로그인] 역할 선택 페이지 및 각 로그인 관련 경로는 모두 허용
                .requestMatchers("/common/login",
                        "/common/logout",
                        "/admin/login",
                        "/admin/login-process",
                        "/kiosk/login",
                        "/error" // Security에서 막혀서 로그인 페이지로 리다이렉트돼서 /error 경로를 허용해서 에러 페이지가 제대로 표시
                        ).permitAll()

                // [키오스크 전용] /kiosk/로 시작하는 모든 경로는 TABLE 권한 필수
                .requestMatchers("/kiosk/**").access(new KioskAuthorizationManager()) // 아래 설명
                // [관리자 전용] /admin/으로 시작하는 모든 경로는 ADMIN 또는 STAFF 권한 필수
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "STAFF")
                // [그 외] 나머지 모든 요청은 로그인(인증)된 사용자만 접근 가능
                .anyRequest().authenticated()
        );

        // 2. 폼 로그인 설정 (관리자 대시보드 로그인용)
        // Spring Security에서 폼 기반 로그인을 설정
        httpSecurity.formLogin(config -> {
            config.loginPage("/common/login")  // 미인증 사용자가 접근 시 이동할 페이지
                    .loginProcessingUrl("/admin/login-process") // HTML Form의 action과 일치
                    .usernameParameter("username")          // HTML input의 name="username"
                    .passwordParameter("password")          // HTML input의 name="password"
                    .defaultSuccessUrl("/admin/dashboard", true) // 로그인 성공 시 '무조건' 대시보드로 이동
                    .permitAll();
        });


        // 5. 로그아웃 설정
        httpSecurity.logout(logout -> logout
                .logoutUrl("/admin/logout")  // 로그아웃 처리 경로
                .logoutSuccessUrl("/common/login") // 로그아웃 시 역할 선택창으로
                .invalidateHttpSession(true)  // 세션 무효화
                .deleteCookies("JSESSIONID")  // 쿠키 삭제
        );

        // 3. CSRF 토큰 비활성화 (개발단계)
//        httpSecurity.csrf(csrf -> csrf.disable());
        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        // 4. 예외 처리 (Handler403 설정)
        httpSecurity.exceptionHandling(config -> {
            config.accessDeniedHandler(accessDeniedHandler());
        });

        return httpSecurity.build();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new Handler403();
        // 얘를 config에 등록해줘야함
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        /* Spring Security에서 정적 리소스나 보안 필터 제외 대상을 설정할 때 사용 */
        log.info("-----web configure-----");

        // 정적 파일 경로에 시큐리티 적용을 안함
        return (web -> web.ignoring().
                requestMatchers(PathRequest.toStaticResources().atCommonLocations()));
    }
}
