import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { pinia } from './stores'
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import './style.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

// 创建Vue应用
const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 使用Vue Router
app.use(router)

// 使用Pinia状态管理
app.use(pinia)

// 使用Element Plus
app.use(ElementPlus, {
  locale: zhCn
})

// 挂载应用
app.mount('#app')
