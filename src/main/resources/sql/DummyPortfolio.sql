-- ============================================================
--  보드게임 카페 키오스크 — 포트폴리오 시현용 더미 데이터
--  작성일: 2026-04-09
--
--  [시현 시나리오 설계]
--  ┌─────────────────────────────────────────────────────────┐
--  │ 관리자 대시보드에서 보여줄 것                              │
--  │  - 테이블 12개 중 4개 OCCUPIED / 1개 CLEANING / 7개 EMPTY│
--  │  - 각 점유 테이블마다 주문·메시지·게임대여 이력 존재        │
--  │  - 3월 전체 + 오늘 통계 데이터로 차트 즉시 렌더링          │
--  │                                                         │
--  │ 키오스크에서 보여줄 것                                    │
--  │  - 재로그인 시 partySize·packageId 세션 복구 정상 동작    │
--  │  - 장바구니에 아이템 담긴 상태로 진입 가능                 │
--  │  - 포인트 잔액 있는 고객 전화번호로 포인트 조회 가능        │
--  └─────────────────────────────────────────────────────────┘
--
--  [FK 의존 삽입 순서]
--  manager → cafe_table → customer → category → cafe_package
--  → table_session → UPDATE cafe_table
--  → menu → orders → order_item
--  → game → game_item → game_history
--  → cart → cart_item
--  → point → point_history
--  → macro_message → table_message
--  → 통계 프로시저 → item_sales_history → daily_sales_summary
-- ============================================================

USE `board_cafe_kiosk_2603`;

-- ============================================================
-- 1. manager
-- ============================================================
-- 계정/비밀번호 요약
--   admin   / 1111  / ADMIN / 활성
--   admin02 / 2222  / ADMIN / 비활성 (비교용)
--   super   / 1234  / ADMIN / 활성  (사장님)
--   pass    / 1234  / SUPER / 활성  (포트폴리오 시연용)
--   staff01 / 1111  / STAFF / 활성
--   staff02 / 2222  / STAFF / 활성
--   staff03 / 3333  / STAFF / 활성
--   staff04~06      / STAFF / 비활성 (퇴사 직원 시나리오)
INSERT INTO `manager` (`login_id`, `password`, `name`, `email`, `role`, `is_active`)
VALUES
    ('admin',   '$2a$10$I/U.nHfsL/6wBqXAJV1A3u0KwyHn9wiOVRK7ZVI6rAptphEgRW1Qi', '김관리', 'admin@boardcafe.com',   'ADMIN', TRUE),
    ('admin02', '$2a$10$RySZbh.V/f9khlbVamY3O.Mg8uY9qbwNTbykKep1SqqtbZ9OMB4xe', '이관리', 'admin02@boardcafe.com', 'ADMIN', FALSE),
    ('super',   '$2a$10$BTMMVv2aPEqCnTF4aWn7u.Tyuh.yruDyPVk1buElSdgCwbMUWOFRi', '박사장', 'wndus6110@naver.com',   'ADMIN', TRUE),
    ('pass',    '$2a$10$BTMMVv2aPEqCnTF4aWn7u.Tyuh.yruDyPVk1buElSdgCwbMUWOFRi', '시연계정', 'example@naver.com',   'SUPER', TRUE),
    ('staff01', '$2a$10$VW29gAYZYxDRdWhNP.KYUOVAkPeS1DZYSrcxywKGdjGpx4z0QitDa', '최직원', 'staff01@boardcafe.com', 'STAFF', TRUE),
    ('staff02', '$2a$10$OhUaODvgez2RlesuWWlyXeMzwWRNhYvTrNjgOy07//KxK8sdWaDFG', '정직원', 'staff02@boardcafe.com', 'STAFF', TRUE),
    ('staff03', '$2a$10$VW29gAYZYxgRdWhNP.KYUOVAkPeS1DZYSrcxywKGdjGpx4z0QitDa', '한직원', 'staff03@boardcafe.com', 'STAFF', TRUE),
    ('staff04', '$2a$10$OhUaODvgez3RlesuWWlyXeMzwWRNhYvTrNjgOy07//KxK8sdWaDFG', '구직원', 'staff04@boardcafe.com', 'STAFF', FALSE),
    ('staff05', '$2a$10$VW29gAYZYxfRdWhNP.KYUOVAkPeS1DZYSrcxywKGdjGpx4z0QitDa', '신직원', 'staff05@boardcafe.com', 'STAFF', FALSE),
    ('staff06', '$2a$10$OhUaODvgez4RlesuWWlyXeMzwWRNhYvTrNjgOy07//KxK8sdWaDFG', '임직원', 'staff06@boardcafe.com', 'STAFF', FALSE);

-- ============================================================
-- 2. cafe_table (12개)
-- 시현 구성: OCCUPIED 4개(1,2,5,8) / CLEANING 1개(6) / EMPTY 7개
-- current_session_id는 table_session 삽입 후 UPDATE
-- 비밀번호: 테이블번호 = 비밀번호 (1번=1111, 10번=1010 등)
-- ============================================================
INSERT INTO `cafe_table` (`table_number`, `password`, `status`, `access_token`, `current_session_id`)
VALUES
    (1,  '$2a$12$6.m99XxVXQhLA.kW.pV.8.yAkQtntwMG6zJ2XEzCYdIt6F92AHZoa', 'EMPTY', NULL, NULL),
    (2,  '$2a$12$jMyxkDnEXF6zTzs.6odIHuCtzfR35EDFxZmflbbamUHc9drejGipa', 'EMPTY', NULL, NULL),
    (3,  '$2a$12$wPexDR2riZFgwKTtj925FOXZFGPaf6U13GkiNK4Gd43M.1hltvlBS', 'EMPTY',    NULL, NULL),
    (4,  '$2a$12$6UqwKwlaRu05xPzlTbzBQeC68kViy7OrQscQQq.MzUiMyV9eyOlcO', 'EMPTY',    NULL, NULL),
    (5,  '$2a$12$BoILW/Dwdq267pCpVPlxWuKsjctBoWy1Jz8XY9KHJiA/v86.pPxYe', 'EMPTY', NULL, NULL),
    (6,  '$2a$12$EKiULQjPsNUuxtwfm1K9V.tmr1lkGAUZTzdKkFXiKRGt8N.oC2qwq', 'EMPTY', NULL, NULL),
    (7,  '$2a$12$epx2tbnDEk1tuGNOcYu1/.Ciww5olY7rULAIuiUEkn1CLGU1zLV3u', 'EMPTY',    NULL, NULL),
    (8,  '$2a$12$LANNhG93KYJLa7QpyB5t1uJ.pQBpy7CUFg8r8J.9WAX6ARxsNZzJC', 'EMPTY', NULL, NULL),
    (9,  '$2a$12$fHPOFdBDC9dlEIeR648aTeGAIEsK9SBC8UgsCq7vcV2KF6dMk6WDi', 'EMPTY',    NULL, NULL),
    (10, '$2a$12$A13LCSatRIIpBFoKiTNyLep7invKMx2KUClmbX28sHYLzEwK4Y4ui', 'EMPTY',    NULL, NULL),
    (11, '$2a$12$YoAiJOrZMD4Kk/9lHYi1BOjV2Y3kFSeslzr44L75nfKt6cEOCrgNa', 'EMPTY',    NULL, NULL),
    (12, '$2a$12$wXHXtdMIS3U7ASVzH1K4T.nbxE5X5nTcNbdv8BHk4zLOcIDSpv0pu', 'EMPTY',    NULL, NULL);

-- ============================================================
-- 3. customer
-- 포인트 잔액이 있는 고객을 여러 명 배치 → 키오스크 포인트 조회 시연용
-- ============================================================
INSERT INTO `customer` (`phone`, `is_active`)
VALUES
    ('010-1234-5678', TRUE),  -- id=1: 포인트 1200 (단골)
    ('010-2345-6789', TRUE),  -- id=2: 포인트 800
    ('010-3456-7890', TRUE),  -- id=3: 포인트 300
    ('010-4567-8901', TRUE),  -- id=4: 포인트 2500 (VIP)
    ('010-5678-9012', TRUE),  -- id=5: 포인트 550
    ('010-6789-0123', TRUE),  -- id=6: 포인트 75
    ('010-7890-1234', FALSE); -- id=7: 탈퇴 고객 시나리오

-- ============================================================
-- 4. category
-- ============================================================
INSERT INTO `category` (`name`, `type`)
VALUES
    ('커피·에스프레소', 'DRINK'), -- id=1
    ('논커피·에이드',   'DRINK'), -- id=2
    ('스낵·과자',       'FOOD'),  -- id=3
    ('식사류',          'FOOD'),  -- id=4
    ('전략 게임',       'GAME'),  -- id=5
    ('파티 게임',       'GAME'),  -- id=6
    ('협력 게임',       'GAME'),  -- id=7
    ('추가 인원',       'GUEST'); -- id=8

-- ============================================================
-- 5. cafe_package
-- ============================================================
INSERT INTO `cafe_package` (`name`, `type`, `duration_minutes`, `base_price`, `extra_price_per_min`, `is_active`)
VALUES
    ('1시간 패키지',    'HOURLY', 60,   5000,  NULL,  TRUE),  -- id=1
    ('2시간 패키지',    'HOURLY', 120,  8000,  NULL,  TRUE),  -- id=2
    ('3시간 패키지',    'HOURLY', 180,  11000, NULL,  TRUE),  -- id=3
    ('종일 자유이용권', 'FREE',   NULL, 15000, NULL,  TRUE),  -- id=4
    ('초과 시간 요금',  'HOURLY', 60,   2000,  35.00, FALSE); -- id=5 (내부용·비활성)

-- ============================================================
-- 6. table_session
--
-- [설계 의도]
--   과거 세션(id 1~5): 어제(오늘-1일) 완료 — 히스토리 조회 시연용
--   활성 세션(id 6~9): 오늘 현재 진행 중 — 대시보드 실시간 현황 시연용
--
--   활성 세션 check_in_time을 CURDATE() 기준으로 설정하여
--   실행 날짜와 무관하게 항상 "오늘 이용 중" 상태로 보임
-- ============================================================
INSERT INTO `table_session`
(`table_id`, `package_id`, `initial_guest_cnt`,
 `check_in_time`, `check_out_time`, `is_active`, `total_amount`)
VALUES
    -- 과거 완료 세션 (어제 날짜 기준)
    (1, 2, 2, DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 13 HOUR,
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '15:10' HOUR_MINUTE,
     FALSE, 24500), -- id=1
    (2, 3, 4, DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '15:30' HOUR_MINUTE,
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '18:45' HOUR_MINUTE,
     FALSE, 58000), -- id=2
    (3, 1, 1, DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '17:00' HOUR_MINUTE,
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '18:05' HOUR_MINUTE,
     FALSE,  7500), -- id=3
    (5, 2, 3, DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '19:00' HOUR_MINUTE,
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '21:15' HOUR_MINUTE,
     FALSE, 35000), -- id=4
    (8, 4, 5, DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '11:00' HOUR_MINUTE,
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '23:00' HOUR_MINUTE,
     FALSE, 95000), -- id=5
    -- 현재 활성 세션 (오늘 기준 — 실행 시각 무관하게 항상 "진행 중"으로 표시)
    (1, 2, 2, CURDATE() + INTERVAL '13:30' HOUR_MINUTE, NULL, TRUE, 10000), -- id=6  table1
    (2, 3, 3, CURDATE() + INTERVAL '14:00' HOUR_MINUTE, NULL, TRUE, 15500), -- id=7  table2
    (5, 1, 2, CURDATE() + INTERVAL '15:00' HOUR_MINUTE, NULL, TRUE,  7000), -- id=8  table5
    (8, 2, 4, CURDATE() + INTERVAL '12:00' HOUR_MINUTE, NULL, TRUE, 21000); -- id=9  table8

-- OCCUPIED 테이블에 활성 세션 연결
# UPDATE `cafe_table` SET `current_session_id` = 6 WHERE `table_number` = 1;
# UPDATE `cafe_table` SET `current_session_id` = 7 WHERE `table_number` = 2;
# UPDATE `cafe_table` SET `current_session_id` = 8 WHERE `table_number` = 5;
# UPDATE `cafe_table` SET `current_session_id` = 9 WHERE `table_number` = 8;

-- ============================================================
-- 7. menu
-- id=1~8  : 음료(DRINK)
-- id=9~15 : 음식(FOOD)  — id=15 핫도그는 품절 시나리오
-- id=16   : 추가인원(GUEST)
-- id=17~22: 게임(GAME)  — cart_item FK 연결용 price=0
-- ============================================================
INSERT INTO `menu` (`category_id`, `name`, `price`, `description`, `is_available`, `is_deleted`)
VALUES
    (1, '아메리카노',       3000, '깔끔하고 진한 에스프레소 베이스',   TRUE,  FALSE), -- id=1
    (1, '카페라떼',         3500, '우유와 에스프레소의 조화',          TRUE,  FALSE), -- id=2
    (1, '카푸치노',         3500, '풍성한 우유 거품과 에스프레소',     TRUE,  FALSE), -- id=3
    (1, '바닐라라떼',       4000, '달콤한 바닐라 시럽 추가',          TRUE,  FALSE), -- id=4
    (2, '레몬에이드',       4000, '상큼한 국산 레몬 착즙',            TRUE,  FALSE), -- id=5
    (2, '자몽에이드',       4000, '달콤 쌉싸름한 자몽 에이드',        TRUE,  FALSE), -- id=6
    (2, '녹차라떼',         3500, '국내산 말차 분말 사용',            TRUE,  FALSE), -- id=7
    (2, '유자차',           3500, '따뜻하게도 아이스로도',            TRUE,  FALSE), -- id=8
    (3, '팝콘 (오리지널)',  2000, '고소한 버터 팝콘',                TRUE,  FALSE), -- id=9
    (3, '팝콘 (카라멜)',    2500, '달콤한 카라멜 코팅',              TRUE,  FALSE), -- id=10
    (3, '나초 + 살사소스',  3000, '바삭한 나초와 살사소스 콤보',      TRUE,  FALSE), -- id=11
    (3, '믹스 너트',        3500, '7가지 프리미엄 너트 혼합',        TRUE,  FALSE), -- id=12
    (4, '토스트 세트',      5000, '계란 토스트 + 음료 세트',         TRUE,  FALSE), -- id=13
    (4, '컵라면',           1500, '신라면·짜파게티 선택 가능',        TRUE,  FALSE), -- id=14
    (4, '핫도그',           3000, '국산 돼지고기 소시지 사용',        FALSE, FALSE), -- id=15 품절 시나리오
    (8, '인원 추가 (1명)',  5000, '기본 패키지 인당 추가 요금',       TRUE,  FALSE), -- id=16
    (5, '맞춤법 게임',         0, '맞춤법을 맞추는 파티 게임',        TRUE,  FALSE), -- id=17
    (6, '숫자 맞추기',         0, '숫자를 맞추는 게임',              TRUE,  FALSE), -- id=18
    (6, '동물 맞추기',         0, '동물 카드 게임',                  TRUE,  FALSE), -- id=19
    (7, '색상 맞추기',         0, '색상을 맞추는 협력 게임',         TRUE,  FALSE), -- id=20
    (5, '스피드 게임',         0, '빠르게 반응하는 전략 게임',        TRUE,  FALSE), -- id=21
    (6, '퀴즈 게임',           0, '다양한 퀴즈 보드게임',            TRUE,  FALSE); -- id=22

-- ============================================================
-- 8. orders
-- 과거 세션(1~5) 주문: 모두 COMPLETED
-- 활성 세션(6~9) 주문: 다양한 status로 관리자 대시보드 상태 표현
--   session6(table1): COMPLETED (이미 처리됨)
--   session7(table2): COMPLETED
--   session8(table5): COOKING  (현재 조리 중 시나리오)
--   session9(table8): ORDERED  (방금 주문 들어온 시나리오)
--
-- orders.total_amount = 해당 order_item 합계와 반드시 일치
-- ============================================================
INSERT INTO `orders`
(`session_id`, `table_id`, `customer_phone`, `status`, `total_amount`, `ordered_at`)
VALUES
    -- 과거 세션 주문
    (1, 1, '010-1234-5678', 'COMPLETED', 13000,
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '13:10' HOUR_MINUTE), -- id=1
    (1, 1, '010-1234-5678', 'COMPLETED',  7500,
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '14:00' HOUR_MINUTE), -- id=2
    (2, 2, '010-2345-6789', 'COMPLETED', 34000,
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '15:45' HOUR_MINUTE), -- id=3
    (2, 2, NULL,            'COMPLETED', 10000,
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '17:00' HOUR_MINUTE), -- id=4
    (3, 3, '010-3456-7890', 'COMPLETED',  5000,
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '17:10' HOUR_MINUTE), -- id=5
    (4, 5, '010-4567-8901', 'COMPLETED', 21500,
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '19:15' HOUR_MINUTE), -- id=6
    (5, 8, NULL,            'COMPLETED', 46000,
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '12:00' HOUR_MINUTE), -- id=7
    -- 활성 세션 주문 (오늘)
    (6, 1, '010-1234-5678', 'COMPLETED', 10000,
     CURDATE() + INTERVAL '13:40' HOUR_MINUTE),  -- id=8  table1 처리완료
    (7, 2, '010-5678-9012', 'COMPLETED', 15500,
     CURDATE() + INTERVAL '14:20' HOUR_MINUTE),  -- id=9  table2 처리완료
    (8, 5, NULL,            'COOKING',    7000,
     CURDATE() + INTERVAL '15:10' HOUR_MINUTE),  -- id=10 table5 조리중
    (9, 8, '010-6789-0123', 'ORDERED',   21000,
     CURDATE() + INTERVAL '12:30' HOUR_MINUTE);  -- id=11 table8 신규주문

-- ============================================================
-- 9. order_item
-- 각 order의 total_amount와 price*quantity 합계 검증:
--   order1 : 아메리카노×2(6000) + 팝콘오리지널×1(2000) + 토스트세트×1(5000) = 13000 ✔
--   order2 : 레몬에이드×1(4000) + 믹스너트×1(3500)                           =  7500 ✔
--   order3 : 카페라떼×4(14000) + 팝콘카라멜×2(5000) + 토스트세트×3(15000)    = 34000 ✔
--   order4 : 녹차라떼×2(7000)  + 컵라면×2(3000)                              = 10000 ✔
--   order5 : 아메리카노×1(3000) + 팝콘오리지널×1(2000)                        =  5000 ✔
--   order6 : 카푸치노×3(10500) + 나초×2(6000) + 인원추가×1(5000)              = 21500 ✔
--   order7 : 바닐라라떼×4(16000)+ 팝콘오리지널×5(10000)+ 토스트세트×4(20000) = 46000 ✔
--   order8 : 아메리카노×2(6000) + 자몽에이드×1(4000)                          = 10000 ✔
--   order9 : 카페라떼×3(10500) + 토스트세트×1(5000)                           = 15500 ✔
--   order10: 레몬에이드×1(4000) + 컵라면×2(3000)                              =  7000 ✔
--   order11: 바닐라라떼×4(16000)+ 팝콘카라멜×2(5000)                          = 21000 ✔
-- ============================================================
INSERT INTO `order_item` (`order_id`, `menu_id`, `menu_name`, `price`, `quantity`)
VALUES
    (1,  1,  '아메리카노',      3000, 2),
    (1,  9,  '팝콘 (오리지널)', 2000, 1),
    (1,  13, '토스트 세트',     5000, 1),
    (2,  5,  '레몬에이드',      4000, 1),
    (2,  12, '믹스 너트',       3500, 1),
    (3,  2,  '카페라떼',        3500, 4),
    (3,  10, '팝콘 (카라멜)',   2500, 2),
    (3,  13, '토스트 세트',     5000, 3),
    (4,  7,  '녹차라떼',        3500, 2),
    (4,  14, '컵라면',          1500, 2),
    (5,  1,  '아메리카노',      3000, 1),
    (5,  9,  '팝콘 (오리지널)', 2000, 1),
    (6,  3,  '카푸치노',        3500, 3),
    (6,  11, '나초 + 살사소스', 3000, 2),
    (6,  16, '인원 추가 (1명)', 5000, 1),
    (7,  4,  '바닐라라떼',      4000, 4),
    (7,  9,  '팝콘 (오리지널)', 2000, 5),
    (7,  13, '토스트 세트',     5000, 4),
    (8,  1,  '아메리카노',      3000, 2),
    (8,  6,  '자몽에이드',      4000, 1),
    (9,  2,  '카페라떼',        3500, 3),
    (9,  13, '토스트 세트',     5000, 1),
    (10, 5,  '레몬에이드',      4000, 1),
    (10, 14, '컵라면',          1500, 2),
    (11, 4,  '바닐라라떼',      4000, 4),
    (11, 10, '팝콘 (카라멜)',   2500, 2);

-- ============================================================
-- 10. game
-- game.category_id → category(GAME 타입): 전략=5, 파티=6, 협력=7
-- ============================================================
INSERT INTO `game` (`category_id`, `name`, `min_players`, `max_players`, `play_time`, `is_active`)
VALUES
    (6, '맞춤법 게임', 2, 6,  20, TRUE), -- id=1  재고 NORMAL 3개
    (6, '숫자 맞추기', 2, 4,  15, TRUE), -- id=2  재고 NORMAL 2개
    (6, '동물 맞추기', 2, 6,  20, TRUE), -- id=3  재고 NORMAL 0개 (전부 대여/파손)
    (7, '색상 맞추기', 2, 5,  25, TRUE), -- id=4  재고 NORMAL 1개
    (5, '스피드 게임', 2, 8,  10, TRUE), -- id=5  재고 NORMAL 0개 (전부 대여중)
    (6, '퀴즈 게임',   2, 10, 30, TRUE); -- id=6  재고 NORMAL 4개

-- ============================================================
-- 11. game_item
-- id 자동증가 순서:
--   SPL-001~005 → id 1~5
--   NUM-001~003 → id 6~8
--   ANM-001~003 → id 9~11
--   CLR-001~003 → id 12~14
--   SPD-001~002 → id 15~16
--   QUZ-001~005 → id 17~21
-- ============================================================
INSERT INTO `game_item` (`game_id`, `serial_number`, `status`)
VALUES
    (1, 'SPL-001', 'NORMAL'),  -- id=1
    (1, 'SPL-002', 'NORMAL'),  -- id=2
    (1, 'SPL-003', 'NORMAL'),  -- id=3
    (1, 'SPL-004', 'RENTED'),  -- id=4  현재 대여 중
    (1, 'SPL-005', 'DAMAGED'), -- id=5  파손 시나리오
    (2, 'NUM-001', 'NORMAL'),  -- id=6
    (2, 'NUM-002', 'NORMAL'),  -- id=7
    (2, 'NUM-003', 'RENTED'),  -- id=8  현재 대여 중
    (3, 'ANM-001', 'RENTED'),  -- id=9
    (3, 'ANM-002', 'RENTED'),  -- id=10
    (3, 'ANM-003', 'DAMAGED'), -- id=11
    (4, 'CLR-001', 'NORMAL'),  -- id=12
    (4, 'CLR-002', 'RENTED'),  -- id=13 현재 대여 중
    (4, 'CLR-003', 'LOST'),    -- id=14 분실 시나리오
    (5, 'SPD-001', 'RENTED'),  -- id=15
    (5, 'SPD-002', 'RENTED'),  -- id=16
    (6, 'QUZ-001', 'NORMAL'),  -- id=17
    (6, 'QUZ-002', 'NORMAL'),  -- id=18
    (6, 'QUZ-003', 'NORMAL'),  -- id=19
    (6, 'QUZ-004', 'NORMAL'),  -- id=20
    (6, 'QUZ-005', 'RENTED');  -- id=21 현재 대여 중

-- ============================================================
-- 12. game_history
-- 과거 세션: 반납 완료(RETURNED)
-- 활성 세션: 현재 대여 중(RENTING) — game_item.status=RENTED 와 1:1 대응
--
-- game_item_id 참조 매핑 (id 기준):
--   SPL-004=4, NUM-001=6, NUM-003=8, ANM-001=9, ANM-002=10
--   CLR-002=13, SPD-001=15, SPD-002=16, QUZ-005=21
-- ============================================================
INSERT INTO `game_history`
(`session_id`, `game_item_id`, `rented_at`, `returned_at`, `status`)
VALUES
    -- 과거 세션 반납 완료
    (1, 4,
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '13:05' HOUR_MINUTE,
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '14:50' HOUR_MINUTE,
     'RETURNED'),
    (2, 9,
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '15:35' HOUR_MINUTE,
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '18:30' HOUR_MINUTE,
     'RETURNED'),
    (3, 6,
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '17:05' HOUR_MINUTE,
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '18:00' HOUR_MINUTE,
     'RETURNED'),
    (4, 13,
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '19:05' HOUR_MINUTE,
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '21:10' HOUR_MINUTE,
     'RETURNED'),
    (5, 15,
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '11:05' HOUR_MINUTE,
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '22:50' HOUR_MINUTE,
     'RETURNED'),
    -- 현재 대여 중 (활성 세션 6~9)
    (6, 4,  CURDATE() + INTERVAL '13:35' HOUR_MINUTE, NULL, 'RENTING'), -- table1: SPL-004
    (7, 9,  CURDATE() + INTERVAL '14:05' HOUR_MINUTE, NULL, 'RENTING'), -- table2: ANM-001
    (7, 10, CURDATE() + INTERVAL '14:05' HOUR_MINUTE, NULL, 'RENTING'), -- table2: ANM-002
    (8, 13, CURDATE() + INTERVAL '15:05' HOUR_MINUTE, NULL, 'RENTING'), -- table5: CLR-002
    (8, 15, CURDATE() + INTERVAL '15:05' HOUR_MINUTE, NULL, 'RENTING'), -- table5: SPD-001
    (8, 16, CURDATE() + INTERVAL '15:05' HOUR_MINUTE, NULL, 'RENTING'), -- table5: SPD-002
    (9, 8,  CURDATE() + INTERVAL '12:05' HOUR_MINUTE, NULL, 'RENTING'), -- table8: NUM-003
    (9, 21, CURDATE() + INTERVAL '12:05' HOUR_MINUTE, NULL, 'RENTING'); -- table8: QUZ-005

-- ============================================================
-- 13. cart (테이블 12개 전부 생성 — 키오스크 화면 렌더링 필수)
-- cart.id = 테이블 삽입 순서와 동일 (1~12)
-- ============================================================
INSERT INTO `cart` (`table_id`, `updated_at`)
VALUES
    (1,  NOW()), (2,  NOW()), (3,  NOW()), (4,  NOW()),
    (5,  NOW()), (6,  NOW()), (7,  NOW()), (8,  NOW()),
    (9,  NOW()), (10, NOW()), (11, NOW()), (12, NOW());

-- ============================================================
-- 14. cart_item
-- 활성 세션 테이블(1,2,5,8)에 장바구니 아이템 배치
-- → 키오스크 재로그인 후 장바구니 유지 시나리오 시연
-- cart_id는 위 cart 삽입 순서 기준 (table_id=1→cart_id=1, ...)
-- ============================================================
INSERT INTO `cart_item` (`cart_id`, `menu_id`, `quantity`)
VALUES
    (1, 1,  1), -- table1: 아메리카노 1개
    (1, 9,  1), -- table1: 팝콘(오리지널) 1개
    (2, 2,  2), -- table2: 카페라떼 2개
    (2, 13, 1), -- table2: 토스트세트 1개
    (5, 5,  1), -- table5: 레몬에이드 1개
    (8, 4,  2), -- table8: 바닐라라떼 2개
    (8, 12, 1); -- table8: 믹스너트 1개

-- ============================================================
-- 15. payment (과거 완료 세션 결제 내역)
-- 활성 세션(6~9)은 아직 결제 전이므로 미삽입
-- payment.session_id → UNIQUE 제약이 있으므로 세션당 1건만
-- ============================================================
INSERT INTO `payment`
(`session_id`, `table_number`, `status`, `final_amount`, `method`, `paid_at`)
VALUES
    (1, 1, 'DONE', 24500, '카드',    DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '15:10' HOUR_MINUTE),
    (2, 2, 'DONE', 58000, '카드',    DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '18:45' HOUR_MINUTE),
    (3, 3, 'DONE',  7500, '간편결제', DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '18:05' HOUR_MINUTE),
    (4, 5, 'DONE', 35000, '카드',    DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '21:15' HOUR_MINUTE),
    (5, 8, 'DONE', 95000, '간편결제', DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '23:00' HOUR_MINUTE);

-- ============================================================
-- 16. point (고객 포인트 계좌)
-- customer.phone 과 point.phone 1:1 대응
-- ============================================================
INSERT INTO `point` (`phone`, `balance`)
VALUES
    ('010-1234-5678', 1200), -- 단골 고객
    ('010-2345-6789',  800),
    ('010-3456-7890',  300),
    ('010-4567-8901', 2500), -- VIP 고객
    ('010-5678-9012',  550),
    ('010-6789-0123',   75),
    ('010-7890-1234',    0); -- 탈퇴 고객

-- ============================================================
-- 17. point_history
-- point.id 삽입 순서: 010-1234-5678=1, ..., 010-7890-1234=7
-- order_id FK: orders 테이블 id와 일치
-- ============================================================
INSERT INTO `point_history`
(`point_id`, `order_id`, `type`, `amount`, `balance_after`, `created_at`)
VALUES
    (1, 1,    'EARN', 130,  130,
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '13:10' HOUR_MINUTE),
    (1, 2,    'EARN',  75,  205,
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '14:00' HOUR_MINUTE),
    (1, 8,    'EARN', 100,  305,
     CURDATE() + INTERVAL '13:40' HOUR_MINUTE),
    (1, NULL, 'USE',  200,  105,
     CURDATE() + INTERVAL '13:45' HOUR_MINUTE),
    (1, NULL, 'EARN', 1095, 1200,
     CURDATE() + INTERVAL '14:00' HOUR_MINUTE),
    (2, 3,    'EARN',  340,  340,
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '15:45' HOUR_MINUTE),
    (2, NULL, 'EARN',  460,  800,
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '18:00' HOUR_MINUTE),
    (3, 5,    'EARN',   50,   50,
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '17:10' HOUR_MINUTE),
    (3, NULL, 'EARN',  250,  300,
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '18:00' HOUR_MINUTE),
    (4, 6,    'EARN',  215,  215,
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '19:15' HOUR_MINUTE),
    (4, NULL, 'EARN', 2285, 2500,
     DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL '21:00' HOUR_MINUTE),
    (5, 9,    'EARN',  155,  155,
     CURDATE() + INTERVAL '14:20' HOUR_MINUTE),
    (5, NULL, 'EARN',  395,  550,
     CURDATE() + INTERVAL '15:00' HOUR_MINUTE),
    (6, 11,   'EARN',  210,  210,
     CURDATE() + INTERVAL '12:30' HOUR_MINUTE),
    (6, NULL, 'USE',   135,   75,
     CURDATE() + INTERVAL '13:00' HOUR_MINUTE);

-- ============================================================
-- 18. macro_message
-- ============================================================
INSERT INTO `macro_message` (`direction`, `message_text`, `is_active`)
VALUES
    ('STAFF_TO_TABLE', '주문하신 음료와 스낵이 준비되었습니다. 카운터에서 수령해 주세요.', TRUE),
    ('STAFF_TO_TABLE', '이용 시간이 10분 남았습니다. 연장을 원하시면 카운터에 문의해 주세요.', TRUE),
    ('STAFF_TO_TABLE', '주문하신 메뉴가 품절되어 취소 처리되었습니다. 죄송합니다.', TRUE),
    ('STAFF_TO_TABLE', '보드게임 반납 구역이 혼잡하오니 테이블에 두시면 직원이 치워드리겠습니다.', TRUE),
    ('STAFF_TO_TABLE', '진행 중인 이벤트에 당첨되셨습니다! 카운터에서 선물을 확인하세요.', TRUE),
    ('STAFF_TO_TABLE', '외부 음식 반입은 금지되어 있습니다. 양해 부탁드립니다.', TRUE),
    ('TABLE_TO_STAFF', '게임 설명이 필요합니다. 직원을 호출해 주세요.', TRUE),
    ('TABLE_TO_STAFF', '테이블이 지저분합니다. 청소 부탁드려요.', TRUE),
    ('TABLE_TO_STAFF', '물티슈나 티슈가 부족합니다. 가져다주세요.', TRUE),
    ('TABLE_TO_STAFF', '에어컨/히터 온도 조절 부탁드립니다. (추워요/더워요)', TRUE),
    ('TABLE_TO_STAFF', '음료를 쏟았습니다. 도움이 필요합니다.', TRUE),
    ('TABLE_TO_STAFF', '결제 방식 변경이나 오류 문의로 호출합니다.', TRUE);

-- ============================================================
-- 19. table_message
-- 읽지 않은 메시지(is_read=FALSE) → 대시보드 알림 뱃지 시연
-- macro_id FK → macro_message.id 참조 (NULL 허용)
-- ============================================================
INSERT INTO `table_message` (`table_id`, `macro_id`, `content`, `is_read`)
VALUES
    (1, NULL, '물 좀 주세요!',               FALSE), -- 미확인 알림
    (1, NULL, '물티슈 좀 주세요!',            FALSE), -- 미확인 알림
    (2, NULL, '2인용 보드게임 추천 부탁드려요.', FALSE), -- 미확인 알림
    (5, NULL, '결제할게요~',                  TRUE),  -- 확인 완료
    (8, NULL, '게임 규칙 설명 부탁드립니다.',  FALSE); -- 미확인 알림

-- ============================================================
-- 20. 3월 통계 데이터 생성 프로시저
-- 1000건 → 차트·통계 화면이 풍부하게 렌더링됨
-- ============================================================
DELIMITER $$

DROP PROCEDURE IF EXISTS generate_march_stats_data$$

CREATE PROCEDURE generate_march_stats_data()
BEGIN
    DECLARE i               INT     DEFAULT 1;
    DECLARE rand_date       DATETIME;
    DECLARE rand_table_id   INT;
    DECLARE rand_pkg_id     INT;
    DECLARE rand_guest_cnt  INT;
    DECLARE rand_duration   INT;
    DECLARE last_session_id BIGINT;
    DECLARE last_order_id   INT;
    DECLARE ord_cnt         INT;

    WHILE i <= 1000 DO
            SET rand_date = FROM_UNIXTIME(
                    UNIX_TIMESTAMP('2026-03-01 11:00:00')
                        + FLOOR(RAND() * (UNIX_TIMESTAMP('2026-03-31 23:00:00')
                        - UNIX_TIMESTAMP('2026-03-01 11:00:00')))
                            );
            SET rand_table_id  = CEIL(RAND() * 12);
            SET rand_pkg_id    = CEIL(RAND() * 4);
            SET rand_guest_cnt = FLOOR(1 + RAND() * 5);
            SET rand_duration  = FLOOR(60 + RAND() * 180);

            INSERT INTO `table_session`
            (table_id, package_id, initial_guest_cnt,
             check_in_time, check_out_time, is_active, total_amount)
            VALUES
                (rand_table_id, rand_pkg_id, rand_guest_cnt,
                 rand_date, DATE_ADD(rand_date, INTERVAL rand_duration MINUTE),
                 FALSE, 0);

            SET last_session_id = LAST_INSERT_ID();
            SET ord_cnt = FLOOR(1 + RAND() * 2);

            WHILE ord_cnt > 0 DO
                    INSERT INTO `orders` (session_id, table_id, status, total_amount, ordered_at)
                    VALUES (last_session_id, rand_table_id, 'COMPLETED', 0,
                            DATE_ADD(rand_date, INTERVAL (ord_cnt * 15) MINUTE));

                    SET last_order_id = LAST_INSERT_ID();

                    INSERT INTO `order_item` (order_id, menu_id, menu_name, price, quantity)
                    SELECT last_order_id, id, name, price, FLOOR(1 + RAND() * 2)
                    FROM   `menu`
                    WHERE  id = FLOOR(1 + RAND() * 16)
                      AND  is_deleted = FALSE
                    LIMIT 1;

                    UPDATE `orders`
                    SET total_amount = (
                        SELECT IFNULL(SUM(price * quantity), 0)
                        FROM `order_item` WHERE order_id = last_order_id
                    )
                    WHERE id = last_order_id;

                    SET ord_cnt = ord_cnt - 1;
                END WHILE;

            UPDATE `table_session`
            SET total_amount = (
                SELECT IFNULL(SUM(total_amount), 0)
                FROM `orders` WHERE session_id = last_session_id
            )
            WHERE id = last_session_id;

            INSERT INTO `payment` (session_id, status, final_amount, paid_at)
            SELECT id, 'DONE', total_amount,
                   IFNULL(check_out_time, DATE_ADD(check_in_time, INTERVAL rand_duration MINUTE))
            FROM `table_session` WHERE id = last_session_id;

            SET i = i + 1;
        END WHILE;
END$$

DELIMITER ;

CALL generate_march_stats_data();

-- ============================================================
-- 21. 오늘 실시간 통계 데이터 생성 프로시저 (100건)
-- ============================================================
DELIMITER $$

DROP PROCEDURE IF EXISTS generate_today_stats_data$$

CREATE PROCEDURE generate_today_stats_data()
BEGIN
    DECLARE i               INT     DEFAULT 1;
    DECLARE rand_date       DATETIME;
    DECLARE rand_table_id   INT;
    DECLARE rand_pkg_id     INT;
    DECLARE rand_guest_cnt  INT;
    DECLARE rand_duration   INT;
    DECLARE last_session_id BIGINT;
    DECLARE last_order_id   INT;
    DECLARE ord_cnt         INT;
    DECLARE is_extra_guest  INT;

    WHILE i <= 100 DO
            SET rand_date      = DATE_ADD(CURDATE(), INTERVAL FLOOR(10*60 + RAND() * 12*60) MINUTE);
            SET rand_table_id  = CEIL(RAND() * 12);
            SET rand_pkg_id    = CEIL(RAND() * 4);
            SET rand_guest_cnt = FLOOR(1 + RAND() * 4);
            SET rand_duration  = FLOOR(60 + RAND() * 120);

            INSERT INTO `table_session`
            (table_id, package_id, initial_guest_cnt,
             check_in_time, check_out_time, is_active, total_amount)
            VALUES
                (rand_table_id, rand_pkg_id, rand_guest_cnt,
                 rand_date, DATE_ADD(rand_date, INTERVAL rand_duration MINUTE),
                 FALSE, 0);

            SET last_session_id = LAST_INSERT_ID();
            SET ord_cnt = FLOOR(1 + RAND() * 3);

            WHILE ord_cnt > 0 DO
                    INSERT INTO `orders` (session_id, table_id, status, total_amount, ordered_at)
                    VALUES (last_session_id, rand_table_id, 'COMPLETED', 0,
                            DATE_ADD(rand_date, INTERVAL (ord_cnt * 15) MINUTE));

                    SET last_order_id  = LAST_INSERT_ID();
                    SET is_extra_guest = FLOOR(1 + RAND() * 10);

                    IF is_extra_guest <= 3 THEN
                        INSERT INTO `order_item` (order_id, menu_id, menu_name, price, quantity)
                        SELECT last_order_id, id, name, price, FLOOR(1 + RAND() * 2)
                        FROM `menu` WHERE name = '인원 추가 (1명)' LIMIT 1;
                    ELSE
                        INSERT INTO `order_item` (order_id, menu_id, menu_name, price, quantity)
                        SELECT last_order_id, id, name, price, FLOOR(1 + RAND() * 2)
                        FROM `menu`
                        WHERE name != '인원 추가 (1명)' AND is_deleted = FALSE
                        ORDER BY RAND() LIMIT 1;
                    END IF;

                    UPDATE `orders`
                    SET total_amount = (
                        SELECT IFNULL(SUM(price * quantity), 0)
                        FROM `order_item` WHERE order_id = last_order_id
                    )
                    WHERE id = last_order_id;

                    SET ord_cnt = ord_cnt - 1;
                END WHILE;

            UPDATE `table_session`
            SET total_amount = (
                SELECT IFNULL(SUM(total_amount), 0)
                FROM `orders` WHERE session_id = last_session_id
            )
            WHERE id = last_session_id;

            INSERT INTO `payment` (session_id, status, final_amount, paid_at)
            SELECT id, 'DONE', total_amount,
                   IFNULL(check_out_time, DATE_ADD(check_in_time, INTERVAL rand_duration MINUTE))
            FROM `table_session` WHERE id = last_session_id;

            SET i = i + 1;
        END WHILE;
END$$

DELIMITER ;

CALL generate_today_stats_data();

-- ============================================================
-- 22. item_sales_history (3월 + 오늘 통합)
-- ============================================================
INSERT IGNORE INTO `item_sales_history`
(stat_date, product_id, category, sales_qty, sales_amount)
SELECT
    DATE(p.paid_at),
    oi.menu_id,
    c.type,
    SUM(oi.quantity),
    SUM(oi.price * oi.quantity)
FROM   `payment`    p
           JOIN   `orders`     o  ON p.session_id  = o.session_id
           JOIN   `order_item` oi ON o.id          = oi.order_id
           JOIN   `menu`       m  ON oi.menu_id    = m.id
           JOIN   `category`   c  ON m.category_id = c.id
WHERE  p.status = 'DONE'
  AND  c.type NOT IN ('GAME', 'GUEST')
GROUP BY DATE(p.paid_at), oi.menu_id, c.type;

-- ============================================================
-- 23. daily_sales_summary (3월 + 오늘 통합)
-- ============================================================
INSERT IGNORE INTO `daily_sales_summary`
(stat_date, total_revenue, order_count, visit_count, avg_usage_time)
SELECT
    DATE(p.paid_at),
    SUM(p.final_amount),
    COUNT(DISTINCT o.id),
    SUM(ts.initial_guest_cnt + IFNULL(ex.extra_qty, 0)),
    IFNULL(AVG(TIMESTAMPDIFF(MINUTE, ts.check_in_time, ts.check_out_time)), 0)
FROM `payment` p
         JOIN `table_session` ts ON p.session_id = ts.id
         LEFT JOIN `orders` o    ON ts.id = o.session_id
         LEFT JOIN (
    SELECT o2.session_id, SUM(oi2.quantity) AS extra_qty
    FROM   `order_item` oi2
               JOIN   `orders`     o2  ON oi2.order_id   = o2.id
               JOIN   `menu`       m2  ON oi2.menu_id    = m2.id
               JOIN   `category`   c2  ON m2.category_id = c2.id
    WHERE  c2.type = 'GUEST'
    GROUP BY o2.session_id
) ex ON p.session_id = ex.session_id
WHERE  p.status = 'DONE'
GROUP BY DATE(p.paid_at);