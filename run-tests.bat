@echo off
chcp 65001 > nul
@REM echo 清理项目...
@REM call .\mvnw.cmd clean

echo 编译项目...
call .\mvnw.cmd compile -DskipTests

echo 创建简单的测试类...
if not exist src\test\java\com\sems\sportseventmanagementsystem\controller\WebMvcPingTest.java (
  echo package com.sems.sportseventmanagementsystem.controller; > src\test\java\com\sems\sportseventmanagementsystem\controller\WebMvcPingTest.java
  echo. >> src\test\java\com\sems\sportseventmanagementsystem\controller\WebMvcPingTest.java
  echo import org.junit.jupiter.api.Test; >> src\test\java\com\sems\sportseventmanagementsystem\controller\WebMvcPingTest.java
  echo import org.springframework.beans.factory.annotation.Autowired; >> src\test\java\com\sems\sportseventmanagementsystem\controller\WebMvcPingTest.java
  echo import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest; >> src\test\java\com\sems\sportseventmanagementsystem\controller\WebMvcPingTest.java
  echo import org.springframework.test.web.servlet.MockMvc; >> src\test\java\com\sems\sportseventmanagementsystem\controller\WebMvcPingTest.java
  echo import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get; >> src\test\java\com\sems\sportseventmanagementsystem\controller\WebMvcPingTest.java
  echo import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status; >> src\test\java\com\sems\sportseventmanagementsystem\controller\WebMvcPingTest.java
  echo. >> src\test\java\com\sems\sportseventmanagementsystem\controller\WebMvcPingTest.java
  echo @WebMvcTest^(PingController.class^) >> src\test\java\com\sems\sportseventmanagementsystem\controller\WebMvcPingTest.java
  echo public class WebMvcPingTest { >> src\test\java\com\sems\sportseventmanagementsystem\controller\WebMvcPingTest.java
  echo. >> src\test\java\com\sems\sportseventmanagementsystem\controller\WebMvcPingTest.java
  echo     @Autowired >> src\test\java\com\sems\sportseventmanagementsystem\controller\WebMvcPingTest.java
  echo     private MockMvc mockMvc; >> src\test\java\com\sems\sportseventmanagementsystem\controller\WebMvcPingTest.java
  echo. >> src\test\java\com\sems\sportseventmanagementsystem\controller\WebMvcPingTest.java
  echo     @Test >> src\test\java\com\sems\sportseventmanagementsystem\controller\WebMvcPingTest.java
  echo     public void pingTest^(^) throws Exception { >> src\test\java\com\sems\sportseventmanagementsystem\controller\WebMvcPingTest.java
  echo         mockMvc.perform^(get^("/ping"^)^) >> src\test\java\com\sems\sportseventmanagementsystem\controller\WebMvcPingTest.java
  echo                 .andExpect^(status^(^).isOk^(^)^); >> src\test\java\com\sems\sportseventmanagementsystem\controller\WebMvcPingTest.java
  echo     } >> src\test\java\com\sems\sportseventmanagementsystem\controller\WebMvcPingTest.java
  echo } >> src\test\java\com\sems\sportseventmanagementsystem\controller\WebMvcPingTest.java
)

echo 编译测试文件...
call .\mvnw.cmd test-compile -DskipTests

echo 运行全部API测试...
call .\mvnw.cmd -Dtest=com.sems.sportseventmanagementsystem.controller.* test

pause 