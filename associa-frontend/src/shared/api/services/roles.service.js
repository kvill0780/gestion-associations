import apiClient from '../client';
import { API_ENDPOINTS } from '@config/api.config';
import { unwrapApiResponse } from '@api/http/unwrapApiResponse';

export const rolesService = {
  getAll: async (params) => {
    const { data } = await apiClient.get(API_ENDPOINTS.ROLES, { params });
    return data;
  },

  create: async (roleData) => {
    const { data } = await apiClient.post(API_ENDPOINTS.ROLES, roleData);
    return unwrapApiResponse(data);
  },

  update: async (roleId, roleData) => {
    const { data } = await apiClient.put(`${API_ENDPOINTS.ROLES}/${roleId}`, roleData);
    return unwrapApiResponse(data);
  },

  getPermissionsGrouped: async () => {
    const { data } = await apiClient.get(API_ENDPOINTS.PERMISSIONS_GROUPED);
    return data;
  }
};
