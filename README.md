# 智慧停车管理平台

> Smart Parking Management Platform - P0 MVP

**版本**: 1.0.0  
**技术栈**: Spring Cloud Alibaba + PostgreSQL 15 + Docker Compose  
**目标**: 10 周内完成 MVP 开发并上线试运营 (3-5 个试点)

---

## 📋 项目概述

智慧停车管理平台是一个覆盖封闭停车场和道路泊位的智能化停车管理系统，实现车主、运营方、监管方的多方共赢。

### P0 阶段目标
- ✅ 实施模式：多点并行 (3-5 个试点)
- ✅ 支持场景：封闭停车场 + 路侧泊位
- ✅ 完整流程：入场→停车→缴费→出场
- ✅ 处理能力：日均订单≥500 单
- ✅ 系统可用性：≥99%

### P0 功能范围
**包含**:
- Web 管理后台：6 大子系统，50 个功能点
- 车主微信小程序：8 个核心功能
- 海康 SDK 对接：基础车牌识别 + 道闸控制
- 聚合支付对接：微信 + 支付宝
- 试点部署：3-5 个点

**不包含**:
- ❌ POS 端功能 (P1 阶段)
- ❌ 包期业务 (P1 阶段)
- ❌ 营销中心 (P1 阶段)
- ❌ 领导驾驶舱 (P2 阶段)

---

## 🏗️ 技术架构

### 微服务拆分
采用 Spring Cloud Alibaba 微服务架构，按业务领域拆分为 7 个独立服务:

| 服务名称 | 端口 | 职责 | 数据库 |
|---------|------|------|--------|
| gateway | 8080 | API 网关、认证鉴权 | Redis |
| user-service | 8081 | 用户管理、角色权限、登录认证 | pg_user |
| parking-service | 8082 | 停车场、车道、收费规则管理 | pg_parking |
| order-service | 8083 | 订单创建、状态流转、计费计算 | pg_order |
| payment-service | 8084 | 支付对接、退款、对账 | pg_payment |
| device-service | 8085 | 海康 SDK 对接、设备状态监控 | pg_device |
| report-service | 8086 | 统计分析、报表生成 | pg_report + Redis |

### 技术栈详情
```yaml
后端：Spring Cloud Alibaba 2021.x, MyBatis Plus 3.5, Java 17
数据库：PostgreSQL 15 (主从复制 + 分区表)
缓存：Redis 6.0 (集群模式)
消息队列：RabbitMQ 3.9
配置中心：Nacos 2.0
容器化：Docker + Docker Compose
```

---

## 🚀 快速开始

### 环境要求
- **JDK**: 17+
- **Docker**: 20.10+
- **Docker Compose**: 2.0+
- **Maven**: 3.8+

### 一键启动所有服务

```bash
# 启动基础设施 (数据库、缓存、消息队列、配置中心)
docker-compose up -d postgres redis rabbitmq nacos

# 等待 30 秒让基础设施就绪
timeout /t 30

# 启动所有微服务
docker-compose up -d gateway user-service parking-service order-service payment-service device-service report-service
```

### 验证服务状态

```bash
# 查看所有服务状态
docker-compose ps

# 预期输出：所有服务状态为 "Up"

# 测试 Nacos 控制台
curl http://localhost:8848/nacos/

# 测试网关健康检查
curl http://localhost:8080/actuator/health
```

### 访问服务

| 服务 | 地址 | 说明 |
|------|------|------|
| API Gateway | http://localhost:8080 | 统一 API 入口 |
| Nacos Console | http://localhost:8848/nacos | 配置中心 (nacos/nacos) |
| RabbitMQ Console | http://localhost:15672 | 消息队列管理 (admin/admin123) |
| PostgreSQL | localhost:5432 | 数据库 (postgres/postgres123) |
| Redis | localhost:6379 | 缓存 |

---

## 📁 项目结构

```
smart-parking-platform/
├── gateway/                    # API 网关服务
├── user-service/              # 用户服务
├── parking-service/           # 车场服务
├── order-service/             # 订单服务
├── payment-service/           # 支付服务
├── device-service/            # 设备服务
├── report-service/            # 报表服务
├── database/
│   └── init-db/              # 数据库初始化脚本
│       ├── 01-create-databases.sql
│       └── 02-create-tables.sql
├── config/
│   └── nacos/                # Nacos 配置文件
│       └── application-common.yaml
├── docker-compose.yml         # Docker 编排配置
├── pom.xml                    # Maven 父工程配置
└── README.md                  # 项目说明文档
```

---

## 📊 开发计划

### Sprint 1 (Week 1-2): 基础设施搭建
- [x] Task 1.1: 项目初始化与开发环境搭建
- [ ] Task 1.2: 数据库设计与初始化
- [ ] Task 1.3: Nacos 配置中心搭建

### Sprint 2 (Week 3-4): 基础服务开发
- [ ] Task 2.1: 用户服务开发
- [ ] Task 2.2: 车场服务开发

### Sprint 3 (Week 5-6): 核心业务开发
- [ ] Task 3.1: 订单服务开发
- [ ] Task 3.2: SDK 对接

### Sprint 4 (Week 7-8): 支付与报表
- [ ] Task 4.1: 支付服务开发
- [ ] Task 4.2: 报表服务开发

### Sprint 5 (Week 9-10): 集成测试与上线
- [ ] Task 5.1: 全链路集成测试
- [ ] Task 5.2: 试点部署
- [ ] Task 5.3: MVP 验收

---

## 🔧 常用命令

### Docker 相关

```bash
# 启动所有服务
docker-compose up -d

# 停止所有服务
docker-compose down

# 查看服务日志
docker-compose logs -f gateway

# 重启某个服务
docker-compose restart parking-service

# 重新构建镜像
docker-compose build --no-cache
```

### Maven 相关

```bash
# 清理并编译所有模块
mvn clean install

# 跳过测试编译
mvn clean install -DskipTests

# 编译单个模块
mvn clean install -pl parking-service -am
```

---

## 📝 数据库配置

### 分库策略
每个微服务使用独立数据库:

```sql
CREATE DATABASE pg_user;
CREATE DATABASE pg_parking;
CREATE DATABASE pg_order;
CREATE DATABASE pg_payment;
CREATE DATABASE pg_device;
CREATE DATABASE pg_report;
CREATE DATABASE nacos_config;
```

### 连接信息
- **Host**: localhost:5432
- **Username**: postgres
- **Password**: postgres123

---

## ⚠️ 常见问题

### 1. 端口冲突
如果端口被占用，修改 `docker-compose.yml` 中的端口映射:

```yaml
ports:
  - "8081:8081"  # 改为其他可用端口
```

### 2. Docker 启动失败
检查 Docker 是否正常运行:

```bash
docker version
docker-compose version
```

### 3. 数据库初始化失败
确保 PostgreSQL 容器完全启动后再执行脚本:

```bash
docker-compose logs postgres
```

---

## 📞 联系方式

- **项目经理**:待定
- **技术负责人**:待定
- **项目文档**:`docs/` 目录

---

## 📄 许可证

内部项目，仅限公司内部使用
