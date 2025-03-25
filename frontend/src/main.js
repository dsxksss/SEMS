import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import axios from 'axios'
import { useUserStore } from './stores/user'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './assets/css/index.css'

// 创建应用实例
const app = createApp(App)

// 注册 Pinia 状态管理
const pinia = createPinia()
app.use(pinia)

// 设置 API 基础 URL - 使用固定的/api路径
axios.defaults.baseURL = '/api'

// 在应用挂载前初始化用户状态
const userStore = useUserStore()
// 尝试从本地存储恢复用户会话
if (userStore.token) {
  // 设置请求拦截器中的认证头
  axios.defaults.headers.common['Authorization'] = `Bearer ${userStore.token}`
  // 加载用户信息
  userStore.loadUser().catch(console.error)
}

// 使用插件
app.use(router)
app.use(ElementPlus)

// 挂载应用
app.mount('#app') 