# 单元测试资源目录

这个目录包含了项目单元测试所需的各种资源文件。

## 单元测试类型

项目包含以下几种类型的单元测试：

1. **独立单元测试**：不依赖Spring上下文的纯Java测试
   - `SimpleUnitTest` - 基本断言测试
   - `SimpleStringTest` - 字符串操作测试

2. **模拟对象测试**：使用内部模拟对象的服务层测试
   - `MockUserServiceTest` - 用户服务测试
   - `EventRegistrationMockTest` - 赛事报名流程测试

3. **流程测试**：模拟系统完整工作流程的测试
   - `WorkflowTest` - 模拟赛事从创建到结束的完整流程

## 运行单元测试

项目提供了几个批处理脚本来运行不同的测试：

- `simple-unit-test.bat` - 运行简单单元测试
- `mock-user-test.bat` - 运行用户服务模拟测试
- `event-test.bat` - 运行赛事报名流程测试
- `workflow-test.bat` - 运行完整流程测试
- `run-all-unit-tests.bat` - 运行所有单元测试

这些测试不依赖Spring上下文，执行速度快，可靠性高，适合快速验证业务逻辑。 