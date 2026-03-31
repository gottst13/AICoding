-- V2.0__add_zone_support.sql
-- 增强型车场模型支持 - 数据库迁移脚本
-- 创建日期：2026-03-31
-- PostgreSQL 15+

-- =====================================================
-- 1. 修改停车场表：增加计费模式配置
-- =====================================================

ALTER TABLE parking_lots 
ADD COLUMN fee_mode SMALLINT DEFAULT 1,  -- 1:统一计费 2:分区计费
ADD COLUMN support_multi_zone BOOLEAN DEFAULT true,
ADD COLUMN allow_cross_zone BOOLEAN DEFAULT true;

COMMENT ON COLUMN parking_lots.fee_mode IS '计费模式：1-统一计费 2-分区计费';
COMMENT ON COLUMN parking_lots.support_multi_zone IS '是否支持多区域';
COMMENT ON COLUMN parking_lots.allow_cross_zone IS '是否允许跨区移动';


-- =====================================================
-- 2. 创建区域表（支持二级嵌套）
-- =====================================================

CREATE TABLE parking_zones (
    id BIGSERIAL PRIMARY KEY,
    parking_lot_id BIGINT NOT NULL REFERENCES parking_lots(id) ON DELETE CASCADE,
    parent_zone_id BIGINT REFERENCES parking_zones(id),
    
    -- 基本信息
    name VARCHAR(50) NOT NULL,  -- 如"B1 层"、"A 栋写字楼区"
    code VARCHAR(32) NOT NULL,  -- 如"B1-A"、"T1-OFFICE"
    zone_type SMALLINT NOT NULL,  -- 1:主区域 2:子区域
    zone_category SMALLINT,       -- 1:商场区 2:办公区 3:酒店区 4:住宅区 5:充电区
    
    -- 空间信息
    floor_level INTEGER,          -- 楼层（-1 表示地下一层）
    total_spaces INTEGER NOT NULL DEFAULT 0,
    available_spaces INTEGER DEFAULT 0,
    
    -- 出口配置
    has_independent_exit BOOLEAN DEFAULT false,  -- 是否有独立出口
    exit_lane_ids BIGINT[],                      -- 关联的出口车道 ID 列表
    
    -- 状态
    status SMALLINT DEFAULT 1,    -- 0:停用 1:启用 2:已满
    config JSONB,                 -- {"min_height": 2.0, "charging_spots": 10}
    
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    
    -- 约束
    UNIQUE(parking_lot_id, code),
    CHECK (parent_zone_id IS NULL OR parent_zone_id != id)
);

COMMENT ON TABLE parking_zones IS '停车场区域表 - 支持二级嵌套';
COMMENT ON COLUMN parking_zones.zone_type IS '1:主区域 2:子区域';
COMMENT ON COLUMN parking_zones.has_independent_exit IS '是否有独立出口，可直接离场';
COMMENT ON COLUMN parking_zones.exit_lane_ids IS '关联的出口车道 ID 列表';
COMMENT ON COLUMN parking_zones.config IS '区域特定配置（JSON 格式）';


-- =====================================================
-- 3. 创建索引
-- =====================================================

CREATE INDEX idx_zones_lot_id ON parking_zones(parking_lot_id);
CREATE INDEX idx_zones_parent_id ON parking_zones(parent_zone_id);
CREATE INDEX idx_zones_type ON parking_zones(zone_type);
CREATE INDEX idx_zones_category ON parking_zones(zone_category);
CREATE INDEX idx_zones_status ON parking_zones(status);


-- =====================================================
-- 4. 创建防止超过二级嵌套的触发器
-- =====================================================

CREATE OR REPLACE FUNCTION check_zone_hierarchy_depth()
RETURNS TRIGGER AS $$
DECLARE
    depth INTEGER;
BEGIN
    IF NEW.parent_zone_id IS NOT NULL THEN
        WITH RECURSIVE zone_path AS (
            SELECT id, parent_zone_id, 1 as level
            FROM parking_zones
            WHERE id = NEW.parent_zone_id
            UNION ALL
            SELECT z.id, z.parent_zone_id, zp.level + 1
            FROM parking_zones z
            INNER JOIN zone_path zp ON z.id = zp.parent_zone_id
        )
        SELECT MAX(level) INTO depth FROM zone_path;
        
        IF depth >= 2 THEN
            RAISE EXCEPTION 'Zone hierarchy depth cannot exceed 2 levels, current depth: %', depth;
        END IF;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION check_zone_hierarchy_depth() IS '检查区域层级深度，防止超过 2 级嵌套';

CREATE TRIGGER trg_check_zone_depth
BEFORE INSERT OR UPDATE ON parking_zones
FOR EACH ROW EXECUTE FUNCTION check_zone_hierarchy_depth();


-- =====================================================
-- 5. 创建自动更新可用车位数的触发器（为后续车位表准备）
-- =====================================================

CREATE OR REPLACE FUNCTION update_zone_available_spaces()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' AND NEW.status = 1 THEN
        UPDATE parking_zones SET available_spaces = available_spaces + 1 
        WHERE id = NEW.zone_id;
    ELSIF TG_OP = 'DELETE' AND OLD.status = 1 THEN
        UPDATE parking_zones SET available_spaces = available_spaces - 1 
        WHERE id = OLD.zone_id;
    ELSIF TG_OP = 'UPDATE' THEN
        IF OLD.status != 1 AND NEW.status = 1 THEN
            UPDATE parking_zones SET available_spaces = available_spaces + 1 
            WHERE id = NEW.zone_id;
        ELSIF OLD.status = 1 AND NEW.status != 1 THEN
            UPDATE parking_zones SET available_spaces = available_spaces - 1 
            WHERE id = NEW.zone_id;
        END IF;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION update_zone_available_spaces() IS '自动更新区域可用车位数';

-- 注意：此触发器将在车位表创建后生效
CREATE TRIGGER trg_update_zone_spaces
AFTER INSERT OR DELETE OR UPDATE ON parking_spaces
FOR EACH ROW EXECUTE FUNCTION update_zone_available_spaces();


-- =====================================================
-- 6. 创建车位表
-- =====================================================

CREATE TABLE parking_spaces (
    id BIGSERIAL PRIMARY KEY,
    zone_id BIGINT NOT NULL REFERENCES parking_zones(id) ON DELETE CASCADE,
    space_no VARCHAR(20) NOT NULL,      -- 车位编号，如"A-001"、"B1-023"
    space_type SMALLINT NOT NULL DEFAULT 1,  -- 1:小型车 2:大型车 3:无障碍 4:充电车位
    location_info JSONB,                -- {"x": 10, "y": 20, "direction": "N"}
    
    -- 状态
    status SMALLINT NOT NULL DEFAULT 1, -- 0:占用 1:空闲 2:锁定 3:预约
    occupied_by_plate VARCHAR(20),      -- 当前停放车牌号
    occupied_since TIMESTAMPTZ,         -- 占用开始时间
    
    -- 属性
    is_charging BOOLEAN DEFAULT false,
    charging_device_id BIGINT,
    width_cm INTEGER,                   -- 车位宽度（厘米）
    length_cm INTEGER,                  -- 车位长度
    height_limit_cm INTEGER,            -- 限高
    
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    
    -- 约束
    UNIQUE(zone_id, space_no)
);

COMMENT ON TABLE parking_spaces IS '车位表';
COMMENT ON COLUMN parking_spaces.space_type IS '1:小型车 2:大型车 3:无障碍 4:充电车位';
COMMENT ON COLUMN parking_spaces.status IS '0:占用 1:空闲 2:锁定 3:预约';
COMMENT ON COLUMN parking_spaces.location_info IS '车位位置信息（JSON 格式）';


-- =====================================================
-- 7. 创建车位表索引
-- =====================================================

CREATE INDEX idx_spaces_zone_id ON parking_spaces(zone_id);
CREATE INDEX idx_spaces_status ON parking_spaces(status);
CREATE INDEX idx_spaces_plate ON parking_spaces(occupied_by_plate) 
    WHERE occupied_by_plate IS NOT NULL;


-- =====================================================
-- 8. 修改订单表：增加区域相关字段
-- =====================================================

ALTER TABLE orders
ADD COLUMN parking_lot_id BIGINT REFERENCES parking_lots(id),
ADD COLUMN initial_zone_id BIGINT REFERENCES parking_zones(id),
ADD COLUMN current_zone_id BIGINT REFERENCES parking_zones(id),
ADD COLUMN fee_mode SMALLINT NOT NULL DEFAULT 1,
ADD COLUMN fee_rule_snapshot JSONB,
ADD COLUMN has_cross_zone BOOLEAN DEFAULT false,
ADD COLUMN cross_zone_count INTEGER DEFAULT 0;

COMMENT ON COLUMN orders.parking_lot_id IS '停车场 ID';
COMMENT ON COLUMN orders.initial_zone_id IS '首次进入区域 ID';
COMMENT ON COLUMN orders.current_zone_id IS '当前所在区域 ID';
COMMENT ON COLUMN orders.fee_mode IS '计费模式：1-统一计费 2-分区计费';
COMMENT ON COLUMN orders.fee_rule_snapshot IS '计费规则快照（JSON 格式）';
COMMENT ON COLUMN orders.has_cross_zone IS '是否有跨区移动';
COMMENT ON COLUMN orders.cross_zone_count IS '跨区次数';

-- 创建订单表索引
CREATE INDEX idx_orders_zone ON orders(initial_zone_id, current_zone_id);
CREATE INDEX idx_orders_parking_lot ON orders(parking_lot_id);


-- =====================================================
-- 9. 创建订单分段明细表（用于分区计费）
-- =====================================================

CREATE TABLE order_segments (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL REFERENCES orders(order_no) ON DELETE CASCADE,
    segment_no INTEGER NOT NULL,         -- 分段序号，从 1 开始
    
    -- 区域信息
    zone_id BIGINT NOT NULL REFERENCES parking_zones(id),
    zone_name VARCHAR(50) NOT NULL,      -- 冗余存储，避免关联查询
    
    -- 时间信息
    enter_time TIMESTAMPTZ NOT NULL,
    exit_time TIMESTAMPTZ NOT NULL,
    duration_minutes INTEGER NOT NULL,
    
    -- 计费信息
    fee_rule_id BIGINT REFERENCES fee_rules(id),
    fee_rule_snapshot JSONB NOT NULL,    -- 计费规则快照
    fee_amount DECIMAL(10,2) NOT NULL,
    
    -- 备注
    remark VARCHAR(200),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

COMMENT ON TABLE order_segments IS '订单分段明细表';
COMMENT ON COLUMN order_segments.segment_no IS '分段序号，从 1 开始';
COMMENT ON COLUMN order_segments.fee_rule_snapshot IS '计费规则快照（JSON 格式）';

-- 创建订单分段表索引
CREATE INDEX idx_order_segments_order_no ON order_segments(order_no);
CREATE INDEX idx_order_segments_zone_id ON order_segments(zone_id);
CREATE UNIQUE INDEX idx_order_segments_unique ON order_segments(order_no, segment_no);

-- 添加约束
ALTER TABLE order_segments ADD CONSTRAINT chk_segment_no CHECK (segment_no > 0);


-- =====================================================
-- 迁移完成验证
-- =====================================================

-- 验证查询示例：
-- SELECT column_name, data_type, column_default 
-- FROM information_schema.columns 
-- WHERE table_name = 'parking_zones' 
-- ORDER BY ordinal_position;

-- SELECT tgname, tgtype 
-- FROM pg_trigger 
-- WHERE tgrelid = 'parking_zones'::regclass;
