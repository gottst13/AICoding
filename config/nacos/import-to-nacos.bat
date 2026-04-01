@echo off
chcp 65001 >nul
echo ========================================
echo   智慧停车管理平台 - Nacos 配置导入脚本
echo ========================================
echo.

set NACOS_URL=http://localhost:8848/nacos
set CONFIG_FILE=config\nacos\application-common.yaml

echo [信息] Nacos 地址：%NACOS_URL%
echo [信息] 配置文件：%CONFIG_FILE%
echo.

REM 检查文件是否存在
if not exist "%CONFIG_FILE%" (
    echo [错误] 配置文件不存在：%CONFIG_FILE%
    pause
    exit /b 1
)

echo [执行] 正在导入配置到 Nacos...
echo.

REM 使用 curl 导入配置
curl -X POST "%NACOS_URL%/v1/cs/configs" ^
  -d "dataId=application-common.yaml" ^
  -d "group=DEFAULT_GROUP" ^
  -d "content=@%CONFIG_FILE%"

echo.
echo ========================================
echo   配置导入完成！
echo ========================================
echo.
echo 请访问 Nacos 控制台验证：
echo http://localhost:8848/nacos
echo.
echo 登录账号：nacos/nacos
echo.

pause
