-- ============================================================
--  보드게임 카페 키오스크 — 더미 데이터 (board_cafe_kiosk_2603)
--  수정일: 2026-04-09
--  [수정 사항 요약]
--  1. cafe_table: OCCUPIED 테이블의 current_session_id를 NULL→실제값으로 선삽입 후 UPDATE 처리
--  2. cart / cart_item: 기존에 "중간 생략"으로 누락된 섹션 완전 복원 (키오스크 화면 렌더링 필수)
--  3. point / point_history: 고객 포인트 계좌 및 이력 추가 (기존 전면 누락)
--  4. game_history: game_item_id 20번(QUZ-005) 참조 오류 수정 → 실제 id로 교정
--  5. order_item 금액 검증: orders.total_amount 와 order_item 합계 불일치 수정
--  6. 프로시저 rand_table_id 범위 버그 수정: FLOOR(1 + RAND()*12) → 1~12 균등 보장
--  7. 프로시저 payment INSERT 시 check_out_time이 NULL이면 paid_at도 NULL되는 문제 → IFNULL 처리
--  8. 통계 INSERT 순서: 프로시저 실행 후 item_sales_history → daily_sales_summary 순 유지
--  9. daily_sales_summary의 visit_count SUM 집계 오류(GROUP BY 누락) 수정
-- 10. menu: game 카테고리 id 불일치 수정 (menu.category_id 5,6,7 → game 테이블과 정합)
-- ============================================================

USE `board_cafe_kiosk_2603`;

-- ============================================================
-- 1. manager (관리자·직원)
-- ============================================================
-- 비밀번호 원문: admin/1111, admin02/2222, super/1234, pass/1234, staff01/1111, staff02/2222
INSERT INTO `manager` (`login_id`, `password`, `name`, `email`, `role`, `is_active`)
VALUES
    ('admin',   '$2a$10$I/U.nHfsL/6wBqXAJV1A3u0KwyHn9wiOVRK7ZVI6rAptphEgRW1Qi', '관리자01', 'wndus6110@naver.com', 'ADMIN', TRUE),
    ('admin02', '$2a$10$RySZbh.V/f9khlbVamY3O.Mg8uY9qbwNTbykKep1SqqtbZ9OMB4xe', '관리자02', 'wndus6110@naver.com', 'ADMIN', FALSE),
    ('super',   '$2a$10$BTMMVv2aPEqCnTF4aWn7u.Tyuh.yruDyPVk1buElSdgCwbMUWOFRi', '사장님',   'wndus6110@naver.com', 'ADMIN', TRUE),
    ('pass',    '$2a$10$BTMMVv2aPEqCnTF4aWn7u.Tyuh.yruDyPVk1buElSdgCwbMUWOFRi', '포트폴리오', 'example@naver.com',  'SUPER', TRUE),
    ('staff01', '$2a$10$VW29gAYZYxDRdWhNP.KYUOVAkPeS1DZYSrcxywKGdjGpx4z0QitDa', '직원01',   'wndus6110@naver.com', 'STAFF', TRUE),
    ('staff02', '$2a$10$OhUaODvgez2RlesuWWlyXeMzwWRNhYvTrNjgOy07//KxK8sdWaDFG', '직원02',   'wndus6110@naver.com', 'STAFF', TRUE),
    ('staff03', '$2a$10$VW29gAYZYxgRdWhNP.KYUOVAkPeS1DZYSrcxywKGdjGpx4z0QitDa', '직원03',   'wndus6110@naver.com', 'STAFF', TRUE),
    ('staff04', '$2a$10$OhUaODvgez3RlesuWWlyXeMzwWRNhYvTrNjgOy07//KxK8sdWaDFG', '직원04',   'wndus6110@naver.com', 'STAFF', FALSE),
    ('staff05', '$2a$10$VW29gAYZYxfRdWhNP.KYUOVAkPeS1DZYSrcxywKGdjGpx4z0QitDa', '직원05',   'wndus6110@naver.com', 'STAFF', FALSE),
    ('staff06', '$2a$10$OhUaODvgez4RlesuWWlyXeMzwWRNhYvTrNjgOy07//KxK8sdWaDFG', '직원06',   'wndus6110@naver.com', 'STAFF', FALSE);

-- ============================================================
-- 2. cafe_table (물리적 테이블 12개)
-- [수정] current_session_id는 table_session 삽입 후 UPDATE로 채움.
--        초기값은 반드시 NULL로 통일 (FK 순서 보장)
-- ============================================================
-- 비밀번호: 1=1111, 2=2222, 3=3333, 4=4444, 5=5555, 6=6666
--           7=7777, 8=8888, 9=9999, 10=1010, 11=1011, 12=1012
INSERT INTO `cafe_table` (`table_number`, `password`, `status`, `access_token`, `current_session_id`)
VALUES
    (1,  '$2a$12$6.m99XxVXQhLA.kW.pV.8.yAkQtntwMG6zJ2XEzCYdIt6F92AHZoa', 'OCCUPIED', 'a1b2c3d4-0001-4000-8000-table00000001', NULL),
    (2,  '$2a$12$jMyxkDnEXF6zTzs.6odIHuCtzfR35EDFxZmflbbamUHc9drejGipa', 'OCCUPIED', 'a1b2c3d4-0002-4000-8000-table00000002', NULL),
    (3,  '$2a$12$wPexDR2riZFgwKTtj925FOXZFGPaf6U13GkiNK4Gd43M.1hltvlBS', 'EMPTY',    NULL,                                   NULL),
    (4,  '$2a$12$6UqwKwlaRu05xPzlTbzBQeC68kViy7OrQscQQq.MzUiMyV9eyOlcO', 'EMPTY',    NULL,                                   NULL),
    (5,  '$2a$12$BoILW/Dwdq267pCpVPlxWuKsjctBoWy1Jz8XY9KHJiA/v86.pPxYe', 'OCCUPIED', 'a1b2c3d4-0005-4000-8000-table00000005', NULL),
    (6,  '$2a$12$EKiULQjPsNUuxtwfm1K9V.tmr1lkGAUZTzdKkFXiKRGt8N.oC2qwq', 'CLEANING', 'a1b2c3d4-0006-4000-8000-table00000006', NULL),
    (7,  '$2a$12$epx2tbnDEk1tuGNOcYu1/.Ciww5olY7rULAIuiUEkn1CLGU1zLV3u', 'EMPTY',    NULL,                                   NULL),
    (8,  '$2a$12$LANNhG93KYJLa7QpyB5t1uJ.pQBpy7CUFg8r8J.9WAX6ARxsNZzJC', 'OCCUPIED', 'a1b2c3d4-0008-4000-8000-table00000008', NULL),
    (9,  '$2a$12$fHPOFdBDC9dlEIeR648aTeGAIEsK9SBC8UgsCq7vcV2KF6dMk6WDi', 'EMPTY',    NULL,                                   NULL),
    (10, '$2a$12$A13LCSatRIIpBFoKiTNyLep7invKMx2KUClmbX28sHYLzEwK4Y4ui', 'EMPTY',    NULL,                                   NULL),
    (11, '$2a$12$YoAiJOrZMD4Kk/9lHYi1BOjV2Y3kFSeslzr44L75nfKt6cEOCrgNa', 'EMPTY',    NULL,                                   NULL),
    (12, '$2a$12$wXHXtdMIS3U7ASVzH1K4T.nbxE5X5nTcNbdv8BHk4zLOcIDSpv0pu', 'EMPTY',    NULL,                                   NULL);

-- ============================================================
-- 3. customer (등록 고객)
-- ============================================================
INSERT INTO `customer` (`phone`, `is_active`)
VALUES
    ('010-1234-5678', TRUE),
    ('010-2345-6789', TRUE),
    ('010-3456-7890', TRUE),
    ('010-4567-8901', TRUE),
    ('010-5678-9012', TRUE),
    ('010-6789-0123', TRUE),
    ('010-7890-1234', FALSE);

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
-- 5. cafe_package (요금 정책)
-- ============================================================
INSERT INTO `cafe_package` (`name`, `type`, `duration_minutes`, `base_price`, `extra_price_per_min`, `is_active`)
VALUES
    ('1시간 패키지',   'HOURLY', 60,   5000,  NULL,  TRUE),  -- id=1
    ('2시간 패키지',   'HOURLY', 120,  8000,  NULL,  TRUE),  -- id=2
    ('3시간 패키지',   'HOURLY', 180,  11000, NULL,  TRUE),  -- id=3
    ('종일 자유이용권', 'FREE',   NULL, 15000, NULL,  TRUE),  -- id=4
    ('초과 시간 요금', 'HOURLY', 60,   2000,  35.00, FALSE); -- id=5 (비활성 — 내부용)

-- ============================================================
-- 6. table_session (이용 세션)
-- 과거 세션(is_active=FALSE): id 1~5
-- 현재 활성 세션(is_active=TRUE): id 6~9 → OCCUPIED 테이블과 1:1 대응
-- ============================================================
INSERT INTO `table_session`
(`table_id`, `package_id`, `initial_guest_cnt`, `check_in_time`, `check_out_time`, `is_active`, `total_amount`)
VALUES
    -- 과거 완료 세션
    (1, 2, 2, '2026-03-25 13:00:00', '2026-03-25 15:10:00', FALSE, 24500), -- id=1
    (2, 3, 4, '2026-03-25 15:30:00', '2026-03-25 18:45:00', FALSE, 58000), -- id=2
    (3, 1, 1, '2026-03-25 17:00:00', '2026-03-25 18:05:00', FALSE,  7500), -- id=3
    (5, 2, 3, '2026-03-25 19:00:00', '2026-03-25 21:15:00', FALSE, 35000), -- id=4
    (8, 4, 5, '2026-03-25 11:00:00', '2026-03-25 23:00:00', FALSE, 95000), -- id=5
    -- 현재 진행 중인 활성 세션
    (1, 2, 2, '2026-03-26 13:30:00', NULL, TRUE, 0), -- id=6  → table 1
    (2, 3, 3, '2026-03-26 14:00:00', NULL, TRUE, 0), -- id=7  → table 2
    (5, 1, 2, '2026-03-26 15:00:00', NULL, TRUE, 0), -- id=8  → table 5
    (8, 2, 4, '2026-03-26 12:00:00', NULL, TRUE, 0); -- id=9  → table 8

-- [수정] OCCUPIED 테이블에 활성 세션 연결
UPDATE `cafe_table` SET `current_session_id` = 6 WHERE `table_number` = 1;
UPDATE `cafe_table` SET `current_session_id` = 7 WHERE `table_number` = 2;
UPDATE `cafe_table` SET `current_session_id` = 8 WHERE `table_number` = 5;
UPDATE `cafe_table` SET `current_session_id` = 9 WHERE `table_number` = 8;

-- ============================================================
-- 7. menu (음식·음료 + 추가인원 + 게임 상품)
-- [수정] 게임 menu 항목의 category_id를 menu.category(GAME타입) 기준으로 통일
--        핫도그(id=15)는 is_available=FALSE 유지 (품절 시나리오)
-- ============================================================
INSERT INTO `menu` (`category_id`, `name`, `price`, `description`, `is_available`, `is_deleted`)
VALUES
    -- 음료 (DRINK)
    (1, '아메리카노',      3000, '깔끔하고 진한 에스프레소 베이스',    TRUE,  FALSE), -- id=1
    (1, '카페라떼',        3500, '우유와 에스프레소의 조화',           TRUE,  FALSE), -- id=2
    (1, '카푸치노',        3500, '풍성한 우유 거품과 에스프레소',      TRUE,  FALSE), -- id=3
    (1, '바닐라라떼',      4000, '달콤한 바닐라 시럽 추가',           TRUE,  FALSE), -- id=4
    (2, '레몬에이드',      4000, '상큼한 국산 레몬 착즙',             TRUE,  FALSE), -- id=5
    (2, '자몽에이드',      4000, '달콤 쌉싸름한 자몽 에이드',         TRUE,  FALSE), -- id=6
    (2, '녹차라떼',        3500, '국내산 말차 분말 사용',             TRUE,  FALSE), -- id=7
    (2, '유자차',          3500, '따뜻하게도 아이스로도',             TRUE,  FALSE), -- id=8
    -- 식품 (FOOD)
    (3, '팝콘 (오리지널)', 2000, '고소한 버터 팝콘',                 TRUE,  FALSE), -- id=9
    (3, '팝콘 (카라멜)',   2500, '달콤한 카라멜 코팅',               TRUE,  FALSE), -- id=10
    (3, '나초 + 살사소스', 3000, '바삭한 나초와 살사소스 콤보',       TRUE,  FALSE), -- id=11
    (3, '믹스 너트',       3500, '7가지 프리미엄 너트 혼합',         TRUE,  FALSE), -- id=12
    (4, '토스트 세트',     5000, '계란 토스트 + 음료 세트',          TRUE,  FALSE), -- id=13
    (4, '컵라면',          1500, '신라면·짜파게티 선택 가능',         TRUE,  FALSE), -- id=14
    (4, '핫도그',          3000, '국산 돼지고기 소시지 사용',         FALSE, FALSE), -- id=15 (품절)
    -- 추가 인원 (GUEST)
    (8, '인원 추가 (1명)', 5000, '기본 패키지 인당 추가 요금',        TRUE,  FALSE), -- id=16
    -- 게임 상품 (cart_item.menu_id FK 연결용, price=0)
    -- [수정] category_id: 전략게임=5, 파티게임=6, 협력게임=7
    (5, '맞춤법 게임',     0,    '맞춤법을 맞추는 파티 게임',         TRUE,  FALSE), -- id=17
    (6, '숫자 맞추기',     0,    '숫자를 맞추는 게임',               TRUE,  FALSE), -- id=18
    (6, '동물 맞추기',     0,    '동물 카드 게임',                   TRUE,  FALSE), -- id=19
    (7, '색상 맞추기',     0,    '색상을 맞추는 협력 게임',           TRUE,  FALSE), -- id=20
    (5, '스피드 게임',     0,    '빠르게 반응하는 전략 게임',         TRUE,  FALSE), -- id=21
    (6, '퀴즈 게임',       0,    '다양한 퀴즈 보드게임',             TRUE,  FALSE); -- id=22

-- ============================================================
-- 8. orders (주문 헤더)
-- [수정] total_amount를 order_item 실합계와 일치시킴
--   order 1: 아메리카노×2(6000) + 팝콘오리지널×1(2000) + 토스트세트×1(5000) = 13000 ✔
--   order 2: 레몬에이드×1(4000) + 믹스너트×1(3500) = 7500 ✔
--   order 3: 카페라떼×4(14000) + 팝콘카라멜×2(5000) + 토스트세트×3(15000) = 34000 ✔
--   order 4: 녹차라떼×2(7000) + 컵라면×2(3000) = 10000 ✔
--   order 5: 아메리카노×1(3000) + 팝콘오리지널×1(2000) = 5000 ✔
--   order 6: 카푸치노×3(10500) + 나초×2(6000) + 인원추가×1(5000) = 21500 ✔
--   order 7: 바닐라라떼×4(16000) + 팝콘오리지널×5(10000) + 토스트세트×4(20000) = 46000 [수정]
--   order 8: 아메리카노×2(6000) + 자몽에이드×1(4000) = 10000 ✔
--   order 9: 카페라떼×3(10500) + 토스트세트×1(5000) = 15500 ✔
--   order10: 레몬에이드×1(4000) + 컵라면×2(3000) = 7000 ✔
--   order11: 바닐라라떼×4(16000) + 팝콘카라멜×2(5000) = 21000 ✔
-- ============================================================
INSERT INTO `orders` (`session_id`, `table_id`, `customer_phone`, `status`, `total_amount`, `ordered_at`)
VALUES
    (1, 1, '010-1234-5678', 'COMPLETED', 13000, '2026-03-25 13:10:00'), -- id=1
    (1, 1, '010-1234-5678', 'COMPLETED',  7500, '2026-03-25 14:00:00'), -- id=2
    (2, 2, '010-2345-6789', 'COMPLETED', 34000, '2026-03-25 15:45:00'), -- id=3
    (2, 2, NULL,            'COMPLETED', 10000, '2026-03-25 17:00:00'), -- id=4
    (3, 3, '010-3456-7890', 'COMPLETED',  5000, '2026-03-25 17:10:00'), -- id=5
    (4, 5, '010-4567-8901', 'COMPLETED', 21500, '2026-03-25 19:15:00'), -- id=6
    (5, 8, NULL,            'COMPLETED', 46000, '2026-03-25 12:00:00'), -- id=7  [수정: 42000→46000]
    (6, 1, '010-1234-5678', 'COMPLETED', 10000, '2026-03-26 13:40:00'), -- id=8
    (7, 2, '010-5678-9012', 'COMPLETED', 15500, '2026-03-26 14:20:00'), -- id=9
    (8, 5, NULL,            'COOKING',    7000, '2026-03-26 15:10:00'), -- id=10
    (9, 8, '010-6789-0123', 'ORDERED',   21000, '2026-03-26 12:30:00'); -- id=11

-- ============================================================
-- 9. order_item (주문 상세)
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
    (7,  13, '토스트 세트',     5000, 4), -- [수정] order7 합계: 16000+10000+20000=46000
    (8,  1,  '아메리카노',      3000, 2),
    (8,  6,  '자몽에이드',      4000, 1),
    (9,  2,  '카페라떼',        3500, 3),
    (9,  13, '토스트 세트',     5000, 1),
    (10, 5,  '레몬에이드',      4000, 1),
    (10, 14, '컵라면',          1500, 2),
    (11, 4,  '바닐라라떼',      4000, 4),
    (11, 10, '팝콘 (카라멜)',   2500, 2);

-- ============================================================
-- 10. game (보드게임 종목)
-- [수정] category_id: 전략=5, 파티=6, 협력=7 — menu 테이블과 동일하게 통일
-- ============================================================
INSERT INTO `game` (`category_id`, `name`, `min_players`, `max_players`, `play_time`, `is_active`)
VALUES
    (6, '맞춤법 게임', 2, 6,  20, TRUE), -- id=1  NORMAL 재고 3개
    (6, '숫자 맞추기', 2, 4,  15, TRUE), -- id=2  NORMAL 재고 2개
    (6, '동물 맞추기', 2, 6,  20, TRUE), -- id=3  NORMAL 재고 0개 (전부 대여중/파손)
    (7, '색상 맞추기', 2, 5,  25, TRUE), -- id=4  NORMAL 재고 1개
    (5, '스피드 게임', 2, 8,  10, TRUE), -- id=5  NORMAL 재고 0개 (전부 대여중)
    (6, '퀴즈 게임',   2, 10, 30, TRUE); -- id=6  NORMAL 재고 4개

-- ============================================================
-- 11. game_item (실물 박스 재고)
-- game_item id 자동 증가 순서:
--   SPL-001~005 → id 1~5
--   NUM-001~003 → id 6~8
--   ANM-001~003 → id 9~11
--   CLR-001~003 → id 12~14
--   SPD-001~002 → id 15~16
--   QUZ-001~005 → id 17~21
-- ============================================================
INSERT INTO `game_item` (`game_id`, `serial_number`, `status`)
VALUES
    -- 맞춤법 게임 (game_id=1)
    (1, 'SPL-001', 'NORMAL'),  -- id=1
    (1, 'SPL-002', 'NORMAL'),  -- id=2
    (1, 'SPL-003', 'NORMAL'),  -- id=3
    (1, 'SPL-004', 'RENTED'),  -- id=4  현재 대여 중
    (1, 'SPL-005', 'DAMAGED'), -- id=5  파손
    -- 숫자 맞추기 (game_id=2)
    (2, 'NUM-001', 'NORMAL'),  -- id=6
    (2, 'NUM-002', 'NORMAL'),  -- id=7
    (2, 'NUM-003', 'RENTED'),  -- id=8  현재 대여 중
    -- 동물 맞추기 (game_id=3)
    (3, 'ANM-001', 'RENTED'),  -- id=9
    (3, 'ANM-002', 'RENTED'),  -- id=10
    (3, 'ANM-003', 'DAMAGED'), -- id=11
    -- 색상 맞추기 (game_id=4)
    (4, 'CLR-001', 'NORMAL'),  -- id=12
    (4, 'CLR-002', 'RENTED'),  -- id=13 현재 대여 중
    (4, 'CLR-003', 'LOST'),    -- id=14 분실
    -- 스피드 게임 (game_id=5)
    (5, 'SPD-001', 'RENTED'),  -- id=15
    (5, 'SPD-002', 'RENTED'),  -- id=16
    -- 퀴즈 게임 (game_id=6)
    (6, 'QUZ-001', 'NORMAL'),  -- id=17
    (6, 'QUZ-002', 'NORMAL'),  -- id=18
    (6, 'QUZ-003', 'NORMAL'),  -- id=19
    (6, 'QUZ-004', 'NORMAL'),  -- id=20
    (6, 'QUZ-005', 'RENTED');  -- id=21 현재 대여 중

-- ============================================================
-- 12. game_history (게임 대여 이력)
-- [수정] 기존 id=20(QUZ-005) 참조 → 실제 game_item id=21로 수정
--        game_item id=6(NUM-001) 반납 기록 → id=6으로 명시
-- ============================================================
INSERT INTO `game_history` (`session_id`, `game_item_id`, `rented_at`, `returned_at`, `status`)
VALUES
    -- 과거 세션 반납 완료
    (1, 4,  '2026-03-25 13:05:00', '2026-03-25 14:50:00', 'RETURNED'), -- 맞춤법 SPL-004
    (2, 9,  '2026-03-25 15:35:00', '2026-03-25 18:30:00', 'RETURNED'), -- 동물  ANM-001
    (3, 6,  '2026-03-25 17:05:00', '2026-03-25 18:00:00', 'RETURNED'), -- 숫자  NUM-001 [수정: id=6]
    (4, 13, '2026-03-25 19:05:00', '2026-03-25 21:10:00', 'RETURNED'), -- 색상  CLR-002 [수정: id=13]
    (5, 15, '2026-03-25 11:05:00', '2026-03-25 22:50:00', 'RETURNED'), -- 스피드 SPD-001
    -- 현재 대여 중 (session 6~9)
    (6, 4,  '2026-03-26 13:35:00', NULL, 'RENTING'), -- table1: SPL-004
    (7, 9,  '2026-03-26 14:05:00', NULL, 'RENTING'), -- table2: ANM-001
    (7, 10, '2026-03-26 14:05:00', NULL, 'RENTING'), -- table2: ANM-002
    (8, 13, '2026-03-26 15:05:00', NULL, 'RENTING'), -- table5: CLR-002 [수정: id=13]
    (8, 15, '2026-03-26 15:05:00', NULL, 'RENTING'), -- table5: SPD-001
    (8, 16, '2026-03-26 15:05:00', NULL, 'RENTING'), -- table5: SPD-002
    (9, 8,  '2026-03-26 12:05:00', NULL, 'RENTING'), -- table8: NUM-003 [수정: id=8]
    (9, 21, '2026-03-26 12:05:00', NULL, 'RENTING'); -- table8: QUZ-005 [수정: id=21]

-- ============================================================
-- 13. cart (장바구니 헤더)
-- [추가] 기존 더미에서 완전 누락 — 키오스크 화면 렌더링에 필수
--        OCCUPIED 테이블(1,2,5,8)은 장바구니 필수 보유
--        EMPTY/CLEANING 테이블도 cart 레코드 생성 (입장 시 즉시 필요)
-- ============================================================
INSERT INTO `cart` (`table_id`, `updated_at`)
VALUES
    (1,  NOW()), -- table 1 (OCCUPIED)
    (2,  NOW()), -- table 2 (OCCUPIED)
    (3,  NOW()), -- table 3 (EMPTY)
    (4,  NOW()), -- table 4 (EMPTY)
    (5,  NOW()), -- table 5 (OCCUPIED)
    (6,  NOW()), -- table 6 (CLEANING)
    (7,  NOW()), -- table 7 (EMPTY)
    (8,  NOW()), -- table 8 (OCCUPIED)
    (9,  NOW()), -- table 9 (EMPTY)
    (10, NOW()), -- table 10 (EMPTY)
    (11, NOW()), -- table 11 (EMPTY)
    (12, NOW()); -- table 12 (EMPTY)

-- ============================================================
-- 14. cart_item (장바구니 담긴 항목)
-- [추가] 현재 OCCUPIED 세션의 테이블에 진행 중인 장바구니 시나리오
--        cart id는 table_id 순 삽입이므로 cart.id = table_id와 동일
-- ============================================================
INSERT INTO `cart_item` (`cart_id`, `menu_id`, `quantity`)
VALUES
    -- table 1 (cart_id=1): 아메리카노 1개, 팝콘오리지널 1개 담아둔 상태
    (1, 1,  1),
    (1, 9,  1),
    -- table 2 (cart_id=2): 카페라떼 2개, 토스트세트 1개
    (2, 2,  2),
    (2, 13, 1),
    -- table 5 (cart_id=5): 레몬에이드 1개
    (5, 5,  1),
    -- table 8 (cart_id=8): 바닐라라떼 2개, 믹스너트 1개
    (8, 4,  2),
    (8, 12, 1);

-- ============================================================
-- 15. point (포인트 계좌)
-- [추가] 기존 더미에서 완전 누락 — customer 전화번호와 연결
-- ============================================================
INSERT INTO `point` (`phone`, `balance`)
VALUES
    ('010-1234-5678', 1200),
    ('010-2345-6789',  800),
    ('010-3456-7890',  300),
    ('010-4567-8901', 2500),
    ('010-5678-9012',  550),
    ('010-6789-0123',   75),
    ('010-7890-1234',    0); -- 비활성 고객

-- ============================================================
-- 16. point_history (포인트 적립·사용 이력)
-- [추가] point.id는 삽입 순서대로 1~7
-- ============================================================
INSERT INTO `point_history` (`point_id`, `order_id`, `type`, `amount`, `balance_after`, `created_at`)
VALUES
    (1, 1,    'EARN', 130,  130,  '2026-03-25 13:10:00'), -- 주문1 1% 적립
    (1, 2,    'EARN',  75,  205,  '2026-03-25 14:00:00'),
    (1, 8,    'EARN', 100,  305,  '2026-03-26 13:40:00'),
    (1, NULL, 'USE',  200,  105,  '2026-03-26 13:45:00'), -- 포인트 사용
    (1, NULL, 'EARN',1095, 1200,  '2026-03-26 14:00:00'), -- 누적 보정
    (2, 3,    'EARN', 340,  340,  '2026-03-25 15:45:00'),
    (2, NULL, 'EARN', 460,  800,  '2026-03-25 18:00:00'),
    (3, 5,    'EARN',  50,   50,  '2026-03-25 17:10:00'),
    (3, NULL, 'EARN', 250,  300,  '2026-03-25 18:00:00'),  -- 누적 보정
    (4, 6,    'EARN', 215,  215,  '2026-03-25 19:15:00'),
    (4, NULL, 'EARN',2285, 2500,  '2026-03-25 21:00:00'),
    (5, 9,    'EARN', 155,  155,  '2026-03-26 14:20:00'),
    (5, NULL, 'EARN', 395,  550,  '2026-03-26 15:00:00'),
    (6, 11,   'EARN', 210,  210,  '2026-03-26 12:30:00'),
    (6, NULL, 'EARN', 865,   75,  '2026-03-26 13:00:00'); -- 사용 후 잔액

-- ============================================================
-- 17. macro_message
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
-- 18. table_message
-- ============================================================
INSERT INTO `table_message` (`table_id`, `macro_id`, `content`, `is_read`)
VALUES
    (1, NULL, '물 좀 주세요!',            FALSE),
    (1, NULL, '물티슈 좀 주세요!',         FALSE),
    (2, NULL, '2인용 보드게임 추천 부탁드려요.', FALSE),
    (5, NULL, '결제할게요~',              TRUE);

-- ============================================================
-- 19. 통계용 3월 데이터 생성 프로시저
-- [수정]
--   ① rand_table_id: FLOOR(1 + RAND()*12) → 최대 12 보장
--   ② payment INSERT: check_out_time이 NULL이면 paid_at도 NULL 방지 → IFNULL로 현재시간 대체
--   ③ order_item이 없을 경우(WHERE id=FLOOR... 이 0건 반환) total_amount=0 처리 강화
-- ============================================================
DELIMITER $$

DROP PROCEDURE IF EXISTS generate_march_stats_data$$

CREATE PROCEDURE generate_march_stats_data()
BEGIN
    DECLARE i              INT     DEFAULT 1;
    DECLARE rand_date      DATETIME;
    DECLARE rand_table_id  INT;
    DECLARE rand_pkg_id    INT;
    DECLARE rand_guest_cnt INT;
    DECLARE rand_duration  INT;
    DECLARE last_session_id BIGINT;
    DECLARE last_order_id   INT;
    DECLARE ord_cnt         INT;

    WHILE i <= 1000 DO
            -- 2026-03-01 11:00 ~ 2026-03-31 23:00 사이 랜덤 시각
            SET rand_date = FROM_UNIXTIME(
                    UNIX_TIMESTAMP('2026-03-01 11:00:00')
                        + FLOOR(RAND() * (UNIX_TIMESTAMP('2026-03-31 23:00:00')
                        - UNIX_TIMESTAMP('2026-03-01 11:00:00')))
                            );
            -- [수정] 1~12 균등 분포: CEIL(RAND()*12)
            SET rand_table_id  = CEIL(RAND() * 12);
            SET rand_pkg_id    = CEIL(RAND() * 4);
            SET rand_guest_cnt = FLOOR(1 + RAND() * 5);
            SET rand_duration  = FLOOR(60 + RAND() * 180);

            INSERT INTO `table_session`
            (table_id, package_id, initial_guest_cnt, check_in_time, check_out_time, is_active, total_amount)
            VALUES
                (rand_table_id, rand_pkg_id, rand_guest_cnt,
                 rand_date, DATE_ADD(rand_date, INTERVAL rand_duration MINUTE),
                 FALSE, 0);

            SET last_session_id = LAST_INSERT_ID();

            SET ord_cnt = FLOOR(1 + RAND() * 2); -- 주문 1~3건
            WHILE ord_cnt > 0 DO
                    INSERT INTO `orders` (session_id, table_id, status, total_amount, ordered_at)
                    VALUES (last_session_id, rand_table_id, 'COMPLETED', 0,
                            DATE_ADD(rand_date, INTERVAL (ord_cnt * 15) MINUTE));

                    SET last_order_id = LAST_INSERT_ID();

                    -- 메뉴 1~16 중 랜덤 1개 (게임 메뉴 제외)
                    INSERT INTO `order_item` (order_id, menu_id, menu_name, price, quantity)
                    SELECT last_order_id, id, name, price, FLOOR(1 + RAND() * 2)
                    FROM   `menu`
                    WHERE  id = FLOOR(1 + RAND() * 16)
                      AND  is_deleted = FALSE
                    LIMIT 1;

                    UPDATE `orders`
                    SET total_amount = (
                        SELECT IFNULL(SUM(price * quantity), 0)
                        FROM   `order_item`
                        WHERE  order_id = last_order_id
                    )
                    WHERE id = last_order_id;

                    SET ord_cnt = ord_cnt - 1;
                END WHILE;

            UPDATE `table_session`
            SET total_amount = (
                SELECT IFNULL(SUM(total_amount), 0)
                FROM   `orders`
                WHERE  session_id = last_session_id
            )
            WHERE id = last_session_id;

            -- [수정] check_out_time이 NULL이면 paid_at을 rand_date + duration으로 명시
            INSERT INTO `payment` (session_id, status, final_amount, paid_at)
            SELECT id, 'DONE', total_amount,
                   IFNULL(check_out_time, DATE_ADD(check_in_time, INTERVAL rand_duration MINUTE))
            FROM   `table_session`
            WHERE  id = last_session_id;

            SET i = i + 1;
        END WHILE;
END$$

DELIMITER ;

CALL generate_march_stats_data();

-- ============================================================
-- 20. item_sales_history (3월 통계)
-- ============================================================
INSERT IGNORE INTO `item_sales_history`
(stat_date, product_id, category, sales_qty, sales_amount)
SELECT
    DATE(p.paid_at)        AS stat_date,
    oi.menu_id             AS product_id,
    c.type                 AS category,
    SUM(oi.quantity)       AS sales_qty,
    SUM(oi.price * oi.quantity) AS sales_amount
FROM   `payment`    p
           JOIN   `orders`     o  ON p.session_id = o.session_id
           JOIN   `order_item` oi ON o.id         = oi.order_id
           JOIN   `menu`       m  ON oi.menu_id   = m.id
           JOIN   `category`   c  ON m.category_id = c.id
WHERE  p.status = 'DONE'
  AND  DATE(p.paid_at) BETWEEN '2026-03-01' AND '2026-03-31'
  AND  c.type NOT IN ('GAME', 'GUEST')
GROUP BY DATE(p.paid_at), oi.menu_id, c.type;

-- ============================================================
-- 21. daily_sales_summary (3월 통계)
-- [수정] visit_count: SUM(extra_qty) 집계 시 GROUP BY 단위 오류 보정
--        → extra 서브쿼리를 session 단위로 집계 후 JOIN
-- ============================================================
INSERT IGNORE INTO `daily_sales_summary`
(stat_date, total_revenue, order_count, visit_count, avg_usage_time)
SELECT
    DATE(p.paid_at)                                         AS stat_date,
    SUM(p.final_amount)                                     AS total_revenue,
    COUNT(DISTINCT o.id)                                    AS order_count,
    SUM(ts.initial_guest_cnt + IFNULL(ex.extra_qty, 0))    AS visit_count,
    IFNULL(AVG(TIMESTAMPDIFF(MINUTE, ts.check_in_time, ts.check_out_time)), 0) AS avg_usage_time
FROM `payment` p
         JOIN `table_session` ts ON p.session_id = ts.id
         LEFT JOIN `orders`   o  ON ts.id = o.session_id
         LEFT JOIN (
    -- [수정] session 단위 GUEST 수량 집계
    SELECT o2.session_id, SUM(oi2.quantity) AS extra_qty
    FROM   `order_item` oi2
               JOIN   `orders`     o2  ON oi2.order_id   = o2.id
               JOIN   `menu`       m2  ON oi2.menu_id    = m2.id
               JOIN   `category`   c2  ON m2.category_id = c2.id
    WHERE  c2.type = 'GUEST'
    GROUP BY o2.session_id
) ex ON p.session_id = ex.session_id
WHERE  p.status = 'DONE'
  AND  DATE(p.paid_at) BETWEEN '2026-03-01' AND '2026-03-31'
GROUP BY DATE(p.paid_at);

-- ============================================================
-- 22. 오늘 날짜 실시간 통계용 데이터 생성 프로시저
-- [수정] 동일하게 rand_table_id CEIL 방식 + paid_at NULL 방지 적용
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
            -- 오늘 10:00 ~ 22:00 사이 랜덤 시각
            SET rand_date     = DATE_ADD(CURDATE(), INTERVAL FLOOR(10*60 + RAND() * 12*60) MINUTE);
            -- [수정] CEIL 방식으로 1~12 균등
            SET rand_table_id  = CEIL(RAND() * 12);
            SET rand_pkg_id    = CEIL(RAND() * 4);
            SET rand_guest_cnt = FLOOR(1 + RAND() * 4);
            SET rand_duration  = FLOOR(60 + RAND() * 120);

            INSERT INTO `table_session`
            (table_id, package_id, initial_guest_cnt, check_in_time, check_out_time, is_active, total_amount)
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

                    SET last_order_id   = LAST_INSERT_ID();
                    SET is_extra_guest  = FLOOR(1 + RAND() * 10);

                    IF is_extra_guest <= 3 THEN
                        INSERT INTO `order_item` (order_id, menu_id, menu_name, price, quantity)
                        SELECT last_order_id, id, name, price, FLOOR(1 + RAND() * 2)
                        FROM   `menu`
                        WHERE  name = '인원 추가 (1명)'
                        LIMIT 1;
                    ELSE
                        INSERT INTO `order_item` (order_id, menu_id, menu_name, price, quantity)
                        SELECT last_order_id, id, name, price, FLOOR(1 + RAND() * 2)
                        FROM   `menu`
                        WHERE  name != '인원 추가 (1명)'
                          AND  is_deleted = FALSE
                        ORDER BY RAND()
                        LIMIT 1;
                    END IF;

                    UPDATE `orders`
                    SET total_amount = (
                        SELECT IFNULL(SUM(price * quantity), 0)
                        FROM   `order_item`
                        WHERE  order_id = last_order_id
                    )
                    WHERE id = last_order_id;

                    SET ord_cnt = ord_cnt - 1;
                END WHILE;

            UPDATE `table_session`
            SET total_amount = (
                SELECT IFNULL(SUM(total_amount), 0)
                FROM   `orders`
                WHERE  session_id = last_session_id
            )
            WHERE id = last_session_id;

            -- [수정] paid_at NULL 방지
            INSERT INTO `payment` (session_id, status, final_amount, paid_at)
            SELECT id, 'DONE', total_amount,
                   IFNULL(check_out_time, DATE_ADD(check_in_time, INTERVAL rand_duration MINUTE))
            FROM   `table_session`
            WHERE  id = last_session_id;

            SET i = i + 1;
        END WHILE;
END$$

DELIMITER ;

CALL generate_today_stats_data();

-- ============================================================
-- 23. 오늘 item_sales_history (실시간 통계 확인용)
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
  AND  DATE(p.paid_at) = CURDATE()
  AND  c.type NOT IN ('GAME', 'GUEST')
GROUP BY DATE(p.paid_at), oi.menu_id, c.type;

-- ============================================================
-- 24. 오늘 daily_sales_summary
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
         LEFT JOIN `orders`   o  ON ts.id = o.session_id
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
  AND  DATE(p.paid_at) = CURDATE()
GROUP BY DATE(p.paid_at);