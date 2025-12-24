CREATE DATABASE IF NOT EXISTS user_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE user_db;

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    role ENUM('ADMIN','USER') DEFAULT 'USER',
    max_borrow_count INT DEFAULT 5,
    borrowed_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO users (username, password, email, role, max_borrow_count, borrowed_count)
VALUES ('admin', '$2b$10$N.zKp9RWXHMS2uRB9n6lXu1Xy6P5TUlyqAA0Oj9Gj5GEjYDyTwQqK', 'admin@lib.com', 'ADMIN', 20, 0);

INSERT INTO users (username, password, email, role, max_borrow_count, borrowed_count)
VALUES ('alice', '$2b$10$N.zKp9RWXHMS2uRB9n6lXu1Xy6P5TUlyqAA0Oj9Gj5GEjYDyTwQqK', 'alice@example.com', 'USER', 5, 2);

INSERT INTO users (username, password, email, role, max_borrow_count, borrowed_count)
VALUES ('bob', '$2b$10$N.zKp9RWXHMS2uRB9n6lXu1Xy6P5TUlyqAA0Oj9Gj5GEjYDyTwQqK', 'bob@example.com', 'USER', 5, 0);

INSERT INTO users (username, password, email, role, max_borrow_count, borrowed_count)
VALUES ('carol', '$2b$10$N.zKp9RWXHMS2uRB9n6lXu1Xy6P5TUlyqAA0Oj9Gj5GEjYDyTwQqK', 'carol@example.com', 'USER', 10, 5);

INSERT INTO users (username, password, email, role, max_borrow_count, borrowed_count)
VALUES ('david', '$2b$10$N.zKp9RWXHMS2uRB9n6lXu1Xy6P5TUlyqAA0Oj9Gj5GEjYDyTwQqK', 'david@example.com', 'USER', 5, 1);