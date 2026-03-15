import apiClient from '../client';
import { API_ENDPOINTS } from '@config/api.config';
import { unwrapApiResponse } from '@api/http/unwrapApiResponse';

export const associationsService = {
  getAll: async (params) => {
    const { data } = await apiClient.get(API_ENDPOINTS.ASSOCIATIONS, { params });
    return data;
  },

  getById: async (id) => {
    const { data } = await apiClient.get(API_ENDPOINTS.ASSOCIATION_DETAIL(id));
    return data;
  },

  getBySlug: async (slug) => {
    const { data } = await apiClient.get(API_ENDPOINTS.ASSOCIATION_BY_SLUG(slug));
    return data;
  },

  create: async (payload) => {
    const { data } = await apiClient.post(API_ENDPOINTS.ASSOCIATIONS, payload);
    return unwrapApiResponse(data);
  },

  update: async (id, payload) => {
    const { data } = await apiClient.put(API_ENDPOINTS.ASSOCIATION_DETAIL(id), payload);
    return unwrapApiResponse(data);
  },

  remove: async (id) => {
    const { data } = await apiClient.delete(API_ENDPOINTS.ASSOCIATION_DETAIL(id));
    return unwrapApiResponse(data);
  },

  suspend: async (id) => {
    const { data } = await apiClient.post(API_ENDPOINTS.ASSOCIATION_SUSPEND(id));
    return unwrapApiResponse(data);
  },

  activate: async (id) => {
    const { data } = await apiClient.post(API_ENDPOINTS.ASSOCIATION_ACTIVATE(id));
    return unwrapApiResponse(data);
  },

  archive: async (id) => {
    const { data } = await apiClient.post(API_ENDPOINTS.ASSOCIATION_ARCHIVE(id));
    return unwrapApiResponse(data);
  },

  getStats: async (id) => {
    const { data } = await apiClient.get(API_ENDPOINTS.ASSOCIATION_STATS(id));
    return data;
  },

  getExecutiveBoard: async (id) => {
    const { data } = await apiClient.get(API_ENDPOINTS.ASSOCIATION_EXECUTIVE_BOARD(id));
    return data;
  },

  getActiveMembers: async (id) => {
    const { data } = await apiClient.get(API_ENDPOINTS.ASSOCIATION_ACTIVE_MEMBERS(id));
    return data;
  }
};
