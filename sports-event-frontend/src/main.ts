import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import './style.css'
import './assets/styles/el-plus-overrides.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
// import { MotionPlugin } from '@vueuse/motion'

// 创建Vue应用
const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 使用Vue Router
app.use(router)

// 使用Pinia状态管理
app.use(createPinia())

// 使用Element Plus
app.use(ElementPlus, {
  locale: zhCn
})

// app.use(MotionPlugin)

// 挂载应用
app.mount('#app')
