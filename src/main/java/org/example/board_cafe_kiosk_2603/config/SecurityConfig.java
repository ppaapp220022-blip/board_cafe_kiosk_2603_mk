package org.example.board_cafe_kiosk_2603.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.security.KioskAuthorizationManager;
import org.example.board_cafe_kiosk_2603.security.KioskUserDetailsService;
import org.example.board_cafe_kiosk_2603.security.ManagerUserDetailsService;
import org.example.board_cafe_kiosk_2603.security.handler.Handler403;
import org.example.board_cafe_kiosk_2603.security.handler.KioskLoginSuccessHandler;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Log4j2
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final DataSource dataSource;  // DB 연결 정보 (Remember-Me용)
    private final KioskUserDetailsService kioskUserDetailsService;  // 키오스크 로그인 로직
    private final KioskLoginSuccessHandler kioskLoginSuccessHandler;  // 키오스크 로그인 성공 시 처리
    private final ManagerUserDetailsService managerUserDetailsService;  // 관리자 로그인 로직

    // ── Chain 1: 키오스크 (/kiosk/**) ──────────────────────────
    @Bean
    @Order(1)
    public SecurityFilterChain kioskChain(HttpSecurity http) throws Exception {
        log.info("--- [SecurityConfig] Kiosk Security Chain 구성 시작 ---");

        http.securityMatcher("/kiosk/**")
                .userDetailsService(kioskUserDetailsService)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/kiosk/login", "/kiosk/login-process").permitAll()
                        // 신규 주문 조회 및 주문 API
                        .requestMatchers(
                                "/kiosk/order/pending",
                                "/kiosk/order/create",
                                "/kiosk/order/api/**",
                                "/kiosk/order/latest",
                                "/kiosk/order/active",
                                "/kiosk/order/session/**"
                        ).permitAll()

                        // PATCH, DELETE 요청도 인증 불필요
                        .requestMatchers("/kiosk/order/**").permitAll()

                        // WebSocket
                        .requestMatchers("/ws/**", "/app/**")
                        .permitAll()

                        // JS 파일
                        .requestMatchers("/js/**", "/css/**", "/images/**").permitAll()

                        // 카트 관련 (TABLE 역할 필요)
                        .requestMatchers(
                                "/kiosk/cart/add",
                                "/kiosk/cart/update",
                                "/kiosk/cart/clear",
                                "/kiosk/cart/items"
                        ).hasRole("TABLE")
                        .anyRequest().hasRole("TABLE")
                )
                .formLogin(config -> config
                        .loginPage("/kiosk/login")
                        .loginProcessingUrl("/kiosk/login-process")
                        .usernameParameter("tableNumber")
                        .passwordParameter("password")
                        .successHandler(kioskLoginSuccessHandler)
                        .failureUrl("/kiosk/login?error")   // ← 추가
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .rememberMeParameter("remember-me")
                        .tokenRepository(persistentTokenRepository())
                        .tokenValiditySeconds(60 * 60 * 24 * 30) // 30일
                        .userDetailsService(kioskUserDetailsService)
                )
                .logout(logout -> logout
                        .logoutUrl("/kiosk/logout")
                        .logoutSuccessUrl("/kiosk/login")
                        .deleteCookies("JSESSIONID", "remember-me")
                )
                .exceptionHandling(config -> config
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .csrf(AbstractHttpConfigurer::disable);

        log.info("--- [SecurityConfig] Kiosk Security Chain 구성 완료 ---");
        return http.build();
    }

    // ── Chain 2: 관리자 (그 외 전체) ──────────────────────────
    @Bean
    @Order(2)
    public SecurityFilterChain adminChain(HttpSecurity http) throws Exception {
        log.info("--- [SecurityConfig] Admin Security Chain 구성 시작 ---");

        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers(
                                "/common/login", "/common/logout",
                                "/admin/login", "/admin/login-process",
                                "/error",
                                "/ws/**",  // WebSocket 엔드포인트
                                "/app/**",  // WebSocket /app/** 경로
                                "/kiosk/order/**",  // 관리자 대시보드용 신규 주문 조회
                                "/admin/orders/**",
                                "/js/**", "/css/**", "/images/**" // JS, css, 이미지 파일
                        ).permitAll()
                        // ROLE_TABLE(키오스크) 계정이 /admin/** 에 접근하지 못하도록 명시
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "STAFF")
                        .anyRequest().authenticated()
                )
                // ★ 추가: adminChain 에 ManagerUserDetailsService 명시적 연결
                .userDetailsService(managerUserDetailsService)
                .formLogin(config -> config
                        .loginPage("/common/login")
                        .loginProcessingUrl("/admin/login-process")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/admin/dashboard", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/common/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                .exceptionHandling(config -> config
                        .accessDeniedHandler(accessDeniedHandler())
                )
                // WebSocket은 CSRF 보호 불필요
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/ws/**", "/app/**")
                        .ignoringRequestMatchers("/admin/orders/**")
                );

        log.info("--- [SecurityConfig] Admin Security Chain 구성 완료 ---");
        return http.build();
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        log.info("--- [SecurityConfig] PersistentTokenRepository(Remember-Me DB 저장소) 초기화 ---");
        JdbcTokenRepositoryImpl repo = new JdbcTokenRepositoryImpl();
        repo.setDataSource(dataSource);
        // 테이블명 커스텀 설정
        repo.setCreateTableOnStartup(false);
        return repo;
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        log.info("--- [SecurityConfig] AccessDeniedHandler(Handler403) 등록 ---");
        return new Handler403();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // 정적 리소스들이 위치하는 경로를 시큐리티 체크에서 제외
        return (web) -> web.ignoring()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                .requestMatchers("/js/**", "/css/**", "/images/**", "/favicon.ico");
    }

    // -> 제거: webSecurityCustomizer (adminChain의 permitAll과 중복)
    //   Spring Security 6.x 권장 방식인 체인 내 permitAll() 로 대체됨

//    @Bean
//    public WebSecurityCustomizer webSecurityCustomizer() {
//        log.info("-----web configure-----");
//        return web -> web.ignoring()
//                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
//    }
}
