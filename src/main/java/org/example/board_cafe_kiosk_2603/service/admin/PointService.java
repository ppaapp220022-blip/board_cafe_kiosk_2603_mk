package org.example.board_cafe_kiosk_2603.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.admin.Point;
import org.example.board_cafe_kiosk_2603.domain.admin.PointHistory;
import org.example.board_cafe_kiosk_2603.dto.admin.PointAdminDTO;
import org.example.board_cafe_kiosk_2603.dto.admin.PointHistoryDTO;
import org.example.board_cafe_kiosk_2603.mapper.admin.PointMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class PointService {

    private final PointMapper pointMapper;

    // ===================================================
    // 조회
    // ===================================================

    /** 전체 포인트 계좌 목록 (관리자 화면용) */
    public List<PointAdminDTO> getAllPoints() {
        return pointMapper.findAll().stream()
                .map(PointAdminDTO::from)
                .collect(Collectors.toList());
    }

    /** 전화번호로 포인트 계좌 조회 — 없으면 null */
    public PointAdminDTO getPointByPhone(String phone) {
        Point point = pointMapper.findByPhone(phone);
        return point != null ? PointAdminDTO.from(point) : null;
    }

    /** 특정 계좌의 이력 목록 */
    public List<PointHistoryDTO> getHistoryByPointId(int pointId) {
        return pointMapper.findHistoryByPointId(pointId).stream()
                .map(PointHistoryDTO::from)
                .collect(Collectors.toList());
    }

    // ===================================================
    // 통계
    // ===================================================

    public int getTotalCustomers() { return pointMapper.countAll(); }

    public int getTotalPoints()    { return pointMapper.sumTotalBalance(); }

    public int getAvgPoints() {
        int count = pointMapper.countAll();
        return count == 0 ? 0 : pointMapper.sumTotalBalance() / count;
    }

    // ===================================================
    // 계좌 생성
    // ===================================================

    /** 포인트 계좌 신규 생성 (이력 없음, 키오스크 신규 회원용) */
    @Transactional
    public void createAccount(String phone) {
        if (pointMapper.findByPhone(phone) == null) {
            pointMapper.insert(Point.builder().phone(phone).balance(0).build());
            log.info("포인트 계좌 신규 생성 - 전화번호: {}", phone);
        }
    }

    // ===================================================
    // 포인트 적립 (EARN)
    // ===================================================

    @Transactional
    public void earnPoint(String phone, int amount, Integer orderId) {
        Point point = getOrCreatePoint(phone);

        int newBalance = point.getBalance() + amount;
        pointMapper.updateBalance(Point.builder()
                .id(point.getId())
                .phone(point.getPhone())
                .balance(newBalance)
                .build());

        pointMapper.insertHistory(PointHistory.builder()
                .pointId(point.getId())
                .orderId(orderId)
                .type("EARN")
                .amount(amount)
                .balanceAfter(newBalance)
                .build());

        log.info("포인트 적립 - 전화번호: {}, 적립: {}P, 잔액: {}P", phone, amount, newBalance);
    }

    // ===================================================
    // 포인트 사용 (USE)
    // ===================================================

    @Transactional
    public void usePoint(String phone, int amount, Integer orderId) {
        Point point = pointMapper.findByPhone(phone);
        if (point == null) {
            throw new IllegalArgumentException("포인트 계좌를 찾을 수 없습니다: " + phone);
        }
        if (point.getBalance() < amount) {
            throw new IllegalArgumentException(
                    "포인트 잔액이 부족합니다. 현재 잔액: " + point.getBalance());
        }

        int newBalance = point.getBalance() - amount;
        pointMapper.updateBalance(Point.builder()
                .id(point.getId())
                .phone(point.getPhone())
                .balance(newBalance)
                .build());

        pointMapper.insertHistory(PointHistory.builder()
                .pointId(point.getId())
                .orderId(orderId)
                .type("USE")
                .amount(amount)
                .balanceAfter(newBalance)
                .build());

        log.info("포인트 사용 - 전화번호: {}, 사용: {}P, 잔액: {}P", phone, amount, newBalance);
    }

    // ===================================================
    // 헬퍼
    // ===================================================

    private Point getOrCreatePoint(String phone) {
        Point point = pointMapper.findByPhone(phone);
        if (point == null) {
            pointMapper.insert(Point.builder().phone(phone).balance(0).build());
            point = pointMapper.findByPhone(phone);
        }
        return point;
    }
}
