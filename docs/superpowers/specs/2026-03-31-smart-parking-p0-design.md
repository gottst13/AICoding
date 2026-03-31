# 智慧停车管理平台 - P0 功能需求规格说明书

**版本**: V1.0  
**编制日期**: 2026 年 3 月 31 日  
**保密级别**: 内部公开  

---

## 📋 文档修订历史

| 版本 | 日期 | 修订人 | 修订说明 |
|------|------|--------|----------|
| V1.0 | 2026-03-31 | AI 助手 | 初始版本创建 |

---

## 目录

1. [项目概述](#1-项目概述)
2. [实施模式决策](#2-实施模式决策)
3. [技术架构设计](#3-技术架构设计)
4. [P0 功能模块详细设计](#4-p0 功能模块详细设计)
5. [数据库设计](#5-数据库设计)
6. [部署架构](#6-部署架构)
7. [开发计划](#7-开发计划)
8. [资源投入与预算](#8-资源投入与预算)
9. [风险评估与应对](#9-风险评估与应对)
10. [验收标准](#10-验收标准)

---

## 1. 项目概述

### 1.1 项目背景

智慧停车管理平台采购询价项目基于《0203智慧停车管理平台_采购询价功能清单.xlsx》需求文档，总计 276 个功能点。本项目旨在构建一个覆盖封闭停车场和道路泊位的智能化停车管理系统，实现车主、运营方、监管方的多方共赢。

### 1.2 项目目标（P0 阶段）

**MVP 目标**: 在 Week 10 前完成最小可行产品上线，验证核心商业模式

**具体目标**:
- ✅ 实现 3-5 个试点同时运营（多点并行模式）
- ✅ 支持封闭停车场和道路泊位两种场景
- ✅ 打通"入场→停车→缴费→出场"完整业务流程
- ✅ 日均处理订单≥500 单
- ✅ 系统可用性≥99%

### 1.3 P0 功能范围

**包含内容**:
- Web 管理后台：6 大子系统，50 个功能点
- 车主微信小程序：8 个核心功能
- 海康 SDK 对接：基础车牌识别 + 道闸控制
- 聚合支付对接：微信 + 支付宝
- 试点部署：3-5 个点

**不包含内容**:
- ❌ POS 端功能（后续单独讨论）
- ❌ 包期业务（P1 阶段）
- ❌ 营销中心（P1 阶段）
- ❌ 领导驾驶舱（P2 阶段）
- ❌ 高级数据分析（P2 阶段）

---

## 2. 实施模式决策

### 2.1 关键决策汇总

经过多轮方案对比和分析，已确认以下关键决策：

| 决策点 | 选择方案 | 理由 |
|--------|----------|------|
| **实施模式** | 多点并行（3-5 个试点） | 全面验证不同场景，虽然复杂度高但能发现更多问题 |
| **SDK 对接** | 基础对接（10 人天） | 快速上线，聚焦核心功能，P1 再考虑深度对接 |
| **支付策略** | 混合方案 | MVP 用聚合支付快速验证，P1 切换官方直连降低成本 |
| **小程序部署** | 独立小程序 | 自建品牌，利于长期运营和资本运作 |
| **POS 设备** | 暂不考虑 | 聚焦软件功能，硬件后续单独讨论 |
| **小程序功能** | 极简方案（6 功能） | 确保按时上线，增值服务 P1 补充 |
| **技术架构** | 微服务 + PostgreSQL 15 | 前瞻性技术栈，便于后续扩展 |
| **部署方式** | Docker Compose 单机 | 成本可控，快速部署，适合 MVP |

### 2.2 试点选址要求

**试点类型**:
- 封闭停车场×2（商场/写字楼各 1 个）
- 路侧泊位×1（商业街区）
- 混合型×1-2 个（可选）

**试点规模**:
- 每个停车场配置 2-4 条车道（入口 + 出口）
- 路侧泊位 50-100 个
- 总泊位数≥300 个

**试点周期**:
- Week 9: 现场部署、设备安装
- Week 10: 试运营、真实订单验证
- 正式运营后：持续优化

---

## 3. 技术架构设计

### 3.1 总体架构

采用 Spring Cloud Alibaba 微服务架构，PostgreSQL 15 作为主数据库，支持高并发、高可用、易扩展的企业级应用。

```
┌─────────────────────────────────────────────────────────────┐
│                      用户访问层                               │
├──────────────────────┬──────────────────────────────────────┤
│   微信小程序          │     Web 管理后台                      │
│   (Uni-app)          │     (React + Ant Design)              │
└──────────┬───────────┴────────────┬─────────────────────────┘
           │                        │
           ▼                        ▼
┌─────────────────────────────────────────────────────────────┐
│                    Nginx 负载均衡                            │
│              (SSL 终止、静态资源、反向代理)                   │
└─────────────────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│                  Spring Cloud Gateway                        │
│         (认证鉴权、限流熔断、路由转发、日志监控)               │
└─────────────────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│                     微服务集群                               │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │用户服务  │ │车场服务  │ │订单服务  │ │支付服务  │       │
│  │8081      │ │8082      │ │8083      │ │8084      │       │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘       │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐                     │
│  │设备服务  │ │报表服务  │ │Nacos 配置 │                     │
│  │8085      │ │8086      │ │8848      │                     │
│  └──────────┘ └──────────┘ └──────────┘                     │
└─────────────────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│                     中间件层                                 │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │PostgreSQL│ │  Redis   │ │RabbitMQ  │ │  ELK     │       │
│  │15 主从    │ │6.0 集群  │ │3.9 集群  │ │日志系统  │       │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘       │
└─────────────────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│                   第三方服务集成                             │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │海康 SDK  │ │聚合支付  │ │微信登录  │ │短信服务  │       │
│  │(TCP)     │ │(HTTP)    │ │OAuth     │ │(HTTP)    │       │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘       │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 技术栈选型

#### **后端技术栈**
```yaml
框架：Spring Cloud Alibaba 2021.x
  - Nacos 2.0: 服务注册与配置中心
  - Sentinel 1.8: 流量防卫兵（限流、熔断、降级）
  - Seata 1.5: 分布式事务解决方案
  - Gateway 3.x: API 网关

ORM: MyBatis Plus 3.5
数据库：PostgreSQL 15
  - 主从复制：1 主 2 从
  - 连接池：HikariCP
  - 分区表：按月分表（订单表）

缓存：Redis 6.0
  - 集群模式：3 主 3 从
  - 分布式锁：Redisson

消息队列：RabbitMQ 3.9
  - 延迟队列：订单超时处理
  - 死信队列：异常消息处理

监控：
  - SkyWalking 8.x: 链路追踪
  - Prometheus + Grafana: 性能监控
  - ELK Stack: 日志收集与分析
```

#### **前端技术栈**
```yaml
Web 管理后台:
  - React 18
  - Ant Design 4.x
  - Umi 4: 企业级前端框架
  - Ahooks: React Hooks 库

车主小程序:
  - Uni-app: 跨端开发框架
    - 微信小程序
    - H5（预留）
```

#### **DevOps**
```yaml
容器化：Docker + Docker Compose
CI/CD: Jenkins + GitLab
代码管理：GitLab
接口文档：Swagger/OpenAPI
```

### 3.3 微服务拆分

基于领域驱动设计（DDD），按业务边界拆分为 7 个微服务：

| 服务名称 | 端口 | 职责 | 数据库 | 实例数 |
|---------|------|------|--------|--------|
| gateway | 8080 | API 网关、认证鉴权、路由转发 | Redis | 2 |
| user-service | 8081 | 用户管理、角色权限、登录认证 | pg_user | 1 |
| parking-service | 8082 | 停车场、车道、收费规则管理 | pg_parking | 2 |
| order-service | 8083 | 订单创建、状态流转、计费计算 | pg_order | 3 |
| payment-service | 8084 | 支付对接、退款、对账 | pg_payment | 2 |
| device-service | 8085 | 海康 SDK 对接、设备状态监控 | pg_device | 2 |
| report-service | 8086 | 统计分析、报表生成 | pg_report<br>Redis | 1 |

**服务依赖关系**:
```
gateway → 所有服务
order-service → parking-service（获取收费规则）
payment-service → order-service（更新订单状态）
report-service → order-service, payment-service（统计数据）
device-service → 无依赖（独立服务）
parking-service → 无依赖（独立服务）
user-service → 无依赖（独立服务）
```

---

## 4. P0 功能模块详细设计

### 4.1 系统管理子系统（11 人天）

#### **4.1.1 用户登录**
**功能描述**: 提供账号密码登录，基于 JWT Token 的认证机制

**接口列表**:
```java
POST /api/v1/auth/login
  - 请求：{ username, password, captcha }
  - 响应：{ token, expires_in, user_info }
  
POST /api/v1/auth/logout
  - 请求：{ token }
  - 响应：{ success }
```

**技术要点**:
- JWT Token 有效期 2 小时
- Refresh Token 机制（可选）
- 登录失败 5 次锁定账号 30 分钟
- 支持多地点登录（同账号可多处登录）

#### **4.1.2 用户管理**
**功能描述**: 用户的增删改查、重置密码、启用/禁用

**接口列表**:
```java
GET /api/v1/users?page=1&size=20&keyword=xxx
POST /api/v1/users
PUT /api/v1/users/{id}
DELETE /api/v1/users/{id}
POST /api/v1/users/{id}/reset-password
PUT /api/v1/users/{id}/status
```

**数据权限**: 
- 超级管理员：查看所有用户
- 普通管理员：只能查看本停车场用户

#### **4.1.3 角色管理**
**功能描述**: 角色定义、权限配置、用户绑定

**接口列表**:
```java
GET /api/v1/roles
POST /api/v1/roles
PUT /api/v1/roles/{id}
DELETE /api/v1/roles/{id}
PUT /api/v1/roles/{id}/permissions
PUT /api/v1/roles/{id}/users
```

**预置角色**:
- 超级管理员：全部权限
- 运营管理员：车场、订单、报表管理
- 财务人员：支付、退款、对账管理
- 车场管理员：只能查看本车场数据

#### **4.1.4 日志管理**
**功能描述**: 记录用户操作日志，支持审计追溯

**接口列表**:
```java
GET /api/v1/logs/login?page=1&size=20&user_id=&start_time=&end_time=
GET /api/v1/logs/operation?page=1&size=20&user_id=&module=&action=
GET /api/v1/logs/export?start_time=&end_time=
```

**日志内容**:
- 登录日志：IP、时间、浏览器、结果
- 操作日志：谁、何时、做了什么、结果如何

---

### 4.2 车场管理子系统（15 人天）

#### **4.2.1 停车场管理**
**功能描述**: 停车场的基础信息管理

**接口列表**:
```java
GET /api/v1/parking-lots?page=1&size=20&status=&type=
POST /api/v1/parking-lots
PUT /api/v1/parking-lots/{id}
DELETE /api/v1/parking-lots/{id}
GET /api/v1/parking-lots/{id}
GET /api/v1/parking-lots/{id}/qr-code
```

**数据模型**:
```sql
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
```

#### **4.2.2 车道管理**
**功能描述**: 停车场出入口车道管理，绑定海康设备

**接口列表**:
```java
GET /api/v1/parking-lots/{lot_id}/lanes
POST /api/v1/parking-lots/{lot_id}/lanes
PUT /api/v1/parking-lanes/{id}
DELETE /api/v1/parking-lanes/{id}
POST /api/v1/parking-lanes/{id}/control  -- 远程控闸
```

**数据模型**:
```sql
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
```

#### **4.2.3 收费规则配置**
**功能描述**: 灵活的计费规则引擎

**接口列表**:
```java
GET /api/v1/parking-lots/{lot_id}/fee-rules
POST /api/v1/parking-lots/{lot_id}/fee-rules
PUT /api/v1/parking-lots/{lot_id}/fee-rules/{id}
DELETE /api/v1/parking-lots/{lot_id}/fee-rules/{id}
POST /api/v1/parking-lots/{lot_id}/fee-rules/calculate  -- 费用试算
```

**规则引擎设计**:
```java
// 策略模式实现
public interface FeeStrategy {
    BigDecimal calculate(ParkingContext context);
}

// 按时长计费策略
public class DurationFeeStrategy implements FeeStrategy {
    @Override
    public BigDecimal calculate(ParkingContext context) {
        // 首小时费用 + 续时费用 - 优惠
        // 封顶价保护
    }
}

// 按次计费策略
public class FlatFeeStrategy implements FeeStrategy {
    @Override
    public BigDecimal calculate(ParkingContext context) {
        return fixedAmount;
    }
}
```

**数据模型**:
```sql
CREATE TABLE fee_rules (
    id BIGSERIAL PRIMARY KEY,
    parking_lot_id BIGINT NOT NULL,
    vehicle_type SMALLINT,  -- 1:小型车 2:大型车
    rule_type SMALLINT,  -- 1:按时长 2:按次 3:分时
    first_hour_amount DECIMAL(10,2),
    additional_hour_amount DECIMAL(10,2),
    daily_max_amount DECIMAL(10,2),
    free_duration_minutes INTEGER DEFAULT 15,
    time_range_start TIME,  -- 分时计费开始时间
    time_range_end TIME,    -- 分时计费结束时间
    effective_date DATE,
    expiry_date DATE,
    config JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
```

---

### 4.3 SDK 对接子系统（10 人天）

#### **4.3.1 海康 SDK 环境搭建**
**技术栈**:
- 海康威视 SDK 版本：HCNetSDK V6.1.6
- 支持操作系统：Windows Server 2019 / CentOS 7.9
- 开发语言：Java (JNI 调用 C++ SDK)

**环境准备**:
```bash
# 安装依赖
yum install gcc-c++ make -y

# 复制 SDK 动态库
cp libhcnetsdk.so /usr/lib/
cp libSdkParser.so /usr/lib/
ldconfig

# Java 项目引入
<dependency>
    <groupId>com.hikvision</groupId>
    <artifactId>hcnetsdk</artifactId>
    <version>6.1.6</version>
</dependency>
```

#### **4.3.2 设备注册**
**接口列表**:
```java
POST /api/v1/devices/register
  - 请求：{ parking_lot_id, lane_id, device_ip, device_port, username, password }
  - 响应：{ device_id, device_serial, status }

GET /api/v1/devices/{id}/status
  - 响应：{ online, last_heartbeat, signal_strength }
```

**初始化流程**:
```java
@Service
public class HikvisionDeviceService {
    
    // 初始化 SDK
    @PostConstruct
    public void init() {
        HCNetSDK hCNetSDK = HCNetSDK.getInstance();
        hCNetSDK.NET_DVR_Init();
        hCNetSDK.NET_DVR_SetLogToFile(3, "C:\\SdkLog\\", 0);
    }
    
    // 注册设备
    public Long registerDevice(DeviceConfig config) {
        NET_DVR_USER_LOGIN_INFO loginInfo = new NET_DVR_USER_LOGIN_INFO();
        loginInfo.sDeviceAddress = config.getDeviceIp();
        loginInfo.wPort = config.getDevicePort();
        loginInfo.sUserName = config.getUsername();
        loginInfo.sPassword = config.getPassword();
        
        Long userId = hCNetSDK.NET_DVR_Login_V40(loginInfo, null);
        if (userId == -1) {
            throw new DeviceLoginException("设备登录失败");
        }
        return userId;
    }
}
```

#### **4.3.3 车牌识别回调**
**接口设计**:
```java
// TCP Server 接收设备回调
@Service
public class DeviceCallbackServer {
    
    @Value("${device.callback.port:9000}")
    private int callbackPort;
    
    @EventListener(ApplicationReadyEvent.class)
    public void startServer() {
        // 启动车牌识别回调监听
        new Thread(() -> {
            ServerSocket server = new ServerSocket(callbackPort);
            while (!Thread.currentThread().isInterrupted()) {
                Socket client = server.accept();
                handleCallback(client);
            }
        }).start();
    }
    
    private void handleCallback(Socket client) {
        // 解析车牌识别结果
        byte[] buffer = new byte[1024];
        int len = client.getInputStream().read(buffer);
        
        PlateRecognitionResult result = parsePlateResult(buffer);
        
        // 发布事件到消息队列
        eventPublisher.publishEvent(new PlateRecognizedEvent(this, result));
    }
}

// 事件处理器
@Component
public class PlateRecognitionEventHandler {
    
    @Autowired
    private OrderService orderService;
    
    @EventListener
    @Async
    public void handlePlateRecognized(PlateRecognizedEvent event) {
        PlateRecognitionResult result = event.getResult();
        
        if (result.isEntry()) {
            // 入场事件：创建订单
            orderService.createOrder(result);
        } else {
            // 出场事件：触发计费
            orderService.processExit(result);
        }
    }
}
```

**数据模型**:
```sql
CREATE TABLE plate_recognition_logs (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL,
    plate_no VARCHAR(20) NOT NULL,
    plate_color SMALLINT,  -- 0:未知 1:蓝 2:黄 3:绿
    recognition_time TIMESTAMPTZ NOT NULL,
    image_url VARCHAR(255),  -- 抓拍图片 URL
    confidence SMALLINT,  -- 识别置信度 0-100
    direction SMALLINT,  -- 1:入场 2:出场
    raw_data JSONB,  -- 原始报文
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_plate ON plate_recognition_logs(plate_no);
CREATE INDEX idx_time ON plate_recognition_logs(recognition_time);
```

#### **4.3.4 道闸控制**
**接口列表**:
```java
POST /api/v1/devices/{device_id}/control/barrier
  - 请求：{ action: "open" | "close" | "stop" }
  - 响应：{ success, message }

POST /api/v1/devices/{device_id}/control/led
  - 请求：{ content: "欢迎光临", color: "red" }
  - 响应：{ success }
```

**实现代码**:
```java
@Service
public class BarrierControlService {
    
    @Autowired
    private DeviceManager deviceManager;
    
    public void openBarrier(Long deviceId) {
        NET_DVR_CONTROL_PARAM controlParam = new NET_DVR_CONTROL_PARAM();
        controlParam.dwControlType = 0;  // 开闸
        controlParam.dwChannel = 1;
        
        Long userId = deviceManager.getUserId(deviceId);
        int result = hCNetSDK.NET_DVR_RemoteControl(userId, 
            HCNetSDK.NET_DVR_CTRL_BARRIER, controlParam);
        
        if (result != 0) {
            throw new DeviceControlException("开闸失败");
        }
    }
}
```

---

### 4.4 订单管理子系统（7 人天）

#### **4.4.1 临停订单生成**
**接口列表**:
```java
// 内部接口，由事件触发
@EventListener
public void createOrder(PlateRecognizedEvent event) {
    PlateRecognitionResult result = event.getResult();
    
    // 幂等性检查（防止重复创建）
    String lockKey = "order:create:" + result.getPlateNo();
    RLock lock = redissonClient.getLock(lockKey);
    
    try {
        if (lock.tryLock(3, TimeUnit.SECONDS)) {
            // 检查是否已有在场订单
            TempOrder existingOrder = tempOrderRepository
                .findByPlateNoAndStatus(result.getPlateNo(), OrderStatus.ENTERED);
            
            if (existingOrder != null) {
                log.warn("车辆已在场，忽略本次入场事件：{}", result.getPlateNo());
                return;
            }
            
            // 创建订单
            TempOrder order = new TempOrder();
            order.setOrderNo(generateOrderNo());
            order.setPlateNo(result.getPlateNo());
            order.setParkingLotId(result.getParkingLotId());
            order.setLaneId(result.getLaneId());
            order.setEnterTime(result.getRecognitionTime());
            order.setStatus(OrderStatus.ENTERED);
            order.setMetadata(JSON.toJSONString(result.getImages()));
            
            tempOrderRepository.save(order);
            
            // 发送订单创建事件
            eventPublisher.publishEvent(new OrderCreatedEvent(this, order));
        }
    } finally {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
```

**订单号生成规则**:
```java
private String generateOrderNo() {
    // 格式：TP + 年月日时分秒 (14 位) + 随机数 (6 位)
    String timestamp = LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
    String random = String.format("%06d", new Random().nextInt(1000000));
    return "TP" + timestamp + random;
}
```

#### **4.4.2 订单状态流转**
**状态机设计**:
```java
public enum OrderStatus {
    ENTERED(0, "已入场"),
    WAITING_PAYMENT(1, "待支付"),
    PAID(2, "已支付"),
    EXITED(3, "已出场"),
    OVERDUE(4, "欠费");
    
    private final int code;
    private final String desc;
}

@Service
public class OrderStateMachine {
    
    public void transition(TempOrder order, OrderStatus newStatus) {
        OrderStatus oldStatus = order.getStatus();
        
        // 状态合法性校验
        if (!isValidTransition(oldStatus, newStatus)) {
            throw new InvalidStatusTransitionException(
                String.format("非法状态转换：%s -> %s", oldStatus, newStatus));
        }
        
        order.setStatus(newStatus);
        
        // 状态变更时间记录
        switch (newStatus) {
            case WAITING_PAYMENT:
                order.setPaymentTime(LocalDateTime.now());
                break;
            case PAID:
                order.setPaidTime(LocalDateTime.now());
                break;
            case EXITED:
                order.setExitTime(LocalDateTime.now());
                break;
        }
        
        tempOrderRepository.update(order);
    }
    
    private boolean isValidTransition(OrderStatus from, OrderStatus to) {
        switch (from) {
            case ENTERED:
                return to == OrderStatus.WAITING_PAYMENT;
            case WAITING_PAYMENT:
                return to == OrderStatus.PAID || to == OrderStatus.OVERDUE;
            case PAID:
                return to == OrderStatus.EXITED;
            default:
                return false;
        }
    }
}
```

#### **4.4.3 停车费计算**
**接口列表**:
```java
GET /api/v1/orders/{order_no}/fee-calculation
  - 响应：{ 
      duration_seconds: 3600,
      original_amount: 10.00,
      discount_amount: 2.00,
      payable_amount: 8.00,
      rule_description: "首小时 10 元，优惠 2 元"
    }
```

**计费引擎**:
```java
@Service
public class FeeCalculationService {
    
    @Autowired
    private FeeRuleRepository feeRuleRepository;
    
    public FeeResult calculate(TempOrder order) {
        // 获取适用的收费规则
        List<FeeRule> rules = feeRuleRepository.findByParkingLotIdAndVehicleType(
            order.getParkingLotId(), 
            order.getVehicleType()
        );
        
        // 匹配当前时间的有效规则
        FeeRule matchedRule = findMatchingRule(rules, order.getEnterTime());
        
        // 计算时长
        long durationSeconds = ChronoUnit.SECONDS.between(
            order.getEnterTime(), 
            order.getExitTime() != null ? order.getExitTime() : LocalDateTime.now()
        );
        
        // 免费时长判断
        if (durationSeconds <= matchedRule.getFreeDurationMinutes() * 60) {
            return FeeResult.free();
        }
        
        // 计费逻辑
        BigDecimal amount = calculateFee(matchedRule, durationSeconds);
        
        // 优惠券抵扣（P1 阶段）
        BigDecimal discount = BigDecimal.ZERO;
        
        // 封顶价保护
        if (amount.compareTo(matchedRule.getDailyMaxAmount()) > 0) {
            amount = matchedRule.getDailyMaxAmount();
        }
        
        return FeeResult.builder()
            .originalAmount(amount)
            .discountAmount(discount)
            .payableAmount(amount.subtract(discount))
            .build();
    }
    
    private BigDecimal calculateFee(FeeRule rule, long durationSeconds) {
        switch (rule.getRuleType()) {
            case 1: // 按时长计费
                return calculateByDuration(rule, durationSeconds);
            case 2: // 按次计费
                return rule.getFixedAmount();
            case 3: // 分时计费
                return calculateByTimeRange(rule, durationSeconds);
            default:
                throw new IllegalArgumentException("未知的计费类型");
        }
    }
}
```

---

### 4.5 支付管理子系统（8 人天）

#### **4.5.1 聚合支付对接**
**服务商选择**: Ping++ (推荐) 或 Payjs

**接口列表**:
```java
POST /api/v1/payment/charge
  - 请求：{ 
      order_no: "TP260331120000001",
      amount: 10.00,
      channel: "wechat",  // wechat/alipay
      subject: "停车费"
    }
  - 响应：{ 
      charge_id: "ch_xxx",
      credential: { ... },  // 前端调起支付所需凭证
      url: "https://..."    // H5 支付链接
    }

POST /api/v1/payment/webhook
  - 异步通知接口（由 Ping++ 回调）
  - 验签、处理支付结果
```

**实现代码**:
```java
@Service
public class PaymentService {
    
    @Value("${pingpp.api.key}")
    private String apiKey;
    
    @Value("${pingpp.app.id}")
    private String appId;
    
    /**
     * 创建支付订单
     */
    public ChargeDTO createCharge(PaymentRequest request) {
        Pingpp.setApiKey(apiKey);
        Pingpp.setMaxNetworkTimeout(10000);
        
        Map<String, Object> chargeMap = new HashMap<>();
        chargeMap.put("app", appId);
        chargeMap.put("channel", request.getChannel());
        chargeMap.put("order_no", request.getOrderNo());
        chargeMap.put("client_ip", "127.0.0.1");
        chargeMap.put("amount", request.getAmount().multiply(new BigDecimal(100)).intValue());
        chargeMap.put("currency", "cny");
        chargeMap.put("subject", request.getSubject());
        chargeMap.put("body", request.getBody());
        
        Charge charge = Charge.create(chargeMap);
        
        // 保存支付流水
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setTransactionNo(charge.getId());
        transaction.setOrderNo(request.getOrderNo());
        transaction.setChannel(request.getChannel());
        transaction.setAmount(request.getAmount());
        transaction.setStatus(PaymentStatus.PENDING);
        paymentTransactionRepository.save(transaction);
        
        return convertToDTO(charge);
    }
    
    /**
     * 处理支付回调
     */
    @Transactional
    public void handlePaymentWebhook(String webhookData) {
        EventObject event = Webhooks.parseEvent(webhookData);
        
        if ("charge.succeeded".equals(event.getType())) {
            Charge charge = (Charge) event.getDataObject();
            
            // 更新支付流水状态
            PaymentTransaction transaction = paymentTransactionRepository
                .findByTransactionNo(charge.getId());
            transaction.setStatus(PaymentStatus.SUCCESS);
            transaction.setPayTime(LocalDateTime.now());
            transaction.setNotifyData(JSON.toJSONString(charge));
            paymentTransactionRepository.update(transaction);
            
            // 更新订单状态
            TempOrder order = tempOrderRepository.findByOrderNo(charge.getOrderNo());
            order.setStatus(OrderStatus.PAID);
            order.setPaidAmount(new BigDecimal(charge.getAmount()).divide(new BigDecimal(100)));
            tempOrderRepository.update(order);
        }
    }
}
```

#### **4.5.2 对账功能**
**接口列表**:
```java
GET /api/v1/payment/reconciliation/download?date=2026-03-31
  - 下载聚合支付对账单
  
POST /api/v1/payment/reconciliation/process
  - 手动触发对账任务
  
GET /api/v1/payment/reconciliation/result?id=123
  - 查询对账结果
```

**对账流程**:
```java
@Service
public class ReconciliationService {
    
    @Scheduled(cron = "0 0 2 * * ?")  // 每天凌晨 2 点执行
    public void dailyReconciliation() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        
        // 1. 下载 Ping++ 对账单
        List<PingppTransaction> pingppTransactions = downloadPingppStatement(yesterday);
        
        // 2. 查询系统内支付流水
        List<PaymentTransaction> systemTransactions = 
            paymentTransactionRepository.findByDate(yesterday);
        
        // 3. 比对差异
        ReconciliationResult result = compare(pingppTransactions, systemTransactions);
        
        // 4. 处理差异
        if (result.hasDiscrepancy()) {
            // 长款：Ping++ 有，系统没有
            result.getLongPayments().forEach(this::handleLongPayment);
            
            // 短款：系统有，Ping++ 没有
            result.getShortPayments().forEach(this::handleShortPayment);
        }
        
        // 5. 发送对账报告
        sendReconciliationReport(result);
    }
}
```

---

### 4.6 报表服务子系统（6 人天）

#### **4.6.1 数据看板**
**接口列表**:
```java
GET /api/v1/dashboard/today
  - 响应：{
      today_revenue: 5000.00,
      yesterday_revenue: 4500.00,
      growth_rate: 11.11,
      entering_count: 320,
      exiting_count: 315,
      parked_count: 150,
      payment_distribution: [
        { channel: "wechat", amount: 3000.00, percentage: 60 },
        { channel: "alipay", amount: 2000.00, percentage: 40 }
      ]
    }

GET /api/v1/dashboard/trend?metric=revenue&days=7
  - 响应：{
      dates: ["2026-03-25", "2026-03-26", ...],
      values: [4500.00, 4800.00, ...]
    }
```

**物化视图优化**:
```sql
-- 创建今日统计物化视图
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

-- 每小时刷新
REFRESH MATERIALIZED VIEW CONCURRENTLY mv_today_stats;
```

---

### 4.7 车主微信小程序（20 人天）

#### **4.7.1 微信授权登录**
**技术流程**:
```javascript
// 小程序端
wx.login({
  success: (res) => {
    const { code } = res;
    
    // 发送给后端
    request.post('/api/v1/wechat/login', { code })
      .then(({ token, userInfo }) => {
        wx.setStorageSync('token', token);
        this.setData({ userInfo });
      });
  }
});
```

**后端接口**:
```java
@PostMapping("/wechat/login")
public LoginResponse wechatLogin(@RequestBody WechatLoginRequest request) {
    // 1. code 换 openid
    String openid = wechatService.getOpenid(request.getCode());
    
    // 2. 查询或创建用户
    User user = userRepository.findByWechatOpenid(openid);
    if (user == null) {
        user = new User();
        user.setWechatOpenid(openid);
        user.setNickname("用户" + System.currentTimeMillis());
        userRepository.save(user);
    }
    
    // 3. 生成 JWT Token
    String token = jwtTokenProvider.generateToken(user.getId());
    
    return LoginResponse.builder()
        .token(token)
        .userInfo(convertToUserInfo(user))
        .build();
}
```

#### **4.7.2 附近停车场**
**技术实现**:
```javascript
// 获取当前位置
wx.getLocation({
  type: 'gcj02',
  success: (res) => {
    const latitude = res.latitude;
    const longitude = res.longitude;
    
    // 查询周边停车场
    request.get(`/api/v1/parking-lots/nearby?lat=${latitude}&lng=${longitude}&radius=5000`)
      .then((parkingLots) => {
        this.setData({ parkingLots });
      });
  }
});
```

**后端接口**:
```java
@GetMapping("/parking-lots/nearby")
public List<ParkingLotDTO> getNearbyParkingLots(
    @RequestParam Double lat,
    @RequestParam Double lng,
    @RequestParam(defaultValue = "5000") Integer radius
) {
    // PostgreSQL 空间查询（使用 PostGIS 扩展）
    return parkingLotRepository.findNearby(lat, lng, radius);
}
```

#### **4.7.3 扫码入场/出场**
**二维码格式**:
```
https://m.smartparking.com/park/entry?lane_id=123&sign=xxx
```

**小程序扫码**:
```javascript
wx.scanCode({
  success: (res) => {
    const url = res.result;
    const params = parseUrl(url);
    
    if (params.lane_id) {
      // 调用入场接口
      request.post('/api/v1/parking/entry', {
        lane_id: params.lane_id,
        plate_no: this.data.currentPlate
      }).then(() => {
        wx.showModal({
          title: '入场成功',
          content: '祝您用车愉快'
        });
      });
    }
  }
});
```

#### **4.7.4 停车缴费**
**页面流程**:
```
输入车牌号 → 查询订单 → 展示金额 → 调起支付 → 支付成功
```

**关键代码**:
```javascript
// 查询订单
queryOrder(plateNo) {
  return request.get(`/api/v1/orders/calculate?plate_no=${plateNo}`)
    .then((order) => {
      this.setData({ 
        currentOrder: order,
        showPayment: true
      });
    });
},

// 发起支付
payOrder() {
  const { currentOrder } = this.data;
  
  request.post('/api/v1/payment/charge', {
    order_no: currentOrder.order_no,
    amount: currentOrder.payable_amount,
    channel: 'wechat'
  }).then((charge) => {
    // 调起微信支付
    wx.requestPayment({
      ...charge.credential,
      success: () => {
        wx.showModal({
          title: '支付成功',
          showCancel: false
        });
        // 跳转到订单详情页
        wx.navigateTo({
          url: `/pages/order/detail?order_no=${currentOrder.order_no}`
        });
      }
    });
  });
}
```

---

## 5. 数据库设计

### 5.1 数据库规划

采用分库策略，每个微服务独立数据库：

```sql
-- 创建数据库
CREATE DATABASE pg_user WITH ENCODING = 'UTF8';
CREATE DATABASE pg_parking WITH ENCODING = 'UTF8';
CREATE DATABASE pg_order WITH ENCODING = 'UTF8';
CREATE DATABASE pg_payment WITH ENCODING = 'UTF8';
CREATE DATABASE pg_device WITH ENCODING = 'UTF8';
CREATE DATABASE pg_report WITH ENCODING = 'UTF8';
```

### 5.2 核心表结构

#### **用户服务数据库（pg_user）**
```sql
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

-- 角色表
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 用户角色关联表
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id),
    role_id BIGINT NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

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
```

#### **车场服务数据库（pg_parking）**
```sql
-- 停车场表（见 4.2.1）

-- 车道表（见 4.2.2）

-- 收费规则表（见 4.2.3）

-- 泊位表
CREATE TABLE parking_spaces (
    id BIGSERIAL PRIMARY KEY,
    parking_lot_id BIGINT NOT NULL REFERENCES parking_lots(id),
    space_no VARCHAR(20) NOT NULL,  -- 泊位编号
    space_type SMALLINT,  -- 1:小型车 2:大型车 3:无障碍
    location VARCHAR(100),  -- 位置描述（如"A 区 -01"）
    status SMALLINT DEFAULT 0,  -- 0:空闲 1:占用 2:锁定
    occupied_by_plate VARCHAR(20),  -- 占用车牌号
    occupied_since TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_space_lot ON parking_spaces(parking_lot_id);
CREATE INDEX idx_space_status ON parking_spaces(status);
```

#### **订单服务数据库（pg_order）**
```sql
-- 临停订单表（见 4.4.1，需增加分区）

-- 创建分区表
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
    status SMALLINT NOT NULL,
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
-- ... 依此类推

-- 索引
CREATE INDEX idx_temp_orders_plate ON temp_orders(plate_no);
CREATE INDEX idx_temp_orders_lot ON temp_orders(parking_lot_id);
CREATE INDEX idx_temp_orders_status ON temp_orders(status);
```

#### **支付服务数据库（pg_payment）**
```sql
-- 支付流水表（见 4.5.1）

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
```

#### **设备服务数据库（pg_device）**
```sql
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

-- 设备操作日志表
CREATE TABLE device_operation_logs (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL,
    operation_type VARCHAR(50) NOT NULL,  -- open_barrier/close_barrier/led_display
    operator_id BIGINT,
    operator_name VARCHAR(50),
    request_params JSONB,
    response_result JSONB,
    status SMALLINT,  -- 0:失败 1:成功
    error_message TEXT,
    operation_time TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_device_log_device ON device_operation_logs(device_id);
CREATE INDEX idx_device_log_time ON device_operation_logs(operation_time);
```

---

## 6. 部署架构

### 6.1 Docker Compose 配置

**docker-compose.yml**:
```yaml
version: '3.8'

services:
  # API 网关
  gateway:
    image: smart-parking/gateway:1.0.0
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - NACOS_SERVER_ADDR=nacos:8848
    depends_on:
      - nacos
    networks:
      - smart-parking-net

  # 用户服务
  user-service:
    image: smart-parking/user-service:1.0.0
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - NACOS_SERVER_ADDR=nacos:8848
      - DB_HOST=postgres
      - DB_NAME=pg_user
    depends_on:
      - postgres
      - nacos
    networks:
      - smart-parking-net

  # 车场服务
  parking-service:
    image: smart-parking/parking-service:1.0.0
    ports:
      - "8082:8082"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - NACOS_SERVER_ADDR=nacos:8848
      - DB_HOST=postgres
      - DB_NAME=pg_parking
    depends_on:
      - postgres
      - nacos
    networks:
      - smart-parking-net

  # 订单服务
  order-service:
    image: smart-parking/order-service:1.0.0
    ports:
      - "8083:8083"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - NACOS_SERVER_ADDR=nacos:8848
      - DB_HOST=postgres
      - DB_NAME=pg_order
    depends_on:
      - postgres
      - nacos
    networks:
      - smart-parking-net
    deploy:
      replicas: 2

  # 支付服务
  payment-service:
    image: smart-parking/payment-service:1.0.0
    ports:
      - "8084:8084"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - NACOS_SERVER_ADDR=nacos:8848
      - DB_HOST=postgres
      - DB_NAME=pg_payment
    depends_on:
      - postgres
      - nacos
    networks:
      - smart-parking-net

  # 设备服务
  device-service:
    image: smart-parking/device-service:1.0.0
    ports:
      - "8085:8085"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - NACOS_SERVER_ADDR=nacos:8848
      - DB_HOST=postgres
      - DB_NAME=pg_device
    depends_on:
      - postgres
      - nacos
    networks:
      - smart-parking-net

  # 报表服务
  report-service:
    image: smart-parking/report-service:1.0.0
    ports:
      - "8086:8086"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - NACOS_SERVER_ADDR=nacos:8848
      - DB_HOST=postgres
      - DB_NAME=pg_report
    depends_on:
      - postgres
      - redis
      - nacos
    networks:
      - smart-parking-net

  # Nacos 配置中心
  nacos:
    image: nacos/nacos-server:2.0.3
    ports:
      - "8848:8848"
    environment:
      - MODE=standalone
      - SPRING_DATASOURCE_PLATFORM=postgresql
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_USER=postgres
      - DB_PASSWORD=postgres123
      - DB_NAME=nacos_config
    volumes:
      - ./nacos/init.d:/home/nacos/init.d
    networks:
      - smart-parking-net

  # PostgreSQL 数据库
  postgres:
    image: postgres:15
    ports:
      - "5432:5432"
    environment:
      - POSTGRES_PASSWORD=postgres123
      - POSTGRES_DB=master
    volumes:
      - ./data/postgres:/var/lib/postgresql/data
      - ./init-db:/docker-entrypoint-initdb.d
    networks:
      - smart-parking-net

  # Redis 缓存
  redis:
    image: redis:6-alpine
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes
    volumes:
      - ./data/redis:/data
    networks:
      - smart-parking-net

  # RabbitMQ 消息队列
  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"   # AMQP 端口
      - "15672:15672" # 管理界面
    environment:
      - RABBITMQ_DEFAULT_USER=admin
      - RABBITMQ_DEFAULT_PASS=admin123
    volumes:
      - ./data/rabbitmq:/var/lib/rabbitmq
    networks:
      - smart-parking-net

  # Nginx 反向代理
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf
      - ./nginx/conf.d:/etc/nginx/conf.d
      - ./ssl:/etc/nginx/ssl
    depends_on:
      - gateway
    networks:
      - smart-parking-net

networks:
  smart-parking-net:
    driver: bridge
```

### 6.2 Nginx 配置

**nginx.conf**:
```nginx
worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /tmp/nginx.pid;

events {
    worker_connections 1024;
    use epoll;
    multi_accept on;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';

    access_log /var/log/nginx/access.log main;

    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    types_hash_max_size 2048;

    client_max_body_size 10M;

    # Gzip 压缩
    gzip on;
    gzip_vary on;
    gzip_proxied any;
    gzip_comp_level 6;
    gzip_types text/plain text/css text/xml text/javascript 
               application/json application/javascript application/xml+rss 
               application/rss+xml font/truetype font/opentype application/vnd.ms-fontobject 
               image/svg+xml;

    # 上游服务器
    upstream gateway_server {
        server gateway:8080 max_fails=3 fail_timeout=30s;
    }

    # HTTPS 配置
    server {
        listen 443 ssl http2;
        server_name api.smartparking.com;

        ssl_certificate /etc/nginx/ssl/api.smartparking.com.crt;
        ssl_certificate_key /etc/nginx/ssl/api.smartparking.com.key;
        ssl_session_timeout 1d;
        ssl_session_cache shared:SSL:50m;
        ssl_protocols TLSv1.2 TLSv1.3;
        ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE:ECDH:AES:HIGH:!NULL:!aNULL:!MD5:!ADH:!RC4;
        ssl_prefer_server_ciphers on;

        # 安全头
        add_header Strict-Transport-Security "max-age=63072000" always;
        add_header X-Frame-Options DENY;
        add_header X-Content-Type-Options nosniff;

        location / {
            proxy_pass http://gateway_server;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            proxy_connect_timeout 60s;
            proxy_send_timeout 60s;
            proxy_read_timeout 60s;
        }

        # 健康检查
        location /health {
            access_log off;
            return 200 "healthy\n";
            add_header Content-Type text/plain;
        }
    }

    # HTTP 重定向到 HTTPS
    server {
        listen 80;
        server_name api.smartparking.com;
        return 301 https://$server_name$request_uri;
    }
}
```

---

## 7. 开发计划

### 7.1 总体时间线

```
Week 1-2:  ████████████████████  项目启动 + 基础设施搭建
Week 3-4:  ████████████████████  系统管理 + 车场管理
Week 5-6:  ████████████████████  SDK 对接 + 订单管理
Week 7-8:  ████████████████████  支付管理 + 报表模块
Week 9:    ████████████████████  小程序开发（并行）
Week 10:   ████████████████████  集成测试 + 试点部署
```

### 7.2 详细周计划

详见正文 4.8 节"P0 总体开发计划表"。

---

## 8. 资源投入与预算

### 8.1 人力资源

| 角色 | 人数 | 周期 | 人天 | 单价 (元/天) | 小计 (万元) |
|------|------|------|------|-------------|------------|
| 项目经理 | 1 | 10 周 | 50 | 2000 | 10.0 |
| 产品经理 | 1 | 6 周 | 30 | 1500 | 4.5 |
| 架构师 | 1 | 4 周 | 20 | 3000 | 6.0 |
| 后端开发 | 4 | 10 周 | 200 | 1500 | 30.0 |
| 前端开发 | 3 | 8 周 | 120 | 1200 | 14.4 |
| 测试工程师 | 2 | 4 周 | 40 | 1000 | 4.0 |
| 运维工程师 | 1 | 3 周 | 15 | 1500 | 2.25 |
| 实施工程师 | 2 | 2 周 | 20 | 1000 | 2.0 |
| **合计** | **14** | - | **495** | - | **73.15** |

### 8.2 硬件与软件

| 项目 | 规格 | 数量 | 单价 (元) | 小计 (万元) | 备注 |
|------|------|------|-----------|------------|------|
| 应用服务器 | 8 核 16G 500G SSD | 2 台 | 3000/月 | 0.6 | 按 2 个月计 |
| 数据库服务器 | 4 核 8G 1T SSD | 1 台 | 2000/月 | 0.4 | 按 2 个月计 |
| 海康道闸设备 | iDS-TMG402 | 4 套 | 8000 | 3.2 | 3-5 个试点 |
| LED 显示屏 | 单色条屏 | 4 块 | 1000 | 0.4 | 配套设备 |
| 网络交换机 | 千兆 8 口 | 2 台 | 500 | 0.1 | - |
| 施工布线 | - | 4 个点 | 2000 | 0.8 | 含人工 |
| 域名 SSL 证书 | 企业版 | 1 个 | 2000/年 | 0.2 | - |
| 聚合支付押金 | - | 1 个 | 5000 | 0.5 | 可退 |
| 小程序认证 | 腾讯官方 | 1 个 | 300 | 0.03 | - |
| **合计** | - | - | - | **6.23** | - |

### 8.3 P0 总预算

**总计**: **79.38 万元**

其中:
- 人力成本：73.15 万元（92.2%）
- 硬件软件：6.23 万元（7.8%）

---

## 9. 风险评估与应对

### 9.1 技术风险

| 风险 | 概率 | 影响 | 应对措施 |
|------|------|------|----------|
| 海康 SDK 兼容性 | 中 | 高 | Week 4 提前预研；准备手动录入备用方案 |
| PostgreSQL 性能 | 低 | 中 | 提前压测；优化索引和 SQL |
| 微服务通信故障 | 中 | 中 | Sentinel 熔断降级；重试机制 |
| Redis 缓存穿透 | 中 | 中 | 布隆过滤器；空值缓存 |

### 9.2 进度风险

| 风险 | 概率 | 影响 | 应对措施 |
|------|------|------|----------|
| 小程序审核被拒 | 中 | 高 | 提前准备资质；预留 1 周缓冲 |
| 多停车场并发 | 中 | 中 | Week 8 专项压测；Redis 优化 |
| 第三方接口延期 | 低 | 高 | _mock_ 接口先行；合同约束 |

### 9.3 实施风险

| 风险 | 概率 | 影响 | 应对措施 |
|------|------|------|----------|
| 现场网络不稳定 | 高 | 中 | 4G 备份；离线模式 |
| 试点配合度低 | 中 | 中 | 签订协议；明确责任 |
| 用户培训不到位 | 中 | 低 | 编制手册；视频教程 |

### 9.4 业务风险

| 风险 | 概率 | 影响 | 应对措施 |
|------|------|------|----------|
| 车主使用意愿低 | 中 | 高 | 首单免费；宣传推广 |
| 支付费率上涨 | 低 | 低 | P1 切换官方直连；多家比价 |

---

## 10. 验收标准

### 10.1 技术指标

- ✅ 系统可用性 ≥ 99%
- ✅ 车牌识别准确率 ≥ 95%（白天）、≥ 90%（夜间）
- ✅ 支付成功率 ≥ 98%
- ✅ API 响应时间 ≤ 500ms（P95）
- ✅ 支持并发用户数 ≥ 100
- ✅ 支持并发设备连接 ≥ 10

### 10.2 业务指标

- ✅ 3-5 个试点全部上线运营
- ✅ 日均停车订单 ≥ 500 单
- ✅ 电子支付渗透率 ≥ 80%
- ✅ 车主满意度 ≥ 85%
- ✅ 无重大资金安全问题

### 10.3 交付物清单

- ✅ Web 管理后台（6 大模块 50 个功能）
- ✅ 车主微信小程序（8 个功能）
- ✅ 海康 SDK 对接（4 套设备）
- ✅ 聚合支付对接（微信 + 支付宝）
- ✅ 技术文档（API 文档、部署文档、操作手册）
- ✅ 测试报告（功能测试、性能测试、安全测试）

---

## 附录

### 附录 A：术语表

| 术语 | 解释 |
|------|------|
| MVP | Minimum Viable Product，最小可行产品 |
| P0/P1/P2 | 优先级等级，P0 为最高优先级 |
| SDK | Software Development Kit，软件开发工具包 |
| T+0 | 交易当天完成资金清算 |
| LBS | Location Based Service，基于位置的服务 |
| JWT | JSON Web Token，一种令牌认证标准 |
| RBAC | Role-Based Access Control，基于角色的访问控制 |

### 附录 B：参考文档

1. 《0203智慧停车管理平台_采购询价功能清单.xlsx》
2. Spring Cloud Alibaba 官方文档
3. PostgreSQL 15 官方文档
4. 海康威视 HCNetSDK 开发指南
5. 微信小程序开发文档

---

**文档结束**
