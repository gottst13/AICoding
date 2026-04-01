@echo off
echo ========================================
echo 智慧停车平台 - JDK 21 环境配置
echo ========================================
echo.

:: 设置 JDK 路径
set JAVA_HOME=E:\java\jdk-21.0.10
set MAVEN_OPTS=-Djava.home=%JAVA_HOME%

echo [√] JDK 路径已设置：%JAVA_HOME%
echo [√] MAVEN_OPTS: %MAVEN_OPTS%
echo.

:: 验证 Java 版本
echo 正在检查 Java 版本...
"%JAVA_HOME%\bin\java" -version
if errorlevel 1 (
    echo [!] 警告：无法在指定路径找到 JDK，请确认路径正确
    echo     路径：%JAVA_HOME%
) else (
    echo [√] Java 版本验证成功
)

echo.
echo ========================================
echo 环境配置完成!
echo 现在可以使用以下命令启动服务:
echo   mvn clean install
echo   mvn spring-boot:run
echo ========================================
