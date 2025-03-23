@echo off
echo 清理项目...
call mvn clean

echo 编译项目...
call mvn compile -DskipTests

echo 编译测试文件...
call mvn test-compile -DskipTests

echo 运行所有单元测试...
call mvn -Dtest=com.sems.sportseventmanagementsystem.unit.*,com.sems.sportseventmanagementsystem.controller.SimpleUnitTest test

echo 测试完成，请检查结果

pause 