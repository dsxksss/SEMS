import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 3000,
    hmr: true,
    open: true,
    cors: true,
    host: '0.0.0.0',
    proxy: {
      '/api': {
        target: 'http://localhost:25466',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api'),
        configure: (proxy, options) => {
          proxy.options.proxyTimeout = 30000
          proxy.options.timeout = 30000
          
          proxy.on('error', (err, req, res) => {
            console.log('代理错误:', err)
            if (!res.headersSent) {
              res.writeHead(500, {
                'Content-Type': 'application/json',
              })
              res.end(JSON.stringify({ 
                message: '代理请求失败，可能是后端服务未启动或网络问题',
                error: err.message,
                code: 500
              }))
            }
          })
          
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('发送请求到目标服务器:', req.method, req.url)
          })
          
          proxy.on('proxyRes', (proxyRes, req, res) => {
            console.log('收到目标服务器响应:', proxyRes.statusCode, req.url)
            if (proxyRes.statusCode !== 200) {
              console.warn(`警告: 请求 ${req.url} 返回了 ${proxyRes.statusCode} 状态码`)
              
              let responseBody = ''
              proxyRes.on('data', (chunk) => {
                responseBody += chunk.toString('utf8')
              })
              
              proxyRes.on('end', () => {
                try {
                  console.warn('响应内容:', responseBody || '(空)')
                } catch (e) {
                  console.warn('无法解析响应内容')
                }
              })
            }
          })
        }
      }
    }
  }
}) 