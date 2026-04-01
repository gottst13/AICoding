-- =====================================================
-- 智慧停车管理平台 - 数据库初始化脚本
-- 文件：01-create-databases.sql
-- 说明：创建所有微服务所需的数据库
-- =====================================================

-- 用户服务数据库
CREATE DATABASE pg_user WITH ENCODING = 'UTF8';

-- 车场服务数据库
CREATE DATABASE pg_parking WITH ENCODING = 'UTF8';

-- 订单服务数据库
CREATE DATABASE pg_order WITH ENCODING = 'UTF8';

-- 支付服务数据库
CREATE DATABASE pg_payment WITH ENCODING = 'UTF8';

-- 设备服务数据库
CREATE DATABASE pg_device WITH ENCODING = 'UTF8';

-- 报表服务数据库
CREATE DATABASE pg_report WITH ENCODING = 'UTF8';

-- Nacos 配置中心数据库
CREATE DATABASE nacos_config WITH ENCODING = 'UTF8';

-- 验证数据库创建成功
\l

-- 输出提示信息
SELECT '数据库创建成功！' AS message;
SELECT '共创建 7 个数据库:' AS message;
SELECT datname FROM pg_database WHERE datname IN ('pg_user', 'pg_parking', 'pg_order', 'pg_payment', 'pg_device', 'pg_report', 'nacos_config');
