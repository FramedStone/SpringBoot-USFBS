import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { NodeGlobalsPolyfillPlugin } from '@esbuild-plugins/node-globals-polyfill'
import { fileURLToPath, URL } from 'url'

export default defineConfig({
  plugins: [react()],
  define: {
    'process.env': {},
    global: 'globalThis', 
  },
  resolve: {
    alias: {
      buffer: 'buffer',
      '@components': fileURLToPath(new URL('./src/components', import.meta.url)),
      '@styles': fileURLToPath(new URL('./src/styles', import.meta.url)),
      '@pages': fileURLToPath(new URL('./src/pages', import.meta.url)),
      '@utils': fileURLToPath(new URL('./src/utils', import.meta.url)),
    }
  },
  optimizeDeps: {
    include: ['buffer'],
    esbuildOptions: {
      define: { global: 'globalThis' },
      plugins: [
        NodeGlobalsPolyfillPlugin({ buffer: true })
      ]
    }
  },
  build: {
    rollupOptions: {
      plugins: [
        // Polyfill Buffer for production build
        {
          name: 'buffer-polyfill',
          resolveId(id) {
            if (id === 'buffer') return require.resolve('buffer/');
          }
        }
      ]
    }
  }
});
