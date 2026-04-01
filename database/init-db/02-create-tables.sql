-- =====================================================
-- 智慧停车管理平台 - 表结构初始化脚本
-- 文件：02-create-tables.sql
-- 说明：创建所有微服务的核心表结构
-- =====================================================

-- =====================================================
-- Part 1: 用户服务数据库 (pg_user)
-- =====================================================

\c pg_user

-- 用户表
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    avatar_url VARCHAR(255),
    wechat_openid VARCHAR(64),
    status SMALLINT DEFAULT 1,  -- 0:禁用 1:正常
    last_login_at TIMESTAMPTZ,
    last_login_ip VARCHAR(50),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_username ON users(username);
CREATE INDEX idx_wechat_openid ON users(wechat_openid);
CREATE INDEX idx_phone ON users(phone);

-- 角色表
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 插入默认角色
INSERT INTO roles (name, code, description) VALUES
('超级管理员', 'SUPER_ADMIN', '系统最高权限角色'),
('运营管理员', 'OPERATION_ADMIN', '运营管理权限'),
('财务人员', 'FINANCE', '财务管理权限'),
('车场管理员', 'PARKING_ADMIN', '车场管理权限');

-- 用户角色关联表
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id),
    role_id BIGINT NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_user_roles_user ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role_id);

-- 权限表
CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(100) UNIQUE NOT NULL,
    type SMALLINT NOT NULL,  -- 1:菜单 2:按钮 3:接口
    parent_id BIGINT,
    path VARCHAR(255),
    icon VARCHAR(50),
    sort_order INTEGER DEFAULT 0
);

-- 角色权限关联表
CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL REFERENCES roles(id),
    permission_id BIGINT NOT NULL REFERENCES permissions(id),
    PRIMARY KEY (role_id, permission_id)
);

CREATE INDEX idx_role_perms_role ON role_permissions(role_id);
CREATE INDEX idx_role_perms_perm ON role_permissions(permission_id);

-- 登录日志表
CREATE TABLE login_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    username VARCHAR(50),
    ip_address VARCHAR(50),
    user_agent TEXT,
    login_time TIMESTAMPTZ DEFAULT NOW(),
    login_result SMALLINT,  -- 0:失败 1:成功
    fail_reason VARCHAR(200)
);

CREATE INDEX idx_login_logs_user ON login_logs(user_id);
CREATE INDEX idx_login_logs_time ON login_logs(login_time);

-- =====================================================
-- Part 2: 车场服务数据库 (pg_parking)
-- =====================================================

\c pg_parking

-- 停车场表
CREATE TABLE parking_lots (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(32) UNIQUE NOT NULL,
    address VARCHAR(200),
    total_spaces INTEGER NOT NULL DEFAULT 0,
    type SMALLINT NOT NULL,  -- 1:封闭车场 2:路侧泊位
    status SMALLINT DEFAULT 1,  -- 0:停用 1:启用
    config JSONB,
    qr_code_url VARCHAR(255),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_parking_lots_code ON parking_lots(code);
CREATE INDEX idx_parking_lots_status ON parking_lots(status);

-- 停车区域表
CREATE TABLE parking_zones (
    id BIGSERIAL PRIMARY KEY,
    parking_lot_id BIGINT NOT NULL REFERENCES parking_lots(id),
    parent_zone_id BIGINT REFERENCES parking_zones(id),
    name VARCHAR(50) NOT NULL,
    code VARCHAR(32) UNIQUE NOT NULL,
    zone_type SMALLINT NOT NULL,  -- 1:一级区域 2:子区域
    zone_category SMALLINT,  -- 1:商场区 2:办公区 3:住宅区 4:充电区 5:其他
    floor_level INTEGER,  -- 楼层，-1 表示地下一层
    total_spaces INTEGER DEFAULT 0,
    available_spaces INTEGER DEFAULT 0,
    has_independent_exit BOOLEAN DEFAULT false,
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_zones_parking_lot ON parking_zones(parking_lot_id);
CREATE INDEX idx_zones_parent ON parking_zones(parent_zone_id);

-- 车位表
CREATE TABLE parking_spaces (
    id BIGSERIAL PRIMARY KEY,
    zone_id BIGINT NOT NULL REFERENCES parking_zones(id),
    space_no VARCHAR(20) NOT NULL,
    space_type SMALLINT,  -- 1:小型车 2:大型车 3:无障碍 4:充电车位
    status SMALLINT DEFAULT 0,  -- 0:空闲 1:占用 2:锁定
    width_cm INTEGER DEFAULT 250,
    length_cm INTEGER DEFAULT 500,
    height_limit_cm INTEGER DEFAULT 200,
    occupied_by_plate VARCHAR(20),
    occupied_since TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_spaces_zone ON parking_spaces(zone_id);
CREATE INDEX idx_spaces_status ON parking_spaces(status);
CREATE INDEX idx_spaces_space_no ON parking_spaces(space_no);

-- 车道表
CREATE TABLE parking_lanes (
    id BIGSERIAL PRIMARY KEY,
    parking_lot_id BIGINT NOT NULL REFERENCES parking_lots(id),
    name VARCHAR(50) NOT NULL,
    lane_type SMALLINT NOT NULL,  -- 1:入口 2:出口
    device_ip VARCHAR(50),
    device_port INTEGER DEFAULT 8000,
    device_serial VARCHAR(50),
    status SMALLINT DEFAULT 1,  -- 0:离线 1:在线
    last_heartbeat TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_lanes_parking_lot ON parking_lanes(parking_lot_id);

-- 收费规则表
CREATE TABLE fee_rules (
    id BIGSERIAL PRIMARY KEY,
    parking_lot_id BIGINT NOT NULL REFERENCES parking_lots(id),
    rule_type SMALLINT,  -- 1:按时长 2:按次 3:分时
    rule_name VARCHAR(100) NOT NULL,
    vehicle_type SMALLINT,  -- 1:小型车 2:大型车
    hourly_rate DECIMAL(10,2),
    daily_max DECIMAL(10,2),
    free_minutes INTEGER DEFAULT 30,
    start_time TIME,
    end_time TIME,
    is_active BOOLEAN DEFAULT true,
    effective_date DATE,
    expiry_date DATE,
    config JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_fee_rules_lot ON fee_rules(parking_lot_id);
CREATE INDEX idx_fee_rules_active ON fee_rules(is_active);

-- =====================================================
-- Part 3: 订单服务数据库 (pg_order)
-- =====================================================

\c pg_order

-- 临停订单表 (按月分区)
CREATE TABLE temp_orders (
    id BIGSERIAL,
    order_no VARCHAR(32) UNIQUE NOT NULL,
    plate_no VARCHAR(20) NOT NULL,
    parking_lot_id BIGINT NOT NULL,
    lane_id BIGINT,
    enter_time TIMESTAMPTZ NOT NULL,
    exit_time TIMESTAMPTZ,
    duration_seconds BIGINT,
    original_amount DECIMAL(10,2),
    discount_amount DECIMAL(10,2),
    payable_amount DECIMAL(10,2),
    paid_amount DECIMAL(10,2),
    status SMALLINT NOT NULL,  -- 0:已入场 1:待支付 2:已支付 3:已出场 4:欠费
    payment_channel VARCHAR(20),
    payment_time TIMESTAMPTZ,
    metadata JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    PRIMARY KEY (id, enter_time)
) PARTITION BY RANGE (enter_time);

-- 创建 2026 年各月分区
CREATE TABLE temp_orders_2026_03 PARTITION OF temp_orders
    FOR VALUES FROM ('2026-03-01') TO ('2026-04-01');
CREATE TABLE temp_orders_2026_04 PARTITION OF temp_orders
    FOR VALUES FROM ('2026-04-01') TO ('2026-05-01');
CREATE TABLE temp_orders_2026_05 PARTITION OF temp_orders
    FOR VALUES FROM ('2026-05-01') TO ('2026-06-01');
CREATE TABLE temp_orders_2026_06 PARTITION OF temp_orders
    FOR VALUES FROM ('2026-06-01') TO ('2026-07-01');
CREATE TABLE temp_orders_2026_07 PARTITION OF temp_orders
    FOR VALUES FROM ('2026-07-01') TO ('2026-08-01');
CREATE TABLE temp_orders_2026_08 PARTITION OF temp_orders
    FOR VALUES FROM ('2026-08-01') TO ('2026-09-01');

-- 索引
CREATE INDEX idx_temp_orders_plate ON temp_orders(plate_no);
CREATE INDEX idx_temp_orders_lot ON temp_orders(parking_lot_id);
CREATE INDEX idx_temp_orders_status ON temp_orders(status);
CREATE INDEX idx_temp_orders_enter ON temp_orders(enter_time);

-- 订单分段表 (记录车辆在不同区域的停车时段)
CREATE TABLE order_segments (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL REFERENCES temp_orders(order_no),
    zone_id BIGINT NOT NULL,
    enter_time TIMESTAMPTZ NOT NULL,
    exit_time TIMESTAMPTZ,
    duration_seconds BIGINT,
    fee_amount DECIMAL(10,2),
    status SMALLINT DEFAULT 0,  -- 0:进行中 1:已完成
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_segments_order ON order_segments(order_no);
CREATE INDEX idx_segments_zone ON order_segments(zone_id);
CREATE INDEX idx_segments_enter ON order_segments(enter_time);

-- =====================================================
-- Part 4: 支付服务数据库 (pg_payment)
-- =====================================================

\c pg_payment

-- 支付流水表
CREATE TABLE payment_transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_no VARCHAR(32) UNIQUE NOT NULL,
    order_no VARCHAR(32) NOT NULL,
    channel VARCHAR(20) NOT NULL,  -- wechat/alipay
    amount DECIMAL(10,2) NOT NULL,
    status SMALLINT NOT NULL,  -- 0:待支付 1:支付中 2:成功 3:失败
    pay_time TIMESTAMPTZ,
    notify_data JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_transaction_order ON payment_transactions(order_no);
CREATE INDEX idx_transaction_no ON payment_transactions(transaction_no);
CREATE INDEX idx_transaction_status ON payment_transactions(status);

-- 退款申请表
CREATE TABLE refund_requests (
    id BIGSERIAL PRIMARY KEY,
    refund_no VARCHAR(32) UNIQUE NOT NULL,
    transaction_no VARCHAR(32) NOT NULL,
    order_no VARCHAR(32) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    reason VARCHAR(200),
    status SMALLINT NOT NULL,  -- 0:待审核 1:已通过 2:已拒绝 3:已退款
    applicant_id BIGINT,
    approver_id BIGINT,
    apply_time TIMESTAMPTZ DEFAULT NOW(),
    approve_time TIMESTAMPTZ,
    refund_time TIMESTAMPTZ,
    refund_channel_order_no VARCHAR(64),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_refund_transaction ON refund_requests(transaction_no);
CREATE INDEX idx_refund_status ON refund_requests(status);

-- =====================================================
-- Part 5: 设备服务数据库 (pg_device)
-- =====================================================

\c pg_device

-- 设备信息表
CREATE TABLE devices (
    id BIGSERIAL PRIMARY KEY,
    device_serial VARCHAR(50) UNIQUE NOT NULL,
    device_type SMALLINT NOT NULL,  -- 1:道闸 2:相机 3:LED 屏
    parking_lot_id BIGINT REFERENCES parking_lots(id),
    lane_id BIGINT REFERENCES parking_lanes(id),
    ip_address VARCHAR(50),
    port INTEGER DEFAULT 8000,
    username VARCHAR(50),
    password_hash VARCHAR(255),
    firmware_version VARCHAR(20),
    status SMALLINT DEFAULT 0,  -- 0:离线 1:在线 2:故障
    last_heartbeat TIMESTAMPTZ,
    config JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_devices_serial ON devices(device_serial);
CREATE INDEX idx_devices_status ON devices(status);
CREATE INDEX idx_devices_lot ON devices(parking_lot_id);

-- 车牌识别日志表
CREATE TABLE plate_recognition_logs (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL,
    plate_no VARCHAR(20) NOT NULL,
    plate_color SMALLINT,  -- 0:未知 1:蓝 2:黄 3:绿
    recognition_time TIMESTAMPTZ NOT NULL,
    image_url VARCHAR(255),
    confidence SMALLINT,  -- 识别置信度 0-100
    direction SMALLINT,  -- 1:入场 2:出场
    raw_data JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_plate_logs_device ON plate_recognition_logs(device_id);
CREATE INDEX idx_plate_logs_plate ON plate_recognition_logs(plate_no);
CREATE INDEX idx_plate_logs_time ON plate_recognition_logs(recognition_time);

-- 设备操作日志表
CREATE TABLE device_operation_logs (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL,
    operation_type VARCHAR(50) NOT NULL,
    operator_id BIGINT,
    operator_name VARCHAR(50),
    request_params JSONB,
    response_result JSONB,
    status SMALLINT,
    error_message TEXT,
    operation_time TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_device_log_device ON device_operation_logs(device_id);
CREATE INDEX idx_device_log_time ON device_operation_logs(operation_time);

-- =====================================================
-- Part 6: 报表服务数据库 (pg_report)
-- =====================================================

\c pg_report

-- 日报统计表
CREATE TABLE daily_statistics (
    id BIGSERIAL PRIMARY KEY,
    parking_lot_id BIGINT NOT NULL,
    stat_date DATE NOT NULL,
    entering_count INTEGER DEFAULT 0,
    exiting_count INTEGER DEFAULT 0,
    revenue DECIMAL(10,2) DEFAULT 0,
    avg_duration_minutes INTEGER,
    peak_hour INTEGER,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (parking_lot_id, stat_date)
);

CREATE INDEX idx_daily_stats_lot ON daily_statistics(parking_lot_id);
CREATE INDEX idx_daily_stats_date ON daily_statistics(stat_date);

-- 物化视图：今日统计
CREATE MATERIALIZED VIEW mv_today_stats AS
SELECT 
    parking_lot_id,
    COUNT(*) FILTER (WHERE status = 0) as entering_count,
    COUNT(*) FILTER (WHERE status = 3) as exited_count,
    COUNT(*) FILTER (WHERE status IN (0,1,2)) as parked_count,
    SUM(paid_amount) FILTER (WHERE paid_time::date = CURRENT_DATE) as today_revenue
FROM temp_orders
WHERE enter_time::date >= CURRENT_DATE
GROUP BY parking_lot_id;

CREATE UNIQUE INDEX idx_mv_today_stats_lot ON mv_today_stats(parking_lot_id);

-- =====================================================
-- 脚本完成提示
-- =====================================================

SELECT '表结构创建成功！' AS message;
SELECT '数据库:' || current_database() AS current_db;
