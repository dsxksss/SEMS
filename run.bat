@echo off
echo 正在启动Spring Boot应用...

:: 检查Java环境
if "%JAVA_HOME%"=="" (
  echo 警告：未设置JAVA_HOME环境变量，尝试使用系统PATH中的Java
)

:: 检查Maven Wrapper文件
if not exist "mvnw.cmd" (
  echo 未找到mvnw.cmd，正在运行安装脚本...
  call setup-maven.bat
  if ERRORLEVEL 1 (
    echo Maven环境配置失败，无法继续。
    exit /b 1
  )
)

:: 运行应用
echo 正在运行Spring Boot应用...
call mvnw.cmd spring-boot:run
if ERRORLEVEL 1 (
  echo 应用启动失败，请检查以上错误信息。
  exit /b 1
)

exit /b 0 