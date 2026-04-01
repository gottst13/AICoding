-- ========================================
-- 智慧停车管理平台 - 测试数据初始化脚本
-- 数据库：smart_parking
-- 执行时间：2026-04-01
-- ========================================

-- 1. 创建测试车场
INSERT INTO parking_lots (name, code, address, total_capacity, status) 
VALUES ('智慧停车示范场', 'PARK_001', '北京市朝阳区示范路 1 号', 500, 1);

-- 2. 创建测试区域
-- 区域 A: B1 层商场区
INSERT INTO parking_zones (parking_lot_id, parent_zone_id, name, code, zone_type, zone_category, floor_level, total_spaces, available_spaces, has_independent_exit, status)
VALUES (1, NULL, 'B1 层商场区', 'ZONE_B1_MALL', 1, 1, -1, 100, 85, true, 1);

-- 区域 B: B2 层办公区
INSERT INTO parking_zones (parking_lot_id, parent_zone_id, name, code, zone_type, zone_category, floor_level, total_spaces, available_spaces, has_independent_exit, status)
VALUES (1, NULL, 'B2 层办公区', 'ZONE_B2_OFFICE', 1, 2, -2, 150, 120, true, 1);

-- 区域 C: 地面充电区
INSERT INTO parking_zones (parking_lot_id, parent_zone_id, name, code, zone_type, zone_category, floor_level, total_spaces, available_spaces, has_independent_exit, status)
VALUES (1, NULL, '地面充电区', 'ZONE_G_CHARGE', 1, 5, 0, 50, 30, false, 1);

-- 区域 D: A 座子区域（属于 B2 层办公区）
INSERT INTO parking_zones (parking_lot_id, parent_zone_id, name, code, zone_type, zone_category, floor_level, total_spaces, available_spaces, has_independent_exit, status)
VALUES (1, 2, 'A 座停车区', 'ZONE_B2_A', 2, 2, -2, 50, 40, false, 1);

-- 3. 创建测试车位
-- B1 层商场区 - 小型车位
INSERT INTO parking_spaces (zone_id, space_no, space_type, status, width_cm, length_cm, height_limit_cm)
VALUES 
(1, 'B1-A001', 1, 1, 250, 500, 200),
(1, 'B1-A002', 1, 1, 250, 500, 200),
(1, 'B1-A003', 1, 0, 250, 500, 200), -- 已占用
(1, 'B1-A004', 1, 1, 250, 500, 200),
(1, 'B1-A005', 1, 1, 250, 500, 200);

-- B1 层商场区 - 无障碍车位
INSERT INTO parking_spaces (zone_id, space_no, space_type, status, width_cm, length_cm, height_limit_cm)
VALUES 
(1, 'B1-D001', 3, 1, 350, 600, 200),
(1, 'B1-D002', 3, 1, 350, 600, 200);

-- B2 层办公区 - 小型车位
INSERT INTO parking_spaces (zone_id, space_no, space_type, status, width_cm, length_cm, height_limit_cm)
VALUES 
(2, 'B2-A001', 1, 1, 250, 500, 200),
(2, 'B2-A002', 1, 0, 250, 500, 200), -- 已占用
(2, 'B2-A003', 1, 0, 250, 500, 200), -- 已占用
(2, 'B2-A004', 1, 1, 250, 500, 200),
(2, 'B2-A005', 1, 1, 250, 500, 200);

-- 地面充电区 - 充电车位
INSERT INTO parking_spaces (zone_id, space_no, space_type, status, is_charging, width_cm, length_cm, height_limit_cm)
VALUES 
(3, 'G-C001', 4, 1, true, 280, 550, 220),
(3, 'G-C002', 4, 1, true, 280, 550, 220),
(3, 'G-C003', 4, 0, true, 280, 550, 220), -- 已占用
(3, 'G-C004', 4, 1, true, 280, 550, 220),
(3, 'G-C005', 4, 1, true, 280, 550, 220);

-- 4. 创建测试计费规则
-- 统一计费规则 - 小型车
INSERT INTO fee_rules (parking_lot_id, rule_type, rule_name, vehicle_type, hourly_rate, daily_max, free_minutes, start_time, end_time, is_active)
VALUES 
(1, 1, '全场统一计费 - 小型车', 1, 10.00, 100.00, 30, NULL, NULL, true);

-- 统一计费规则 - 大型车
INSERT INTO fee_rules (parking_lot_id, rule_type, rule_name, vehicle_type, hourly_rate, daily_max, free_minutes, start_time, end_time, is_active)
VALUES 
(1, 1, '全场统一计费 - 大型车', 2, 20.00, 200.00, 30, NULL, NULL, true);

-- 分区计费规则 - B1 层商场区
INSERT INTO fee_rules (parking_lot_id, rule_type, zone_id, rule_name, vehicle_type, hourly_rate, daily_max, free_minutes, start_time, end_time, is_active)
VALUES 
(1, 2, 1, 'B1 层商场区计费', 1, 12.00, 120.00, 15, NULL, NULL, true);

-- 分区计费规则 - B2 层办公区
INSERT INTO fee_rules (parking_lot_id, rule_type, zone_id, rule_name, vehicle_type, hourly_rate, daily_max, free_minutes, start_time, end_time, is_active)
VALUES 
(1, 2, 2, 'B2 层办公区计费', 1, 8.00, 80.00, 30, NULL, NULL, true);

-- 分区计费规则 - 地面充电区
INSERT INTO fee_rules (parking_lot_id, rule_type, zone_id, rule_name, vehicle_type, hourly_rate, daily_max, free_minutes, start_time, end_time, is_active)
VALUES 
(1, 2, 3, '充电区计费', 1, 15.00, 150.00, 30, NULL, NULL, true);

-- 5. 更新区域车位统计
UPDATE parking_zones SET 
    total_spaces = (SELECT COUNT(*) FROM parking_spaces WHERE zone_id = parking_zones.id),
    available_spaces = (SELECT COUNT(*) FROM parking_spaces WHERE zone_id = parking_zones.id AND status = 1)
WHERE id IN (1, 2, 3);

-- 6. 创建测试订单（可选）
-- 订单 1: 正在停车中
INSERT INTO orders (order_no, parking_lot_id, plate_no, vehicle_type, enter_time, initial_zone_id, current_zone_id, fee_mode, status, payment_status)
VALUES 
('ORD' || EXTRACT(EPOCH FROM NOW())::TEXT, 1, '京 A88888', 1, NOW() - INTERVAL '2 hours', 1, 1, 1, 0, 0);

-- 订单 2: 已完成订单
INSERT INTO orders (order_no, parking_lot_id, plate_no, vehicle_type, enter_time, exit_time, initial_zone_id, current_zone_id, fee_mode, total_amount, status, payment_status)
VALUES 
('ORD' || EXTRACT(EPOCH FROM (NOW() - INTERVAL '1 day'))::TEXT, 1, '京 B99999', 1, 
 NOW() - INTERVAL '25 hours', NOW() - INTERVAL '22 hours', 2, 2, 1, 35.00, 1, 1);

-- ========================================
-- 数据验证查询
-- ========================================

-- 查看车场信息
SELECT '车场信息' as category;
SELECT id, name, code, total_capacity, status FROM parking_lots;

-- 查看区域信息
SELECT '区域信息' as category;
SELECT id, name, code, zone_type, total_spaces, available_spaces, status FROM parking_zones ORDER BY id;

-- 查看车位统计
SELECT '车位统计' as category;
SELECT 
    z.name as zone_name,
    COUNT(*) as total_spaces,
    SUM(CASE WHEN ps.status = 1 THEN 1 ELSE 0 END) as available_spaces,
    SUM(CASE WHEN ps.status = 0 THEN 1 ELSE 0 END) as occupied_spaces
FROM parking_zones z
LEFT JOIN parking_spaces ps ON z.id = ps.zone_id
GROUP BY z.id, z.name
ORDER BY z.id;

-- 查看计费规则
SELECT '计费规则' as category;
SELECT id, rule_name, rule_type, vehicle_type, hourly_rate, daily_max, free_minutes, is_active 
FROM fee_rules 
ORDER BY rule_type, id;

-- 查看订单
SELECT '订单信息' as category;
SELECT order_no, plate_no, enter_time, exit_time, status, total_amount 
FROM orders 
ORDER BY created_at DESC 
LIMIT 5;

-- ========================================
-- 完成提示
-- ========================================
SELECT '✅ 测试数据初始化完成！' as message;
