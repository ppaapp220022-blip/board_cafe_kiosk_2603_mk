package org.example.board_cafe_kiosk_2603.kiosk.tableMessage.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.board_cafe_kiosk_2603.kiosk.tableMessage.model.TableMessage;

import java.util.List;

@Mapper

/*
 * 작성자 : 김민기
 * 기능 : 테이블 요청 메시지 데이터 접근 인터페이스
 * 날짜 : 2026-03-30
 */
public interface TableMessageMapper {

    /**
     * 데이터 등록합니다.
     *
     * @param message 전달받은 message 값
     */
    void insert(TableMessage message);

    /**
     * findUnread 처리합니다.
     *
     * @return 처리 결과
     */
    List<TableMessage> findUnread();

    /**
     * 테이블 ID 기준 조회합니다.
     *
     * @param tableId 전달받은 tableId 값
     * @return 처리 결과
     */
    List<TableMessage> findByTableId(@Param("tableId") int tableId);

    /**
     * 테이블의 미읽음 관리자 메시지 조회합니다.
     *
     * @param tableId 전달받은 tableId 값
     * @return 처리 결과
     */
    List<TableMessage> findUnreadStaffByTableId(@Param("tableId") int tableId);

    /**
     * 메시지 읽음 처리합니다.
     *
     * @param id 전달받은 id 값
     */
    void markAsRead(@Param("id") long id);

    /**
     * 전체 메시지 읽음 처리합니다.
     */
    void markAllAsRead();
}
