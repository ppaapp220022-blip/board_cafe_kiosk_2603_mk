package org.example.board_cafe_kiosk_2603.service.admin.cafeTable;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.admin.CafeTable;
import org.example.board_cafe_kiosk_2603.domain.admin.TableSession;
import org.example.board_cafe_kiosk_2603.dto.admin.CafeTableDTO;
import org.example.board_cafe_kiosk_2603.repository.admin.CafeTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class CafeTableServiceImpl implements CafeTableService {
    private final CafeTableRepository cafeTableRepository;

    @Override
    @Transactional(readOnly = true) // 상세 설명: 단순 조회 메서드이므로 성능 최적화를 위해 readOnly 적용
    public List<CafeTableDTO> getAllTableStatus() {
        /* 주 설명: DB 도메인(VO)을 DTO 리스트로 변환하여 컨트롤러로 반환 */
        List<CafeTable> cafeTableList = cafeTableRepository.selectAllTables();

        // 상세 설명: Stream API를 활용하여 DTO 변환 코드를 간결화
        return cafeTableList.stream().map(cafeTable -> CafeTableDTO.builder()
                .id(cafeTable.getId())
                .tableNumber(cafeTable.getTableNumber())
                .status(cafeTable.getStatus())
                .accessToken(cafeTable.getAccessToken())
                .checkInTime(cafeTable.getCheckInTime())
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    public void changeTableStatus(Integer id, String status) {
        /**
         * [핵심] 테이블 상태 전이에 따른 세션(방문 이력) 생명주기 관리
         * 상세 설명: 단순 텍스트 변경이 아닌, OCCUPIED 시 세션을 생성(매핑)하고 CLEANING 시 세션을 마감(해제)함
         */
        log.info("테이블 ID: {} 상태 변경 프로세스 시작 -> {}", id, status);

        switch (status) {
            case "OCCUPIED":
                /* 주 설명: [입실] 신규 방문 세션 생성 및 테이블 포인터 연결 */
                TableSession newSession = TableSession.builder()
                        .tableId(id)
                        .packageId(1) // 임시: 향후 프론트에서 전달받은 패키지 ID 적용
                        .initialGuestCnt(1) // 임시: 향후 프론트에서 전달받은 인원 수 적용
                        .build();

                cafeTableRepository.insertNewSession(newSession);
                Long newSessionId = newSession.getId(); // DB에서 생성된 PK 획득

                cafeTableRepository.updateTableStatusAndSession(id, "OCCUPIED", newSessionId);
                log.info("성공: 세션 {}번 생성 및 테이블 매핑 완료", newSessionId);
                break;

            case "CLEANING":
                /* 주 설명: [퇴실/결제] 현재 이용 중인 세션 마감 및 테이블 포인터 해제 */
                Long currentSessionId = cafeTableRepository.selectCurrentSessionId(id);
                if (currentSessionId != null) {
                    cafeTableRepository.closeSession(currentSessionId);
                    log.info("성공: 세션 {}번 이용 이력 마감 완료", currentSessionId);
                }

                // 매핑 해제 시 sessionId에 null을 명시적으로 전달
                cafeTableRepository.updateTableStatusAndSession(id, "CLEANING", null);
                log.info("성공: 테이블 매핑 해제 및 CLEANING 상태 전환 완료");
                break;

            case "EMPTY":
                /* 주 설명: [청소 완료] 다음 손님 대기 상태로 전환 */
                cafeTableRepository.updateTableStatusAndSession(id, "EMPTY", null);
                log.info("성공: 테이블 EMPTY 상태 전환 완료");
                break;

            default:
                log.warn("알 수 없는 상태값 요청: {}", status);
        }
    }

    @Override
    public String generateNewToken(Integer id) {
        /* 주 설명: UUID를 생성하여 특정 테이블의 access_token 단독 갱신 */
        String newToken = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        // 상세 설명: 주석 내용(8자리 짧은 토큰)과 일치하도록 substring(0, 8)로 유지함

        cafeTableRepository.updateAccessToken(id, newToken);
        log.info("테이블 ID: {} 새 토큰 발급 완료: {}", id, newToken);

        return newToken;
    }

    @Override
    public void resetAllTablesForNewDay() {
        /** * [핵심] 자정 전체 초기화 로직 (트랜잭션 보장)
         * 1. table_session의 모든 활성 세션을 일괄 마감 (is_active = FALSE)
         * 2. cafe_table의 모든 상태를 'EMPTY'로 변경하고 세션 포인터(current_session_id)를 NULL로 해제
         */
        log.info("--- 자정 데이터 리셋 프로세스 시작 ---");

        // 1. 활성 세션 일괄 종료
        int closedSessions = cafeTableRepository.updateAllActiveSessions();
        log.info("자정 데이터 리셋 - 활성 세션 {}개 강제 종료 완료", closedSessions);

        // 2. 전체 테이블 공석 처리 및 연결 해제
        // 상세 설명: for문으로 건건이 쿼리하는 대신, Mapper에 등록된 일괄 업데이트 쿼리를 호출하여 통신 횟수 및 부하 최소화
        int resetTables = cafeTableRepository.resetAllTablesAtMidnight();
        log.info("자정 데이터 리셋 - 전체 테이블 공석(EMPTY) 및 매핑 해제 완료 (적용 건수: {})", resetTables);

        log.info("--- 자정 데이터 리셋 프로세스 종료 ---");
    }
}
