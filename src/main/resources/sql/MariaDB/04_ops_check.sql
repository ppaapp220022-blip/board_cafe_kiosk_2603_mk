-- ============================================================
-- 운영 점검용 SQL
-- 1) Spring Batch 메타테이블 존재 여부
-- 2) 포인트 더미/실데이터 적재 여부
-- 3) 최근 배치 실행 이력
-- Note: Linux MariaDB 환경에서는 batch_* 소문자 이름 기준으로 확인합니다.
-- ============================================================

-- 1. Spring Batch 메타테이블 존재 여부
SELECT table_name
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN (
    'batch_job_instance',
    'batch_job_execution',
    'batch_job_execution_params',
    'batch_step_execution',
    'batch_step_execution_context',
    'batch_job_execution_context'
  )
ORDER BY table_name;

-- 2. Spring Batch 시퀀스 존재 여부
SELECT sequence_name
FROM information_schema.sequences
WHERE sequence_schema = DATABASE()
  AND sequence_name IN (
    'batch_step_execution_seq',
    'batch_job_execution_seq',
    'batch_job_seq'
  )
ORDER BY sequence_name;

-- 3. 최근 배치 Job 실행 이력
SELECT
    ji.job_name,
    je.job_execution_id,
    je.status,
    je.create_time,
    je.start_time,
    je.end_time,
    je.exit_code
FROM batch_job_execution je
JOIN batch_job_instance ji
  ON je.job_instance_id = ji.job_instance_id
ORDER BY je.job_execution_id DESC
LIMIT 20;

-- 4. 최근 Step 실행 이력
SELECT
    step_name,
    status,
    read_count,
    write_count,
    commit_count,
    start_time,
    end_time
FROM batch_step_execution
ORDER BY step_execution_id DESC
LIMIT 20;

-- 5. 포인트 계정 현황
SELECT COUNT(*) AS point_account_count, COALESCE(SUM(balance), 0) AS total_balance
FROM point;

-- 6. 포인트 상위 고객
SELECT id, phone, balance, updated_at
FROM point
ORDER BY balance DESC, id ASC
LIMIT 20;

-- 7. 포인트 이력 최근 20건
SELECT id, point_id, order_id, type, amount, balance_after, created_at
FROM point_history
ORDER BY id DESC
LIMIT 20;
