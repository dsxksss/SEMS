@echo off
chcp 65001 > nul
@REM echo 清理项目...
@REM call .\mvnw.cmd clean

echo 编译项目...
call .\mvnw.cmd compile -DskipTests

echo 编译测试文件...
call .\mvnw.cmd test-compile -DskipTests

echo 运行简单测试...
call .\mvnw.cmd -Dtest=SimpleTest test

pause 