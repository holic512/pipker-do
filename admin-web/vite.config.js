import { defineConfig, loadEnv } from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'node:path';
export default defineConfig(function (_a) {
    var mode = _a.mode;
    var env = loadEnv(mode, process.cwd(), '');
    return {
        plugins: [vue()],
        resolve: {
            alias: {
                '@': path.resolve(__dirname, 'src')
            }
        },
        server: {
            port: 5174,
            proxy: {
                '/api': {
                    target: env.VITE_PROXY_TARGET || 'http://localhost:8080',
                    changeOrigin: true
                }
            }
        }
    };
});
