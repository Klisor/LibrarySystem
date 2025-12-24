-- 图书服务数据库
CREATE DATABASE IF NOT EXISTS book_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE book_db;

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

-- 插入书籍数据（第一部分）
INSERT INTO books (id, isbn, title, author, publisher, publish_year, category, total_copies, available_copies, location, description)
VALUES
(1, '978-7-5327-6426-5', '罪与罚', '陀思妥耶夫斯基', '译林出版社', 1866, 1, 5, 3, 'A1', '西方文学经典之作，心理描写的巅峰。'),
(2, '978-7-5302-1830-1', '白鲸', '赫尔曼·梅尔维尔', '译林出版社', 1851, 2, 4, 2, 'A2', '美国文学经典，人与自然的史诗对决。'),
(3, '978-7-0200-2540-0', '德米安:彷徨少年时', '埃米尔·辛克莱', '人民文学出版社', 1919, 3, 3, 3, 'A3', '探讨个人成长的经典文本。'),
(4, '978-7-1010-0205-0', '奥德赛', '荷马', '中华书局', -800, 4, 6, 4, 'A4', '古希腊史诗，西方文学的奠基之作。'),
(5, '978-7-5433-2172-4', '变形记', '弗兰茨·卡夫卡', '译林出版社', 1915, 5, 5, 3, 'A5', '现代主义文学代表作，荒诞派的先驱。'),
(6, '978-7-5327-4687-0', '呼啸山庄', '艾米莉·勃朗特', '译林出版社', 1847, 6, 4, 2, 'A6', '英国文学经典，哥特式爱情的典范。'),
(7, '978-7-0200-0220-7', '红楼梦', '曹雪芹', '人民文学出版社', 1791, 7, 5, 4, 'A7', '中国古典文学巅峰之作，四大名著之首。'),
(8, '978-7-5133-0000-7', '堂·吉诃德', '米格尔·德·塞万提斯', '译林出版社', 1605, 8, 3, 3, 'A8', '西班牙文学巨著，现代小说的开山之作。'),
(9, '978-7-0200-0034-0', '浮士德', '约翰·沃尔夫冈·冯·歌德', '人民文学出版社', 1808, 9, 4, 2, 'A9', '德国文学巅峰，探索人性与永恒的巨著。'),
(10, '978-7-5442-8007-5', '地狱变', '芥川龙之介', '上海译文出版社', 1918, 10, 3, 3, 'A10', '日本近代文学名篇，对艺术与人性深刻的探讨。');

-- 插入书籍数据（第二部分）
INSERT INTO books (isbn, title, author, publisher, publish_year, category, total_copies, available_copies, location, description) VALUES
('978-7-02-015673-9', '平凡的世界', '路遥', '人民文学出版社', 2017, 1, 5, 3, 'A区-文学-01架', '路遥的代表作，讲述中国当代城乡社会生活'),
('978-7-5327-7366-1', '百年孤独', '加西亚·马尔克斯', '上海译文出版社', 2011, 1, 3, 2, 'A区-文学-02架', '魔幻现实主义文学的代表作'),
('978-7-115-48648-6', 'Python编程从入门到实践', 'Eric Matthes', '人民邮电出版社', 2020, 2, 8, 5, 'B区-科技-03架', 'Python编程入门书籍'),
('978-7-111-54758-1', '深入理解计算机系统', 'Randal E. Bryant', '机械工业出版社', 2019, 2, 4, 1, 'B区-科技-04架', '计算机系统经典教材'),
('978-7-101-05124-6', '史记', '司马迁', '中华书局', 2014, 3, 6, 4, 'C区-历史-01架', '中国古代史学的里程碑之作'),
('978-7-108-04289-4', '中国哲学简史', '冯友兰', '北京大学出版社', 2013, 4, 3, 2, 'D区-哲学-01架', '了解中国哲学思想的入门书籍'),
('978-7-111-61211-3', '经济学原理', 'N. Gregory Mankiw', '机械工业出版社', 2018, 6, 5, 3, 'F区-经济-01架', '经济学经典教材'),
('978-7-04-050694-6', '教育学基础', '全国十二所重点师范大学', '教育科学出版社', 2019, 7, 7, 6, 'G区-教育-01架', '教育学基础理论教材');