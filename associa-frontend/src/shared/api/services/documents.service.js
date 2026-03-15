import apiClient from '../client';
import { API_ENDPOINTS } from '@config/api.config';

const toFrontendDocument = (doc) => {
  if (!doc) return doc;

  return {
    id: doc.id,
    title: doc.title,
    category: doc.category,
    file_type: doc.fileType ?? doc.file_type ?? doc.mimeType ?? doc.mime_type,
    file_size: doc.fileSize ?? doc.file_size ?? doc.size,
    file_path: doc.filePath ?? doc.file_path ?? doc.path,
    created_at: doc.createdAt ?? doc.created_at
  };
};

const normalizeListResponse = (data) => {
  const payload = data?.data ?? data;
  const items = Array.isArray(payload) ? payload : [];
  return { data: items.map(toFrontendDocument) };
};

export const documentsService = {
  getAll: async () => {
    const { data } = await apiClient.get(API_ENDPOINTS.DOCUMENTS);
    return normalizeListResponse(data);
  },

  upload: async (formData) => {
    const { data } = await apiClient.post(API_ENDPOINTS.FILE_UPLOAD, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    const response = data?.data ?? data;
    return response ? toFrontendDocument(response) : response;
  },

  delete: async (documentId) => {
    const { data } = await apiClient.delete(`${API_ENDPOINTS.DOCUMENTS}/${documentId}`);
    return data;
  },

  download: async (documentId) => {
    const { data } = await apiClient.get(`${API_ENDPOINTS.DOCUMENTS}/${documentId}/download`, {
      responseType: 'blob'
    });
    return data;
  }
};
