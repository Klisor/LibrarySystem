-- 图书服务数据库
CREATE DATABASE IF NOT EXISTS borrow_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE borrow_db;

-- 创建借阅记录表
CREATE TABLE IF NOT EXISTS borrow_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    borrow_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    due_date TIMESTAMP NOT NULL,
    return_date TIMESTAMP NULL,
    renewed_count INT DEFAULT 0,
    max_renew_count INT DEFAULT 1,
    status ENUM('BORROWED','RETURNED','OVERDUE','LOST') DEFAULT 'BORROWED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_book_id (book_id),
    INDEX idx_status (status),
    INDEX idx_due_date (due_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
