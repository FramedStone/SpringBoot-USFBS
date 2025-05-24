import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react-swc'
import { NodeGlobalsPolyfillPlugin } from '@esbuild-plugins/node-globals-polyfill'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  optimizeDeps: {
    esbuildOptions: {
      // Provide globalThis for libs expecting "global"
      define: { global: 'globalThis' },
      plugins: [
        NodeGlobalsPolyfillPlugin({ buffer: true })
      ]
    },
    // don’t try to pre-bundle wagmi
    exclude: [
      'wagmi',
      'wagmi/chains',
      'wagmi/connectors'
    ]
  },
  resolve: {
    alias: [
      // Force buffer import to pick up the npm polyfill
      { find: 'buffer', replacement: 'buffer/' }
    ]
  },
  build: {
    rollupOptions: {
      // leave these imports external
      external: [
        'wagmi',
        'wagmi/chains',
        'wagmi/connectors'
      ]
    }
  }
})
