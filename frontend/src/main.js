import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './assets/css/index.css'
import App from './App.vue'
import router from './router'
import axios from 'axios'

// 配置axios默认URL和行为
axios.defaults.baseURL = 'http://localhost:25466'

// 创建应用
const app = createApp(App)

// 使用插件
app.use(createPinia())
app.use(router)
app.use(ElementPlus)

// 挂载应用
app.mount('#app') 