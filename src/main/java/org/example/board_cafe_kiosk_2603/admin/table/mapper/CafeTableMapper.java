package org.example.board_cafe_kiosk_2603.admin.table.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.board_cafe_kiosk_2603.common.tableSession.model.CafeTableSession;
import org.example.board_cafe_kiosk_2603.admin.table.model.CafeTable;
import org.example.board_cafe_kiosk_2603.kiosk.order.dto.OrderItemDTO;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CafeTableMapper {

    List<CafeTable> selectAllTables();

    List<Integer> selectOccupiedTableIds();

    String selectAccessTokenById(@Param("id") Integer id);

    int insertNewSession(CafeTableSession session);

    int updateTableStatusAndSession(@Param("id") Integer id,
                                    @Param("status") String status,
                                    @Param("sessionId") Long sessionId);

    int closeSession(@Param("sessionId") Long sessionId);

    Long selectCurrentSessionId(@Param("tableId") Integer tableId);

    String selectStatusById(@Param("tableId") Integer tableId);

    int updateAccessToken(@Param("tableId") int tableId, @Param("accessToken") String accessToken);

    Long selectActiveSessionByTableId(@Param("tableId") Integer tableId);

    Long selectLatestSessionByTableId(@Param("tableId") Integer tableId);

    int resetAllTablesAtMidnight();

    int updateAllActiveSessions();

    List<OrderItemDTO> selectActiveOrderItems(@Param("sessionId") Long sessionId);

    int updateMessagesReadStatus(@Param("tableId") Integer tableId);

    List<String> selectUnreadMessageContents(@Param("tableId") Integer tableId);

    /**
     * 테이블 번호로 단건 조회합니다.
     *
     * @param tableNumber 전달받은 tableNumber 값
     * @return 처리 결과
     */
    Optional<CafeTable> findByTableNumber(@Param("tableNumber") int tableNumber);

    /**
     * 세션 기준 메시지 읽음 처리합니다.
     *
     * @param sessionId 전달받은 sessionId 값
     * @return 처리 결과
     */
    int updateMessagesReadStatusBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 전체 메시지 읽음 처리합니다.
     *
     * @return 처리 결과
     */
    int updateAllMessagesReadStatus();
}
