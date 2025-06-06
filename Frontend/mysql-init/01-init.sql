-- Create database if not exists
CREATE DATABASE IF NOT EXISTS student_attendance;

-- Use the database
USE student_attendance;

-- Set character set and collation
ALTER DATABASE student_attendance CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create application user with limited privileges
CREATE USER IF NOT EXISTS 'app_user'@'%' IDENTIFIED BY '${MYSQL_PASSWORD}';
GRANT SELECT, INSERT, UPDATE, DELETE ON student_attendance.* TO 'app_user'@'%';

-- Grant root privileges (for development only - remove in production)
GRANT ALL PRIVILEGES ON student_attendance.* TO 'root'@'%';

-- Set performance tuning parameters
SET GLOBAL innodb_buffer_pool_size = 256M;
SET GLOBAL innodb_log_file_size = 64M;
SET GLOBAL innodb_log_buffer_size = 16M;
SET GLOBAL innodb_flush_log_at_trx_commit = 2;
SET GLOBAL innodb_flush_method = O_DIRECT;

FLUSH PRIVILEGES; 