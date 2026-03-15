import apiClient from '../client';
import { API_ENDPOINTS } from '@config/api.config';
import { unwrapApiResponse } from '@api/http/unwrapApiResponse';

export const dashboardService = {
  getDashboard: async () => {
    const { data } = await apiClient.get(API_ENDPOINTS.DASHBOARD);
    return unwrapApiResponse(data);
  },

  getSystemStats: async () => {
    const { data } = await apiClient.get(API_ENDPOINTS.SYSTEM_STATS);
    return unwrapApiResponse(data);
  }
};
