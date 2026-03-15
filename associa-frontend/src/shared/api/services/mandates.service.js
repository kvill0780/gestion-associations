import apiClient from '../client';
import { API_ENDPOINTS } from '@config/api.config';
import { unwrapApiResponse } from '@api/http/unwrapApiResponse';

export const mandatesService = {
  // Endpoint atomique : assigne un poste à un user
  assignPost: async (payload) => {
    const { data } = await apiClient.post(API_ENDPOINTS.MANDATE_ASSIGN, payload);
    return unwrapApiResponse(data);
  },

  revoke: async (mandateId, payload) => {
    const { data } = await apiClient.post(API_ENDPOINTS.MANDATE_REVOKE(mandateId), payload);
    return unwrapApiResponse(data);
  },

  extend: async (mandateId, newEndDate) => {
    const { data } = await apiClient.post(API_ENDPOINTS.MANDATE_EXTEND(mandateId), null, {
      params: { newEndDate }
    });
    return unwrapApiResponse(data);
  },

  getByUser: async (userId, activeOnly = false) => {
    const { data } = await apiClient.get(API_ENDPOINTS.MANDATE_BY_USER(userId), {
      params: { activeOnly }
    });
    return data;
  },

  getCurrentByAssociation: async (associationId) => {
    const { data } = await apiClient.get(API_ENDPOINTS.MANDATE_CURRENT(associationId));
    return data;
  }
};
