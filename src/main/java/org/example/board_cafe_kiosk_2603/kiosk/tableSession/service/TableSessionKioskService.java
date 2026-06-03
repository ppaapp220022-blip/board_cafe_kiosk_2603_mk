package org.example.board_cafe_kiosk_2603.kiosk.tableSession.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;

import java.util.Map;

/*
 * 작성자 : 서민성
 * 기능 : 키오스크 테이블 세션 서비스 인터페이스
 * 날짜 : 2026-03-27
 */
public interface TableSessionKioskService {
    /**
     * 테이블 세션 생성합니다.
     *
     * @param tableId 전달받은 tableId 값
     * @param packageId 전달받은 packageId 값
     * @param initialGuestCnt 전달받은 initialGuestCnt 값
     * @return 처리 결과
     */
    Long createSession(int tableId, int packageId, int initialGuestCnt);

    /**
     * 장바구니 화면 모델 구성합니다.
     *
     * @param model 전달받은 model 값
     * @param tableNumber 전달받은 tableNumber 값
     * @param session 전달받은 session 값
     */
    void buildCartModel(Model model, int tableNumber, HttpSession session);

    /**
     * 결제 화면 모델 구성합니다.
     *
     * @param model 전달받은 model 값
     * @param tableNumber 전달받은 tableNumber 값
     * @param session 전달받은 session 값
     */
    void buildCheckoutModel(Model model, int tableNumber, HttpSession session);

    /**
     * 결제 메타 정보 구성합니다.
     *
     * @param tableId 전달받은 tableId 값
     * @return 처리 결과
     */
    Map<String, Object> buildCheckoutMeta(Integer tableId);
}
