# 体育赛事管理系统

基于 Spring Boot + Vue 技术的体育赛事管理系统，旨在提升体育赛事管理的智能化、数字化水平，优化资源配置，增强用户体验。

## 技术栈

### 后端
- Spring Boot 3.x
- Spring Security + JWT 认证
- Spring Data JPA
- MySQL 数据库
- Swagger API 文档 (SpringDoc)

### 前端
- Vue.js 3.x + Vite
- Element Plus UI 组件库
- Vue Router
- Pinia 状态管理
- Axios HTTP 客户端

## 项目功能模块

本系统主要包含以下功能模块：

1. **管理员功能模块**：管理员登录进入体育赛事管理系统可以对系统首页、个人中心、用户管理、赛事项目管理、运动员报名管理、报表汇总打印管理、赛事结果管理等信息，进行相应操作。

2. **用户管理模块**：负责用户账号的注册、登录与管理工作，并通过选择的不同定义不同的用户角色(观众与运动员)。

3. **观众管理模块**：登录到系统后可以对系统首页、观众报名、个人中心等功能进行操作。

4. **运动员管理模块**：登录到系统后可以对系统首页、运动员报名个人中心等功能进行操作。

5. **首页模块**：网站首页页面主要包括首页、比赛信息、公告信息、后台管理、个人中心、注册登录等内容，并根据需要进行操作。

## 快速开始

### 环境要求
- JDK 17 或更高版本
- Maven 3.6 或更高版本
- MySQL 8.0 或更高版本
- Node.js 16 或更高版本（前端开发）

### 后端启动步骤

1. 克隆项目到本地

```
git clone https://github.com/your-username/sports-event-management-system.git
```

2. 创建 MySQL 数据库

```sql
CREATE DATABASE sems DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. 修改配置文件
   
   编辑 `src/main/resources/application.yml` 文件，根据你的数据库设置修改：

```yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sems?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf-8
    username: your-username
    password: your-password
```

4. 构建并运行项目

```bash
mvn clean install
java -jar target/sports-event-management-system-0.0.1-SNAPSHOT.jar
```

或者直接运行：

```bash
mvn spring-boot:run
```

5. 访问 API 文档
   
   启动应用后，访问 [http://localhost:8080/api/swagger-ui.html](http://localhost:8080/api/swagger-ui.html) 查看 API 文档。

### 初始账号信息

系统初始化时会自动创建以下账号：

- 管理员账号：
  - 用户名：admin
  - 密码：admin123

- 运动员账号：
  - 用户名：athlete
  - 密码：athlete123

- 观众账号：
  - 用户名：spectator
  - 密码：spectator123

## API 接口说明

系统提供了完整的 RESTful API，主要包括：

- 认证接口：`/api/auth/**`
- 用户管理接口：`/api/users/**`
- 赛事管理接口：`/api/events/**`
- 赛事分类接口：`/api/categories/**`
- 报名管理接口：`/api/registrations/**`
- 赛事结果接口：`/api/results/**`
- 公告管理接口：`/api/announcements/**`
- 文件上传接口：`/api/files/**`

详细的 API 文档请查看 Swagger UI。

## 许可证

本项目采用 [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0) 许可证。 