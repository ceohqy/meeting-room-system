-- =============================================
-- 清理脏数据（tenant_id 为 null 的会议室）
-- =============================================
DELETE FROM meeting_room WHERE tenant_id IS NULL;

-- =============================================
-- 会议室数据（tenant_id=4 是comC公司，tenant_id=5 是comD公司）
-- =============================================
INSERT INTO meeting_room (room_name, capacity, location, equipment, status, tenant_id) VALUES
('C-大会议室', 20, 'C栋3楼', '投影仪,白板,视频会议', '1', 4),
('C-小会议室', 8,  'C栋2楼', '电视,白板', '1', 4),
('C-培训室',   30, 'C栋4楼', '投影仪,麦克风,白板', '1', 4),
('D-大会议室', 25, 'D栋3楼', '投影仪,白板,视频会议', '1', 5),
('D-小会议室', 6,  'D栋1楼', '电视', '1', 5),
('D-洽谈室',   4,  'D栋2楼', '白板', '1', 5);
-- 插入后 id: C-大会议室=9, C-小会议室=10, C-培训室=11, D-大会议室=12, D-小会议室=13, D-洽谈室=14

-- =============================================
-- 设备数据
-- =============================================
INSERT INTO equipment (name, type, room_id, serial_no, status, remark, tenant_id) VALUES
('投影仪C-1',   'projector', 9,  'SN-C-001', 1, 'C栋大会议室投影仪', 4),
('白板C-1',     'whiteboard', 9, 'SN-C-002', 1, 'C栋大会议室白板', 4),
('视频会议C-1', 'video',      9, 'SN-C-003', 1, 'C栋大会议室视频会议系统', 4),
('电视C-1',     'tv',        10, 'SN-C-004', 1, 'C栋小会议室电视', 4),
('投影仪C-2',   'projector', 11, 'SN-C-005', 1, 'C栋培训室投影仪', 4),
('麦克风C-1',   'mic',       11, 'SN-C-006', 1, 'C栋培训室麦克风', 4),
('投影仪D-1',   'projector', 12, 'SN-D-001', 1, 'D栋大会议室投影仪', 5),
('白板D-1',     'whiteboard',12, 'SN-D-002', 1, 'D栋大会议室白板', 5),
('电视D-1',     'tv',        13, 'SN-D-003', 1, 'D栋小会议室电视', 5),
('白板D-2',     'whiteboard',14, 'SN-D-004', 1, 'D栋洽谈室白板', 5);

-- =============================================
-- 预约数据
-- user_id: comC_ad1=5, comC_us1=7, comD_ad1=6, comD_us1=8
-- =============================================
INSERT INTO reservation (user_id, room_id, reservation_date, start_time, end_time, status, applicant, attendees, purpose, tenant_id, create_time) VALUES
(5,  9, CURDATE(),                          '09:00:00', '11:00:00', 'APPROVED', '曾蕙鑫', 15, '季度产品评审会', 4, NOW()),
(7, 10, CURDATE(),                          '14:00:00', '16:00:00', 'PENDING',  '罗婷',    6, '项目进度同步',   4, NOW()),
(5, 11, CURDATE(),                          '10:00:00', '12:00:00', 'APPROVED', '曾蕙鑫', 25, '新员工培训',     4, NOW()),
(5,  9, DATE_ADD(CURDATE(), INTERVAL 1 DAY),'09:00:00', '10:00:00', 'PENDING',  '曾蕙鑫', 10, '客户拜访',       4, NOW()),
(6, 12, CURDATE(),                          '09:00:00', '10:30:00', 'APPROVED', '胡宇晴',  8, '技术方案讨论',   5, NOW()),
(8, 14, CURDATE(),                          '15:00:00', '16:00:00', 'PENDING',  '邹欢',    3, '商务洽谈',       5, NOW()),
(6, 12, DATE_ADD(CURDATE(), INTERVAL 1 DAY),'14:00:00', '16:00:00', 'APPROVED', '胡宇晴', 20, '月度总结会',     5, NOW()),
(8, 13, DATE_ADD(CURDATE(), INTERVAL 2 DAY),'10:00:00', '11:00:00', 'PENDING',  '邹欢',    5, '需求评审',       5, NOW());

-- =============================================
-- 审批记录
-- =============================================
INSERT INTO approval_record (reservation_id, approver_id, approval_result, approval_time, remark)
SELECT id, 5, 'PENDING', NOW(), '待审批'
FROM reservation WHERE status = 'PENDING' AND tenant_id = 4;

INSERT INTO approval_record (reservation_id, approver_id, approval_result, approval_time, remark)
SELECT id, 6, 'PENDING', NOW(), '待审批'
FROM reservation WHERE status = 'PENDING' AND tenant_id = 5;
