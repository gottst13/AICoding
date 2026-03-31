# 智慧停车管理平台 - 增强型车场模型设计

**日期**: 2026-03-31  
**版本**: v2.0  
**状态**: 已批准  

---

## 1. 概述

### 1.1 需求背景

原 P0 设计中的车场模型较为简单，仅支持单一停车场结构，无法满足实际业务中复杂的停车场管理需求：
- 多层地下停车场（如商场 B1-B3 层）
- 园区型停车场（如科技园区 A/B/C/D 栋）
- 混合功能停车场（商场 + 写字楼 + 酒店）
- 不同区域独立计费、独立出口

### 1.2 需求目标

构建增强型车场模型，支持：
1. **多级区域嵌套**: 停车场 → 主区域 → 子区域（二级结构）
2. **灵活计费模式**: 统一计费 / 分区计费可配置
3. **独立出口管理**: 子区域可直接离场
4. **跨区移动追踪**: 车辆在场内不同区域间移动的记录与计费
5. **精细化车位管理**: 车位级别的状态监控与分配

### 1.3 适用范围

本设计适用于智慧停车管理平台 P0+ 阶段，向后兼容现有车场模型。

---

## 2. 系统架构影响

### 2.1 服务边界调整

#### parking-service（增强）
**新增职责**:
- 区域管理（主区域、子区域的增删改查）
- 车位管理（车位的分配、状态监控）
- 计费规则引擎（支持区域级别规则配置）
- 车辆移动追踪（跨区移动记录）

**数据表变更**:
- `parking_lots`: 新增计费模式字段
- `parking_zones`: 新增区域表
- `parking_spaces`: 新增车位表
- `fee_rules`: 增加区域关联字段

#### order-service（增强）
**新增职责**:
- 分段订单管理（支持一单多段）
- 双模式计费计算（统一计费/分区计费）
- 跨区移动处理（自动检测与记录）

**数据表变更**:
- `orders`: 增加区域相关字段
- `order_segments`: 新增订单分段表
- `vehicle_movements`: 新增车辆移动轨迹表

### 2.2 服务依赖关系

```
gateway
├── parking-service
│   ├── 区域管理 API
│   ├── 车位管理 API
│   └── 计费规则 API
└── order-service
    ├── 订单创建（含区域信息）
    ├── 计费计算（调用 FeeStrategy）
    └── 车辆移动记录
```

---

## 3. 数据模型设计

### 3.1 核心实体关系图

```mermaid
erDiagram
    PARKING_LOTS ||--o{ PARKING_ZONES : contains
    PARKING_ZONES ||--o{ PARKING_ZONES : parent-child
    PARKING_ZONES ||--o{ PARKING_SPACES : contains
    PARKING_LOTS ||--o{ ORDERS : parks
    PARKING_ZONES ||--o{ ORDERS : initial_zone
    PARKING_ZONES ||--o{ ORDERS : current_zone
    ORDERS ||--o{ ORDER_SEGMENTS : consists_of
    ORDERS ||--o{ VEHICLE_MOVEMENTS : tracks
    PARKING_ZONES ||--o{ VEHICLE_MOVEMENTS : from/to
    FEE_RULES }o--|| PARKING_ZONES : applies_to
```

### 3.2 数据表详细设计

#### 3.2.1 停车场表（parking_lots）- 增强

```sql
CREATE TABLE parking_lots (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(32) UNIQUE NOT NULL,
    address VARCHAR(200),
    
    -- 新增字段：计费模式配置
    fee_mode SMALLINT NOT NULL DEFAULT 1,  -- 1:统一计费 2:分区计费
    support_multi_zone BOOLEAN DEFAULT true,  -- 是否支持多区域
    allow_cross_zone BOOLEAN DEFAULT true,    -- 是否允许跨区移动
    
    total_spaces INTEGER NOT NULL DEFAULT 0,
    type SMALLINT NOT NULL,  -- 1:封闭车场 2:路侧泊位 3:混合车场
    status SMALLINT DEFAULT 1,  -- 0:停用 1:启用 2:已满
    
    config JSONB,  -- {default_fee_mode: 1, max_zones: 10, ...}
    qr_code_url VARCHAR(255),
    
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 索引优化
CREATE INDEX idx_parking_lots_code ON parking_lots(code);
CREATE INDEX idx_parking_lots_type_status ON parking_lots(type, status);

-- 注释
COMMENT ON COLUMN parking_lots.fee_mode IS '计费模式：1-统一计费（全场统一费率）2-分区计费（各区域独立费率）';
COMMENT ON COLUMN parking_lots.support_multi_zone IS '是否支持多区域：true-支持区域划分 false-简单车场';
COMMENT ON COLUMN parking_lots.allow_cross_zone IS '是否允许跨区移动：true-允许场内换区 false-禁止换区';
```

#### 3.2.2 区域表（parking_zones）- 新增

```sql
CREATE TABLE parking_zones (
    id BIGSERIAL PRIMARY KEY,
    parking_lot_id BIGINT NOT NULL REFERENCES parking_lots(id) ON DELETE CASCADE,
    parent_zone_id BIGINT REFERENCES parking_zones(id),  -- 父区域 ID（支持二级嵌套）
    
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

-- 索引
CREATE INDEX idx_zones_lot_id ON parking_zones(parking_lot_id);
CREATE INDEX idx_zones_parent_id ON parking_zones(parent_zone_id);
CREATE INDEX idx_zones_type ON parking_zones(zone_type);
CREATE INDEX idx_zones_category ON parking_zones(zone_category);

-- 触发器：防止超过二级嵌套
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
            RAISE EXCEPTION 'Zone hierarchy depth cannot exceed 2 levels';
        END IF;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_zone_depth
BEFORE INSERT OR UPDATE ON parking_zones
FOR EACH ROW EXECUTE FUNCTION check_zone_hierarchy_depth();
```

#### 3.2.3 车位表（parking_spaces）- 新增

```sql
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

-- 索引
CREATE INDEX idx_spaces_zone_id ON parking_spaces(zone_id);
CREATE INDEX idx_spaces_status ON parking_spaces(status);
CREATE INDEX idx_spaces_plate ON parking_spaces(occupied_by_plate) 
    WHERE occupied_by_plate IS NOT NULL;

-- 触发器：自动更新区域可用车位数
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

CREATE TRIGGER trg_update_zone_spaces
AFTER INSERT OR DELETE OR UPDATE ON parking_spaces
FOR EACH ROW EXECUTE FUNCTION update_zone_available_spaces();
```

#### 3.2.4 收费规则表（fee_rules）- 增强

```sql
CREATE TABLE fee_rules (
    id BIGSERIAL PRIMARY KEY,
    parking_lot_id BIGINT NOT NULL REFERENCES parking_lots(id) ON DELETE CASCADE,
    zone_id BIGINT REFERENCES parking_zones(id),  -- NULL 表示全场规则，有值表示仅适用该区域
    
    -- 适用对象
    vehicle_type SMALLINT,              -- 1:小型车 2:大型车 NULL:所有车型
    user_type SMALLINT,                 -- 1:临时车 2:月租车 3:VIP NULL:所有用户
    
    -- 规则类型
    rule_type SMALLINT NOT NULL,        -- 1:按时长 2:按次 3:分时 4:包段
    rule_name VARCHAR(50) NOT NULL,     -- 如"商场购物优惠"、"写字楼标准价"
    
    -- 计费参数
    first_hour_amount DECIMAL(10,2),
    additional_hour_amount DECIMAL(10,2),
    additional_minute_amount DECIMAL(10,2),  -- 不足 1 小时按分钟计
    daily_max_amount DECIMAL(10,2),
    monthly_amount DECIMAL(10,2),
    
    -- 免费政策
    free_duration_minutes INTEGER DEFAULT 15,
    grace_period_minutes INTEGER DEFAULT 15,   -- 缴费后宽限期
    
    -- 分时计费参数
    time_range_start TIME,              -- 分时计费开始时间（如 08:00）
    time_range_end TIME,                -- 分时计费结束时间（如 20:00）
    peak_hour_amount DECIMAL(10,2),     -- 高峰时段费率
    off_peak_hour_amount DECIMAL(10,2), -- 平峰时段费率
    
    -- 有效期
    effective_date DATE NOT NULL,
    expiry_date DATE,
    
    -- 优先级（当多条规则匹配时使用）
    priority INTEGER DEFAULT 100,       -- 数字越小优先级越高
    
    -- 扩展配置
    config JSONB,                       -- {"shopping_discount": 0.8, "validate_required": true}
    
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    
    -- 约束
    CHECK (priority >= 0 AND priority <= 1000)
);

-- 索引
CREATE INDEX idx_fee_rules_lot_id ON fee_rules(parking_lot_id);
CREATE INDEX idx_fee_rules_zone_id ON fee_rules(zone_id);
CREATE INDEX idx_fee_rules_type ON fee_rules(rule_type, vehicle_type);
CREATE INDEX idx_fee_rules_validity ON fee_rules(effective_date, expiry_date);

-- 创建唯一索引：同一时间段内同一区域的同类型规则只能有一条生效
CREATE UNIQUE INDEX idx_fee_rules_unique_active 
ON fee_rules(parking_lot_id, COALESCE(zone_id, -1), vehicle_type, user_type, rule_type)
WHERE expiry_date IS NULL OR expiry_date >= CURRENT_DATE;
```

#### 3.2.5 订单表（orders）- 增强

```sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(32) UNIQUE NOT NULL,
    
    -- 车辆信息
    plate_no VARCHAR(20) NOT NULL,
    vehicle_type SMALLINT NOT NULL DEFAULT 1,
    plate_color SMALLINT DEFAULT 1,     -- 1:蓝牌 2:绿牌 3:黄牌
    
    -- 停车场信息
    parking_lot_id BIGINT NOT NULL REFERENCES parking_lots(id),
    initial_zone_id BIGINT NOT NULL REFERENCES parking_zones(id),  -- 首次进入区域
    current_zone_id BIGINT NOT NULL REFERENCES parking_zones(id),  -- 当前所在区域
    enter_lane_id BIGINT,
    exit_lane_id BIGINT,
    
    -- 时间信息
    enter_time TIMESTAMPTZ NOT NULL,
    expected_exit_time TIMESTAMPTZ,
    actual_exit_time TIMESTAMPTZ,
    payment_time TIMESTAMPTZ,
    
    -- 计费模式（快照）
    fee_mode SMALLINT NOT NULL DEFAULT 1,  -- 1:统一计费 2:分区计费
    fee_rule_snapshot JSONB,               -- 下单时的计费规则快照
    
    -- 费用信息
    base_fee_amount DECIMAL(10,2),         -- 基础停车费
    additional_fees JSONB,                 -- 附加费用 [{type: "charging", amount: 10}]
    discount_amount DECIMAL(10,2) DEFAULT 0,  -- 优惠金额
    total_amount DECIMAL(10,2) NOT NULL,
    paid_amount DECIMAL(10,2) DEFAULT 0,
    
    -- 订单状态
    status SMALLINT NOT NULL,              -- 1:进行中 2:已结束待支付 3:已完成 4:已取消
    payment_status SMALLINT DEFAULT 0,     -- 0:未支付 1:已支付 2:部分退款 3:全额退款
    
    -- 跨区信息
    has_cross_zone BOOLEAN DEFAULT false,
    cross_zone_count INTEGER DEFAULT 0,
    
    -- 扩展信息
    ext_data JSONB,                        -- {shopping_mall_no: "A123", validated: true}
    
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    
    -- 索引优化
    CONSTRAINT chk_status CHECK (status IN (1, 2, 3, 4)),
    CONSTRAINT chk_payment_status CHECK (payment_status IN (0, 1, 2, 3))
);

-- 索引
CREATE INDEX idx_orders_plate_no ON orders(plate_no);
CREATE INDEX idx_orders_parking_lot ON orders(parking_lot_id);
CREATE INDEX idx_orders_enter_time ON orders(enter_time);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_payment_status ON orders(payment_status);
CREATE INDEX idx_orders_current_zone ON orders(current_zone_id);

-- 分区表设计（按月分区）
CREATE TABLE orders_history (
    LIKE orders INCLUDING ALL
) PARTITION BY RANGE (enter_time);
```

#### 3.2.6 订单分段明细表（order_segments）- 新增

```sql
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

-- 索引
CREATE INDEX idx_order_segments_order_no ON order_segments(order_no);
CREATE INDEX idx_order_segments_zone_id ON order_segments(zone_id);
CREATE UNIQUE INDEX idx_order_segments_unique ON order_segments(order_no, segment_no);

-- 约束
CHECK (segment_no > 0)
```

#### 3.2.7 车辆移动轨迹表（vehicle_movements）- 新增

```sql
CREATE TABLE vehicle_movements (
    id BIGSERIAL PRIMARY KEY,
    movement_no VARCHAR(32) UNIQUE NOT NULL,
    
    -- 关联信息
    order_no VARCHAR(32) NOT NULL REFERENCES orders(order_no),
    plate_no VARCHAR(20) NOT NULL,
    
    -- 移动信息
    from_zone_id BIGINT REFERENCES parking_zones(id),  -- NULL 表示入场
    to_zone_id BIGINT NOT NULL REFERENCES parking_zones(id),
    movement_time TIMESTAMPTZ NOT NULL,
    
    -- 检测设备
    detected_by_device_id BIGINT,
    detected_by_lane_id BIGINT,
    snapshot_image_url VARCHAR(255),
    snapshot_video_url VARCHAR(255),
    
    -- 移动类型
    movement_type SMALLINT NOT NULL,     -- 1:入场 2:跨区移动 3:出场
    movement_reason SMALLINT,            -- 1:主动移动 2:调度引导 3:误入纠正
    
    -- 置信度
    confidence DECIMAL(3,2) DEFAULT 1.0, -- 识别置信度 0.00-1.00
    
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 索引
CREATE INDEX idx_movements_order_no ON vehicle_movements(order_no);
CREATE INDEX idx_movements_plate_time ON vehicle_movements(plate_no, movement_time);
CREATE INDEX idx_movements_zone ON vehicle_movements(to_zone_id, movement_time);

-- 触发器：自动更新订单的跨区信息
CREATE OR REPLACE FUNCTION update_order_cross_zone_info()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.movement_type = 2 THEN  -- 跨区移动
        UPDATE orders 
        SET has_cross_zone = true,
            cross_zone_count = cross_zone_count + 1,
            current_zone_id = NEW.to_zone_id,
            updated_at = NOW()
        WHERE order_no = NEW.order_no;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_order_movement
AFTER INSERT ON vehicle_movements
FOR EACH ROW EXECUTE FUNCTION update_order_cross_zone_info();
```

---

## 4. 业务逻辑设计

### 4.1 计费引擎策略模式

#### 4.1.1 策略接口定义

```java
package com.smartparking.order.service.fee.strategy;

/**
 * 计费策略接口
 */
public interface FeeStrategy {
    /**
     * 计算停车费用
     * @param context 停车上下文
     * @return 应收费用
     */
    FeeResult calculate(ParkingContext context);
}
```

#### 4.1.2 统一计费策略实现

```java
@Component
@FeeStrategyType("UNIFIED")
public class UnifiedFeeStrategy implements FeeStrategy {
    
    @Autowired
    private FeeRuleRepository feeRuleRepository;
    
    @Override
    public FeeResult calculate(ParkingContext context) {
        // 计算总时长
        Duration totalDuration = Duration.between(
            context.getEnterTime(),
            context.getExitTime() != null ? context.getExitTime() : LocalDateTime.now()
        );
        
        // 获取车场统一计费规则
        FeeRule rule = feeRuleRepository.findUnifiedRule(
            context.getParkingLotId(),
            context.getVehicleType(),
            LocalDateTime.now()
        );
        
        if (rule == null) {
            throw new BusinessException("FEE_001", "未找到适用的计费规则");
        }
        
        // 计算费用
        BigDecimal amount = calculateDurationFee(totalDuration, rule);
        
        // 构建结果
        FeeDetail detail = FeeDetail.builder()
            .zoneId(null)
            .zoneName("全场通用")
            .duration(totalDuration)
            .amount(amount)
            .ruleDescription(rule.getRuleName())
            .build();
        
        return new FeeResult(amount, Collections.singletonList(detail), Map.of());
    }
    
    private BigDecimal calculateDurationFee(Duration duration, FeeRule rule) {
        long minutes = duration.toMinutes();
        
        // 检查免费时长
        if (minutes <= rule.getFreeDurationMinutes()) {
            return BigDecimal.ZERO;
        }
        
        // 扣除免费时长
        minutes -= rule.getFreeDurationMinutes();
        
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        
        BigDecimal amount = BigDecimal.ZERO;
        
        // 首小时费用
        if (hours > 0) {
            amount = amount.add(rule.getFirstHourAmount());
            hours--;
        }
        
        // 续时费用
        if (hours > 0) {
            amount = amount.add(
                rule.getAdditionalHourAmount().multiply(BigDecimal.valueOf(hours))
            );
        }
        
        // 剩余分钟费用
        if (remainingMinutes > 0 && rule.getAdditionalMinuteAmount() != null) {
            amount = amount.add(
                rule.getAdditionalMinuteAmount().multiply(BigDecimal.valueOf(remainingMinutes))
            );
        }
        
        // 封顶价保护
        if (rule.getDailyMaxAmount() != null) {
            amount = amount.min(rule.getDailyMaxAmount());
        }
        
        return amount.max(BigDecimal.ZERO);
    }
}
```

#### 4.1.3 分区计费策略实现

```java
@Component
@FeeStrategyType("ZONED")
public class ZonedFeeStrategy implements FeeStrategy {
    
    @Autowired
    private OrderSegmentRepository segmentRepository;
    
    @Autowired
    private FeeRuleRepository feeRuleRepository;
    
    @Override
    public FeeResult calculate(ParkingContext context) {
        // 获取所有分段
        List<OrderSegment> segments = segmentRepository.findByOrderNo(
            context.getOrderNo(),
            Sort.by(Sort.Order.asc("segmentNo"))
        );
        
        if (segments.isEmpty()) {
            throw new BusinessException("FEE_003", "订单没有分段信息");
        }
        
        List<FeeDetail> feeDetails = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (OrderSegment segment : segments) {
            // 计算该分段的费用
            BigDecimal segmentFee = calculateSegmentFee(segment);
            totalAmount = totalAmount.add(segmentFee);
            
            // 构建费用明细
            FeeDetail detail = FeeDetail.builder()
                .zoneId(segment.getZoneId())
                .zoneName(segment.getZoneName())
                .duration(Duration.ofMinutes(segment.getDurationMinutes()))
                .amount(segmentFee)
                .ruleDescription(segment.getFeeRuleSnapshot().get("ruleName").asText())
                .build();
            
            feeDetails.add(detail);
        }
        
        Map<String, Object> metadata = Map.of(
            "segmentCount", segments.size(),
            "hasCrossZone", segments.size() > 1
        );
        
        return new FeeResult(totalAmount, feeDetails, metadata);
    }
    
    private BigDecimal calculateSegmentFee(OrderSegment segment) {
        Duration duration = Duration.ofMinutes(segment.getDurationMinutes());
        FeeRule rule = parseFeeRule(segment.getFeeRuleSnapshot());
        
        // 复用统一计费的计算逻辑
        return calculateDurationFee(duration, rule);
    }
}
```

#### 4.1.4 计费策略工厂

```java
@Component
public class FeeStrategyFactory {
    
    private final Map<String, FeeStrategy> strategies = new ConcurrentHashMap<>();
    
    public FeeStrategyFactory(List<FeeStrategy> strategyList) {
        // 注册所有策略
        for (FeeStrategy strategy : strategyList) {
            FeeStrategyType annotation = strategy.getClass()
                .getAnnotation(FeeStrategyType.class);
            if (annotation != null) {
                strategies.put(annotation.value(), strategy);
            }
        }
    }
    
    /**
     * 根据计费模式获取策略
     */
    public FeeStrategy getStrategy(FeeMode feeMode) {
        switch (feeMode) {
            case UNIFIED:
                return strategies.get("UNIFIED");
            case ZONED:
                return strategies.get("ZONED");
            default:
                throw new IllegalArgumentException("未知的计费模式：" + feeMode);
        }
    }
    
    /**
     * 根据停车场配置获取策略
     */
    public FeeStrategy getStrategy(ParkingLot parkingLot) {
        FeeMode mode = FeeMode.fromCode(parkingLot.getFeeMode());
        return getStrategy(mode);
    }
}
```

### 4.2 车辆跨区移动处理流程

```java
@Service
public class VehicleMovementService {
    
    @Autowired
    private MovementRepository movementRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    /**
     * 记录车辆跨区移动
     */
    @Transactional
    public MovementVO recordMovement(RecordMovementRequest request) {
        // 1. 验证订单状态
        Order order = orderRepository.findByOrderNo(request.getOrderNo());
        if (order == null || order.getStatus() != OrderStatus.PARKING) {
            throw new BusinessException("ORDER_001", "订单不存在或已结束");
        }
        
        // 2. 验证区域有效性
        Zone fromZone = zoneRepository.findById(request.getFromZoneId());
        Zone toZone = zoneRepository.findById(request.getToZoneId());
        
        if (!fromZone.getParkingLotId().equals(order.getParkingLotId()) ||
            !toZone.getParkingLotId().equals(order.getParkingLotId())) {
            throw new BusinessException("ZONE_002", "区域不属于同一停车场");
        }
        
        // 3. 结算上一分段订单
        if (order.getFeeMode() == FeeMode.ZONED) {
            OrderSegment lastSegment = createOrderSegment(
                order.getOrderNo(),
                order.getCurrentZoneId(),
                request.getFromZoneId(),
                request.getMovementTime()
            );
            feeCalculationService.calculateSegment(lastSegment);
        }
        
        // 4. 创建移动记录
        VehicleMovement movement = VehicleMovement.builder()
            .movementNo(generateMovementNo())
            .orderNo(request.getOrderNo())
            .plateNo(order.getPlateNo())
            .fromZoneId(request.getFromZoneId())
            .toZoneId(request.getToZoneId())
            .movementTime(request.getMovementTime())
            .detectedByDeviceId(request.getDeviceId())
            .snapshotImageUrl(request.getSnapshotUrl())
            .movementType(MovementType.CROSS_ZONE)
            .confidence(request.getConfidence())
            .build();
        
        movementRepository.save(movement);
        
        // 5. 触发器会自动更新订单的 current_zone_id 和 cross_zone_count
        
        return convertToVO(movement);
    }
    
    /**
     * 创建订单分段
     */
    private OrderSegment createOrderSegment(
        String orderNo, 
        Long zoneId, 
        Long exitZoneId, 
        LocalDateTime exitTime
    ) {
        // 查找该分段的进入时间（上一个分段的退出时间，或订单的进入时间）
        OrderSegment lastSegment = segmentRepository.findLastByOrderNo(orderNo);
        LocalDateTime enterTime = lastSegment != null ? 
            lastSegment.getExitTime() : 
            orderRepository.findByOrderNo(orderNo).getEnterTime();
        
        OrderSegment segment = OrderSegment.builder()
            .orderNo(orderNo)
            .segmentNo(segmentRepository.countByOrderNo(orderNo) + 1)
            .zoneId(zoneId)
            .zoneName(getZoneName(zoneId))
            .enterTime(enterTime)
            .exitTime(exitTime)
            .durationMinutes((int) Duration.between(enterTime, exitTime).toMinutes())
            .build();
        
        return segmentRepository.save(segment);
    }
}
```

---

## 5. API 接口设计

### 5.1 区域管理接口

```java
@RestController
@RequestMapping("/api/v1/parking-lots/{lotId}/zones")
public class ParkingZoneController {
    
    /**
     * 获取车场区域树形结构
     */
    @GetMapping("/tree")
    public ApiResponse<List<ZoneTreeVO>> getZoneTree(
        @PathVariable Long lotId
    ) {
        List<ZoneTreeVO> tree = zoneService.getZoneTree(lotId);
        return ApiResponse.success(tree);
    }
    
    /**
     * 创建区域
     */
    @PostMapping
    public ApiResponse<ZoneVO> createZone(
        @PathVariable Long lotId,
        @Valid @RequestBody CreateZoneRequest request
    ) {
        ZoneVO zone = zoneService.createZone(lotId, request);
        return ApiResponse.success(zone);
    }
    
    /**
     * 更新区域
     */
    @PutMapping("/{id}")
    public ApiResponse<ZoneVO> updateZone(
        @PathVariable Long id,
        @Valid @RequestBody UpdateZoneRequest request
    ) {
        ZoneVO zone = zoneService.updateZone(id, request);
        return ApiResponse.success(zone);
    }
    
    /**
     * 删除区域（级联删除子区域和车位）
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteZone(@PathVariable Long id) {
        zoneService.deleteZone(id);
        return ApiResponse.success();
    }
    
    /**
     * 获取区域车位列表
     */
    @GetMapping("/{id}/spaces")
    public ApiResponse<Page<SpaceVO>> getZoneSpaces(
        @PathVariable Long id,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Page<SpaceVO> spaces = spaceService.getZoneSpaces(id, page, size);
        return ApiResponse.success(spaces);
    }
    
    /**
     * 批量创建车位
     */
    @PostMapping("/{id}/spaces/batch")
    public ApiResponse<List<SpaceVO>> batchCreateSpaces(
        @PathVariable Long id,
        @Valid @RequestBody BatchCreateSpaceRequest request
    ) {
        List<SpaceVO> spaces = spaceService.batchCreateSpaces(id, request);
        return ApiResponse.success(spaces);
    }
}
```

### 5.2 车辆移动接口

```java
@RestController
@RequestMapping("/api/v1/vehicle-movements")
public class VehicleMovementController {
    
    /**
     * 记录车辆跨区移动
     */
    @PostMapping
    public ApiResponse<MovementVO> recordMovement(
        @Valid @RequestBody RecordMovementRequest request
    ) {
        MovementVO movement = movementService.recordMovement(request);
        return ApiResponse.success(movement);
    }
    
    /**
     * 查询车辆移动历史
     */
    @GetMapping
    public ApiResponse<Page<MovementVO>> getMovementHistory(
        @RequestParam String plateNo,
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Page<MovementVO> movements = movementService.getMovementHistory(
            plateNo, startDate, endDate, page, size
        );
        return ApiResponse.success(movements);
    }
}
```

### 5.3 计费试算接口（增强版）

```java
@RestController
@RequestMapping("/api/v1/parking-lots/{lotId}/fee-rules")
public class FeeRuleController {
    
    /**
     * 停车费用试算（支持跨区）
     */
    @PostMapping("/calculate")
    public ApiResponse<FeeEstimateVO> estimateFee(
        @PathVariable Long lotId,
        @Valid @RequestBody FeeEstimateRequest request
    ) {
        FeeEstimateVO estimate = feeService.estimateFee(lotId, request);
        return ApiResponse.success(estimate);
    }
}

@Data
public class FeeEstimateRequest {
    @NotBlank(message = "车牌号不能为空")
    private String plateNo;
    
    @NotNull(message = "入场时间不能为空")
    private LocalDateTime enterTime;
    
    private LocalDateTime exitTime;
    
    @NotNull(message = "首次进入区域不能为空")
    private Long initialZoneId;
    
    /**
     * 跨区移动列表
     */
    private List<MovementPlan> movements;
    
    @Data
    public static class MovementPlan {
        private Long toZoneId;
        private LocalDateTime moveTime;
    }
}

@Data
public class FeeEstimateVO {
    private BigDecimal totalAmount;
    private BigDecimal baseAmount;
    private BigDecimal discountAmount;
    private Integer totalDurationMinutes;
    private List<FeeSegmentVO> segments;
    private String feeMode;
    private Map<String, Object> metadata;
}
```

---

## 6. 错误处理设计

### 6.1 业务异常定义

```java
public class ParkingException extends RuntimeException {
    private final String code;
    private final Object[] args;
    
    public ParkingException(String code, String message, Object... args) {
        super(String.format(message, args));
        this.code = code;
        this.args = args;
    }
}

// 异常码定义
public interface ExceptionCodes {
    // 区域相关 5001-5099
    String ZONE_NOT_FOUND = "ZONE_001";
    String ZONE_INVALID_PARENT = "ZONE_002";  // 父区域不存在或不属于同一车场
    String ZONE_CROSS_LEVEL = "ZONE_003";     // 不支持跨级嵌套
    String ZONE_MAX_LEVEL_EXCEEDED = "ZONE_004";  // 超过最大层级限制
    
    // 车位相关 5101-5199
    String SPACE_NOT_FOUND = "SPACE_001";
    String SPACE_OCCUPIED = "SPACE_002";
    String SPACE_TYPE_MISMATCH = "SPACE_003";
    
    // 跨区相关 5201-5299
    String CROSS_ZONE_NOT_ALLOWED = "CROSS_001";
    String CROSS_ZONE_INVALID_SEQUENCE = "CROSS_002";  // 移动顺序不合理
    String CROSS_ZONE_MISSING_RECORD = "CROSS_003";    // 缺少移动记录
    
    // 计费相关 5301-5399
    String FEE_RULE_NOT_FOUND = "FEE_001";
    String FEE_RULE_CONFLICT = "FEE_002";  // 规则冲突
    String FEE_CALCULATION_ERROR = "FEE_003";
}
```

### 6.2 全局异常处理器

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ParkingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleParkingException(ParkingException e) {
        log.warn("业务异常：code={}, message={}, args={}", e.getCode(), e.getMessage(), e.getArgs());
        return ApiResponse.error(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("参数异常：{}", e.getMessage());
        return ApiResponse.error("PARAM_ERROR", e.getMessage());
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleInternalError(Exception e) {
        log.error("系统内部异常", e);
        return ApiResponse.error("INTERNAL_ERROR", "系统繁忙，请稍后再试");
    }
}
```

---

## 7. 测试策略

### 7.1 单元测试

```java
@SpringBootTest
class ZonedFeeStrategyTest {
    
    @Autowired
    private ZonedFeeStrategy zonedFeeStrategy;
    
    @Autowired
    private OrderSegmentRepository segmentRepository;
    
    @Test
    @DisplayName("分区计费 - 单区域停车")
    void testSingleZone() {
        // Given
        ParkingContext context = new ParkingContext();
        context.setOrderNo("ORD20260331001");
        context.setFeeMode(FeeMode.ZONED);
        
        OrderSegment segment = new OrderSegment();
        segment.setZoneId(1L);
        segment.setDurationMinutes(120);
        segment.setFeeRuleSnapshot(JsonNodeFactory.instance.objectNode()
            .put("firstHourAmount", 10)
            .put("additionalHourAmount", 5)
            .put("freeDurationMinutes", 15));
        
        segmentRepository.save(segment);
        
        // When
        FeeResult result = zonedFeeStrategy.calculate(context);
        
        // Then
        assertEquals(new BigDecimal("15.00"), result.getTotalAmount());
        assertEquals(1, result.getFeeDetails().size());
    }
    
    @Test
    @DisplayName("分区计费 - 跨区停车")
    void testCrossZone() {
        // Given
        ParkingContext context = new ParkingContext();
        context.setOrderNo("ORD20260331002");
        context.setFeeMode(FeeMode.ZONED);
        
        // 第一段：区域 A（10 元/小时）
        OrderSegment segment1 = new OrderSegment();
        segment1.setZoneId(1L);
        segment1.setDurationMinutes(60);
        segment1.setFeeRuleSnapshot(createRuleJson(10, 5));
        
        // 第二段：区域 B（15 元/小时）
        OrderSegment segment2 = new OrderSegment();
        segment2.setZoneId(2L);
        segment2.setDurationMinutes(60);
        segment2.setFeeRuleSnapshot(createRuleJson(15, 8));
        
        segmentRepository.saveAll(List.of(segment1, segment2));
        
        // When
        FeeResult result = zonedFeeStrategy.calculate(context);
        
        // Then
        assertEquals(new BigDecimal("25.00"), result.getTotalAmount());
        assertEquals(2, result.getFeeDetails().size());
        assertTrue(result.getMetadata().getBoolean("hasCrossZone"));
    }
}
```

### 7.2 集成测试

```java
@SpringBootTest
@AutoConfigureMockMvc
class ZoneManagementIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateZoneHierarchy() throws Exception {
        // 创建主区域
        CreateZoneRequest mainZone = new CreateZoneRequest();
        mainZone.setName("B1 层");
        mainZone.setCode("B1");
        mainZone.setZoneType(1);  // 主区域
        
        String response = mockMvc.perform(post("/api/v1/parking-lots/1/zones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mainZone)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        
        ZoneVO mainZoneVO = objectMapper.readValue(response, ZoneVO.class);
        
        // 创建子区域
        CreateZoneRequest subZone = new CreateZoneRequest();
        subZone.setName("B1-A 区");
        subZone.setCode("B1-A");
        subZone.setZoneType(2);  // 子区域
        subZone.setParentZoneId(mainZoneVO.getId());
        
        mockMvc.perform(post("/api/v1/parking-lots/1/zones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(subZone)))
            .andExpect(status().isOk());
        
        // 验证树形结构
        mockMvc.perform(get("/api/v1/parking-lots/1/zones/tree"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("B1 层"))
            .andExpect(jsonPath("$[0].children[0].name").value("B1-A 区"));
    }
}
```

---

## 8. 部署与迁移

### 8.1 数据库迁移脚本

```sql
-- V2.0__add_zone_support.sql

-- 1. 修改停车场表
ALTER TABLE parking_lots 
ADD COLUMN fee_mode SMALLINT DEFAULT 1,
ADD COLUMN support_multi_zone BOOLEAN DEFAULT true,
ADD COLUMN allow_cross_zone BOOLEAN DEFAULT true;

-- 2. 创建区域表
CREATE TABLE parking_zones (
    id BIGSERIAL PRIMARY KEY,
    parking_lot_id BIGINT NOT NULL REFERENCES parking_lots(id) ON DELETE CASCADE,
    parent_zone_id BIGINT REFERENCES parking_zones(id),
    name VARCHAR(50) NOT NULL,
    code VARCHAR(32) NOT NULL,
    zone_type SMALLINT NOT NULL,
    zone_category SMALLINT,
    floor_level INTEGER,
    total_spaces INTEGER NOT NULL DEFAULT 0,
    available_spaces INTEGER DEFAULT 0,
    has_independent_exit BOOLEAN DEFAULT false,
    exit_lane_ids BIGINT[],
    status SMALLINT DEFAULT 1,
    config JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(parking_lot_id, code),
    CHECK (parent_zone_id IS NULL OR parent_zone_id != id)
);

-- 3. 创建车位表
CREATE TABLE parking_spaces (
    id BIGSERIAL PRIMARY KEY,
    zone_id BIGINT NOT NULL REFERENCES parking_zones(id) ON DELETE CASCADE,
    space_no VARCHAR(20) NOT NULL,
    space_type SMALLINT NOT NULL DEFAULT 1,
    location_info JSONB,
    status SMALLINT NOT NULL DEFAULT 1,
    occupied_by_plate VARCHAR(20),
    occupied_since TIMESTAMPTZ,
    is_charging BOOLEAN DEFAULT false,
    charging_device_id BIGINT,
    width_cm INTEGER,
    length_cm INTEGER,
    height_limit_cm INTEGER,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(zone_id, space_no)
);

-- 4. 修改订单表
ALTER TABLE orders
ADD COLUMN parking_lot_id BIGINT REFERENCES parking_lots(id),
ADD COLUMN initial_zone_id BIGINT REFERENCES parking_zones(id),
ADD COLUMN current_zone_id BIGINT REFERENCES parking_zones(id),
ADD COLUMN fee_mode SMALLINT NOT NULL DEFAULT 1,
ADD COLUMN fee_rule_snapshot JSONB,
ADD COLUMN has_cross_zone BOOLEAN DEFAULT false,
ADD COLUMN cross_zone_count INTEGER DEFAULT 0;

-- 5. 创建订单分段表
CREATE TABLE order_segments (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL REFERENCES orders(order_no) ON DELETE CASCADE,
    segment_no INTEGER NOT NULL,
    zone_id BIGINT NOT NULL REFERENCES parking_zones(id),
    zone_name VARCHAR(50) NOT NULL,
    enter_time TIMESTAMPTZ NOT NULL,
    exit_time TIMESTAMPTZ NOT NULL,
    duration_minutes INTEGER NOT NULL,
    fee_rule_id BIGINT REFERENCES fee_rules(id),
    fee_rule_snapshot JSONB NOT NULL,
    fee_amount DECIMAL(10,2) NOT NULL,
    remark VARCHAR(200),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    CHECK (segment_no > 0)
);

-- 6. 创建车辆移动表
CREATE TABLE vehicle_movements (
    id BIGSERIAL PRIMARY KEY,
    movement_no VARCHAR(32) UNIQUE NOT NULL,
    order_no VARCHAR(32) NOT NULL REFERENCES orders(order_no),
    plate_no VARCHAR(20) NOT NULL,
    from_zone_id BIGINT REFERENCES parking_zones(id),
    to_zone_id BIGINT NOT NULL REFERENCES parking_zones(id),
    movement_time TIMESTAMPTZ NOT NULL,
    detected_by_device_id BIGINT,
    detected_by_lane_id BIGINT,
    snapshot_image_url VARCHAR(255),
    snapshot_video_url VARCHAR(255),
    movement_type SMALLINT NOT NULL,
    movement_reason SMALLINT,
    confidence DECIMAL(3,2) DEFAULT 1.0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 7. 创建索引
CREATE INDEX idx_zones_lot_id ON parking_zones(parking_lot_id);
CREATE INDEX idx_zones_parent_id ON parking_zones(parent_zone_id);
CREATE INDEX idx_spaces_zone_id ON parking_spaces(zone_id);
CREATE INDEX idx_orders_zone ON orders(initial_zone_id, current_zone_id);
CREATE INDEX idx_order_segments_order_no ON order_segments(order_no);
CREATE INDEX idx_movements_plate_time ON vehicle_movements(plate_no, movement_time);

-- 8. 创建触发器
-- （前面定义的触发器 SQL）

-- 9. 数据迁移（如果有历史数据）
UPDATE parking_lots SET fee_mode = 1, support_multi_zone = true WHERE fee_mode IS NULL;
```

---

## 9. 实施路线图

### Phase 1 (P0 - MVP): 基础区域模型
**周期**: Week 1-4  
**范围**:
- ✅ 基础区域模型（一级区域，暂不支持嵌套）
- ✅ 统一计费模式
- ✅ 车位管理基础功能
- ⏸️ 跨区移动追踪（暂不实现）

**交付物**:
- parking_zones 表（简化版）
- parking_spaces 表
- 区域管理 API
- 车位管理 API
- 统一计费策略

### Phase 2 (P1 - 增强): 完整功能
**周期**: Week 5-8  
**范围**:
- ✅ 二级区域嵌套
- ✅ 分区计费模式
- ✅ 订单分段支持
- ✅ 车辆移动追踪

**交付物**:
- 完整的区域层级支持
- ZonedFeeStrategy 实现
- order_segments 表
- vehicle_movements 表
- 跨区移动 API

### Phase 3 (P2 - 智能化): 高级功能
**周期**: Week 9-12  
**范围**:
- ⏸️ 动态路径引导
- ⏸️ 智能推荐车位
- ⏸️ 跨区流量优化

---

## 10. 风险评估与缓解

| 风险项 | 影响程度 | 发生概率 | 缓解措施 |
|--------|----------|----------|----------|
| 数据库性能下降 | 高 | 中 | 分区表、索引优化、物化视图 |
| 跨区逻辑复杂导致 bug | 高 | 高 | 充分的单元测试、集成测试 |
| 历史数据迁移问题 | 中 | 中 | 灰度发布、双写过渡期 |
| SDK 对接复杂度增加 | 中 | 高 | 提前与海康确认技术方案 |

---

## 11. 验收标准

### 11.1 功能验收

- [ ] 可以创建二级嵌套的区域结构
- [ ] 可以为不同区域配置独立的计费规则
- [ ] 支持统一计费和分区计费两种模式
- [ ] 车辆跨区移动自动记录并分段计费
- [ ] 车位状态实时更新
- [ ] 所有 API 接口通过 Postman 测试

### 11.2 性能验收

- [ ] 区域查询响应时间 < 100ms
- [ ] 计费计算响应时间 < 200ms
- [ ] 支持并发 1000+ 车辆同时跨区移动
- [ ] 数据库查询使用索引，无全表扫描

### 11.3 质量验收

- [ ] 单元测试覆盖率 > 80%
- [ ] 关键业务逻辑单元测试覆盖率 > 90%
- [ ] 通过 SonarQube 代码质量扫描
- [ ] 无严重及以上安全漏洞

---

## 12. 附录

### 12.1 术语表

| 术语 | 定义 |
|------|------|
| 主区域 | 直接属于停车场的区域，如"B1 层"、"A 栋" |
| 子区域 | 属于主区域的下一级区域，如"B1-A 区" |
| 统一计费 | 整个停车场采用统一的计费规则 |
| 分区计费 | 不同区域采用不同的计费规则 |
| 跨区移动 | 车辆在停车场内从一个区域移动到另一个区域 |
| 订单分段 | 一个停车订单包含多个区域的停车记录 |

### 12.2 参考资料

- PostgreSQL 官方文档：https://www.postgresql.org/docs/
- Spring Cloud Alibaba 官方文档：https://spring-cloud-alibaba-group.github.io/
- 海康威视 HCNetSDK 开发手册：https://www.hikvision.com/cn/support/resources/developer-tools/

---

**文档变更记录**:

| 版本 | 日期 | 作者 | 变更内容 |
|------|------|------|----------|
| v1.0 | 2026-03-31 | AI Assistant | 初始版本 |
| v2.0 | 2026-03-31 | AI Assistant | 增强型车场模型设计 |
