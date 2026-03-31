# CREATE DATABASE IF NOT EXISTS `board_cafe_kiosk_2603`;
# USE `board_cafe_kiosk_2603`;

--  보드게임 카페 키오스크 시스템 — 최종 고도화 스키마
--  수정 사항: table_session 추가(22번), GUEST 카테고리 확장, 세션 기반 관계 재설정
--
--  테이블 목록 (총 22개)
--  ┌─────┬─────────────────────┬────────────────────────────────────────────────┐
--  │  #  │ 테이블명              │ 역할 (비고)                                     │
--  ├─────┼─────────────────────┼────────────────────────────────────────────────┤
--  │  1  │ manager             │ 관리자·직원 계정 (ADMIN / STAFF)                  │
--  │  2  │ cafe_table          │ 물리적 테이블 (UUID access_token 추가)            │
--  │  3  │ customer            │ 전화번호 등록 고객 (포인트 대상)                     │
--  │  4  │ category            │ 메뉴·게임·인원(GUEST) 공통 카테고리                │
--  │  5  │ menu                │ 음식·음료 및 추가인원 상품                        │
--  │  6  │ game                │ 보드게임 종목                                   │
--  │  7  │ game_item           │ 보드게임 실물 재고 (박스 단위)                      │
--  │  8  │ cart                │ 테이블별 장바구니 헤더                            │
--  │  9  │ cart_item           │ 장바구니 담긴 메뉴 항목                            │
--  │ 10  │ macro_message       │ 1클릭 매크로 메시지                               │
--  │ 11  │ table_session       │ [NEW] 테이블 이용 히스토리 및 세션 관리 (핵심)        │
--  │ 12  │ orders              │ 주문 헤더 (session_id 외래키 추가)                │
--  │ 13  │ order_item          │ 주문 상세 항목 (메뉴·가격 스냅샷)                   │
--  │ 14  │ payment             │ 결제 헤더 (세션 단위 정산으로 변경)                  │
--  │ 15  │ toss_payment        │ Toss Payments API 전용 데이터                  │
--  │ 16  │ point               │ 전화번호 기반 포인트 계좌                          │
--  │ 17  │ point_history       │ 포인트 적립·사용 이력                             │
--  │ 18  │ table_message       │ 통합 메시지 로그                                 │
--  │ 19  │ item_sales_history  │ 일일 상품별 판매 통계                             │
--  │ 20  │ daily_sales_summary │ 매장 전체 일별 매출 요약                          │
--  │ 21  │ cafe_package        │ 패키지 요금 정책                                 │
--  │ 22  │ game_history        │ 게임 대여 이력 (session_id 기반으로 변경)           │
--  └─────┴─────────────────────┴────────────────────────────────────────────────┘

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
  DEFAULT CHARSET = utf8mb4 COMMENT ='관리자·직원 계정.';

-- 2. cafe_table
-- 수정사항: 로그인 유지를 위한 access_token(UUID) 및 현재 세션 추적용 컬럼 추가
CREATE TABLE `cafe_table`
(
    `id`                 INT                                  NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `table_number`       INT                                  NOT NULL UNIQUE COMMENT '표시 테이블 번호',
    `password`           VARCHAR(100)                         NOT NULL COMMENT '태블릿 최초 인증 비밀번호',
    `status`             ENUM ('EMPTY','OCCUPIED','CLEANING') NOT NULL DEFAULT 'EMPTY',
    `access_token`       VARCHAR(255)                                  DEFAULT NULL UNIQUE COMMENT 'UUID 기반 자동 로그인 토큰',
    `current_session_id` BIGINT                                        DEFAULT NULL COMMENT '현재 진행 중인 table_session ID'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='카페 물리 테이블 정보.';

-- 3. customer
CREATE TABLE `customer`
(
    `id`         INT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `phone`      VARCHAR(20) NOT NULL UNIQUE COMMENT '전화번호 (유일 식별자)',
    `is_active`  BOOLEAN     NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='전화번호 등록 고객 정보.';

-- 4. category
-- 수정사항: 인원 추가 관리를 위한 GUEST 타입 확장
CREATE TABLE `category`
(
    `id`   INT                                  NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50)                          NOT NULL COMMENT '카테고리명',
    `type` ENUM ('DRINK','FOOD','GAME','GUEST') NOT NULL COMMENT 'GUEST: 인원추가 전용'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='메뉴·게임·인원 공통 대분류.';

-- 21. cafe_package (순서 조정: session 참조용)
CREATE TABLE `cafe_package`
(
    `id`                  INT                                 NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `name`                VARCHAR(50)                         NOT NULL,
    `type`                ENUM ('HOURLY','FIXED_TIME','FREE') NOT NULL,
    `duration_minutes`    INT                                          DEFAULT NULL,
    `base_price`          INT                                 NOT NULL DEFAULT 0 COMMENT '1인당 기본 요금',
    `extra_price_per_min` DECIMAL(7, 2)                                DEFAULT NULL,
    `is_active`           BOOLEAN                             NOT NULL DEFAULT TRUE,
    `updated_at`          TIMESTAMP                           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='패키지 요금 정책.';

-- 11. table_session [NEW]
-- 추가이유: 요구하신 '테이블 이용 이력 히스토리' 및 세션 로그인 유지를 위한 핵심 테이블
CREATE TABLE `table_session`
(
    `id`                BIGINT    NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `table_id`          INT       NOT NULL COMMENT '이용 테이블 (FK)',
    `package_id`        INT       NOT NULL COMMENT '선택 패키지 (FK)',
    `initial_guest_cnt` INT       NOT NULL DEFAULT 1 COMMENT '최초 입장 인원',
    `check_in_time`     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '입장 시간',
    `check_out_time`    TIMESTAMP NULL COMMENT '퇴장 시간',
    `is_active`         BOOLEAN   NOT NULL DEFAULT TRUE COMMENT '현재 세션 활성화 여부',
    `total_amount`      INT       NOT NULL DEFAULT 0 COMMENT '최종 정산 금액 (퇴실 시 합산)',
    CONSTRAINT `fk_session_table` FOREIGN KEY (`table_id`) REFERENCES `cafe_table` (`id`),
    CONSTRAINT `fk_session_package` FOREIGN KEY (`package_id`) REFERENCES `cafe_package` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='테이블 이용 세션 및 방문 히스토리.';

-- 5. menu
-- 수정사항: 추가 인원(GUEST) 상품이 이 테이블에 등록됨
CREATE TABLE `menu`
(
    `id`           INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `category_id`  INT                   DEFAULT NULL,
    `name`         VARCHAR(100) NOT NULL,
    `price`        INT          NOT NULL COMMENT '판매 가격',
    `description`  TEXT                  DEFAULT NULL,
    `image_url`    VARCHAR(255)          DEFAULT NULL,
    `is_available` BOOLEAN      NOT NULL DEFAULT TRUE,
    `is_deleted`   BOOLEAN      NOT NULL DEFAULT FALSE,
    `created_at`   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_menu_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='판매 메뉴 및 인원추가 상품.';

-- 12. orders
-- 수정사항: session_id를 추가하여 '어느 방문 건'의 주문인지 명확히 식별
CREATE TABLE `orders`
(
    `id`             INT                                                                                      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `session_id`     BIGINT                                                                                   NOT NULL COMMENT '방문 세션 ID (FK)',
    `table_id`       INT                                                                                      NOT NULL COMMENT '주문 테이블 (FK)',
    `customer_phone` VARCHAR(20)                                                                                       DEFAULT NULL,
    `status`         ENUM ('PENDING', 'PAID', 'CONFIRMED', 'COOKING', 'DELIVERING', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'PAID',
    `total_amount`   INT                                                                                      NOT NULL DEFAULT 0 COMMENT '주문 총액',
    `ordered_at`     TIMESTAMP                                                                                NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_orders_session` FOREIGN KEY (`session_id`) REFERENCES `table_session` (`id`),
    CONSTRAINT `fk_orders_table` FOREIGN KEY (`table_id`) REFERENCES `cafe_table` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='주문 헤더.';

-- 13. order_item
CREATE TABLE `order_item`
(
    `id`        INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `order_id`  INT          NOT NULL,
    `menu_id`   INT DEFAULT NULL,
    `menu_name` VARCHAR(100) NOT NULL,
    `price`     INT          NOT NULL,
    `quantity`  INT          NOT NULL,
    CONSTRAINT `fk_orderitem_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='주문 상세 항목.';

-- 6. game / 7. game_item
CREATE TABLE `game`
(
    `id`          INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `category_id` INT                   DEFAULT NULL,
    `name`        VARCHAR(100) NOT NULL,
    `min_players` INT,
    `max_players` INT,
    `play_time`   INT,
    `is_active`   BOOLEAN      NOT NULL DEFAULT TRUE,
    `image_url`   VARCHAR(255)          DEFAULT NULL,
    CONSTRAINT `fk_game_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `game_item`
(
    `id`            INT                                       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `game_id`       INT                                       NOT NULL,
    `serial_number` VARCHAR(50)                               NOT NULL UNIQUE,
    `status`        ENUM ('NORMAL','RENTED','DAMAGED','LOST') NOT NULL DEFAULT 'NORMAL',
    CONSTRAINT `fk_gameitem_game` FOREIGN KEY (`game_id`) REFERENCES `game` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 8. cart / 9. cart_item
CREATE TABLE `cart`
(
    `id`         INT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `table_id`   INT       NOT NULL UNIQUE,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_cart_table` FOREIGN KEY (`table_id`) REFERENCES `cafe_table` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `cart_item`
(
    `id`       INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `cart_id`  INT NOT NULL,
    `menu_id`  INT NOT NULL,
    `quantity` INT NOT NULL DEFAULT 1,
    UNIQUE KEY `uq_cartitem_cart_menu` (`cart_id`, `menu_id`),
    CONSTRAINT `fk_cartitem_cart` FOREIGN KEY (`cart_id`) REFERENCES `cart` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_cartitem_menu` FOREIGN KEY (`menu_id`) REFERENCES `menu` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 22. game_history
-- 수정사항: table_id 대신 session_id를 사용하여 히스토리 추적성 강화
CREATE TABLE `game_history` (
                                `id`           BIGINT    NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                `session_id`   BIGINT    NOT NULL COMMENT '방문 세션 ID (FK)',
                                `game_item_id` INT       NOT NULL,
                                `rented_at`    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                `returned_at`  TIMESTAMP NULL,
                                `status`       ENUM ('RENTING','RETURNED','DAMAGED','LOST') NOT NULL DEFAULT 'RENTING',
                                CONSTRAINT `fk_rental_session` FOREIGN KEY (`session_id`) REFERENCES `table_session` (`id`),
                                CONSTRAINT `fk_rental_item` FOREIGN KEY (`game_item_id`) REFERENCES `game_item` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='게임 대여 이력 로그.';

-- 14. payment / 15. toss_payment
CREATE TABLE `payment`
(
    `id`           INT                   NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `session_id`   BIGINT                NOT NULL UNIQUE COMMENT '세션당 최종 1회 결제',
    `status`       ENUM ('READY','DONE') NOT NULL DEFAULT 'READY',
    `final_amount` INT                   NOT NULL,
    `paid_at`      TIMESTAMP                      DEFAULT NULL,
    CONSTRAINT `fk_payment_session` FOREIGN KEY (`session_id`) REFERENCES `table_session` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `toss_payment`
(
    `id`            INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `payment_id`    INT          NOT NULL UNIQUE,
    `payment_key`   VARCHAR(200) NOT NULL UNIQUE,
    `order_id_toss` VARCHAR(64)  NOT NULL,
    `method`        ENUM ('간편결제','계좌이체') DEFAULT NULL,
    `raw_response`  JSON                 DEFAULT NULL,
    `approved_at`   TIMESTAMP            DEFAULT NULL,
    CONSTRAINT `fk_toss_payment` FOREIGN KEY (`payment_id`) REFERENCES `payment` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 16. point / 17. point_history
CREATE TABLE `point`
(
    `id`         INT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `phone`      VARCHAR(20) NOT NULL UNIQUE,
    `balance`    INT         NOT NULL DEFAULT 0,
    `updated_at` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `point_history`
(
    `id`            BIGINT              NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `point_id`      INT                 NOT NULL,
    `order_id`      INT                          DEFAULT NULL,
    `type`          ENUM ('EARN','USE') NOT NULL,
    `amount`        INT                 NOT NULL,
    `balance_after` INT                 NOT NULL,
    `created_at`    TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_pointhistory_point` FOREIGN KEY (`point_id`) REFERENCES `point` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_pointhistory_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 10. macro_message / 18. table_message
CREATE TABLE `macro_message`
(
    `id`           INT                                      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `direction`    ENUM ('STAFF_TO_TABLE','TABLE_TO_STAFF') NOT NULL,
    `message_text` VARCHAR(255)                             NOT NULL,
    `is_active`    BOOLEAN                                  NOT NULL DEFAULT TRUE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `table_message`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `table_id`   INT          NOT NULL,
    `macro_id`   INT                   DEFAULT NULL,
    `content`    VARCHAR(255) NOT NULL,
    `is_read`    BOOLEAN      NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_tablemsg_table` FOREIGN KEY (`table_id`) REFERENCES `cafe_table` (`id`),
    CONSTRAINT `fk_tablemsg_macro` FOREIGN KEY (`macro_id`) REFERENCES `macro_message` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 19. item_sales_history / 20. daily_sales_summary
CREATE TABLE `item_sales_history`
(
    `id`           INT                                     NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `stat_date`    DATE                                    NOT NULL,
    `product_id`   INT                                     NOT NULL,
    `category`     ENUM ('DRINK', 'FOOD', 'GAME', 'GUEST') NOT NULL,
    `sales_qty`    INT                                     NOT NULL DEFAULT 0,
    `sales_amount` BIGINT                                  NOT NULL DEFAULT 0,
    UNIQUE KEY `uq_stat_date_product` (`stat_date`, `product_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `daily_sales_summary`
(
    `id`             BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `stat_date`      DATE   NOT NULL UNIQUE,
    `total_revenue`  BIGINT NOT NULL DEFAULT 0,
    `order_count`    INT    NOT NULL DEFAULT 0,
    `visit_count`    INT    NOT NULL DEFAULT 0,
    `avg_usage_time` INT             DEFAULT 0
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 사용자 생성 및 권한
--
CREATE USER IF NOT EXISTS `admin`@`%` IDENTIFIED BY '0331';
GRANT ALL PRIVILEGES ON `board_cafe_kiosk_2603`.* TO `admin`@`%`;
FLUSH PRIVILEGES;