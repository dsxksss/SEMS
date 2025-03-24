-- 用户表
CREATE TABLE IF NOT EXISTS user_table (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    real_name VARCHAR(50),
    role VARCHAR(20) NOT NULL,
    status INT NOT NULL,
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL
);

-- 赛事表
CREATE TABLE IF NOT EXISTS event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    location VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    max_participants INT NOT NULL,
    current_participants INT NOT NULL,
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL
);

-- 赛事标签关联表
CREATE TABLE IF NOT EXISTS event_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL,
    FOREIGN KEY (event_id) REFERENCES event(id)
);

-- 报名表
CREATE TABLE IF NOT EXISTS registration (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    remark TEXT,
    admin_remark TEXT,
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL,
    FOREIGN KEY (event_id) REFERENCES event(id),
    FOREIGN KEY (user_id) REFERENCES user_table(id)
);

-- 赛事结果表
CREATE TABLE IF NOT EXISTS event_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    score VARCHAR(50),
    rank INT,
    remark TEXT,
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL,
    FOREIGN KEY (event_id) REFERENCES event(id),
    FOREIGN KEY (user_id) REFERENCES user_table(id)
);

-- 公告表
CREATE TABLE IF NOT EXISTS announcement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(20) NOT NULL,
    status INT NOT NULL,
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL
);

-- 赛事图片表
CREATE TABLE IF NOT EXISTS event_image (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    create_time TIMESTAMP NOT NULL,
    FOREIGN KEY (event_id) REFERENCES event(id)
);

-- 运动员信息表
CREATE TABLE IF NOT EXISTS athlete_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    gender VARCHAR(10) NOT NULL,
    birthdate DATE NOT NULL,
    primary_sport VARCHAR(50) NOT NULL,
    skill_level VARCHAR(20) NOT NULL,
    years_of_experience INT NOT NULL,
    competition_experience TEXT,
    achievements TEXT,
    height INT,
    weight INT,
    health_status VARCHAR(20),
    medical_conditions TEXT,
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user_table(id)
);

-- 观众信息表
CREATE TABLE IF NOT EXISTS audience_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    gender VARCHAR(10) NOT NULL,
    age_group VARCHAR(20) NOT NULL,
    city VARCHAR(50),
    seat_preference VARCHAR(20),
    special_requirements TEXT,
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user_table(id)
);

-- 初始管理员账号
INSERT INTO user_table (username, password, email, real_name, role, status, create_time, update_time)
SELECT 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin@example.com', '管理员', 'ADMIN', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()
FROM (VALUES(0)) AS dual
WHERE NOT EXISTS (SELECT 1 FROM user_table WHERE username = 'admin'); 