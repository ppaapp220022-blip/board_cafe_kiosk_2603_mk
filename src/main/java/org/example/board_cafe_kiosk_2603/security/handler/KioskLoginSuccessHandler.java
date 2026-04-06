package org.example.board_cafe_kiosk_2603.security.handler;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.mapper.admin.table.CafeTableMapper;
import org.example.board_cafe_kiosk_2603.security.dto.KioskDTO;
import org.example.board_cafe_kiosk_2603.service.admin.cafeTable.CafeTableService;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

@Log4j2
@Component
@RequiredArgsConstructor
public class KioskLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final CafeTableService cafeTableService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        KioskDTO kiosk = (KioskDTO) authentication.getPrincipal();
        int tableId = kiosk.getTableId();

        log.info("--- [KioskLoginSuccess] 로그인 성공 | tableId: {}, tableNumber: {} ---",
                tableId, kiosk.getUsername());

        // ────────────────────────────────────────────────────────
        // 1. 세션 세팅
        //    ★ 수정: tableNumber를 String → Integer로 통일
        //    이유: CartController 등 다른 컨트롤러에서
        //          (Integer) session.getAttribute("tableNumber") 로 캐스팅하므로
        //          String으로 저장하면 ClassCastException 발생
        // ────────────────────────────────────────────────────────
        request.getSession().setAttribute("tableNumber", Integer.parseInt(kiosk.getUsername()));
        request.getSession().setAttribute("tableId", tableId);
        request.getSession().setAttribute("cart", new ArrayList<>());
        request.getSession().setMaxInactiveInterval(60 * 60 * 8); // 세션 유지 8시간

        log.info("--- [KioskLoginSuccess] 세션 저장 완료 | tableNumber(Integer): {}, tableId: {}, sessionId: {} ---",
                Integer.parseInt(kiosk.getUsername()), tableId, request.getSession().getId());

        // ────────────────────────────────────────────────────────
        // 2. access_token 갱신
        //    로그인 성공 시마다 새 UUID를 발급하여 DB에 저장
        //    자정 초기화 후 재로그인 시에도 토큰이 복구됨
        // ────────────────────────────────────────────────────────
        String newToken = UUID.randomUUID().toString();
        cafeTableService.updateAccessToken(tableId, newToken);
        log.info("--- [KioskLoginSuccess] access_token 갱신 완료 | tableId: {}, token: {} ---",
                tableId, newToken);

        // ────────────────────────────────────────────────────────
        // 3. cafe_table.current_session_id 조회
        //    정상 상태라면 여기서 sessionId가 조회됨
        // ────────────────────────────────────────────────────────
        Long currentSessionId = cafeTableService.findCurrentSessionId(tableId);
        log.info("--- [KioskLoginSuccess] cafe_table.current_session_id 조회 결과: {} ---",
                currentSessionId);

        // ────────────────────────────────────────────────────────
        // 4. table_session에서 직접 활성 세션 조회 (불일치 복구용)
        //    cafe_table.current_session_id 와 table_session.is_active 가
        //    동기화되지 않은 경우를 방어
        //    예: 이전 세션 생성 후 서버 재기동, 강제 초기화 등
        // ────────────────────────────────────────────────────────
        Long activeSessionId = cafeTableService.findActiveSessionByTableId(tableId);
        log.info("--- [KioskLoginSuccess] table_session 활성 세션 조회 결과: {} ---",
                activeSessionId);

        if (activeSessionId != null) {

            // 활성 세션이 존재하는데 cafe_table.current_session_id 와 다를 경우 복구
            if (currentSessionId == null || !currentSessionId.equals(activeSessionId)) {
                log.warn("--- [KioskLoginSuccess] 데이터 불일치 감지 | " +
                                "cafe_table.current_session_id: {}, table_session.id: {} → 복구 시작 ---",
                        currentSessionId, activeSessionId);

                // cafe_table.status = OCCUPIED, current_session_id = activeSessionId 로 동기화
                cafeTableService.syncTableWithSession(tableId, activeSessionId);

                log.info("--- [KioskLoginSuccess] 복구 완료 | current_session_id: {}, status: OCCUPIED ---",
                        activeSessionId);
            } else {
                log.info("--- [KioskLoginSuccess] 데이터 정상 일치 | current_session_id: {} ---",
                        currentSessionId);
            }

            // 진행 중인 세션 있음 → 메뉴 화면으로 이동
            log.info("--- [KioskLoginSuccess] 활성 세션 존재 → /kiosk/menu 이동 ---");
            response.sendRedirect("/kiosk/menu");

        } else {
            // 활성 세션 없음 → 최초 로그인 또는 정산 완료 후
            log.info("--- [KioskLoginSuccess] 활성 세션 없음 → /kiosk/session/start 이동 ---");
            response.sendRedirect("/kiosk/session/start");
        }

        log.info("--- [KioskLoginSuccess] 로그인 처리 완료 | tableId: {} ---", tableId);
    }

    @PostConstruct
    public void init() {
        log.info("--- [KioskLoginSuccessHandler] 주입된 서비스: {} ---",
                cafeTableService.getClass().getName());
    }
}
