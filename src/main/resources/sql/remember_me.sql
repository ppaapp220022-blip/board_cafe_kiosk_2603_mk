-- Remember-Me Persistent 방식에서 사용하는 토큰 저장 테이블
-- Spring Security가 자동으로 읽고 씀 (컬럼명, 타입 변경 불가)
CREATE TABLE persistent_logins
(
    -- Remember-Me 토큰의 식별자 (쿠키에 저장되는 값의 앞부분)
    -- 같은 기기에서 로그인하면 동일 series가 재사용됨
    series    VARCHAR(64)  NOT NULL,

    -- 실제 로그인 주체 식별자
    -- 키오스크의 경우 tableNumber가 저장됨 (KioskUserDetailsService의 username)
    username  VARCHAR(64)  NOT NULL,

    -- 매 요청마다 갱신되는 랜덤 토큰값 (보안 강화 목적)
    -- series는 같지만 token이 다르면 탈취로 간주하고 세션 무효화
    token     VARCHAR(64)  NOT NULL,

    -- 마지막으로 Remember-Me 인증이 사용된 시각
    -- 토큰 유효기간 계산에 활용됨
    last_used TIMESTAMP    NOT NULL,

    PRIMARY KEY (series)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT = 'Spring Security Remember-Me Persistent 토큰 저장소. 관리자 전체 리셋 시 전체 삭제됨.';