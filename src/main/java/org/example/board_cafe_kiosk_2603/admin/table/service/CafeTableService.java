package org.example.board_cafe_kiosk_2603.admin.table.service;

import org.example.board_cafe_kiosk_2603.admin.table.model.CafeTable;
import org.example.board_cafe_kiosk_2603.admin.table.dto.CafeTableDTO;
import org.example.board_cafe_kiosk_2603.kiosk.order.dto.OrderItemDTO;

import java.util.List;
import java.util.Optional;

/*
 * 기능 : 관리자 테이블 관리 서비스 인터페이스
 */
public interface CafeTableService {

    List<CafeTableDTO> getAllTableStatus();

    void changeTableStatus(Integer id, String status);

    String generateNewToken(Integer id);

    void resetAllTablesForNewDay();

    /**
     * 활성 주문 목록 조회합니다.
     *
     * @param tableId 전달받은 tableId 값
     * @return 처리 결과
     */
    List<OrderItemDTO> getActiveOrders(Integer tableId);

    List<String> getUnreadMessages(Integer tableId);

    void markMessagesAsRead(Integer tableId);

    Optional<CafeTable> login(int tableNumber, String password);

    void updateAccessToken(int tableId, String accessToken);

    /**
     * 테이블 상태 조회합니다.
     *
     * @param tableId 전달받은 tableId 값
     * @return 처리 결과
     */
    String getTableStatus(int tableId);

    Long findCurrentSessionId(int tableId);

    Long findActiveSessionByTableId(int tableId);

    void syncTableWithSession(int tableId, Long sessionId);

    /**
     * 테이블 접근 토큰 조회합니다.
     *
     * @param tableId 전달받은 tableId 값
     * @return 처리 결과
     */
    String getTableAccessToken(int tableId);

}
