declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

// 声明Element Plus组件库的语言包
declare module 'element-plus/es/locale/lang/zh-cn' {
  const zhCn: any
  export default zhCn
} 