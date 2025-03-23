@echo off
echo 清理项目...
call mvn clean

echo 编译项目...
call mvn compile -DskipTests

echo 编译测试文件...
call mvn test-compile -DskipTests

echo 运行模拟用户服务测试...
call mvn -Dtest=MockUserServiceTest test

pause 