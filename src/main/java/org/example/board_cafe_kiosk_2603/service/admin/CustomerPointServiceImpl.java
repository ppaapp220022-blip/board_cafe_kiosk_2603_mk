package org.example.board_cafe_kiosk_2603.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.admin.Customer;
import org.example.board_cafe_kiosk_2603.domain.admin.Point;
import org.example.board_cafe_kiosk_2603.domain.admin.PointHistory;
import org.example.board_cafe_kiosk_2603.dto.admin.CustomerResponseDTO;
import org.example.board_cafe_kiosk_2603.dto.admin.PointHistoryResponseDTO;
import org.example.board_cafe_kiosk_2603.dto.admin.PointTransactionDTO;
import org.example.board_cafe_kiosk_2603.mapper.admin.CustomerMapper;
import org.example.board_cafe_kiosk_2603.mapper.admin.PointHistoryMapper;
import org.example.board_cafe_kiosk_2603.mapper.admin.PointMapper;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class CustomerPointServiceImpl implements CustomerPointService {
    private final CustomerMapper customerMapper;
    private final PointMapper pointMapper;
    private  final PointHistoryMapper pointHistoryMapper;

    // 포인트 적립 비율 정책 (변경 시 여기만 수정)
    private static final double EARN_RATE = 0.05; // 결제액의 0.05%

    /*
     * 포인트 통합 처리
     * 1. 전화번호로 customer 조회 → 없으면 신규 등록
     * 2. 전화번호로 point 계좌 조회 → 없으면 신규 생성
     * 3. 포인트 사용 먼저 차감 후 적립 처리
     * 4. point 잔액 업데이트
     * 5. point_history 이력 저장
     * 6. CustomerResponseDTO 반환
     */
    @Override
    public CustomerResponseDTO processPointTransaction(PointTransactionDTO pointTransactionDTO) {
        // 1. customer 조회 또는 신규 등록
        Customer customer = customerMapper.selectByPhone(pointTransactionDTO.getPhone());
        if (customer == null) {
            customer = Customer.builder()
                    .phone(pointTransactionDTO.getPhone())
                    .isActive(true)
                    .build();
            customerMapper.insertCustomer(customer);
            customer = customerMapper.selectByPhone(pointTransactionDTO.getPhone());
        }

        // 2. point 계좌 조회 또는 신규 생성
        Point point = pointMapper.selectByPhone(pointTransactionDTO.getPhone());
        if (point == null) {
            point = Point.builder()
                    .phone(pointTransactionDTO.getPhone())
                    .balance(0)
                    .build();
            pointMapper.insertPoint(point);
            point = pointMapper.selectByPhone(pointTransactionDTO.getPhone());
        }

        int currentBalance = point.getBalance();

        // 3. 포인트 사용 처리 (사용할 포인트가 있을 때)
        if (pointTransactionDTO.getUsePoint() > 0) {
            if (currentBalance < pointTransactionDTO.getUsePoint()) {
                throw new IllegalStateException("포인트 잔액 부족. 현재 잔액: " + currentBalance);
            }
            currentBalance -= pointTransactionDTO.getUsePoint();
            log.info("포인트 사용 - phone: {}, 사용: {}, 잔여: {}",
                    pointTransactionDTO.getPhone(),
                    pointTransactionDTO.getUsePoint(),
                    currentBalance);

            // USE 이력 저장
            pointHistoryMapper.insertHistory(PointHistory.builder()
                    .pointId(point.getId())
                    .orderId(null) /* todo 주문 테이블이 연동X, null로. 나중에 주문 연동되면 주문ID 넣기 */
                    .type("USE")
                    .amount(pointTransactionDTO.getUsePoint())
                    .balanceAfter(currentBalance)
                    .build());
        }

        // 4. 포인트 적립 처리
        int earned = calculateEarnedPoint(pointTransactionDTO.getAmount());
        currentBalance += earned;
        log.info("포인트 적립 - phone: {},결제액: {}, 적립: {}, 잔여: {}",
                pointTransactionDTO.getPhone(), pointTransactionDTO.getAmount(),
                earned, currentBalance);

        // EARN 이력 저장
        pointHistoryMapper.insertHistory(PointHistory.builder()
                .pointId(point.getId())
                .orderId(null)
                .type("EARN")
                .amount(earned)
                .balanceAfter(currentBalance)
                .build());

        // 5.  point 잔액 업데이트
        pointMapper.updateBalance(Point.builder()
                .id(point.getId())
                .phone(point.getPhone())
                .balance(currentBalance)
                .build());

        // 6. 응답 DTO 반환
        return toResponseDTO(customer, currentBalance);
    }

    /* 고객 식별 및 조회
    * 전화번호로 customer + point 계좌 조회 후 DTO 반환
    * */
    @Override
    public CustomerResponseDTO findCustomerByPhone(String phone) {
        Customer customer = customerMapper.selectByPhone(phone);
        if (customer == null) return null;

        Point point = pointMapper.selectByPhone(phone);
        int balance = (point != null) ? point.getBalance() : 0;

        return toResponseDTO(customer, balance);
    }

    /*
     * 적립 포인트 계산
     * 현재 정책(5%) 적용, 정책 변경 시 이 메서드만 수정
     */
    @Override
    public int calculateEarnedPoint(int amount) {
        return (int) (amount * EARN_RATE);
    }

    /*
     * 포인트 이용 내역 가공
     * point_id로 이력 조회 후 DTO 변환하여 반환
     */
    @Override
    public List<PointHistoryResponseDTO> getCustomerHistory(int pointId) {
        List<PointHistory> historyList = pointHistoryMapper.selectByPointId(pointId);

        return historyList.stream().map(history -> {
            String date = new SimpleDateFormat("MM-dd HH:mm").format(history.getCreatedAt());
            int changeAmount = "EARN".equals(history.getType()) ? history.getAmount() : -history.getAmount();
            String description = "EARN".equals(history.getType()) ? "포인트 적립" : "포인트 사용";

            return PointHistoryResponseDTO.builder()
                    .transactionDate(date)
                    .changeAmount(changeAmount)
                    .description(description)
                    .remainingPoint(history.getBalanceAfter())
                    .build();
        }).collect(Collectors.toList());
    }


    private CustomerResponseDTO toResponseDTO(Customer customer, int balance) {
        return CustomerResponseDTO.builder()
                .id(customer.getId())
                .phone(maskPhone(customer.getPhone()))
                .totalPoint(balance)
                .build();

    }

    private String maskPhone(String phone) { // 전화번호 가운데 4자리를 '****'로 가리는 메서드
        if (phone == null || phone.length() < 8) return phone;
        return phone.replaceAll("(\\d{3})-(\\d{4})-(\\d{4})", "$1-****-$3");
    };
}
