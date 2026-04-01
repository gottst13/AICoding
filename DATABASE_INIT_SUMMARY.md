# 📦 数据库初始化完成清单

## ✅ 已创建的文件

### SQL 脚本
- ✅ `db/init_test_data.sql` - 测试数据初始化脚本（147 行）
- ✅ `db/execute_sql.ps1` - PowerShell 自动执行脚本（146 行）
- ✅ `db/README_DATABASE.md` - 详细数据库使用指南（210 行）

---

## 🗄️ 数据库信息

| 配置项 | 值 |
|--------|-----|
| **主机地址** | 192.168.3.43 |
| **端口** | 5432 |
| **数据库名** | smart_parking |
| **用户名** | postgres |
| **密码** | *Ab123456 |

---

## 🚀 快速执行（3 种方法）

### 方法 1: pgAdmin（最简单）✅推荐

```bash
1. 打开 pgAdmin 4
2. 连接到 192.168.3.43
3. 打开 smart_parking 数据库的 Query Tool
4. 复制 db/init_test_data.sql 内容
5. 点击 Execute (▶️)
```

✅ **优点**: 图形界面，可视化操作，适合所有人

---

### 方法 2: psql 命令行

```bash
# Windows PowerShell
$env:PGPASSWORD = "*Ab123456"
psql -h 192.168.3.43 -U postgres -d smart_parking -f "db/init_test_data.sql"
```

✅ **优点**: 自动化程度高，适合批量操作

---

### 方法 3: VSCode 插件

```bash
1. 安装 PostgreSQL 插件
2. 添加数据库连接
3. 右键 → New Query
4. 粘贴 SQL 内容并执行
```

✅ **优点**: 无需离开代码编辑器

---

## 📊 初始化数据概览

### 车场数据
```sql
智慧停车示范场 (PARK_001)
└── 总车位：500
└── 地址：北京市朝阳区示范路 1 号
```

### 区域数据
```
smart_parking
├── B1 层商场区 (ZONE_B1_MALL)
│   ├── 车位：100
│   └── 类型：商场区
├── B2 层办公区 (ZONE_B2_OFFICE)
│   ├── 车位：150
│   └── 类型：办公区
├── 地面充电区 (ZONE_G_CHARGE)
│   ├── 车位：50
│   └── 类型：充电区
└── A 座子区域 (ZONE_B2_A)
    ├── 父区域：B2 层办公区
    └── 车位：50
```

### 车位数据
```
总计：17 个测试车位
├── B1-A001 ~ B1-A005 (小型车，含 1 个占用)
├── B1-D001 ~ B1-D002 (无障碍车位)
├── B2-A001 ~ B2-A005 (小型车，含 2 个占用)
└── G-C001 ~ G-C005 (充电车位，含 1 个占用)
```

### 计费规则
```
统一计费:
├── 小型车：¥10/小时，¥100/天，免费 30 分钟
└── 大型车：¥20/小时，¥200/天，免费 30 分钟

分区计费:
├── B1 层商场区：¥12/小时，¥120/天，免费 15 分钟
├── B2 层办公区：¥8/小时，¥80/天，免费 30 分钟
└── 地面充电区：¥15/小时，¥150/天，免费 30 分钟
```

### 测试订单
```
订单 1: 京 A88888
├── 状态：停车中
├── 入场：2 小时前
└── 区域：B1 层商场区

订单 2: 京 B99999
├── 状态：已完成
├── 停车时长：3 小时
└── 费用：¥35.00
```

---

## 🔍 验证查询

执行以下 SQL 验证数据：

```sql
-- 1. 查看车场
SELECT name, code, total_capacity FROM parking_lots;

-- 2. 查看区域
SELECT name, code, total_spaces, available_spaces 
FROM parking_zones ORDER BY id;

-- 3. 查看车位
SELECT z.name as zone, ps.space_no, 
       CASE ps.status 
           WHEN 0 THEN '占用' 
           WHEN 1 THEN '空闲' 
       END as status
FROM parking_spaces ps
JOIN parking_zones z ON ps.zone_id = z.id
ORDER BY z.id, ps.space_no;

-- 4. 查看计费规则
SELECT rule_name, vehicle_type, hourly_rate, daily_max, free_minutes
FROM fee_rules
WHERE is_active = true
ORDER BY rule_type;
```

---

## ⚠️ 注意事项

### 1. 字符编码
确保使用 UTF-8 编码执行 SQL，避免中文乱码。

### 2. 执行顺序
必须先执行迁移脚本，再执行初始化脚本：
```
1️⃣ V2.0__add_zone_support.sql (建表)
2️⃣ init_test_data.sql (插入数据)
```

### 3. 幂等性
`init_test_data.sql` 可以重复执行，不会造成数据重复（使用了 INSERT）。

### 4. 生产环境
⚠️ **警告**: 此脚本仅用于测试环境！
生产环境请使用专门的初始化脚本。

---

## 🎯 下一步操作

### Step 1: 验证数据
```bash
# 使用 pgAdmin 或任何 PostgreSQL 客户端
# 执行验证查询
```

### Step 2: 修改配置
编辑 `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://192.168.3.43:5432/smart_parking
    username: postgres
    password: *Ab123456  # 确认密码正确
```

### Step 3: 启动服务
```bash
# 双击启动脚本
start.bat

# 或手动启动
cd parking-service
mvn spring-boot:run
```

### Step 4: 测试 API
访问 Swagger UI:
```
http://localhost:8080/swagger-ui.html

测试接口:
GET /api/v1/zones/tree     # 查看区域树
GET /api/v1/spaces         # 查看车位列表
```

---

## 🆘 需要帮助？

如果遇到任何问题：

1. **查看错误日志**
   - pgAdmin 的错误提示
   - PowerShell 的错误输出

2. **检查网络连接**
   ```bash
   ping 192.168.3.43
   telnet 192.168.3.43 5432
   ```

3. **验证数据库权限**
   ```sql
   -- 检查是否有写入权限
   GRANT ALL PRIVILEGES ON DATABASE smart_parking TO postgres;
   ```

4. **查看详细文档**
   - [README_DATABASE.md](file://f:\code\AICoding\coding\AICoding\db\README_DATABASE.md) - 完整数据库指南

---

## 📝 文件清单

所有相关文件：

- ✅ `db/init_test_data.sql` - 测试数据脚本
- ✅ `db/execute_sql.ps1` - PowerShell 执行脚本  
- ✅ `db/README_DATABASE.md` - 数据库使用指南
- ✅ `db/migration/V2.0__add_zone_support.sql` - 数据库迁移脚本
- ✅ `parking-service/src/main/resources/application.yml` - 停车服务配置
- ✅ `order-service/src/main/resources/application.yml` - 订单服务配置

---

**祝您顺利初始化数据库！** 🎉

*最后更新：2026-04-01*
