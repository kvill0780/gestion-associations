import axios from 'axios';
import { API_CONFIG, API_ENDPOINTS } from '@config/api.config';
import { storage } from '@utils/storage';
import { useAuthStore } from '@store/authStore';

const apiClient = axios.create(API_CONFIG);

// ========== Request Interceptor ==========
apiClient.interceptors.request.use(
  (config) => {
    const token = storage.get('auth_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ========== Response Interceptor with Refresh Token ==========
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // Si 401 et pas déjà en train de refresh
    if (error.response?.status === 401 && !originalRequest._retry) {
      // Ne pas refresh sur login/register/refresh
      const publicEndpoints = [
        API_ENDPOINTS.LOGIN,
        API_ENDPOINTS.REGISTER,
        API_ENDPOINTS.REFRESH,
        API_ENDPOINTS.FORGOT_PASSWORD,
        API_ENDPOINTS.RESET_PASSWORD
      ];

      const isPublicEndpoint = publicEndpoints.some(endpoint =>
        originalRequest.url.includes(endpoint)
      );

      if (isPublicEndpoint) {
        return Promise.reject(error);
      }

      if (isRefreshing) {
        // Mettre en queue
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then(token => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return apiClient(originalRequest);
          })
          .catch(err => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const refreshToken = storage.get('refresh_token');

      if (!refreshToken) {
        // Pas de refresh token, rediriger vers login
        useAuthStore.getState().logout();
        window.location.href = '/login';
        return Promise.reject(error);
      }

      try {
        // Utiliser axios direct pour éviter circular dependency
        const refreshClient = axios.create({
          baseURL: API_CONFIG.baseURL,
          timeout: API_CONFIG.timeout
        });

        const { data } = await refreshClient.post(API_ENDPOINTS.REFRESH, {
          refreshToken
        });

        const { accessToken, refreshToken: newRefreshToken } = data.data;

        useAuthStore.getState().updateTokens(accessToken, newRefreshToken);

        apiClient.defaults.headers.common.Authorization = `Bearer ${accessToken}`;
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;

        processQueue(null, accessToken);

        return apiClient(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        useAuthStore.getState().logout();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export { apiClient };
export default apiClient;
