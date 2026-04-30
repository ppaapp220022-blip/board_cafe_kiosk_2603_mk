SELECT 'CREATE DATABASE board_cafe_kiosk_2603'
WHERE NOT EXISTS (
    SELECT FROM pg_database WHERE datname = 'board_cafe_kiosk_2603'
)\gexec