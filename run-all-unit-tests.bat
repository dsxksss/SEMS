@echo off
chcp 65001 > nul
@REM echo 清理项目...
@REM call .\mvnw.cmd clean

echo 编译项目...
call .\mvnw.cmd compile -DskipTests

echo 编译测试文件...
call .\mvnw.cmd test-compile -DskipTests

echo 运行所有单元测试...
call .\mvnw.cmd -Dtest=com.sems.sportseventmanagementsystem.unit.*,com.sems.sportseventmanagementsystem.controller.SimpleUnitTest test

echo 测试完成，请检查结果

pause 