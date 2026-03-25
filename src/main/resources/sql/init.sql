CREATE DATABASE IF NOT EXISTS `board_cafe_kiosk_2603`;
USE `board_cafe_kiosk_2603`;

--  보드게임 카페 키오스크 시스템 — 최종 확정 스키마 (수정)
--  Spring Boot + Thymeleaf + MariaDB + WebSocket/STOMP + Toss Payments
--
--  테이블 목록 (총 21개)
--  ┌─────┬─────────────────────┬──────────────────────────────────────┐
--  │  #  │ 테이블명              │ 역할                                  │
--  ├─────┼─────────────────────┼──────────────────────────────────────┤
--  │  1  │ manager             │ 관리자·직원 계정 (ADMIN / STAFF)         │
--  │  2  │ cafe_table          │ 물리적 테이블 (태블릿 인증 포함)            │
--  │  3  │ customer            │ 전화번호 등록 고객 (포인트 대상)            │
--  │  4  │ category            │ 메뉴·게임 공통 카테고리                   │
--  │  5  │ menu                │ 음식·음료 판매 메뉴                      │
--  │  6  │ game                │ 보드게임 종목                           │
--  │  7  │ game_item           │ 보드게임 실물 재고 (박스 단위)             │
--  │  8  │ cart                │ 테이블별 장바구니 헤더                    │
--  │  9  │ cart_item           │ 장바구니 담긴 메뉴 항목                   │
--  │ 10  │ macro_message       │ 1클릭 매크로 메시지 (하드코딩 관리)        │
--  │ 11  │ orders              │ 주문 헤더                             │
--  │ 12  │ order_item          │ 주문 상세 항목 (메뉴·가격 스냅샷)          │
--  │ 13  │ payment             │ 결제 헤더 (주문↔결제 1:1 연결)           │
--  │ 14  │ toss_payment        │ Toss Payments API 전용 데이터         │
--  │ 15  │ point               │ 전화번호 기반 포인트 계좌                 │
--  │ 16  │ point_history       │ 포인트 적립·사용·수동조정 이력             │
--  │ 17  │ table_message       │ 통합 메시지 로그 (하드코딩 관리)          │
--  │ 18  │ item_sales_history  │ 일일 판매 및 대여 통계 필드                │
--  │ 19  │ daily_sales_summary │ 매장 전체 일별 매출 및 운용 지표 요약        │
--  │ 20  │ package             │ 패키지 요금 정책 (신규)                  │
--  │ 21  │ rental_log          │ 게임 대여 이력 로그 (신규, 동시성 제어)      │
--  └─────┴─────────────────────┴──────────────────────────────────────┘

-- 1. manager
CREATE TABLE `manager`
(
    `id`         INT                    NOT NULL AUTO_INCREMENT COMMENT '관리자/직원 고유 번호 (PK)',
    `login_id`   VARCHAR(50)            NOT NULL COMMENT '로그인 아이디 (중복 불가)',
    `password`   VARCHAR(255)           NOT NULL COMMENT 'BCrypt 암호화 비밀번호',
    `name`       VARCHAR(30)            NOT NULL COMMENT '실명',
    `role`       ENUM ('ADMIN','STAFF') NOT NULL DEFAULT 'STAFF' COMMENT '권한: ADMIN(사장), STAFF(직원)',
    `is_active`  BOOLEAN                NOT NULL DEFAULT TRUE COMMENT '활성 상태 (FALSE=비활성)',
    `created_at` TIMESTAMP              NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '계정 생성 일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_manager_login_id` (`login_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='관리자·직원 계정. ADMIN(사장 전용 기능 포함), STAFF(운영 화면만). 물리 삭제 금지.';

-- 2. cafe_table
CREATE TABLE `cafe_table`
(
    `id`            INT                                  NOT NULL AUTO_INCREMENT COMMENT '테이블 고유 번호 (PK)',
    `table_number`  INT                                  NOT NULL COMMENT '손님 화면에 표시되는 실물 테이블 번호 (중복 불가)',
    `password`      VARCHAR(100)                         NOT NULL COMMENT '태블릿 로그인 비밀번호 (ADMIN 설정, 평문 저장)',
    `status`        ENUM ('EMPTY','OCCUPIED','CLEANING') NOT NULL DEFAULT 'EMPTY' COMMENT '테이블 상태: EMPTY(빈 자리) / OCCUPIED(이용 중) / CLEANING(정리 필요)',
    `access_token`  VARCHAR(255)                                  DEFAULT NULL COMMENT '태블릿 전용 고정 토큰 (로그인 유지용)',
    `check_in_time` TIMESTAMP                                     DEFAULT NULL COMMENT '손님 입장 시각 (OCCUPIED 전환 시 기록, 정산 완료 시 NULL 초기화)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_cafe_table_number` (`table_number`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='카페 물리 테이블. 태블릿 1대=레코드 1개. password로 키오스크 자체 인증.';

-- 3. customer
CREATE TABLE `customer`
(
    `id`         INT         NOT NULL AUTO_INCREMENT COMMENT '고객 고유 번호 (PK)',
    `phone`      VARCHAR(20) NOT NULL COMMENT '전화번호 (유일 식별자). 포인트 계좌,주문 조회 기준',
    `is_active`  BOOLEAN     NOT NULL DEFAULT TRUE COMMENT '활성 상태 (FALSE=비활성)',
    `created_at` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_customer_phone` (`phone`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='전화번호 등록 고객. 포인트 적립 대상.';

-- 4. category
CREATE TABLE `category`
(
    `id`   INT                          NOT NULL AUTO_INCREMENT COMMENT '카테고리 고유 번호 (PK)',
    `name` VARCHAR(50)                  NOT NULL COMMENT '카테고리명 (예: 라면류, 보드게임류)',
    `type` ENUM ('DRINK','FOOD','GAME') NOT NULL COMMENT '분류 타입: DRINK(음료) / FOOD(음식) / GAME(보드게임)',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='메뉴·보드게임 공통 대분류. type으로 키오스크 탭 구분.';

-- 10. macro_message
CREATE TABLE `macro_message`
(
    `id`           INT                                      NOT NULL AUTO_INCREMENT COMMENT '매크로 고유 번호 (PK)',
    `direction`    ENUM ('STAFF_TO_TABLE','TABLE_TO_STAFF') NOT NULL COMMENT '발송 방향',
    `message_text` VARCHAR(255)                             NOT NULL COMMENT '상용구 내용',
    `is_active`    BOOLEAN                                  NOT NULL DEFAULT TRUE COMMENT '사용 여부',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='1클릭 매크로 상용구.';

-- 15. point
CREATE TABLE `point`
(
    `id`         INT         NOT NULL AUTO_INCREMENT COMMENT '포인트 계좌 고유 번호 (PK)',
    `phone`      VARCHAR(20) NOT NULL COMMENT '고객 전화번호 (유일 식별자)',
    `balance`    INT         NOT NULL DEFAULT 0 COMMENT '현재 포인트 잔액',
    `updated_at` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '마지막 변경 일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_point_phone` (`phone`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='전화번호 기반 포인트 계좌. 키오스크에서 전화번호 입력 시 자동 생성.';

-- 20. package
CREATE TABLE `package`
(
    `id`                  INT                                 NOT NULL AUTO_INCREMENT COMMENT '패키지 고유 번호 (PK)',
    `name`                VARCHAR(50)                         NOT NULL COMMENT '패키지 이름',
    `type`                ENUM ('HOURLY','FIXED_TIME','FREE') NOT NULL COMMENT '패키지 유형',
    `duration_minutes`    INT                                          DEFAULT NULL COMMENT '이용 시간 (분)',
    `base_price`          INT                                 NOT NULL DEFAULT 0 COMMENT '기본 요금',
    `extra_price_per_min` DECIMAL(7, 2)                                DEFAULT NULL COMMENT '초과 시간 당 가격',
    `is_active`           BOOLEAN                             NOT NULL DEFAULT TRUE COMMENT '활성화 여부',
    `updated_by`          INT                                          DEFAULT NULL COMMENT '마지막 수정자 ID',
    `updated_at`          TIMESTAMP                           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '마지막 수정 일시',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_package_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `manager` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='패키지 요금 정책.';

-- 5. menu
CREATE TABLE `menu`
(
    `id`           INT          NOT NULL AUTO_INCREMENT COMMENT '메뉴 고유 번호 (PK)',
    `category_id`  INT                   DEFAULT NULL COMMENT '카테고리 ID (FK → category.id)',
    `name`         VARCHAR(100) NOT NULL COMMENT '메뉴명',
    `price`        INT          NOT NULL COMMENT '현재 판매 가격 (원)',
    `description`  TEXT                  DEFAULT NULL COMMENT '메뉴 상세 설명',
    `image_url`    VARCHAR(255)          DEFAULT NULL COMMENT '메뉴 이미지 경로 (로컬 추후 S3 URL)',
    `is_available` BOOLEAN      NOT NULL DEFAULT TRUE COMMENT '판매 가능 여부 (FALSE=품절)',
    `is_deleted`   BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '소프트 삭제 플래그 (TRUE=화면에서 숨김. 물리 삭제 금지)',
    `stock`        INT                   DEFAULT NULL COMMENT '재고 수량. 결제 시 SELECT FOR UPDATE로 동시성 제어',
    `created_at`   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '메뉴 등록 일시',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_menu_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='음식·음료 판매 메뉴. 가격은 order_item 스냅샷으로 이력 보존. 물리 삭제 금지.';

-- 6. game
CREATE TABLE `game`
(
    `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '보드게임 종목 고유 번호 (PK)',
    `category_id` INT                   DEFAULT NULL COMMENT '카테고리 ID (FK → category.id)',
    `name`        VARCHAR(100) NOT NULL COMMENT '보드게임 이름',
    `description` TEXT                  DEFAULT NULL COMMENT '게임 소개 (플레이 방법, 특징 등)',
    `min_players` INT                   DEFAULT NULL COMMENT '최소 플레이 인원',
    `max_players` INT                   DEFAULT NULL COMMENT '최대 플레이 인원',
    `play_time`   INT                   DEFAULT NULL COMMENT '평균 플레이 시간 (분)',
    `is_active`   BOOLEAN      NOT NULL DEFAULT TRUE COMMENT '키오스크 표시 여부 (FALSE=숨김 또는 전체 재고 소진)',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_game_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='보드게임 종목 마스터. 인원수·플레이시간 기반 키오스크 필터링 지원.';

-- 8. cart
CREATE TABLE `cart`
(
    `id`         INT       NOT NULL AUTO_INCREMENT COMMENT '장바구니 고유 번호 (PK)',
    `table_id`   INT       NOT NULL COMMENT '소속 테이블 ID (FK → cafe_table.id). 테이블당 1개',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '장바구니 생성 일시',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '마지막 변경 일시  (비활동 타임아웃 기준)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_cart_table` (`table_id`),
    CONSTRAINT `fk_cart_table` FOREIGN KEY (`table_id`) REFERENCES `cafe_table` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='테이블별 장바구니 헤더. updated_at으로 비활동 타임아웃 판단.';

-- 7. game_item
CREATE TABLE `game_item`
(
    `id`            INT                                       NOT NULL AUTO_INCREMENT COMMENT '게임 실물 고유 번호 (PK)',
    `game_id`       INT                                       NOT NULL COMMENT '소속 보드게임 종목 ID (FK → game.id)',
    `serial_number` VARCHAR(50)                               NOT NULL COMMENT '박스별 고유 일련번호 (분실·파손 추적. 손님 비노출)',
    `status`        ENUM ('NORMAL','RENTED','DAMAGED','LOST') NOT NULL DEFAULT 'NORMAL' COMMENT '상태: NORMAL(대여 가능) / RENTED(대여 중) / DAMAGED(파손) / LOST(분실)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_gameitem_serial` (`serial_number`),
    CONSTRAINT `fk_gameitem_game` FOREIGN KEY (`game_id`) REFERENCES `game` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='보드게임 실물 재고.';

-- 9. cart_item
CREATE TABLE `cart_item`
(
    `id`       INT NOT NULL AUTO_INCREMENT COMMENT '장바구니 항목 고유 번호 (PK)',
    `cart_id`  INT NOT NULL COMMENT '소속 장바구니 ID (FK → cart.id)',
    `menu_id`  INT NOT NULL COMMENT '담긴 메뉴 ID (FK → menu.id)',
    `quantity` INT NOT NULL DEFAULT 1 COMMENT '수량 (최소 1)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_cartitem_cart_menu` (`cart_id`, `menu_id`),
    CONSTRAINT `fk_cartitem_cart` FOREIGN KEY (`cart_id`) REFERENCES `cart` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_cartitem_menu` FOREIGN KEY (`menu_id`) REFERENCES `menu` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='장바구니 담긴 메뉴 항목.';

-- 11. orders (원본의 GENERATED STORED 포함)
CREATE TABLE `orders`
(
    `id`             INT                                                                                      NOT NULL AUTO_INCREMENT COMMENT '주문 고유 번호 (PK)',
    `table_id`       INT                                                                                      NOT NULL COMMENT '주문한 테이블 ID (FK → cafe_table.id)',
    `customer_phone` VARCHAR(20)                                                                                       DEFAULT NULL COMMENT '손님 전화번호 NULL=미입력(포인트 적립 없음)',
    `package_id`     INT                                                                                               DEFAULT NULL COMMENT '선택된 패키지 ID (FK → package.id)',
    `status`         ENUM ('PENDING', 'PAID', 'CONFIRMED', 'COOKING', 'DELIVERING', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'PENDING' COMMENT '주문 상태',
    `total_amount`   INT                                                                                      NOT NULL DEFAULT 0 COMMENT '할인 적용 전 총액(원)',
    `package_amount` INT                                                                                      NOT NULL DEFAULT 0 COMMENT '선택한 패키지 자체의 고유 금액',
    `point_used`     INT                                                                                      NOT NULL DEFAULT 0 COMMENT '포인트 사용액',
    `final_amount`   INT GENERATED ALWAYS AS (`total_amount` + `package_amount` - `point_used`) STORED COMMENT '최종 결제 금액',
    `ordered_at`     TIMESTAMP                                                                                NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '주문 생성 일시',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_orders_table` FOREIGN KEY (`table_id`) REFERENCES `cafe_table` (`id`),
    CONSTRAINT `fk_orders_package` FOREIGN KEY (`package_id`) REFERENCES cafe_package (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='주문 헤더.';

-- 12. order_item (원본의 GENERATED STORED 포함)
CREATE TABLE `order_item`
(
    `id`        INT          NOT NULL AUTO_INCREMENT COMMENT '주문 항목 고유 번호 (PK)',
    `order_id`  INT          NOT NULL COMMENT '소속 주문 ID (FK → orders.id)',
    `menu_id`   INT DEFAULT NULL COMMENT '메뉴 ID (FK → menu.id). 메뉴 소프트 삭제 시 NULL 유지',
    `menu_name` VARCHAR(100) NOT NULL COMMENT '주문 당시 메뉴명 스냅샷',
    `price`     INT          NOT NULL COMMENT '주문 당시 단가 스냅샷 (원)',
    `quantity`  INT          NOT NULL COMMENT '주문 수량',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_orderitem_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='주문 상세 항목.';

-- 13. payment
CREATE TABLE `payment`
(
    `id`           INT                   NOT NULL AUTO_INCREMENT COMMENT '결제 고유 번호 (PK)',
    `order_id`     INT                   NOT NULL COMMENT '연결된 주문 ID (FK → orders.id)',
    `status`       ENUM ('READY','DONE') NOT NULL DEFAULT 'READY' COMMENT '결제 상태',
    `final_amount` INT                   NOT NULL COMMENT '최종 결제 금액(원)',
    `paid_at`      TIMESTAMP                      DEFAULT NULL COMMENT '결제 완료 일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_payment_order` (`order_id`),
    CONSTRAINT `fk_payment_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='결제 헤더.';

-- 14. toss_payment
CREATE TABLE `toss_payment`
(
    `id`            INT          NOT NULL AUTO_INCREMENT COMMENT 'Toss 결제 상세 고유 번호 (PK)',
    `payment_id`    INT          NOT NULL COMMENT '연결된 결제 ID (FK → payment.id)',
    `payment_key`   VARCHAR(200) NOT NULL COMMENT 'Toss paymentKey',
    `order_id_toss` VARCHAR(64)  NOT NULL COMMENT 'Toss 전달용 orderId',
    `method`        ENUM ('간편결제','계좌이체') DEFAULT NULL COMMENT 'Toss 결제 수단',
    `raw_response`  JSON                 DEFAULT NULL COMMENT 'Toss 승인 응답 원본 JSON',
    `approved_at`   TIMESTAMP            DEFAULT NULL COMMENT 'Toss 승인 일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_toss_payment_id` (`payment_id`),
    UNIQUE KEY `uq_toss_payment_key` (`payment_key`),
    CONSTRAINT `fk_toss_payment` FOREIGN KEY (`payment_id`) REFERENCES `payment` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='Toss Payments API 전용.';

-- 16. point_history
CREATE TABLE `point_history`
(
    `id`            BIGINT              NOT NULL AUTO_INCREMENT COMMENT '포인트 이력 고유 번호 (PK)',
    `point_id`      INT                 NOT NULL COMMENT '소속 포인트 계좌 ID (FK → point.id)',
    `order_id`      INT                          DEFAULT NULL COMMENT '관련 주문 ID (FK → orders.id)',
    `type`          ENUM ('EARN','USE') NOT NULL COMMENT '이력 유형',
    `amount`        INT                 NOT NULL COMMENT '변동 포인트',
    `balance_after` INT                 NOT NULL COMMENT '처리 직후 잔액 스냅샷',
    `created_at`    TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '이력 생성 일시',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_pointhistory_point` FOREIGN KEY (`point_id`) REFERENCES `point` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_pointhistory_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='포인트 이력.';

-- 17. table_message (누락 보충)
CREATE TABLE `table_message`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '메시지 로그 고유 번호 (PK)',
    `table_id`   INT          NOT NULL COMMENT '발신/수신 테이블 ID (FK → cafe_table.id)',
    `macro_id`   INT                   DEFAULT NULL COMMENT '참조한 매크로 ID (FK → macro_message.id)',
    `content`    VARCHAR(255) NOT NULL COMMENT '실제 전송된 메시지 내용',
    `is_read`    BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '직원 확인 여부',
    `created_at` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '발송 일시',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_tablemsg_table` FOREIGN KEY (`table_id`) REFERENCES `cafe_table` (`id`),
    CONSTRAINT `fk_tablemsg_macro` FOREIGN KEY (`macro_id`) REFERENCES `macro_message` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='테이블-직원 간 통합 메시지 로그.';

-- 18. item_sales_history (통계)
CREATE TABLE `item_sales_history`
(
    `id`           INT                            NOT NULL AUTO_INCREMENT COMMENT '통계 레코드 고유 번호(PK)',
    `stat_date`    DATE                           NOT NULL COMMENT '통계 집계 일자',
    `product_id`   INT                            NOT NULL COMMENT '관련 상품/게임 ID',
    `category`     ENUM ('DRINK', 'FOOD', 'GAME') NOT NULL COMMENT '상품 카테고리 구분',
    `sales_qty`    INT                            NOT NULL DEFAULT 0 COMMENT '당일 판매 수량',
    `sales_amount` BIGINT                         NOT NULL DEFAULT 0 COMMENT '당일 총 매출 합계',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_stat_date_product` (`stat_date`, `product_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='상품별 일일 판매 통계 요약.';

-- 19. daily_sales_summary (통계)
CREATE TABLE `daily_sales_summary`
(
    `id`             BIGINT NOT NULL AUTO_INCREMENT COMMENT '요약 레코드 고유 번호(PK)',
    `stat_date`      DATE   NOT NULL COMMENT '통계 집계 일자',
    `total_revenue`  BIGINT NOT NULL DEFAULT 0 COMMENT '당일 전체 총 매출액',
    `order_count`    INT    NOT NULL DEFAULT 0 COMMENT '당일 발생한 총 주문 건수',
    `visit_count`    INT    NOT NULL DEFAULT 0 COMMENT '당일 총 방문 고객 수',
    `avg_usage_time` INT             DEFAULT 0 COMMENT '당일 테이블당 평균 이용 시간 (분)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uq_daily_stat_date` (`stat_date`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='매장 전체 일별 매출 및 운용 지표 요약.';

-- 21. rental_log
CREATE TABLE `rental_log`
(
    `id`           BIGINT                                       NOT NULL AUTO_INCREMENT COMMENT '대여 기록 고유 번호 (PK)',
    `table_id`     INT                                          NOT NULL COMMENT '대여한 테이블 ID',
    `game_item_id` INT                                          NOT NULL COMMENT '대여한 게임 실물 ID',
    `order_id`     INT                                                   DEFAULT NULL COMMENT '관련 주문 ID',
    `rented_at`    TIMESTAMP                                    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '대여 시작 일시',
    `returned_at`  TIMESTAMP                                             DEFAULT NULL COMMENT '반납 완료 일시',
    `status`       ENUM ('RENTING','RETURNED','DAMAGED','LOST') NOT NULL DEFAULT 'RENTING' COMMENT '대여 상태',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_rental_table` FOREIGN KEY (`table_id`) REFERENCES `cafe_table` (`id`),
    CONSTRAINT `fk_rental_item` FOREIGN KEY (`game_item_id`) REFERENCES `game_item` (`id`),
    CONSTRAINT `fk_rental_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='게임 대여 이력 로그.';

-- ==========================================================
-- 초기 하드코딩 데이터 삽입 (Master Data)
-- ==========================================================

-- 1. 관리자 계정 (비밀번호: 1234 의 BCrypt 해시값)
INSERT INTO `manager` (`login_id`, `password`, `name`, `role`)
VALUES ('admin', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7u41W3u', '관리자', 'ADMIN');

-- 2. 테이블 정보 (1~10번 테이블)
INSERT INTO `cafe_table` (`table_number`, `password`)
VALUES (1, '1111'),
       (2, '2222'),
       (3, '3333'),
       (4, '4444'),
       (5, '5555'),
       (6, '6666'),
       (7, '7777'),
       (8, '8888'),
       (9, '9999'),
       (10, '1010'),
       (11, '1011'),
       (12, '1012');

-- 3. 카테고리
INSERT INTO `category` (`name`, `type`)
VALUES ('커피/음료', 'DRINK'),
       ('식사/간식', 'FOOD'),
       ('전략 게임', 'GAME'),
       ('파티 게임', 'GAME');

-- 4. 패키지 요금
INSERT INTO cafe_package (`name`, `type`, `duration_minutes`, `base_price`, `extra_price_per_min`)
VALUES ('평일 1시간 권', 'HOURLY', 60, 3000, 50.00),
       ('평일 무제한 권', 'FREE', NULL, 15000, 0.00),
       ('주말 3시간 패키지', 'FIXED_TIME', 180, 8000, 70.00);

-- 5. 매크로 메시지
INSERT INTO `macro_message` (`direction`, `message_text`)
VALUES ('TABLE_TO_STAFF', '물 좀 가져다주세요.'),
       ('TABLE_TO_STAFF', '티슈/물티슈가 필요해요.'),
       ('TABLE_TO_STAFF', '보드게임 정리가 완료되었습니다.'),
       ('TABLE_TO_STAFF', '직원 호출 (기타 문의)'),
       ('STAFF_TO_TABLE', '주문하신 메뉴가 준비되었습니다.'),
       ('STAFF_TO_TABLE', '잠시만 기다려주시면 바로 방문하겠습니다.');

-- 6. 메뉴 (외부 경로 매핑 이미지 예시 적용)
INSERT INTO `menu` (`category_id`, `name`, `price`, `description`, `image_url`)
VALUES (1, '아이스 아메리카노', 4000, '직접 로스팅한 원두로 만든 시원한 커피', '/upload/americano.jpg'),
       (1, '초코 라떼', 5000, '달콤하고 진한 초콜릿 우유', '/upload/choco_latte.jpg'),
       (2, '매콤 분식 라면', 4500, '보드게임하며 즐기는 매콤한 라면', '/upload/ramyeon.jpg'),
       (2, '소금 버터 팝콘', 3500, '갓 튀겨낸 고소한 팝콘', '/upload/popcorn.jpg');

-- 7. 보드게임 종목
INSERT INTO `game` (`category_id`, `name`, `min_players`, `max_players`, `play_time`)
VALUES (3, '스플렌더', 2, 4, 30),
       (4, '할리갈리', 2, 6, 15),
       (3, '카탄의 개척자', 3, 4, 90);

-- 전용 사용자 생성 및 권한 부여
CREATE USER IF NOT EXISTS `admin`@`localhost` IDENTIFIED BY '0331';
GRANT ALL PRIVILEGES ON `board_cafe_kiosk_2603`.* TO `admin`@`localhost`;
# FLUSH PRIVILEGES;