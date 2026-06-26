@echo off
chcp 65001 >nul
echo ============================================
echo   牧集 APP 数据库初始化脚本
echo ============================================
echo.

cd /d "%~dp0"

echo 请确保已启动 PostgreSQL 服务
echo 连接信息：
echo   主机: localhost
echo   端口: 5432
echo   用户: postgres
echo   数据库: mooket_db
echo.

set /p PASSWORD=请输入 postgres 用户密码 [123456]:
if "%PASSWORD%"=="" set PASSWORD=123456

echo.
echo 正在创建数据库 mooket_db...
echo.
"psql" -h localhost -p 5432 -U postgres -c "CREATE DATABASE mooket_db;" 2>nul
if %errorlevel% neq 0 (
    echo 数据库可能已存在，跳过创建...
)

echo.
echo 正在执行建表脚本...
echo.

"psql" -h localhost -p 5432 -U postgres -d mooket_db -f "01_dict_product.sql" -w
"psql" -h localhost -p 5432 -U postgres -d mooket_db -f "02_dict_factory.sql" -w
"psql" -h localhost -p 5432 -U postgres -d mooket_db -f "03_dict_brand.sql" -w
"psql" -h localhost -p 5432 -U postgres -d mooket_db -f "04_dict_merchant.sql" -w
"psql" -h localhost -p 5432 -U postgres -d mooket_db -f "05_biz_offer.sql" -w
"psql" -h localhost -p 5432 -U postgres -d mooket_db -f "06_rel_user_merchant.sql" -w
"psql" -h localhost -p 5432 -U postgres -d mooket_db -f "07_stat_merchant.sql" -w

echo.
echo ============================================
echo   初始化完成！
echo ============================================
echo.

"psql" -h localhost -p 5432 -U postgres -d mooket_db -c "SELECT 'dict_product' as table_name, COUNT(*) as count FROM dict_product UNION ALL SELECT 'dict_factory', COUNT(*) FROM dict_factory UNION ALL SELECT 'dict_brand', COUNT(*) FROM dict_brand UNION ALL SELECT 'dict_merchant', COUNT(*) FROM dict_merchant UNION ALL SELECT 'biz_offer', COUNT(*) FROM biz_offer;" -w

pause
