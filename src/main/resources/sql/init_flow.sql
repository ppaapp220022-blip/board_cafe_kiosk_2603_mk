# 보드카페 키오스크 - 시스템 흐름 예시
# 1. 주요 관계 흐름 - "손님이 입장해서 퇴장하기 까지
# 기 — 입장 (세션 시작)
# 손님 3명이 테이블 5번에 앉는다. 직원이 키오스크로 "2시간 정액제" 패키지를 선택하고 입장 처리를 한다.

-- cafe_package에서 패키지 조회
SELECT * FROM cafe_package WHERE name = '2시간 정액제';
-- id=2, type='HOURLY', base_price=8000

-- table_session 생성 (핵심 엔티티 탄생)
INSERT INTO table_session (table_id, package_id, initial_guest_cnt)
VALUES (5, 2, 3);
-- id=1001 생성됨

-- cafe_table에 현재 세션 등록
UPDATE cafe_table
SET status = 'OCCUPIED', current_session_id = 1001
WHERE id = 5;

# 승 — 이용 (주문 + 게임 대여)
# 손님이 아메리카노 2잔, 치즈볼 1개를 주문하고, 카탄을 대여한다.
-- orders 생성 (session_id + table_id 모두 기록)
INSERT INTO orders (session_id, table_id, customer_phone, status, total_amount)
VALUES (1001, 5, '010-1234-5678', 'ORDERED', 18000);
-- id=77 생성됨

-- order_item 상세 기록 (주문 당시 가격을 스냅샷으로 저장)
INSERT INTO order_item (order_id, menu_name, price, quantity)
VALUES (77, '아메리카노', 4500, 2),
       (77, '치즈볼',    9000, 1);

-- 게임 실물 재고 확인
SELECT gi.id, gi.serial_number
FROM game_item gi JOIN game g ON gi.game_id = g.id
WHERE g.name = '카탄' AND gi.status = 'NORMAL'
LIMIT 1;
-- game_item.id = 33

-- game_history 생성 (session_id 기반으로 히스토리 추적)
INSERT INTO game_history (session_id, game_item_id, status)
VALUES (1001, 33, 'RENTED');

-- game_item 상태 변경
UPDATE game_item SET status = 'RENTED' WHERE id = 33;

# 전 — 포인트 적립
# 주문 시 전화번호를 입력했으므로, 주문 금액의 5%를 포인트로 적립한다.
-- point 계좌가 없으면 생성
INSERT IGNORE INTO point (phone, balance) VALUES ('010-1234-5678', 0);

-- 잔액 업데이트
UPDATE point SET balance = balance + 900
WHERE phone = '010-1234-5678';

-- point_history 이력 기록
INSERT INTO point_history (point_id, order_id, type, amount, balance_after)
VALUES (
           (SELECT id FROM point WHERE phone = '010-1234-5678'),
           77,       -- orders.id
           'EARN',
           900,
           (SELECT balance FROM point WHERE phone = '010-1234-5678')
       );

# 결 — 퇴장 & 결제
# 2시간 후 손님이 퇴장한다. 시간 요금 + 주문 금액을 합산해 토스페이먼츠로 결제한다.
-- 세션 종료 처리
UPDATE table_session
SET check_out_time = NOW(),
    is_active = FALSE,
    total_amount = 42000   -- (8000 × 3명) + 18000 주문
WHERE id = 1001;

-- 게임 반납
UPDATE game_item SET status = 'NORMAL' WHERE id = 33;
UPDATE game_history SET returned_at = NOW(), status = 'NORMAL'
WHERE session_id = 1001 AND game_item_id = 33;

-- payment 생성 (session당 1회, UNIQUE 제약)
INSERT INTO payment (session_id, table_number, status, final_amount,
                     payment_key, order_id_toss, method, approved_at)
VALUES (1001, 5, 'DONE', 42000,
        'tosskey_abc123', 'ORDER_1001_20250416', '카드',
        NOW());

-- 테이블 초기화
UPDATE cafe_table
SET status = 'CLEANING', current_session_id = NULL
WHERE id = 5;

# Spring Security 관련 테이블 — persistent_logins
# 기 — Remember-Me가 뭔가
# 직원이 관리자 페이지에 로그인하면서 "로그인 상태 유지"를 체크한다. 브라우저를 닫았다가 다음 날 다시 열어도 로그인이 유지되어야 한다.
# Spring Security의 PersistentTokenRepository는 이 토큰을 DB에 저장하는 방식(JdbcTokenRepositoryImpl)을 제공하며, 그 저장소가 바로 persistent_logins 테이블이다.
# 승 — 로그인 시 어떤 일이 일어나는가

# 직원 로그인 + "로그인 유지" 체크
#       ↓
# Spring Security가 자동으로 INSERT 수행

-- Spring Security 내부 동작 (개발자가 직접 작성하지 않음)
INSERT INTO persistent_logins (series, username, token, last_used)
VALUES (
           'Xk9mP2...', -- 랜덤 series (쿠키 식별자)
           'staff01',   -- manager.login_id
           'aB3nQ7...', -- 랜덤 token (실제 인증값)
           NOW()
       );
# 브라우저 쿠키에는 series:token 조합이 저장된다.

-- 자동 로그인 성공 시 토큰 갱신 (Spring 내부 동작)
UPDATE persistent_logins
SET token = 'newToken...', last_used = NOW()
WHERE series = 'Xk9mP2...';

# series는 변하지 않고 token만 매 접속마다 교체된다. 토큰이 탈취되어 다른 기기에서 먼저 사용하면, 정상 사용자가 접속할 때 token 불일치가 발생해 탈취 사실을 감지할 수 있다.

# 결 — 관리자 전체 로그아웃 (보안 사고 대응)
-- 비밀번호 유출 등 보안 사고 발생 시
-- 전 직원 강제 로그아웃 → 테이블 전체 삭제
DELETE FROM persistent_logins;
# SQL 주석에도 적혀있듯(관리자 전체 리셋 시 전체 삭제됨), 이 테이블은 보안 사고 대응의 킬 스위치 역할도 한다. 단 한 줄의 DELETE로 모든 자동 로그인 세션을 무효화할 수 있다.