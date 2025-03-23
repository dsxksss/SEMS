# 体育赛事管理系统

## 项目简介

体育赛事管理系统是一个用于管理体育赛事的平台，支持赛事创建、报名管理、用户管理等功能。

## 测试说明

本项目包含多种测试类型，主要分为单元测试和集成测试。为了方便测试执行，我们提供了多个批处理脚本：

### 单元测试脚本

这些测试不依赖Spring上下文，可以快速执行并验证基本功能：

- `simple-test.bat` - 运行基本断言测试
- `mock-user-test.bat` - 运行用户服务模拟测试
- `event-test.bat` - 运行赛事报名流程测试
- `workflow-test.bat` - 运行完整流程测试
- `calendar-event-test.bat` - 运行赛事日历功能测试
- `integration-flow-test.bat` - 运行集成流程测试
- `run-all-unit-tests.bat` - 运行所有单元测试

### 测试类介绍

1. **SimpleTest** - 最基本的单元测试，验证JUnit框架正常工作
2. **WorkflowTest** - 模拟赛事从创建到结束的完整流程测试
3. **MockUserServiceTest** - 用户服务的模拟测试，测试用户注册、状态更新等功能
4. **EventRegistrationMockTest** - 赛事和报名流程的完整模拟测试
5. **IntegrationFlowTest** - 用户-赛事-报名完整流程的集成测试
6. **CalendarEventTest** - 赛事日历功能的测试

## 如何运行测试

### 使用批处理脚本

双击对应的批处理脚本即可运行特定测试。例如，要运行基本测试：

```
double-click simple-test.bat
```

### 使用Maven命令

也可以直接使用Maven命令运行特定测试：

```
mvn -Dtest=SimpleTest test
```

要运行所有单元测试：

```
mvn -Dtest=com.sems.sportseventmanagementsystem.unit.* test
```

## 注意事项

1. 确保已安装Java和Maven
2. 确保已配置好环境变量
3. 单元测试与集成测试相互独立，可以单独运行 