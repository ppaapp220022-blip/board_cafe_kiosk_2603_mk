package org.example.board_cafe_kiosk_2603.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/*
 * 작성자 : 김민기
 * 기능 : restTemplate 설정 추가
 * 날짜 : 2026-03-27
 */

@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
