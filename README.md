# Board Wave

보드게임 카페 운영을 위한 통합 키오스크 및 관리자 대시보드 프로젝트입니다.  
키오스크 주문, 패키지/정산, 포인트, 보드게임 대여, 실시간 주문 처리, 관리자 통계와 운영 기능을 하나의 시스템으로 구현했습니다.

## 협업 및 역할 분담

팀 프로젝트는 기능 단위 분담과 공통 검증을 병행하는 방식으로 진행했습니다.

### 팀원 소개

| 이름 | 담당 영역 | 주요 기여 |
| --- | --- | --- |
| 강수현 | 매크로 메시지, 통계 집계, 관리자 대시보드 | DB 설계, API 명세 정리, Figma UI 설계 |
| 김민기 | 장바구니, 주문, 결제 흐름 | 키오스크 사용자 흐름 설계, 주문/결제 경험 설계 |
| 서민성 | 포인트 서비스, 요금 부과 정책 | 세션별 금액 계산, 포인트 적립/사용 규칙 정리 |
| 서주연 | 인증/인가, 이중 인증, OTP, 임시 비밀번호 발급 | Spring AI 기반 AI 음성 안내 기능 구현 |

### 공통 협업 방식

| 협업 항목 | 내용 |
| --- | --- |
| 형상관리 | GitHub 형상관리, 브랜치 전략, 커밋 컨벤션 사용 |
| 진행 상황 공유 | 대시보드 기반 진행률 시각화로 작업 상태 공유 |
| 통합 검증 | 격일 Merge와 회귀 테스트로 코드 충돌과 기능 간섭 조기 확인 |
| 개발 프로세스 | 4주 애자일 흐름으로 기획 → 설계 → 구현 → 테스트 반복 |
| 품질 점검 | 전수 테스트와 테스트 시트 작성으로 도메인 간 상호 영향도 점검 |

## 프로젝트 개요

Board Wave는 보드게임 카페 환경에 맞춘 웹 기반 운영 시스템입니다.

- 고객은 키오스크에서 인원 선택, 전화번호 등록, 패키지 선택, 메뉴 주문, 결제를 진행할 수 있습니다.
- 직원과 관리자는 대시보드에서 테이블 상태, 실시간 주문, 서비스 요청, 정산, 상품/게임/직원/통계를 관리할 수 있습니다.
- 주문 상태와 서비스 요청은 실시간으로 반영되며, 결제와 포인트 적립/사용 흐름까지 하나로 연결됩니다.

프로젝트가 해결하려는 핵심 운영 문제는 다음과 같습니다.

- 복잡한 이용료 계산과 패키지 정산의 수작업 의존
- 보드게임 대여/반납 및 실물 재고 확인의 비효율
- 고객 테이블과 관리자 사이의 소통 지연
- 주문/결제/통계 데이터가 분리되어 발생하는 운영 가시성 부족

## 주요 기능

### 키오스크

- 테이블 로그인 및 세션 시작
- 인원 선택, 전화번호 등록, 패키지 선택
- 음식/음료/게임 장바구니 및 주문
- 포인트 조회, 사용, 적립
- 토스페이먼츠 기반 결제
- 서비스 요청 메시지 전송
- AI 기반 TTS/STT 챗봇 기능

### 관리자/직원

- Spring Security 기반 로그인
- 이메일 OTP 기반 2차 인증
- 실시간 대시보드
- 주문 상태 관리
- 테이블 메시지 응대
- 메뉴/게임/재고 관리
- 패키지 및 가격 정책 관리
- 직원 계정 및 권한 관리
- 매출/방문/인기 상품 통계

### 보드게임 카페 도메인 기능

- `table_session` 기반 방문 세션 관리
- `orders`, `order_item` 기반 주문 처리
- `game`, `game_item`, `game_history` 기반 게임 대여 이력 관리
- `point`, `point_history` 기반 포인트 적립/사용 관리
- `payment` 기반 세션 단위 정산 관리

## 기술 스택

### Backend

- Java 21
- Spring Boot 3.5.11
- Spring MVC
- Spring Security
- Spring WebSocket / STOMP
- Spring Batch
- Spring Mail
- MyBatis
- ModelMapper
- Lombok

### Frontend

- Thymeleaf
- HTML / CSS / JavaScript
- Thymeleaf Layout Dialect

### Database

- MariaDB
- PostgreSQL + PGVector

### External / AI

- Toss Payments
- Solapi
- Spring AI
- OpenAI

## 핵심 구현 포인트

- WebSocket/STOMP 기반 실시간 양방향 알림
  주문, 테이블 요청, 매크로 메시지를 관리자 화면과 키오스크에 즉시 반영합니다.
- Spring Batch 기반 통계 자동화
  일별 매출/방문/상품 통계를 새벽 배치로 분리 집계해 서비스 트랜잭션 부하를 줄입니다.
- 세션 중심 정산 구조
  `table_session`을 기준으로 주문, 게임 대여, 결제, 포인트 흐름이 연결됩니다.
- 실물 보드게임 재고 추적
  `game_item`을 시리얼 단위로 관리해 `NORMAL`, `RENTED`, `DAMAGED`, `LOST` 상태를 구분합니다.
- AI 음성 안내 파이프라인
  STT → PGVector 유사도 검색 → LLM 응답 → TTS 흐름으로 게임 안내를 제공합니다.

## 시스템 구성

프로젝트는 크게 3개 영역으로 나뉩니다.

- `common`: 로그인, 공통 진입, 보안 보조 흐름
- `kiosk`: 고객용 키오스크 화면, 주문, 결제, 포인트, 메시지, AI 기능
- `admin`: 대시보드, 상품/게임/정책/계정/통계 관리

시스템 아키텍처는 다음 축으로 구성됩니다.

- Client
  키오스크 화면과 관리자 대시보드가 Axios/Fetch, Thymeleaf, Chart.js를 통해 서버와 통신합니다.
- Server
  REST API, Spring Security, WebSocket/STOMP, Spring Batch가 핵심 비즈니스 로직을 담당합니다.
- Database
  MariaDB는 트랜잭션 데이터 저장소, PostgreSQL + PGVector는 AI 검색용 벡터 저장소로 사용합니다.
- External APIs
  Toss Payments, SMTP, Solapi, OpenAI를 통해 결제·인증·알림·AI 기능을 연결합니다.

코드 기준 주요 디렉터리:

- `src/main/java/org/example/board_cafe_kiosk_2603/controller`
- `src/main/java/org/example/board_cafe_kiosk_2603/service`
- `src/main/java/org/example/board_cafe_kiosk_2603/mapper`
- `src/main/resources/templates`
- `src/main/resources/static`
- `src/main/resources/mapper`
- `src/main/resources/sql/MariaDB`

## 주요 화면 및 흐름

### 키오스크 기본 흐름

1. 테이블 로그인
2. 인원 수 선택
3. 전화번호 등록 또는 건너뛰기
4. 패키지 선택
5. 메뉴/게임 주문
6. 장바구니 확인
7. 포인트 조회 및 적용
8. 결제
9. 주문 처리 및 세션 종료

### 관리자 운영 흐름

1. 관리자/직원 로그인
2. 이메일 인증
3. 대시보드 진입
4. 테이블 상태 및 주문 현황 확인
5. 주문 상태 변경 또는 메시지 응대
6. 정산/통계/정책 관리

### AI 음성 안내 흐름

1. 사용자가 키오스크에서 AI 버튼을 누릅니다.
2. 음성 입력을 STT로 텍스트 변환합니다.
3. 변환된 질문으로 PGVector에서 관련 게임 정보를 검색합니다.
4. 검색된 문맥만 사용해 LLM 답변을 생성합니다.
5. 답변을 TTS로 다시 음성 출력합니다.
6. 음성 인식이 부정확한 경우 사용자가 텍스트를 직접 수정해 재질문할 수 있습니다.

## API 구성

산출물 기준으로 다음 영역의 API가 정의되어 있습니다.

- 키오스크 진입 / 세션
- 패키지 선택
- 장바구니
- 주문
- 포인트 조회
- 서비스 요청 / 메시지
- AI TTS / STT / 챗봇

REST API와 서버 렌더링 화면이 혼합된 구조이며, 인증은 Spring Security와 HttpSession을 사용합니다.

## 데이터베이스

문서 기준 총 22개 테이블로 구성되어 있습니다.

대표 테이블:

- `manager`
- `cafe_table`
- `customer`
- `category`
- `cafe_package`
- `table_session`
- `menu`
- `orders`
- `order_item`
- `game`
- `game_item`
- `cart`
- `cart_item`
- `game_history`
- `payment`
- `point`
- `point_history`
- `macro_message`
- `table_message`
- `item_sales_history`
- `daily_sales_summary`
- `persistent_logins`

ERD는 크게 4개 도메인으로 구성됩니다.

- 상품/게임 마스터
  `category`, `menu`, `game`, `game_item`, `cafe_package`
- 테이블 세션
  `cafe_table`, `table_session`, `game_history`
- 주문/결제
  `cart`, `cart_item`, `orders`, `order_item`, `payment`, `point`, `point_history`
- 운영/통계
  `manager`, `macro_message`, `table_message`, `item_sales_history`, `daily_sales_summary`, `persistent_logins`

초기 스키마와 더미 데이터는 아래 경로에 있습니다.

- [01_init.sql](src/main/resources/sql/MariaDB/01_init.sql)
- [02_dummy.sql](src/main/resources/sql/MariaDB/02_dummy.sql)

`02_dummy.sql`에는 메뉴/게임 더미 이미지 경로도 함께 포함되어 있어, DB를 새로 생성한 뒤 더미를 적재하면 키오스크 화면에서 샘플 이미지가 바로 보이도록 구성되어 있습니다.

## 실행 환경

### 요구 사항

- JDK 21
- MariaDB
- PostgreSQL
- Gradle

### 설정 파일

설정 항목 예시:

- MariaDB 연결 정보
- PostgreSQL / PGVector 연결 정보
- Toss Payments 키
- 메일 서버 설정
- OpenAI / Spring AI 설정
- 업로드 경로 설정

현재 프로젝트는 루트의 `.env` 파일을 자동으로 읽도록 설정되어 있습니다.

주요 환경 변수 예시:

- `MARIADB_URL`
- `MARIADB_USERNAME`
- `MARIADB_PASSWORD`
- `PGVECTOR_URL`
- `PGVECTOR_USERNAME`
- `PGVECTOR_PASSWORD`
- `TOSS_PAYMENTS_SECRET_KEY`
- `TOSS_PAYMENTS_CLIENT_KEY`
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`
- `MYAPP_MAIL_FROM`
- `MYAPP_MAIL_FROM_NAME`
- `MAIL_TEST_TO` (선택, 메일 발송 테스트용)
- `PORTFOLIO_SUPER_KEY_ID`
- `PORTFOLIO_SUPER_KEY_OTP`
- `PORTFOLIO_SUPER_KEY_TEMP_PASSWD`
- `OPENAI_API_KEY`
- `SPRING_BATCH_JDBC_INITIALIZE_SCHEMA`

추가 민감 설정도 모두 `src/main/resources/application.properties`에서 환경변수 참조로 읽습니다.

## 실행 방법

1. MariaDB에 `01_init.sql`, `02_dummy.sql`을 적용합니다.
2. PostgreSQL에 PGVector를 사용할 데이터베이스를 준비합니다.
3. 프로젝트 루트의 `.env`에 DB 계정, Toss/메일 키, OpenAI 키 등 실행 값을 입력합니다.
4. 필요한 경우 `application.properties`의 업로드 경로(`my.upload.path`)를 로컬 환경에 맞게 수정합니다.
5. Gradle로 애플리케이션을 실행합니다.

```bash
./gradlew bootRun
```

Windows 환경에서는:

```powershell
.\gradlew.bat bootRun
```

참고:

- MariaDB 계정 정보가 실제 로컬 DB와 다르면 Spring Batch `jobRepository` 생성 단계에서 부팅이 실패할 수 있습니다.
- `OPENAI_API_KEY`가 비어 있으면 AI 관련 빈 생성 시 애플리케이션이 시작되지 않습니다.
- PGVector는 PostgreSQL 연결과 벡터 확장 구성이 정상이어야 AI 검색 기능이 동작합니다.

## 테스트

기본 테스트는 Spring Boot Test, MyBatis Test, Spring Security Test, Spring Batch Test를 사용합니다.

```bash
./gradlew test
```

Windows 환경에서는:

```powershell
.\gradlew.bat test
```

## 프로젝트 문서

프로젝트 산출물에는 다음 문서가 포함되어 있습니다.

- 기능 설명서
- 요구사항 정의서
- 유스케이스
- API 명세서
- 데이터베이스 정의서
- 시연 영상

README 작성 참고 자료:

- `보드게임 카페 기능 설명서`
- `보드게임 카페 요구사항 정의서`
- `보드게임 카페 유스케이스`
- `보드게임 카페 API 명세서`
- `보드게임 카페 데이터베이스 정의서`
- `보드게임 카페 시연 영상`

## 향후 보안 계획

현재 인증, OTP, 결제 검증, CSRF 예외 분리 등 기본 보안 장치를 적용하고 있으며, 향후에는 다음 항목을 우선 강화할 계획입니다.

- 서주연

  관리자 인증 보안 강화를 중심으로 로그인 시도 제한, 계정 잠금 정책, 관리자 IP 접근 제어, 비밀번호 정책 고도화를 추진할 계획입니다.
- 강수현

  관리자 기능의 감사 추적성을 높이기 위해 주문 상태 변경, 정책 수정, 포인트 조정 이력에 대한 감사 로그 체계와 운영 대시보드 관찰성을 보강할 계획입니다.
- 김민기

  결제 및 주문 흐름의 안정성을 높이기 위해 Toss 결제 검증 실패 대응, 수동 처리 이력 추적, 주문-결제 상태 불일치 방지 로직을 더 강화할 계획입니다.
- 서민성

  포인트 및 요금 정책 영역에서 비정상 적립/차감 탐지, 정책 변경 이력 관리, 금액 계산 검증 로직 보강을 추진할 계획입니다.
- 공통 계획

  업로드 파일 검증 강화, WebSocket/STOMP 채널별 권한 검증, OpenAI 및 외부 API 호출 보호와 오용 탐지 로깅은 팀 공통 보안 개선 과제로 유지합니다.

## 회고

프로젝트를 통해 확인한 핵심 학습 포인트는 다음과 같습니다.

- 강수현

  Spring Batch의 Job → Step → Reader/Processor/Writer 라이프사이클을 통계 자동화에 적용하고, MyBatis XML 기반 통계 쿼리 최적화 경험을 축적했습니다.
- 김민기

  장바구니-주문-결제 흐름을 구현하면서 사용자 동선과 서버 트랜잭션 설계를 함께 맞추는 경험을 쌓았습니다.
- 서민성

  포인트와 요금 정책을 구현하며 세션 기반 금액 계산, 적립/차감 규칙, 도메인 정책 분리의 중요성을 체감했습니다.
- 서주연

  Spring Security의 이중 FilterChain, CSRF 처리, OTP 인증 흐름을 실제 서비스 구조에 맞게 조정하는 경험을 쌓았습니다.
- 공통 회고

  WebSocket/STOMP 기반 실시간 시스템이 폴링 대비 운영 반응성을 높인다는 점, Figma 프로토타입 선행이 프론트 수정 비용을 줄인다는 점, 잦은 Merge와 테스트 루프가 충돌 리스크를 낮춘다는 점을 확인했습니다.

향후 개선 방향:

- 매크로 메시지를 넘어 관리자와 고객 간 1:1 실시간 채팅 기능 확장
- 시간대·요일별 이용 패턴 예측을 위한 AI 분석 기능 보강
- AWS/Docker 기반 운영 환경 배포와 인프라 표준화

## 팀

참여자:

- 서주연
- 강수현
- 김민기
- 서민성
- 최종현

## 한 줄 소개

Board Wave는 보드게임 카페의 주문, 정산, 대여, 포인트, 실시간 운영 관리를 하나로 통합한 스마트 키오스크 솔루션입니다.
