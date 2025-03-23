@echo off
echo 清理项目...
call mvn clean

echo 编译项目...
call mvn compile -DskipTests

echo 编译测试文件...
call mvn test-compile -DskipTests

echo 运行简单单元测试...
call mvn -Dtest=SimpleUnitTest test

pause 