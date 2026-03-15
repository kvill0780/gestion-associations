import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import { VitePWA } from 'vite-plugin-pwa';
import path from 'path';
export default ({ mode }) => {
  // load env variables so we can use VITE_API_BASE_URL to configure a dev proxy
  const env = loadEnv(mode, process.cwd(), '');
  const apiBase = env.VITE_API_BASE_URL || 'http://localhost:8080/api';

  // Build a proxy entry if apiBase contains a path (eg. /api)
  const serverProxy = {};
  try {
    const url = new URL(apiBase);
    const prefix = url.pathname.replace(/\/$/, '') || '/';
    // Only proxy common API prefix (like /api)
    if (prefix) {
      serverProxy[prefix] = {
        target: url.origin,
        changeOrigin: true,
        secure: false,
        rewrite: (p) => p.replace(new RegExp(`^${prefix}`), '')
      };
    }
  } catch (e) {
    // ignore malformed URL and leave proxy empty
  }

  return defineConfig({
  plugins: [
    react(),
    VitePWA({
      registerType: 'autoUpdate',
      includeAssets: ['favicon.ico', 'robots.txt', 'apple-touch-icon.png'],
      manifest: {
        name: 'Associa - Gestion d\'Association',
        short_name: 'Associa',
        description: 'Application de gestion d\'association',
        theme_color: '#3b82f6',
        background_color: '#ffffff',
        display: 'standalone',
        icons: [
          {
            src: '/icon-192x192.png',
            sizes: '192x192',
            type: 'image/png'
          },
          {
            src: '/icon-512x512.png',
            sizes: '512x512',
            type: 'image/png'
          }
        ]
      },
      workbox: {
        globPatterns: ['**/*.{js,css,html,ico,png,svg}'],
        runtimeCaching: [
          {
            urlPattern: /^https:\/\/.*\.(?:png|jpg|jpeg|svg|gif)$/,
            handler: 'CacheFirst',
            options: {
              cacheName: 'images',
              expiration: {
                maxEntries: 50,
                maxAgeSeconds: 30 * 24 * 60 * 60
              }
            }
          }
        ]
      }
    })
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
      '@app': path.resolve(__dirname, './src/app'),
      '@components': path.resolve(__dirname, './src/shared/components'),
      '@api': path.resolve(__dirname, './src/shared/api'),
      '@hooks': path.resolve(__dirname, './src/shared/hooks'),
      '@utils': path.resolve(__dirname, './src/shared/utils'),
      '@config': path.resolve(__dirname, './src/shared/config'),
      '@features': path.resolve(__dirname, './src/features'),
      '@store': path.resolve(__dirname, './src/shared/store'),
      '@assets': path.resolve(__dirname, './src/shared/assets')
    }
  },
  server: {
    port: 5174,
    host: true,
    cors: true,
    // Allow overriding HMR host/port from env for access from other devices
    hmr: {
      host: env.VITE_HMR_HOST || undefined,
      port: env.VITE_HMR_PORT ? Number(env.VITE_HMR_PORT) : undefined
    },
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      }
    }
  },
  preview: {
    port: 5174,
    host: true
  },
  build: {
    esbuild: {
      drop: ['console', 'debugger']
    },
    rollupOptions: {
      output: {
        manualChunks: {
          'react-vendor': ['react', 'react-dom', 'react-router-dom'],
          'query-vendor': ['@tanstack/react-query'],
          'form-vendor': ['react-hook-form', 'zod'],
          'chart-vendor': ['recharts'],
          'utils-vendor': ['axios', 'date-fns', 'clsx']
        }
      }
    },
    chunkSizeWarningLimit: 1000
  }
  });
};
