package org.example.board_cafe_kiosk_2603.service.admin;


import org.example.board_cafe_kiosk_2603.dto.admin.CustomerResponseDTO;
import org.example.board_cafe_kiosk_2603.dto.admin.PointHistoryResponseDTO;
import org.example.board_cafe_kiosk_2603.dto.admin.PointTransactionDTO;

import java.util.List;

public interface CustomerPointService {
    // 포인트 적립 또는 사용 통합 처리
    CustomerResponseDTO processPointTransaction(PointTransactionDTO dto);

    // 전화번호로 고객 정보 및 잔여 포인트 조회
    CustomerResponseDTO findCustomerByPhone(String phone);

    // 결제액 기준 적립 포인트 계산 (현재 정책: 5%)
    int calculateEarnedPoint(int amount);

    // 포인트 계좌 ID로 변동 이력 조회
    List<PointHistoryResponseDTO> getCustomerHistory(int pointId);
}
