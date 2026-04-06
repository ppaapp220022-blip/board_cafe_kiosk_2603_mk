-- 기존 payment 테이블에 table_number 컬럼 추가
-- method 컬럼의 사이즈 확대 및 UTF-8 인코딩 확인

ALTER TABLE payment
ADD COLUMN table_number INT NULL AFTER session_id,
MODIFY COLUMN method VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL;

-- 기존 행들을 위해 테이블 세션으로부터 table_number 채우기 (선택사항)
UPDATE payment p
JOIN cafe_table_session cts ON p.session_id = cts.id
SET p.table_number = cts.table_id
WHERE p.table_number IS NULL;

-- 인덱스 추가 (조회 성능 개선)
ALTER TABLE payment
ADD INDEX idx_table_number (table_number),
ADD INDEX idx_method (method);
