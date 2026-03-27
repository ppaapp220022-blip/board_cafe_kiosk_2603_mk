package org.example.board_cafe_kiosk_2603.repository.admin;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.board_cafe_kiosk_2603.domain.admin.TableSession;
import org.example.board_cafe_kiosk_2603.domain.admin.CafeTable;

import java.util.List;

/**
 * 대시보드 테이블 현황 전용 DAO 인터페이스
 * select와 update 기능에 집중하여 설계
 */
@Mapper
public interface CafeTableRepository {
    /**
     * [Select] 전체 테이블 현황 조회
     * 주 설명: 모든 테이블의 번호, 상태, 현재 세션 ID 포인터를 가져옴
     */
    List<CafeTable> selectAllTables();

    /**
     * [Insert] 신규 세션 생성 (입장 시)
     * 주 설명: table_session 테이블에 신규 행을 추가하고, 생성된 PK(id)를 session 객체에 채워줌
     */
    int insertNewSession(TableSession session);

    /**
     * [Update] 테이블 상태 및 세션 포인터 갱신 (핵심)
     * @param id 테이블 PK
     * @param status 'EMPTY', 'OCCUPIED', 'CLEANING'
     * @param sessionId 연결할 세션 ID (입장 시 할당, 퇴실 시 NULL)
     */
    int updateTableStatusAndSession(@Param("id") Integer id,
                                    @Param("status") String status,
                                    @Param("sessionId") Long sessionId);

    /**
     * [Update] 세션 종료 처리 (퇴실 시)
     * 주 설명: 세션의 isActive를 false로 바꾸고 check_out_time을 현재 시각으로 기록
     */
    int closeSession(@Param("sessionId") Long sessionId);

    /**
     * [Select] 특정 테이블의 현재 세션 ID 포인터 조회
     */
    Long selectCurrentSessionId(@Param("id") Integer id);

    /**
     * [Update] 액세스 토큰(UUID) 개별 갱신
     */
    int updateAccessToken(@Param("id") Integer id, @Param("accessToken") String accessToken);

    /**
     * [Batch Update] 자정 시스템 초기화
     * 주 설명: 모든 테이블의 상태를 EMPTY로, 세션 포인터를 NULL로 일괄 초기화
     */
    int resetAllTablesAtMidnight();

    /**
     * [Batch Update] 자정 기준 모든 활성 세션 종료 처리
     * 주 설명: 아직 퇴실 처리되지 않은(is_active=TRUE) 세션들의 check_out_time을 현재 시각으로 기록
     */
    int updateAllActiveSessions();
}
