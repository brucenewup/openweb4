import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  base: '/',  // 改为根路径
  build: {
    outDir: 'dist',  // 输出到 dist（Cloudflare Pages 标准目录）
    emptyOutDir: true,
    sourcemap: false,
  },
})
