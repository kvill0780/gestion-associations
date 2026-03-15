import apiClient from '../client';
import { API_ENDPOINTS } from '@config/api.config';
import { unwrapApiResponse } from '@api/http/unwrapApiResponse';

export const membersService = {
  getAll: async () => {
    const { data } = await apiClient.get(API_ENDPOINTS.USERS);
    return data;
  },

  updateRole: async (userId, roleData) => {
    const { data } = await apiClient.post(API_ENDPOINTS.ROLE_ASSIGN, {
      userId,
      roleId: roleData.roleId,
      assignedById: roleData.assignedById,
      termStart: roleData.termStart,
      termEnd: roleData.termEnd,
      notes: roleData.notes
    });
    return unwrapApiResponse(data);
  },

  updateStatus: async (userId, statusData) => {
    const action = statusData?.action;

    if (action === 'activate') {
      const { data } = await apiClient.put(API_ENDPOINTS.USER_ACTIVATE(userId));
      return unwrapApiResponse(data);
    }

    if (action === 'suspend') {
      const { data } = await apiClient.put(API_ENDPOINTS.USER_SUSPEND(userId));
      return unwrapApiResponse(data);
    }

    if (action === 'approve') {
      const { data } = await apiClient.post(API_ENDPOINTS.USER_APPROVE(userId), {
        membershipDate: statusData.membershipDate,
        notes: statusData.notes
      });
      return unwrapApiResponse(data);
    }

    throw new Error(`Action de statut non supportée: ${action}`);
  },

  update: async (userId, userData) => {
    const { data } = await apiClient.put(`${API_ENDPOINTS.USERS}/${userId}`, userData);
    return unwrapApiResponse(data);
  }
};
