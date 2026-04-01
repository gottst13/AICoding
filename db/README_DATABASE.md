# 🗄️ 数据库初始化指南

## 📋 数据库信息

- **数据库地址**: 192.168.3.43
- **端口**: 5432
- **数据库名**: smart_parking
- **用户名**: postgres
- **密码**: *Ab123456

---

## ✅ 方法 1: 使用 pgAdmin（推荐）

### Step 1: 打开 pgAdmin

```bash
# Windows 开始菜单搜索
pgAdmin 4
```

### Step 2: 添加数据库连接

1. 右键 **Servers** → **Create** → **Server**
2. **General** 标签页:
   - Name: `Smart Parking (192.168.3.43)`
3. **Connection** 标签页:
   - Host name/address: `192.168.3.43`
   - Port: `5432`
   - Maintenance database: `postgres`
   - Username: `postgres`
   - Password: `*Ab123456`
4. 点击 **Save**

### Step 3: 创建数据库（如果不存在）

```sql
-- 在 Query Tool 中执行
CREATE DATABASE smart_parking;
```

### Step 4: 执行迁移脚本

1. 展开 **Servers** → **Smart Parking (192.168.3.43)** → **Databases** → **smart_parking**
2. 右键 **smart_parking** → **Query Tool**
3. 复制粘贴以下文件内容:
   ```
   db/migration/V2.0__add_zone_support.sql
   ```
4. 点击 **Execute** (▶️ 按钮)

### Step 5: 执行测试数据脚本

1. 保持 Query Tool 打开
2. 复制粘贴以下文件内容:
   ```
   db/init_test_data.sql
   ```
3. 点击 **Execute** (▶️ 按钮)

✅ 看到 "✅ 测试数据初始化完成！" 提示即成功！

---

## ✅ 方法 2: 使用 psql 命令行

### 安装 PostgreSQL 客户端

```bash
# 下载并安装
https://www.postgresql.org/download/windows/

# 或使用 winget
winget install PostgreSQL.PostgreSQL.15
```

### 执行 SQL 脚本

```bash
# 1. 设置环境变量
$env:PGPASSWORD = "*Ab123456"

# 2. 执行迁移脚本
psql -h 192.168.3.43 -U postgres -d smart_parking -f "db/migration/V2.0__add_zone_support.sql"

# 3. 执行测试数据
psql -h 192.168.3.43 -U postgres -d smart_parking -f "db/init_test_data.sql"
```

---

## ✅ 方法 3: 使用 VSCode 插件

### 安装插件

1. 打开 VSCode
2. 扩展 → 搜索 **PostgreSQL**
3. 安装 **PostgreSQL** by Microsoft

### 添加连接

1. 按 `Ctrl+Shift+P` → 输入 **PostgreSQL: Attach Database**
2. 输入连接信息:
   ```
   Server: 192.168.3.43
   User: postgres
   Password: *Ab123456
   ```

### 执行 SQL

1. 右键数据库 → **New Query**
2. 复制 SQL 内容并执行

---

## 📊 验证数据

执行成功后，运行以下查询验证：

```sql
-- 查看车场
SELECT * FROM parking_lots;

-- 查看区域
SELECT id, name, code, total_spaces, available_spaces 
FROM parking_zones 
ORDER BY id;

-- 查看车位统计
SELECT 
    z.name as zone_name,
    COUNT(*) as total,
    SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as available
FROM parking_zones z
LEFT JOIN parking_spaces ps ON z.id = ps.zone_id
GROUP BY z.id, z.name;

-- 查看计费规则
SELECT rule_name, hourly_rate, daily_max, free_minutes 
FROM fee_rules 
WHERE is_active = true;
```

---

## 🎯 预期结果

### 车场数据
- ✅ 1 个车场：智慧停车示范场

### 区域数据
- ✅ B1 层商场区 (100 车位)
- ✅ B2 层办公区 (150 车位)
- ✅ 地面充电区 (50 车位)
- ✅ A 座子区域 (50 车位)

### 车位数据
- ✅ 小型车位：10 个
- ✅ 无障碍车位：2 个
- ✅ 充电车位：5 个
- ✅ 部分占用状态

### 计费规则
- ✅ 统一计费规则（小型车、大型车）
- ✅ 分区计费规则（各区域不同价格）

### 测试订单
- ✅ 正在停车中的订单
- ✅ 已完成的订单

---

## 🔧 常见问题

### Q1: 无法连接到数据库？

**检查清单**:
- [ ] 网络是否通畅？ `ping 192.168.3.43`
- [ ] PostgreSQL 服务是否运行？
- [ ] 防火墙是否允许 5432 端口？
- [ ] 用户名密码是否正确？

### Q2: 数据库不存在？

```sql
-- 先创建数据库
CREATE DATABASE smart_parking;
```

### Q3: 表不存在？

确保先执行了迁移脚本:
```
db/migration/V2.0__add_zone_support.sql
```

---

## 📝 下一步

数据初始化完成后：

1. ✅ 修改 application.yml 中的数据库密码
2. ✅ 启动后端服务
3. ✅ 访问 Swagger UI 测试 API
4. ✅ 查看实际效果

**祝您成功！** 🚀
