import apiClient from '../client';
import { API_ENDPOINTS } from '@config/api.config';
import { unwrapApiResponse } from '@api/http/unwrapApiResponse';

export const postsService = {
  getAll: async (params) => {
    const { data } = await apiClient.get(API_ENDPOINTS.POSTS, { params });
    return data;
  },

  create: async (postData) => {
    const { data } = await apiClient.post(API_ENDPOINTS.POSTS, postData);
    return unwrapApiResponse(data);
  },

  update: async (postId, postData) => {
    const { data } = await apiClient.put(API_ENDPOINTS.POST_DETAIL(postId), postData);
    return unwrapApiResponse(data);
  },

  remove: async (postId) => {
    const { data } = await apiClient.delete(API_ENDPOINTS.POST_DETAIL(postId));
    return unwrapApiResponse(data);
  },

  activate: async (postId) => {
    const { data } = await apiClient.post(API_ENDPOINTS.POST_ACTIVATE(postId));
    return unwrapApiResponse(data);
  },

  deactivate: async (postId) => {
    const { data } = await apiClient.post(API_ENDPOINTS.POST_DEACTIVATE(postId));
    return unwrapApiResponse(data);
  },

  linkRole: async (postId, roleId) => {
    const { data } = await apiClient.post(API_ENDPOINTS.POST_LINK_ROLE(postId, roleId));
    return unwrapApiResponse(data);
  },

  unlinkRole: async (postId, roleId) => {
    const { data } = await apiClient.delete(API_ENDPOINTS.POST_UNLINK_ROLE(postId, roleId));
    return unwrapApiResponse(data);
  },

  getSuggestedRoles: async (postId) => {
    const { data } = await apiClient.get(API_ENDPOINTS.POST_SUGGESTED_ROLES(postId));
    return data;
  },

  getCurrentHolders: async (postId) => {
    const { data } = await apiClient.get(API_ENDPOINTS.POST_CURRENT_HOLDERS(postId));
    return data;
  },

  getStats: async (postId) => {
    const { data } = await apiClient.get(API_ENDPOINTS.POST_STATS(postId));
    return data;
  }
};
