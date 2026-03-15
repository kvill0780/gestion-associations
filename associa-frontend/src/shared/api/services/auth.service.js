import apiClient from '../client';
import { API_ENDPOINTS } from '@config/api.config';
import { storage } from '@utils/storage';
import { unwrapApiResponse } from '@api/http/unwrapApiResponse';

const normalizeUser = (user) => {
  if (!user) return user;
  return {
    ...user,
    whatsapp: user.whatsapp ?? user.phone ?? user.phoneNumber ?? user.phone_number
  };
};

/**
 * Auth Service
 * 
 * Handles authentication with Spring Boot backend
 * All responses follow ApiResponse<T> format: { success, message, data }
 */
export const authService = {
  /**
   * Login
   * @returns { accessToken, refreshToken, user }
   */
  login: async (credentials) => {
    const { data } = await apiClient.post(API_ENDPOINTS.LOGIN, credentials);
    // Spring returns ApiResponse<LoginResponse>
    const response = unwrapApiResponse(data); // { accessToken, refreshToken, user }
    return {
      ...response,
      user: normalizeUser(response.user)
    };
  },

  /**
   * Logout
   * @param {string} refreshToken - Refresh token to revoke
   */
  logout: async (refreshToken) => {
    const { data } = await apiClient.post(API_ENDPOINTS.LOGOUT, {
      refreshToken,
      revokeAllTokens: false
    });
    return unwrapApiResponse(data);
  },

  /**
   * Register new user
   * @returns { user, message }
   */
  register: async (userData) => {
    const { data } = await apiClient.post(API_ENDPOINTS.REGISTER, userData);
    const response = unwrapApiResponse(data); // { user, message }
    return {
      ...response,
      user: normalizeUser(response.user)
    };
  },

  /**
   * Get current user profile
   * @returns { user, roles, permissions, mandates }
   */
  getProfile: async () => {
    const { data } = await apiClient.get(API_ENDPOINTS.ME);
    return normalizeUser(unwrapApiResponse(data)); // UserInfoDto
  },

  /**
   * Update profile
   * @param {Object} profileData
   * @returns {UserInfoDto}
   */
  updateProfile: async (profileData) => {
    const { data } = await apiClient.put(API_ENDPOINTS.ME, profileData);
    return normalizeUser(unwrapApiResponse(data));
  },

  /**
   * Change password for current user
   * @param {Object} payload { currentPassword, newPassword }
   */
  changePassword: async (payload) => {
    const { data } = await apiClient.put(API_ENDPOINTS.ME_PASSWORD, payload);
    return unwrapApiResponse(data);
  },

  /**
   * Refresh access token
   * @param {string} refreshToken
   * @returns { accessToken, refreshToken }
   */
  refreshToken: async (refreshToken) => {
    const { data } = await apiClient.post(API_ENDPOINTS.REFRESH, { refreshToken });
    return unwrapApiResponse(data); // { accessToken, refreshToken }
  },

  getStoredRefreshToken: () => storage.get('refresh_token'),

  /**
   * Forgot password
   * @param {string} email
   */
  forgotPassword: async (email) => {
    const { data } = await apiClient.post(API_ENDPOINTS.FORGOT_PASSWORD, { email });
    return unwrapApiResponse(data);
  },

  /**
   * Reset password
   * @param {string} token
   * @param {string} newPassword
   */
  resetPassword: async (token, newPassword) => {
    const { data } = await apiClient.post(API_ENDPOINTS.RESET_PASSWORD, {
      token,
      newPassword
    });
    return unwrapApiResponse(data);
  }
};
