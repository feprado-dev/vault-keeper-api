/// <reference types="vitest/config" />
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// O painel é publicado dentro do Spring Boot, na pasta static/painel (acessível em /painel/).
export default defineConfig({
  base: '/painel/',
  plugins: [react()],
  build: {
    outDir: '../src/main/resources/static/painel',
    emptyOutDir: true,
  },
  server: {
    // No "npm run dev" as chamadas /api vão para a API Java (rodando na porta 8080).
    proxy: { '/api': 'http://localhost:8080' },
  },
  test: {
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
  },
})
