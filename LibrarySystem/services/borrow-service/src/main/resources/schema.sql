-- 图书服务数据库
CREATE DATABASE IF NOT EXISTS borrow_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE borrow_db;

-- 创建借阅记录表（无外键版本）
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

-- 插入借阅记录数据
INSERT INTO borrow_records (user_id, book_id, borrow_date, due_date, return_date, renewed_count, status) VALUES
(1, 1, '2024-03-01 10:00:00', '2024-04-01 10:00:00', NULL, 0, 'BORROWED'),
(1, 3, '2024-03-10 14:30:00', '2024-04-10 14:30:00', '2024-04-05 09:15:00', 0, 'RETURNED'),
(3, 4, '2024-03-15 09:00:00', '2024-04-15 09:00:00', NULL, 1, 'BORROWED'),
(5, 2, '2024-02-20 11:00:00', '2024-03-20 11:00:00', NULL, 0, 'OVERDUE'),
(5, 6, '2024-03-05 15:00:00', '2024-04-05 15:00:00', NULL, 0, 'BORROWED'),
(5, 7, '2024-03-12 10:30:00', '2024-04-12 10:30:00', NULL, 0, 'BORROWED');