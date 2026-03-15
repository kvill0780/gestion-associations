import apiClient from '../client';
import { API_ENDPOINTS } from '@config/api.config';

export const auditService = {
  getLogs: async (params) => {
    const { data } = await apiClient.get(API_ENDPOINTS.AUDIT_LOGS, { params });
    return data;
  },

  searchLogs: async (params) => {
    const { data } = await apiClient.get(API_ENDPOINTS.AUDIT_SEARCH, { params });
    return data;
  },

  getStats: async (params) => {
    const { data } = await apiClient.get('/system/audit/stats', { params });
    return data;
  }
};
