-- 测试用户数据（密码均为123456）
INSERT INTO user_table (username, password, email, phone, real_name, role, status, create_time, update_time)
VALUES 
-- 管理员账号
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin@example.com', '13800138000', '系统管理员', 'ADMIN', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
-- 教师账号
('teacher1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'teacher1@example.com', '13800138001', '王教练', 'TEACHER', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('teacher2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'teacher2@example.com', '13800138002', '李教练', 'TEACHER', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
-- 学生账号
('student1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'student1@example.com', '13800138003', '张三', 'STUDENT', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('student2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'student2@example.com', '13800138004', '李四', 'STUDENT', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('student3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'student3@example.com', '13800138005', '王五', 'STUDENT', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('student4', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'student4@example.com', '13800138006', '赵六', 'STUDENT', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('student5', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'student5@example.com', '13800138007', '钱七', 'STUDENT', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 运动员信息
INSERT INTO athlete_info (user_id, gender, birthdate, primary_sport, skill_level, years_of_experience, competition_experience, achievements, height, weight, health_status, medical_conditions, create_time, update_time)
VALUES
(4, '男', '2000-01-15', '长跑', '专业级', 5, '参加过校级、市级多项长跑比赛', '校运会1500米第一名、市运会5000米第三名', 178, 65, '健康', '无', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(5, '男', '2001-03-22', '篮球', '高级', 4, '校级篮球联赛主力队员', '校篮球联赛MVP', 185, 75, '健康', '无', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(6, '女', '2002-05-18', '游泳', '中级', 3, '参加过校级游泳比赛', '校运会自由泳第二名', 165, 52, '健康', '无', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(7, '男', '2001-09-10', '羽毛球', '高级', 5, '校级、市级羽毛球比赛', '市级羽毛球比赛双打第二名', 175, 68, '健康', '无', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(8, '女', '2003-07-25', '田径', '中级', 2, '校级田径比赛', '校运会跳远第三名', 170, 55, '健康', '无', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 观众信息
INSERT INTO audience_info (user_id, gender, age_group, city, seat_preference, special_requirements, create_time, update_time)
VALUES
(2, '男', '30-40', '北京', '中部', '无特殊要求', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(3, '女', '30-40', '上海', '前排', '无特殊要求', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 公告数据
INSERT INTO announcement (title, content, type, status, create_time, update_time)
VALUES 
('校园马拉松即将开跑', '亲爱的同学们：

我们的校园马拉松将于下个月15日正式开跑，欢迎大家踊跃报名参加！这是一次展示你体能和毅力的绝佳机会。

比赛详情请查看赛事页面。', '赛事通知', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('体育中心设施维护通知', '由于体育中心需要进行年度设备维护，篮球馆将在下周三至周五（10月12日至14日）暂停开放。给您带来不便，敬请谅解。', '场馆通知', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('学校足球队招新', '学校足球队现面向全校招收新队员！无论你是有经验的球员还是刚接触足球的新手，只要有激情和毅力，都欢迎加入我们的大家庭。

选拔时间：本周六下午2点
地点：学校足球场', '招募公告', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('游泳培训班开课通知', '暑期游泳培训班将于7月1日开始报名，课程从初级到高级，满足不同水平学员需求。报名从速，名额有限！', '培训通知', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('校运会志愿者招募', '第28届校运会筹备工作正在进行中，现向全校学生招募赛事志愿者。这是锻炼能力、积累经验的好机会，欢迎报名参与！', '招募公告', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('体育器材使用规范', '为保障体育器材的正常使用寿命，请同学们在使用后及时归还并摆放整齐。如发现器材损坏，请及时向管理员报备。', '规章制度', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('体育特长生选拔通知', '学校将于下月初举行体育特长生选拔，涵盖田径、球类、游泳等多个项目，有意向的同学请提前做好准备并按时参加。', '招募公告', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 赛事数据
INSERT INTO event (name, description, start_time, end_time, location, status, max_participants, current_participants, create_time, update_time)
VALUES 
('校园马拉松', '一年一度的校园马拉松，设有全程、半程和迷你马拉松三个项目，欢迎全校师生参与！', DATEADD('DAY', 15, CURRENT_DATE()), DATEADD('DAY', 15, CURRENT_DATE()), '学校操场', 'UPCOMING', 500, 5, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('篮球联赛', '院系间篮球联赛，以班级为单位组队参赛，冠军队伍将获得丰厚奖品！', DATEADD('DAY', 7, CURRENT_DATE()), DATEADD('DAY', 21, CURRENT_DATE()), '体育馆', 'UPCOMING', 200, 32, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('游泳比赛', '校内游泳比赛，设有自由泳、蛙泳、仰泳、蝶泳等多个项目，按照年级分组进行。', DATEADD('DAY', 5, CURRENT_DATE()), DATEADD('DAY', 5, CURRENT_DATE()), '游泳馆', 'UPCOMING', 100, 18, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('羽毛球友谊赛', '与兄弟院校的羽毛球友谊赛，以增进校际交流，提高球技水平。', DATEADD('DAY', 10, CURRENT_DATE()), DATEADD('DAY', 10, CURRENT_DATE()), '羽毛球馆', 'UPCOMING', 50, 8, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('校园定向越野', '在校园内进行的定向越野比赛，考验参与者的体力、智力和团队协作能力。', DATEADD('DAY', 20, CURRENT_DATE()), DATEADD('DAY', 20, CURRENT_DATE()), '全校范围', 'UPCOMING', 300, 45, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('足球友谊赛', '与邻近学校进行的足球友谊赛，促进校际交流与友谊。', DATEADD('DAY', 30, CURRENT_DATE()), DATEADD('DAY', 30, CURRENT_DATE()), '足球场', 'UPCOMING', 80, 22, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('乒乓球锦标赛', '校内乒乓球锦标赛，设单打和双打两个项目，欢迎乒乓球爱好者参与。', DATEADD('DAY', 25, CURRENT_DATE()), DATEADD('DAY', 27, CURRENT_DATE()), '乒乓球室', 'UPCOMING', 120, 36, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('冬季越野跑', '冬季越野跑活动，在校园周边山林进行，享受冬日运动的乐趣。', DATEADD('DAY', 45, CURRENT_DATE()), DATEADD('DAY', 45, CURRENT_DATE()), '校园周边', 'UPCOMING', 150, 12, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('春季运动会', '一年一度的春季运动会，包括田径、团体和趣味比赛多个项目。', DATEADD('DAY', -30, CURRENT_DATE()), DATEADD('DAY', -28, CURRENT_DATE()), '田径场', 'COMPLETED', 600, 520, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('冬季篮球联赛', '室内篮球联赛，丰富冬季校园体育生活。', DATEADD('DAY', -15, CURRENT_DATE()), DATEADD('DAY', -1, CURRENT_DATE()), '体育馆', 'COMPLETED', 160, 144, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 为赛事添加分类
INSERT INTO event_category (event_id, category) 
VALUES 
(1, '长跑'),
(1, '户外'),
(1, '团队'),
(2, '球类'),
(2, '团队'),
(2, '室内'),
(3, '水上'),
(3, '个人'),
(3, '室内'),
(4, '球类'),
(4, '室内'),
(4, '团队'),
(5, '户外'),
(5, '团队'),
(5, '长跑'),
(6, '球类'),
(6, '户外'),
(6, '团队'),
(7, '球类'),
(7, '室内'),
(7, '个人'),
(8, '长跑'),
(8, '户外'),
(8, '个人'),
(9, '田径'),
(9, '户外'),
(9, '团队'),
(10, '球类'),
(10, '室内'),
(10, '团队');

-- 赛事图片数据
INSERT INTO event_image (event_id, image_url, create_time)
VALUES
(1, '/api/images/events/marathon.jpg', CURRENT_TIMESTAMP()),
(2, '/api/images/events/basketball.jpg', CURRENT_TIMESTAMP()),
(3, '/api/images/events/swimming.jpg', CURRENT_TIMESTAMP()),
(4, '/api/images/events/badminton.jpg', CURRENT_TIMESTAMP()),
(5, '/api/images/events/orienteering.jpg', CURRENT_TIMESTAMP()),
(9, '/api/images/events/sports-meet.jpg', CURRENT_TIMESTAMP()),
(10, '/api/images/events/winter-basketball.jpg', CURRENT_TIMESTAMP());

-- 报名数据
INSERT INTO registration (event_id, user_id, type, real_name, phone, status, remark, admin_remark, create_time, update_time, register_time, registration_type)
VALUES
(1, 4, '参赛者', '张三', '13800138003', 'APPROVED', '希望参加全程马拉松', '已确认资格', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), '个人报名'),
(1, 5, '参赛者', '李四', '13800138004', 'APPROVED', '报名半程马拉松', '已确认资格', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), '个人报名'),
(2, 5, '参赛者', '李四', '13800138004', 'APPROVED', '代表计算机学院参赛', '已编入A组', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), '团队报名'),
(3, 6, '参赛者', '王五', '13800138005', 'APPROVED', '参加自由泳和蛙泳项目', '已安排泳道', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), '个人报名'),
(4, 7, '参赛者', '赵六', '13800138006', 'APPROVED', '单打比赛', '已确认', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), '个人报名'),
(5, 8, '参赛者', '钱七', '13800138007', 'PENDING', '初次参加，需要指导', '等待审核', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), '个人报名');

-- 赛事结果数据
INSERT INTO event_result (event_id, user_id, score, rank, remark, create_time, update_time)
VALUES
(9, 4, '3:25.16', 1, '表现优异，刷新校记录', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(9, 5, '3:40.23', 2, '稳定发挥', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(9, 6, '3:56.08', 3, '后程冲刺出色', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(10, 5, '得分王，场均22分', 1, 'MVP球员', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
(10, 7, '场均15分10篮板', 2, '最佳防守球员', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()); 