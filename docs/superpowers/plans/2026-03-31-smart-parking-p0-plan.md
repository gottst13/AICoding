# 智慧停车管理平台 P0 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 10 周内完成智慧停车管理平台 MVP 开发并上线试运营（3-5 个试点）

**Architecture:** 采用 Spring Cloud Alibaba 微服务架构，按业务领域拆分为 7 个独立服务（用户、车场、订单、支付、设备、报表、网关），通过 Nacos 实现服务注册与配置管理，PostgreSQL 15 作为主数据库，Redis 缓存，RabbitMQ 消息队列，Docker Compose 单机部署。

**Tech Stack:** 
- 后端：Spring Cloud Alibaba 2021.x, MyBatis Plus 3.5, PostgreSQL 15
- 前端：React 18 + Ant Design 4.x + Umi 4（Web 端）, Uni-app（小程序）
- 中间件：Redis 6.0, RabbitMQ 3.9, Nacos 2.0
- DevOps: Docker, Docker Compose, Jenkins, GitLab

---

## 📅 Sprint 规划总览

| Sprint | 周期 | 主题 | 主要交付物 | 里程碑 |
|--------|------|------|------------|--------|
| **Sprint 1** | Week 1-2 | 基础设施搭建 | 开发环境、数据库、Nacos、Gateway | ✅ 项目启动 |
| **Sprint 2** | Week 3-4 | 基础服务开发 | 用户服务、车场服务、小程序框架 | ✅ 车场管理完成 |
| **Sprint 3** | Week 5-6 | 核心业务开发 | 订单服务、SDK 对接、小程序首页 | ✅ SDK 对接完成 |
| **Sprint 4** | Week 7-8 | 支付与报表 | 支付服务、报表模块、小程序支付 | ✅ 支付管理完成 |
| **Sprint 5** | Week 9-10 | 集成测试与上线 | 全链路测试、试点部署、MVP 验收 | ✅ **MVP 上线** |

---

## 🔧 WBS 工作分解结构

### Sprint 1: 基础设施搭建（Week 1-2）

#### Task 1.1: 项目初始化与开发环境搭建

**Files:**
- Create: `pom.xml` (父工程)
- Create: `docker-compose.yml`
- Create: `README.md`
- Test: 无

- [ ] **Step 1: 创建 Maven 父工程 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.smartparking</groupId>
    <artifactId>smart-parking-parent</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>
    
    <name>Smart Parking Platform</name>
    <description>智慧停车管理平台</description>
    
    <modules>
        <module>gateway</module>
        <module>user-service</module>
        <module>parking-service</module>
        <module>order-service</module>
        <module>payment-service</module>
        <module>device-service</module>
        <module>report-service</module>
    </modules>
    
    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2021.0.5</spring-cloud.version>
        <spring-cloud-alibaba.version>2021.0.4.0</spring-cloud-alibaba.version>
        <mybatis-plus.version>3.5.3</mybatis-plus.version>
        <postgresql.version>42.6.0</postgresql.version>
    </properties>
    
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>${spring-cloud-alibaba.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

- [ ] **Step 2: 创建 docker-compose.yml**

参考 P0 设计文档第 6.1 节的完整配置

- [ ] **Step 3: 创建项目 README.md**

```markdown
# 智慧停车管理平台

## 快速开始

### 环境要求
- JDK 17
- Docker & Docker Compose
- PostgreSQL 15
- Redis 6.0

### 本地运行
docker-compose up -d

### 访问服务
- API Gateway: http://localhost:8080
- Nacos Console: http://localhost:8848/nacos
- RabbitMQ Console: http://localhost:15672
```

- [ ] **Step 4: 验证环境搭建成功**

```bash
docker-compose ps
# Expected: All services should be "Up"

curl http://localhost:8848/nacos/
# Expected: Nacos login page
```

- [ ] **Step 5: Commit**

```bash
git add pom.xml docker-compose.yml README.md
git commit -m "feat: 项目初始化和基础设施搭建"
```

---

#### Task 1.2: 数据库设计与初始化

**Files:**
- Create: `database/init-db/01-create-databases.sql`
- Create: `database/init-db/02-create-tables.sql`
- Test: 手动验证表结构

- [ ] **Step 1: 创建数据库初始化脚本**

```sql
-- 01-create-databases.sql
CREATE DATABASE pg_user WITH ENCODING = 'UTF8';
CREATE DATABASE pg_parking WITH ENCODING = 'UTF8';
CREATE DATABASE pg_order WITH ENCODING = 'UTF8';
CREATE DATABASE pg_payment WITH ENCODING = 'UTF8';
CREATE DATABASE pg_device WITH ENCODING = 'UTF8';
CREATE DATABASE pg_report WITH ENCODING = 'UTF8';
CREATE DATABASE nacos_config WITH ENCODING = 'UTF8';
```

- [ ] **Step 2: 创建用户服务表结构**

```sql
-- 02-create-tables.sql (pg_user 数据库)

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
    status SMALLINT DEFAULT 1,
    last_login_at TIMESTAMPTZ,
    last_login_ip VARCHAR(50),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_username ON users(username);
CREATE INDEX idx_wechat_openid ON users(wechat_openid);

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
('财务人员', 'FINANCE', '财务管理权限');
```

- [ ] **Step 3: 执行数据库初始化**

```bash
docker-compose exec postgres psql -U postgres -f /docker-entrypoint-initdb.d/01-create-databases.sql
docker-compose exec postgres psql -U postgres -f /docker-entrypoint-initdb.d/02-create-tables.sql
```

- [ ] **Step 4: 验证表结构**

```bash
docker-compose exec postgres psql -U postgres -d pg_user -c "\dt"
# Expected: 显示 users, roles 等表
```

- [ ] **Step 5: Commit**

```bash
git add database/
git commit -m "feat: 数据库表结构初始化"
```

---

#### Task 1.3: Nacos 配置中心搭建

**Files:**
- Modify: `docker-compose.yml:45-60` (Nacos 服务配置)
- Create: `config/nacos/application-common.yaml`
- Test: 访问 Nacos 控制台

- [ ] **Step 1: 配置 Nacos 数据源**

在 docker-compose.yml 中配置 PostgreSQL 数据源（已在 Task 1.1 完成）

- [ ] **Step 2: 创建公共配置文件**

```yaml
# config/nacos/application-common.yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:nacos:8848}
      config:
        server-addr: ${NACOS_SERVER_ADDR:nacos:8848}
        file-extension: yaml
        shared-configs:
          - data-id: application-common.yaml
            refresh: true

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

logging:
  level:
    root: INFO
    com.smartparking: DEBUG
```

- [ ] **Step 3: 导入配置到 Nacos**

```bash
# 使用 Nacos OpenAPI 导入配置
curl -X POST "http://localhost:8848/nacos/v1/cs/configs" \
  -d "dataId=application-common.yaml" \
  -d "group=DEFAULT_GROUP" \
  -d "content=$(cat config/nacos/application-common.yaml)"
```

- [ ] **Step 4: 验证 Nacos 配置**

访问 http://localhost:8848/nacos，登录后查看配置列表

- [ ] **Step 5: Commit**

```bash
git add config/
git commit -m "feat: Nacos 配置中心搭建"
```

---

### Sprint 2: 基础服务开发（Week 3-4）

#### Task 2.1: 用户服务开发

**Files:**
- Create: `user-service/pom.xml`
- Create: `user-service/src/main/java/com/smartparking/userservice/UserServiceApplication.java`
- Create: `user-service/src/main/java/com/smartparking/userservice/controller/UserController.java`
- Test: `user-service/src/test/java/com/smartparking/userservice/UserServiceTest.java`

- [ ] **Step 1: 创建用户服务 Maven 配置**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>com.smartparking</groupId>
        <artifactId>smart-parking-parent</artifactId>
        <version>1.0.0</version>
    </parent>
    
    <artifactId>user-service</artifactId>
    <name>User Service</name>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 2: 创建启动类**

```java
package com.smartparking.userservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.smartparking.userservice.mapper")
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

- [ ] **Step 3: 编写用户登录接口测试**

```java
package com.smartparking.userservice;

import com.smartparking.userservice.controller.UserController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserServiceTest {
    
    @Autowired
    private UserController userController;
    
    @Test
    public void testLogin() {
        // TODO: 实现登录测试
    }
}
```

- [ ] **Step 4: 实现用户登录接口**

```java
package com.smartparking.userservice.controller;

import com.smartparking.userservice.dto.LoginRequest;
import com.smartparking.userservice.dto.LoginResponse;
import com.smartparking.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }
    
    @PostMapping("/logout")
    public void logout(@RequestHeader("Authorization") String token) {
        userService.logout(token);
    }
}
```

- [ ] **Step 5: Commit**

```bash
git add user-service/
git commit -m "feat: 用户服务基础框架"
```

---

## 📊 关键路径分析

```
关键路径任务链:
Week 1-2:  数据库搭建 → Nacos 配置 → Gateway 网关
              ↓
Week 3-4:  用户服务 → 车场服务 → 车道管理
              ↓
Week 5-6:  SDK 对接 → 订单生成 → 计费引擎
              ↓
Week 7-8:  支付对接 → 回调处理 → 报表统计
              ↓
Week 9-10: 联调测试 → 试点部署 → MVP 验收
```

**关键路径总时长**: 10 周  
**缓冲时间**: 已包含在各任务评估中  
**非关键任务**: 小程序开发（可并行）、日志监控（P1 完善）

---

## 👥 资源分配计划

### Sprint 1-2: 基础设施（4 后端 +1 运维）
- 后端 A: 数据库设计与初始化
- 后端 B: Nacos 配置中心
- 后端 C: Gateway 网关
- 后端 D: Docker Compose 编排
- 运维：服务器环境准备

### Sprint 3-4: 基础服务（4 后端 +3 前端）
- 后端 A/B: 用户服务
- 后端 C/D: 车场服务
- 前端 A: Web 管理后台 UI 框架
- 前端 B/C: 小程序框架搭建

### Sprint 5-6: 核心业务（4 后端 +3 前端 +1 测试）
- 后端 A/B: 订单服务
- 后端 C: SDK 对接（技术攻关）
- 后端 D: 设备服务
- 前端 A/B: 小程序功能开发
- 前端 C: Web 端车场管理界面
- 测试：编写测试用例

### Sprint 7-8: 支付与报表（4 后端 +3 前端 +2 测试）
- 后端 A/B: 支付服务
- 后端 C/D: 报表服务
- 前端 A/B: 小程序支付功能
- 前端 C: Web 端报表界面
- 测试：功能测试、性能测试

### Sprint 9-10: 集成上线（全员参与）
- 后端×4: 现场部署、设备调试
- 前端×2: 现场支持、Bug 修复
- 测试×2: 验收测试
- 实施×2: 用户培训

---

## ⚠️ 风险管理计划

| 风险 | 等级 | 应对措施 | 责任人 | 触发条件 |
|------|------|----------|--------|----------|
| 海康 SDK 兼容性问题 | 高 | Week 4 提前预研；准备手动录入备用方案 | 后端 C | 设备无法正常识别车牌 |
| 小程序审核延期 | 中 | 提前 2 周提交审核；准备 H5 备用方案 | 前端 A | 审核状态超过 7 天未通过 |
| 多停车场并发性能 | 中 | Week 8 专项压测；Redis 缓存优化 | 后端 D | 响应时间>1s |
| 聚合支付接口不稳定 | 低 | 准备多家服务商备选；降级为静态二维码 | 后端 A | 支付失败率>5% |
| 试点网络不稳定 | 高 | 4G 备份方案；离线模式支持 | 实施 A | 网络中断>30 分钟 |

---

## ✅ 质量保证计划

### 代码审查
- **日常 Review**: 每日站会后 15 分钟代码互评
- **周 Review**: 每周五下午集体 Code Review
- **Merge Request**: 所有代码必须经过 MR 才能合并

### 测试策略
- **单元测试**: 覆盖率≥80%（Jacoco 检查）
- **集成测试**: 每个 Sprint 末进行
- **性能测试**: Week 8 专项压测（JMeter）
- **安全测试**: Week 9 渗透测试

### 持续集成
```yaml
# .gitlab-ci.yml
stages:
  - build
  - test
  - deploy

build:
  stage: build
  script:
    - mvn clean package
  artifacts:
    paths:
      - target/*.jar

test:
  stage: test
  script:
    - mvn test
    - mvn jacoco:check

deploy:
  stage: deploy
  script:
    - docker-compose up -d
  only:
    - main
```

---

## 📞 沟通管理计划

### 内部沟通
- **每日站会**: 9:30 AM，15 分钟，同步进度
- **周例会**: 周一上午，1 小时，周计划
- **技术分享**: 双周一次，周四下午

### 外部沟通
- **周报**: 每周五提交甲方项目组
- **月汇报**: 每月最后一个周五，现场汇报
- **阶段评审**: 每个 Sprint 结束前邀请甲方评审

### 应急沟通
- **紧急联系群**: 微信群 7×24 小时响应
- **重大问题**: 2 小时内响应，4 小时解决方案
- **系统故障**: 15 分钟响应，1 小时恢复

---

## 🎯 里程碑节点

| 里程碑 | 时间 | 交付物 | 验收标准 |
|--------|------|--------|----------|
| M1: 项目启动 | Week 2 | 技术方案、DB 设计、环境就绪 | 评审通过 |
| M2: 车场管理完成 | Week 4 | 停车场 CRUD、车道管理 | 功能测试通过 |
| M3: SDK 对接完成 | Week 6 | 车牌识别、道闸控制 | 设备联调成功 |
| M4: 支付管理完成 | Week 8 | 聚合支付、对账功能 | 真实支付测试 |
| M5: MVP 上线 | Week 10 | 3-5 个试点运营 | 日均订单≥500 |

---

**Plan Status**: Ready for execution  
**Next Step**: 选择执行方式（Subagent-Driven 或 Inline Execution）
