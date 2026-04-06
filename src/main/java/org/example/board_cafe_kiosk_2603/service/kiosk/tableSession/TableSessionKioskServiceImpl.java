package org.example.board_cafe_kiosk_2603.service.kiosk.tableSession;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.common.cafeTableSession.CafeTableSession;
import org.example.board_cafe_kiosk_2603.mapper.common.cafeTableSession.CafeTableSessionMapper;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class TableSessionKioskServiceImpl implements TableSessionKioskService{

    private final CafeTableSessionMapper tableSessionKioskMapper;

    @Override
    public Long createSession(int tableId, int packageId, int initialGuestCnt) {
        CafeTableSession tableSession = CafeTableSession.builder()
                .tableId(tableId)
                .packageId(packageId)
                .initialGuestCnt(initialGuestCnt)
                .build();

        tableSessionKioskMapper.insert(tableSession);  // useGEneratedKeys로 id 채워짐
        log.info("세션 생성 완료... tableId: {}, packageId: {}, 인원: {}",
                tableId, packageId, initialGuestCnt);
        return tableSession.getId();  // Long 반환
    }
}
