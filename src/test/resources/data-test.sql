-- 测试数据初始化
-- 管理员用户
INSERT INTO user (id, username, password, email, real_name, role, create_time, update_time, status)
VALUES (1, 'admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'admin@example.com', '管理员', 'ADMIN', NOW(), NOW(), 1);

-- 普通用户
INSERT INTO user (id, username, password, email, real_name, role, create_time, update_time, status)
VALUES (2, 'user', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'user@example.com', '测试用户', 'USER', NOW(), NOW(), 1);

-- 测试赛事
INSERT INTO event (id, name, description, start_time, end_time, location, status, create_time, update_time, max_participants, current_participants)
VALUES (1, '测试赛事1', '这是一个测试赛事', DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), '测试地点', 1, NOW(), NOW(), 100, 0);

INSERT INTO event (id, name, description, start_time, end_time, location, status, create_time, update_time, max_participants, current_participants)
VALUES (2, '测试赛事2', '这是另一个测试赛事', DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 4 DAY), '测试地点2', 1, NOW(), NOW(), 50, 0); 