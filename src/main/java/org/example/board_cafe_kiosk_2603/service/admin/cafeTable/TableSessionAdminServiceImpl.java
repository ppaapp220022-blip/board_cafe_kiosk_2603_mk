package org.example.board_cafe_kiosk_2603.service.admin.cafeTable;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.common.cafeTableSession.CafeTableSession;
import org.example.board_cafe_kiosk_2603.mapper.common.cafeTableSession.CafeTableSessionMapper;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class TableSessionAdminServiceImpl implements TableSessionAdminService {

    private final CafeTableSessionMapper tableSessionAdminMapper;

    /*
     * 테이블 ID로 활성 세션 조회
     * 관리자 대시보드에서 현재 이용 중인 테이블 현황 확인용
     */
    @Override
    public CafeTableSession getActiveSession(int tableId) {
        CafeTableSession tableSession = tableSessionAdminMapper.findActiveByTableId(tableId);
        log.info("활성 세션 조회... tableId: {}, session: {}", tableId, tableSession);
        return tableSession;
    }

    @Override
    public void closeSession(int tableId) {

    }

    @Override
    public void updateTotalAmount(Long sessionId, int totalAmount) {

    }
}
