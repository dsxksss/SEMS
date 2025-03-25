-- 用户表
CREATE TABLE IF NOT EXISTS user_table (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '电话',
    real_name VARCHAR(50) COMMENT '真实姓名',
    role VARCHAR(20) NOT NULL COMMENT '角色',
    status INT NOT NULL COMMENT '状态：1-启用，0-禁用',
    create_time TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL COMMENT '更新时间'
);

-- 赛事表
CREATE TABLE IF NOT EXISTS event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '赛事ID',
    name VARCHAR(255) NOT NULL COMMENT '赛事名称',
    description TEXT COMMENT '赛事描述',
    start_time TIMESTAMP NOT NULL COMMENT '开始时间',
    end_time TIMESTAMP NOT NULL COMMENT '结束时间',
    location VARCHAR(255) NOT NULL COMMENT '举办地点',
    status VARCHAR(20) NOT NULL COMMENT '状态：UPCOMING-即将开始，ONGOING-进行中，COMPLETED-已结束，CANCELLED-已取消',
    max_participants INT NOT NULL COMMENT '最大参与人数',
    current_participants INT NOT NULL COMMENT '当前参与人数',
    create_time TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL COMMENT '更新时间'
);

-- 赛事标签关联表
CREATE TABLE IF NOT EXISTS event_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '标签ID',
    event_id BIGINT NOT NULL COMMENT '赛事ID',
    category VARCHAR(50) NOT NULL COMMENT '标签名称',
    FOREIGN KEY (event_id) REFERENCES event(id)
);

-- 报名表
CREATE TABLE IF NOT EXISTS registration (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '报名ID',
    event_id BIGINT NOT NULL COMMENT '赛事ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type VARCHAR(20) NOT NULL COMMENT '报名类型',
    real_name VARCHAR(50) NOT NULL COMMENT '姓名',
    phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    status VARCHAR(20) NOT NULL COMMENT '状态：PENDING-待审核，APPROVED-已通过，REJECTED-已拒绝',
    remark TEXT COMMENT '备注',
    admin_remark TEXT COMMENT '管理员备注',
    create_time TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL COMMENT '更新时间',
    register_time TIMESTAMP NOT NULL COMMENT '报名时间',
    registration_type VARCHAR(255) NOT NULL COMMENT '报名类型：个人报名，团队报名',
    FOREIGN KEY (event_id) REFERENCES event(id),
    FOREIGN KEY (user_id) REFERENCES user_table(id)
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_user_id ON registration(user_id);
CREATE INDEX IF NOT EXISTS idx_event_id ON registration(event_id);

-- 赛事结果表
CREATE TABLE IF NOT EXISTS event_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '结果ID',
    event_id BIGINT NOT NULL COMMENT '赛事ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    score VARCHAR(50) COMMENT '分数/成绩',
    rank INT COMMENT '排名',
    remark TEXT COMMENT '备注',
    create_time TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL COMMENT '更新时间',
    FOREIGN KEY (event_id) REFERENCES event(id),
    FOREIGN KEY (user_id) REFERENCES user_table(id)
);

-- 公告表
CREATE TABLE IF NOT EXISTS announcement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '公告ID',
    title VARCHAR(255) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    type VARCHAR(20) NOT NULL COMMENT '类型',
    status INT NOT NULL COMMENT '状态：1-启用，0-禁用',
    create_time TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL COMMENT '更新时间'
);

-- 赛事图片表
CREATE TABLE IF NOT EXISTS event_image (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '图片ID',
    event_id BIGINT NOT NULL COMMENT '赛事ID',
    image_url VARCHAR(255) NOT NULL COMMENT '图片URL',
    create_time TIMESTAMP NOT NULL COMMENT '创建时间',
    FOREIGN KEY (event_id) REFERENCES event(id)
);

-- 运动员信息表
CREATE TABLE IF NOT EXISTS athlete_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '运动员信息ID',
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    gender VARCHAR(10) NOT NULL COMMENT '性别',
    birthdate DATE NOT NULL COMMENT '出生日期',
    primary_sport VARCHAR(50) NOT NULL COMMENT '主要运动项目',
    skill_level VARCHAR(20) NOT NULL COMMENT '技能水平',
    years_of_experience INT NOT NULL COMMENT '运动年限',
    competition_experience TEXT COMMENT '比赛经验',
    achievements TEXT COMMENT '成就',
    height INT COMMENT '身高(cm)',
    weight INT COMMENT '体重(kg)',
    health_status VARCHAR(20) COMMENT '健康状况',
    medical_conditions TEXT COMMENT '医疗情况',
    create_time TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES user_table(id)
);

-- 观众信息表
CREATE TABLE IF NOT EXISTS audience_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '观众信息ID',
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    gender VARCHAR(10) NOT NULL COMMENT '性别',
    age_group VARCHAR(20) NOT NULL COMMENT '年龄段',
    city VARCHAR(50) COMMENT '城市',
    seat_preference VARCHAR(20) COMMENT '座位偏好',
    special_requirements TEXT COMMENT '特殊需求',
    create_time TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES user_table(id)
); 