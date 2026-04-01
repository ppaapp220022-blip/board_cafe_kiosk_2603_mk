package org.example.board_cafe_kiosk_2603.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

@Log4j2
public class Handler403 implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException)
            throws IOException, ServletException {
        log.info("--- ACCESS DENIED ---");

        response.setStatus(HttpStatus.FORBIDDEN.value());  // 응답 코드에 403

        // JSON(ajax) 요청이었는지 확인
        String contentType = request.getHeader("Content-Type");
//        boolean isJsonRequest = contentType.startsWith("application/json");
        // 작성자가 아닌 read화면에서 modify로 URL 변경시 로그인페이지로 error=ACCESS_DENIED 발생시킴
        boolean isJsonRequest = false;
        if (contentType != null) {

            isJsonRequest = contentType.startsWith("application/json");
        }

        log.info("isJSON: {}", isJsonRequest);

        // 일반 request
        // <form> 방식으로 데이터가 처리되는 경우 로그인 페이지로 리다이렉트
        if (!isJsonRequest) {
            // 로그인 페이지로 보냄
            response.sendRedirect("/common/login?error=ACCESS_DENIED");
        }
    }
}
