import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { fileURLToPath, URL } from 'node:url'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      // main.js 已全量引入样式与组件（use(ElementPlus)），模板中的 el-* 组件走全局注册；
      // 不再使用 ElementPlusResolver 按需导入，避免 'element-plus/es' 与 'element-plus'
      // 生成两份重复的组件 chunk（此前导致 Drawer/Dialog 内部状态更新失效）
      imports: ['vue'],
    }),
    Components({ dts: false }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    // 开发代理：后端 ai-serve (Spring Boot :8080)，前端统一走相对路径 /api 与 /upload
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
      '/upload': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
  optimizeDeps: {
    include: ['element-plus', 'element-plus/es', '@element-plus/icons-vue'],
  },
})
