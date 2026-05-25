package org.example.board_cafe_kiosk_2603.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
/*
 * 작성자 : 김민기
 * 기능 : WebSocket STOMP 설정
 * 날짜 : 2026-04-09
 */

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 내부 메시지 브로커 설정
        config.enableSimpleBroker("/topic");
        
        // 클라이언트가 서버로 메시지를 보낼 때의 접두사
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 엔드포인트
        // SockJS로 폴백 지원 (WebSocket을 지원하지 않는 브라우저에 대해)
        registry.addEndpoint("/ws/orders")
                .setAllowedOrigins("http://localhost:8080")  // 특정 도메인 명시
                .withSockJS();  // SockJS로 폴백 지원
    }
}
