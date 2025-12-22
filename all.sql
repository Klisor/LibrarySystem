-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS library_management;
USE library_management;

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

-- 创建书籍表
CREATE TABLE IF NOT EXISTS books (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    isbn VARCHAR(20) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    publisher VARCHAR(255),
    publish_year INT,
    category TINYINT NOT NULL DEFAULT 10,
    total_copies INT DEFAULT 1,
    available_copies INT DEFAULT 1,
    location VARCHAR(100),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_isbn (isbn),
    INDEX idx_title (title),
    INDEX idx_author (author),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_book_id (book_id),
    INDEX idx_status (status),
    INDEX idx_due_date (due_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建笔记表
CREATE TABLE IF NOT EXISTS notes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    book_id BIGINT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_book_id (book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



-- 插入用户数据
INSERT INTO users (username, password, email, role, max_borrow_count, borrowed_count) VALUES
('张三', '$2y$10$YourHashedPasswordHere', 'zhangsan@example.com', 'USER', 5, 2),
('李四', '$2y$10$YourHashedPasswordHere', 'lisi@example.com', 'USER', 5, 0),
('王五', '$2y$10$YourHashedPasswordHere', 'wangwu@example.com', 'USER', 8, 1),
('管理员', '$2y$10$YourHashedPasswordHere', 'admin@library.com', 'ADMIN', 20, 0),
('赵六', '$2y$10$YourHashedPasswordHere', 'zhaoliu@example.com', 'USER', 5, 3);



INSERT INTO books (id, isbn, title, author, publisher, publish_year, category, total_copies, available_copies, location, description)
VALUES
(1, '9787532764265', '罪与罚', '陀思妥耶夫斯基', '译林出版社', 1866, 1, 5, 3, 'A1', '西方文学经典之作，心理描写的巅峰。'),
(2, '9787530218301', '白鲸', '赫尔曼·梅尔维尔', '译林出版社', 1851, 2, 4, 2, 'A2', '美国文学经典，人与自然的史诗对决。'),
(3, '9787020025400', '德米安:彷徨少年时', '埃米尔·辛克莱', '人民文学出版社', 1919, 3, 3, 3, 'A3', '探讨个人成长的经典文本。'),
(4, '9787101002050', '奥德赛', '荷马', '中华书局', -800, 4, 6, 4, 'A4', '古希腊史诗，西方文学的奠基之作。'),
(5, '9787543321724', '变形记', '弗兰茨·卡夫卡', '译林出版社', 1915, 5, 5, 3, 'A5', '现代主义文学代表作，荒诞派的先驱。'),
(6, '9787532746870', '呼啸山庄', '艾米莉·勃朗特', '译林出版社', 1847, 6, 4, 2, 'A6', '英国文学经典，哥特式爱情的典范。'),
(7, '9787020002207', '红楼梦', '曹雪芹', '人民文学出版社', 1791, 7, 5, 4, 'A7', '中国古典文学巅峰之作，四大名著之首。'),
(8, '9787513300007', '堂·吉诃德', '米格尔·德·塞万提斯', '译林出版社', 1605, 8, 3, 3, 'A8', '西班牙文学巨著，现代小说的开山之作。'),
(9, '9787020000340', '浮士德', '约翰·沃尔夫冈·冯·歌德', '人民文学出版社', 1808, 9, 4, 2, 'A9', '德国文学巅峰，探索人性与永恒的巨著。'),
(10, '9787544280075', '地狱变', '芥川龙之介', '上海译文出版社', 1918, 10, 3, 3, 'A10', '日本近代文学名篇，对艺术与人性深刻的探讨。');

-- 插入书籍数据（分类1-10：1-文学，2-科技，3-历史，4-哲学，5-艺术，6-经济，7-教育，8-医学，9-法律，10-其他）
INSERT INTO books (isbn, title, author, publisher, publish_year, category, total_copies, available_copies, location, description) VALUES
('978-7-02-015673-9', '平凡的世界', '路遥', '人民文学出版社', 2017, 1, 5, 3, 'A区-文学-01架', '路遥的代表作，讲述中国当代城乡社会生活'),
('978-7-5327-7366-1', '百年孤独', '加西亚·马尔克斯', '上海译文出版社', 2011, 1, 3, 2, 'A区-文学-02架', '魔幻现实主义文学的代表作'),
('978-7-115-48648-6', 'Python编程从入门到实践', 'Eric Matthes', '人民邮电出版社', 2020, 2, 8, 5, 'B区-科技-03架', 'Python编程入门书籍'),
('978-7-111-54758-1', '深入理解计算机系统', 'Randal E. Bryant', '机械工业出版社', 2019, 2, 4, 1, 'B区-科技-04架', '计算机系统经典教材'),
('978-7-101-05124-6', '史记', '司马迁', '中华书局', 2014, 3, 6, 4, 'C区-历史-01架', '中国古代史学的里程碑之作'),
('978-7-108-04289-4', '中国哲学简史', '冯友兰', '北京大学出版社', 2013, 4, 3, 2, 'D区-哲学-01架', '了解中国哲学思想的入门书籍'),
('978-7-111-61211-3', '经济学原理', 'N. Gregory Mankiw', '机械工业出版社', 2018, 6, 5, 3, 'F区-经济-01架', '经济学经典教材'),
('978-7-04-050694-6', '教育学基础', '全国十二所重点师范大学', '教育科学出版社', 2019, 7, 7, 6, 'G区-教育-01架', '教育学基础理论教材');

-- 插入借阅记录数据
INSERT INTO borrow_records (user_id, book_id, borrow_date, due_date, return_date, renewed_count, status) VALUES
(1, 1, '2024-03-01 10:00:00', '2024-04-01 10:00:00', NULL, 0, 'BORROWED'),
(1, 3, '2024-03-10 14:30:00', '2024-04-10 14:30:00', '2024-04-05 09:15:00', 0, 'RETURNED'),
(3, 4, '2024-03-15 09:00:00', '2024-04-15 09:00:00', NULL, 1, 'BORROWED'),
(5, 2, '2024-02-20 11:00:00', '2024-03-20 11:00:00', NULL, 0, 'OVERDUE'),
(5, 6, '2024-03-05 15:00:00', '2024-04-05 15:00:00', NULL, 0, 'BORROWED'),
(5, 7, '2024-03-12 10:30:00', '2024-04-12 10:30:00', NULL, 0, 'BORROWED');

-- 插入笔记数据
INSERT INTO notes (user_id, book_id, title, content) VALUES
(1, 1, '《平凡的世界》读书笔记', '这本书深刻地反映了中国改革开放初期农村青年的奋斗历程，孙少安和孙少平两兄弟的形象让人印象深刻。'),
(1, NULL, '个人学习计划', '1. 每周阅读一本技术书籍\n2. 每月写一篇读书笔记\n3. 学习Python高级特性'),
(3, 4, '《深入理解计算机系统》重点', '第二章的浮点数表示和第三章的机器级编程是重点章节，需要反复阅读和实践。'),
(5, 2, '《百年孤独》人物关系梳理', '布恩迪亚家族七代人的关系复杂，制作了人物关系图帮助理解。魔幻与现实的结合很精妙。');
