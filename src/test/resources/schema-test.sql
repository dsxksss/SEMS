-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `real_name` varchar(50) DEFAULT NULL,
  `role` varchar(20) NOT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
);

-- 事件表
DROP TABLE IF EXISTS `event`;
CREATE TABLE `event` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `description` text,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `location` varchar(255) NOT NULL,
  `status` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  `max_participants` int NOT NULL DEFAULT '0',
  `current_participants` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
);

-- 报名表
DROP TABLE IF EXISTS `registration`;
CREATE TABLE `registration` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `event_id` bigint NOT NULL,
  `registration_type` varchar(20) NOT NULL,
  `status` tinyint NOT NULL DEFAULT '0',
  `register_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  `remark` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

CREATE INDEX `idx_user_id` ON `registration` (`user_id`);
CREATE INDEX `idx_event_id` ON `registration` (`event_id`);

-- 比赛结果表
DROP TABLE IF EXISTS `event_result`;
CREATE TABLE `event_result` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `event_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `rank` int DEFAULT NULL,
  `score` varchar(50) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_event_user` (`event_id`,`user_id`)
);

CREATE INDEX `idx_event_id_result` ON `event_result` (`event_id`);
CREATE INDEX `idx_user_id_result` ON `event_result` (`user_id`);

-- 公告表
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `content` text NOT NULL,
  `creator_id` bigint NOT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  `status` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
);

-- 初始管理员账号
INSERT INTO `user` (`username`, `password`, `email`, `real_name`, `role`, `create_time`, `update_time`, `status`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin@example.com', '管理员', 'ADMIN', NOW(), NOW(), 1); 