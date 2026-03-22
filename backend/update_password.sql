-- Update admin password to BCrypt encoded "123456"
UPDATE user SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHCYAKMr4M5.jjv.5VQxYu' WHERE username = 'admin';

-- Update lisi password to BCrypt encoded "123456"
UPDATE user SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHCYAKMr4M5.jjv.5VQxYu' WHERE username = 'lisi';

-- Update wangwu password to BCrypt encoded "123456"
UPDATE user SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHCYAKMr4M5.jjv.5VQxYu' WHERE username = 'wangwu';

-- Verify the update
SELECT id, username, role, password FROM user;
