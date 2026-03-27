package org.example.board_cafe_kiosk_2603.service.kiosk;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.common.TableSession;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.TableSessionKioskMapper;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class TableSessionKioskServiceImpl implements TableSessionKioskService{

    private final TableSessionKioskMapper tableSessionKioskMapper;

    @Override
    public void createSession(int tableId, int packageId, int initialGuestCnt) {
        TableSession tableSession = TableSession.builder()
                .tableId(tableId)
                .packageId(packageId)
                .initialGuestCnt(initialGuestCnt)
                .build();

        tableSessionKioskMapper.insertSession(tableSession);
        log.info("세션 생성 완료... tableId: {}, packageId: {}, 인원: {}",
                tableId, packageId, initialGuestCnt);
    }
}
