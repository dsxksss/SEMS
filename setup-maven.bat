@echo off
echo 正在配置Maven环境...

:: 检查必要文件
if not exist ".mvn\wrapper\maven-wrapper.properties" (
  echo 错误：缺少 .mvn\wrapper\maven-wrapper.properties 文件
  exit /b 1
)

:: 下载或复制maven-wrapper.jar
if not exist ".mvn\wrapper\maven-wrapper.jar" (
  echo 正在下载 maven-wrapper.jar...
  mkdir ".mvn\wrapper" 2>nul
  powershell -Command "& { Invoke-WebRequest -Uri 'https://maven.aliyun.com/repository/public/org/apache/maven/wrapper/maven-wrapper/3.1.0/maven-wrapper-3.1.0.jar' -OutFile '.mvn\wrapper\maven-wrapper.jar' }"
)

:: 创建mvnw.cmd
if not exist "mvnw.cmd" (
  echo 正在创建 mvnw.cmd...
  (
    echo @echo off
    echo setlocal
    echo set MAVEN_PROJECTBASEDIR=%%CD%%
    echo if exist "%%MAVEN_PROJECTBASEDIR%%\.mvn\jvm.config" goto jvmConfig
    echo :jvmConfig
    echo set JAVA_HOME=%%JAVA_HOME%%
    echo if "%%JAVA_HOME%%"=="" goto noJavaHome
    echo set JAVA_EXE=%%JAVA_HOME%%\bin\java.exe
    echo goto init
    echo :noJavaHome
    echo echo 错误：未设置JAVA_HOME环境变量
    echo goto error
    echo :init
    echo set WRAPPER_JAR=%%MAVEN_PROJECTBASEDIR%%\.mvn\wrapper\maven-wrapper.jar
    echo "%%JAVA_EXE%%" -jar "%%WRAPPER_JAR%%" %%*
    echo if ERRORLEVEL 1 goto error
    echo goto end
    echo :error
    echo echo 发生错误，请检查以上输出
    echo exit /b 1
    echo :end
    echo endlocal
  ) > mvnw.cmd
)

echo Maven环境配置完成！
echo 运行命令示例：mvnw.cmd spring-boot:run
echo.
exit /b 0 