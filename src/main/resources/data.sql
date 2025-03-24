-- 测试公告数据
INSERT INTO announcement (title, content, type, status, create_time, update_time)
VALUES 
('校园马拉松即将开跑', '亲爱的同学们：\n\n我们的校园马拉松将于下个月15日正式开跑，欢迎大家踊跃报名参加！这是一次展示你体能和毅力的绝佳机会。\n\n比赛详情请查看赛事页面。', '赛事通知', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('体育中心设施维护通知', '由于体育中心需要进行年度设备维护，篮球馆将在下周三至周五（10月12日至14日）暂停开放。给您带来不便，敬请谅解。', '场馆通知', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('学校足球队招新', '学校足球队现面向全校招收新队员！无论你是有经验的球员还是刚接触足球的新手，只要有激情和毅力，都欢迎加入我们的大家庭。\n\n选拔时间：本周六下午2点\n地点：学校足球场', '招募公告', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('游泳培训班开课通知', '暑期游泳培训班将于7月1日开始报名，课程从初级到高级，满足不同水平学员需求。报名从速，名额有限！', '培训通知', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('校运会志愿者招募', '第28届校运会筹备工作正在进行中，现向全校学生招募赛事志愿者。这是锻炼能力、积累经验的好机会，欢迎报名参与！', '招募公告', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 测试赛事数据
INSERT INTO event (name, description, start_time, end_time, location, status, max_participants, current_participants, create_time, update_time)
VALUES 
('校园马拉松', '一年一度的校园马拉松，设有全程、半程和迷你马拉松三个项目，欢迎全校师生参与！', DATEADD('DAY', 15, CURRENT_DATE()), DATEADD('DAY', 15, CURRENT_DATE()), '学校操场', 'UPCOMING', 500, 0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('篮球联赛', '院系间篮球联赛，以班级为单位组队参赛，冠军队伍将获得丰厚奖品！', DATEADD('DAY', 7, CURRENT_DATE()), DATEADD('DAY', 21, CURRENT_DATE()), '体育馆', 'UPCOMING', 200, 0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('游泳比赛', '校内游泳比赛，设有自由泳、蛙泳、仰泳、蝶泳等多个项目，按照年级分组进行。', DATEADD('DAY', 5, CURRENT_DATE()), DATEADD('DAY', 5, CURRENT_DATE()), '游泳馆', 'UPCOMING', 100, 0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('羽毛球友谊赛', '与兄弟院校的羽毛球友谊赛，以增进校际交流，提高球技水平。', DATEADD('DAY', 10, CURRENT_DATE()), DATEADD('DAY', 10, CURRENT_DATE()), '羽毛球馆', 'UPCOMING', 50, 0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('校园定向越野', '在校园内进行的定向越野比赛，考验参与者的体力、智力和团队协作能力。', DATEADD('DAY', 20, CURRENT_DATE()), DATEADD('DAY', 20, CURRENT_DATE()), '全校范围', 'UPCOMING', 300, 0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 为赛事添加分类
INSERT INTO event_category (event_id, category) 
VALUES 
(1, '长跑'),
(1, '户外'),
(2, '球类'),
(2, '团队'),
(3, '水上'),
(3, '个人'),
(4, '球类'),
(4, '室内'),
(5, '户外'),
(5, '团队'); 