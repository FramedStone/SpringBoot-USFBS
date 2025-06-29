import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { NodeGlobalsPolyfillPlugin } from '@esbuild-plugins/node-globals-polyfill';
import inject from '@rollup/plugin-inject';
import { fileURLToPath, URL } from 'url';
import path from 'path';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173 
  },
  base: './', // Ensure assets use relative paths for static hosting
  define: {
    'process.env': {},
    'process': { env: {} }, 
    global: 'globalThis',
  },
  resolve: {
    alias: {
      // crypto: "crpto-browserify",
      assert: "assert",
      http: "stream-http",
      https: "https-browserify",
      url: "url",
      zlib: "browserify-zlib",
      stream: "stream-browserify",      
      buffer: 'buffer',
      process: path.resolve(__dirname, 'node_modules/process/browser.js'),
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
        NodeGlobalsPolyfillPlugin({ buffer: true, process: true, name: 'node-globals-polyfill' })
      ]
    }
  },
  build: {
    rollupOptions: {
      plugins: [
        inject({
          Buffer: ['buffer', 'Buffer'],
        }),
      ]
    }
  }
});
