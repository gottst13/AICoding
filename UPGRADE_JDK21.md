# JDK 版本升级说明

## 📋 升级信息

**升级时间**: 2026-04-01  
**升级版本**: Java 17 → Java 21  
**JDK 路径**: `E:\java\jdk-21.0.10`

## 🔄 升级内容

### 1. 父 POM 更新
- ✅ `java.version`: 17 → **21**
- ✅ `spring-cloud.version`: 2021.0.5 → **2023.0.0** (支持 Spring Boot 3.2+)
- ✅ `spring-cloud-alibaba.version`: 2021.0.4.0 → **2023.0.0.0-RC1**
- ✅ `mybatis-plus.version`: 3.5.3 → **3.5.5**
- ✅ `postgresql.version`: 42.6.0 → **42.7.2**

### 2. Maven 配置
- ✅ `.mvn/jvm.config`: 指定 JDK 路径为 `E:\java\jdk-21.0.10`
- ✅ `.mvn/wrapper/maven-wrapper.properties`: Maven Wrapper 配置 (Maven 3.9.6)

### 3. 受影响的模块
所有微服务模块均已继承父 POM 配置:
- ✅ gateway (API 网关)
- ✅ user-service (用户服务)
- ✅ parking-service (车场服务)
- ✅ order-service (订单服务)
- ✅ payment-service (支付服务)
- ✅ device-service (设备服务)
- ✅ report-service (报表服务)

## 🚀 使用方法

### 方式一：使用批处理脚本 (推荐)
```bash
# 1. 设置 JDK 环境
set-jdk21.bat

# 2. 编译项目
mvn clean install

# 3. 启动服务 (以用户服务为例)
cd user-service
mvn spring-boot:run
```

### 方式二：手动设置环境变量
```bash
# PowerShell
$env:JAVA_HOME="E:\java\jdk-21.0.10"
$env:MAVEN_OPTS="-Djava.home=$env:JAVA_HOME"
mvn clean install

# CMD
set JAVA_HOME=E:\java\jdk-21.0.10
set MAVEN_OPTS=-Djava.home=%JAVA_HOME%
mvn clean install
```

### 方式三：在 IDE 中配置
**IntelliJ IDEA**:
1. File → Settings → Build, Execution, Deployment → Build Tools → Maven
2. 设置 `User settings file` 和 `Local repository`
3. File → Project Structure → SDKs → 添加 JDK 21 (`E:\java\jdk-21.0.10`)
4. 选择 SDK 为 JDK 21

## ⚠️ 注意事项

1. **JDK 必须预先安装**
   - 确保 `E:\java\jdk-21.0.10` 目录存在且包含完整的 JDK 文件
   - 可通过 `%JAVA_HOME%\bin\java -version` 验证

2. **Spring Cloud 版本兼容性**
   - Spring Cloud 2023.0.0 需要 Spring Boot 3.2.x
   - 如果编译报错，请检查依赖兼容性

3. **IDE 支持**
   - IntelliJ IDEA 2023.2+ 完整支持 Java 21
   - Eclipse 2023-09 (4.29) + 支持 Java 21

## 🔍 验证安装

```bash
# 检查 Java 版本
E:\java\jdk-21.0.10\bin\java -version

# 应该输出类似:
# java version "21.0.10" 2024-01-16 LTS
# Java(TM) SE Runtime Environment (build 21.0.10+8-LTS-241)
# Java HotSpot(TM) 64-Bit Server VM (build 21.0.10+8-LTS-241, mixed mode, sharing)
```

## 📦 Docker Compose 部署

如果使用 Docker 部署，JDK 版本不影响容器镜像，因为 Dockerfile 会单独指定基础镜像。

```bash
docker-compose up -d
```

## 🐛 常见问题

### 问题 1: 找不到 JDK
**解决**: 确认 `E:\java\jdk-21.0.10` 路径正确，或修改 `.mvn/jvm.config` 中的路径

### 问题 2: Maven 编译错误
**解决**: 
```bash
# 清理缓存并重新编译
mvn clean
mvn dependency:purge-local-repository
mvn install
```

### 问题 3: IDE 仍使用旧版本
**解决**: 在 IDE 中重新导入 Maven 项目并更新 SDK 设置

---

**升级完成日期**: 2026-04-01  
**执行人**: Smart Parking Team
