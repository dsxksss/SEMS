# 运动赛事管理系统 API 文档

## 概述

本文档描述了运动赛事管理系统的前端API调用和处理方案。由于后端API可能未完全实现，前端采用了一系列策略确保页面仍能正常展示和操作。

## API 状态

当前系统的API实现状态如下：

### 已实现API

- 用户认证相关API
- 赛事管理相关API
- 用户管理相关API
- 公告管理相关API

### 未完全实现API

- 统计报表相关API
  - `/stats/monthly-reports`
  - `/stats/registration-data`
  - `/stats/results-data` 
  - `/stats/user-activity-data`
- 证书管理相关API
  - `/certificates/event/{eventId}`
  - `/certificates/generate`
  - `/certificates/batch-print`
  - `/certificates/{certificateId}/preview`
  - `/certificates/{certificateId}/print`

## 前端解决方案

为了确保在API未完全实现的情况下前端页面仍能正常运行，系统采用了以下解决方案：

1. **数据模拟**：为不可用的API提供合理的模拟数据
2. **错误处理增强**：对所有API调用进行详细的错误捕获和处理
3. **数据验证**：验证API返回数据的格式和内容
4. **降级策略**：当API失败时提供合理的降级显示
5. **调试日志**：提供详细的控制台日志，帮助识别问题

## 使用说明

### 如何处理API错误

所有API调用都经过增强的错误处理，主要实现在以下函数中：

```typescript
const handleApiError = (error: unknown, defaultData: any): any => {
  // 详细的错误处理逻辑
  // ...
  return defaultData;
};
```

### 数据验证

对API返回的数据进行验证，确保符合预期格式：

```typescript
const validateData = <T>(data: any, defaultData: T, validator: (data: any) => boolean): T => {
  if (!data || !validator(data)) {
    console.warn('API返回的数据格式不正确，使用默认数据');
    return defaultData;
  }
  return data as T;
};
```

### 模拟数据

为未实现的API提供合理的模拟数据，位于`statsAPI.ts`文件中：

```typescript
const DEFAULT_REGISTRATION_DATA: RegistrationData = {
  // 模拟数据...
};
```

## 注意事项

1. 打印报表页面使用了增强的错误处理，即使后端API未完全实现，页面也能正常展示
2. 证书管理功能在后端API未实现时会使用模拟数据
3. 所有导出和打印功能如果后端未实现，会给出适当的提示

## 未来改进

1. 后端完成全部API实现后，移除前端的模拟数据
2. 增加更详细的API响应格式验证
3. 优化打印功能的用户体验 

## 最近改进

### 报表API加强 (2023-xx-xx)

为了改善报表和统计API的可靠性和用户体验，我们进行了以下优化：

1. **创建专用报表HTTP客户端**
   - 实现了单独的`reportClient.ts`，专门处理报表和证书相关API
   - 配置了更长的超时时间(30秒)和更多的重试次数(3次)
   - 添加了特殊的错误处理逻辑，避免报表API错误导致用户登出

2. **增强HTTP请求可靠性**
   - 为主要axios客户端添加了超时重试机制
   - 实现了错误计数和智能延迟重试策略
   - 针对报表和证书API添加了特殊的错误处理路径

3. **打印功能增强**
   - 优化了打印预览样式和布局
   - 添加了打印页眉和页脚
   - 实现了更可靠的证书打印流程，使用iframe确保正确渲染

4. **错误处理改进**
   - 更友好的错误消息
   - 对401/404等错误的特殊处理
   - 添加了调试日志和错误跟踪

这些改进使得报表生成和打印功能更加健壮，即使在后端API不完整或不可用的情况下也能为用户提供更好的体验。 