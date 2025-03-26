import { createPinia } from 'pinia';

// 创建Pinia实例
export const pinia = createPinia();

// 导出所有存储模块
export * from './auth'; 