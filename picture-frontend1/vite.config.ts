import { fileURLToPath, URL } from 'node:url'

import { defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  server: {
    port: 3000,
    open: true,
    proxy: {
      '/api':"http://localhost:8100",
    }
  },
  plugins: [
    vue(),
  ],
  base:'/',
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  }
})
