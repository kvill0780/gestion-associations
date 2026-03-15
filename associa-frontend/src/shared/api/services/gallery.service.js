import apiClient from '../client';
import { API_ENDPOINTS } from '@config/api.config';

const normalizeLower = (value) => {
  if (value === null || value === undefined) return value;
  return String(value).toLowerCase();
};

const toFrontendMedia = (media) => {
  if (!media) return media;

  const mimeType = media.mimeType ?? media.mime_type;
  const derivedType = mimeType?.startsWith('video') ? 'video' : 'photo';

  return {
    id: media.id,
    type: normalizeLower(media.type) || derivedType,
    thumbnail_path: media.thumbnailPath ?? media.thumbnail_path,
    file_path: media.filePath ?? media.file_path ?? media.path,
    caption: media.caption,
    mime_type: mimeType
  };
};

const toFrontendAlbum = (album) => {
  if (!album) return album;

  return {
    event_id: album.eventId ?? album.event_id,
    event: album.event,
    media_count: album.mediaCount ?? album.media_count ?? 0,
    last_updated: album.lastUpdated ?? album.last_updated
  };
};

const normalizeListResponse = (data, mapper) => {
  const payload = data?.data ?? data;
  const items = Array.isArray(payload) ? payload : [];
  return mapper ? items.map(mapper) : items;
};

export const galleryService = {
  getMedia: async ({ eventId } = {}) => {
    const params = eventId ? { eventId } : undefined;
    const { data } = await apiClient.get(API_ENDPOINTS.GALLERY.BASE, { params });
    const items = normalizeListResponse(data, toFrontendMedia);
    return { data: items };
  },

  getAlbums: async () => {
    const { data } = await apiClient.get(API_ENDPOINTS.GALLERY.ALBUMS);
    return normalizeListResponse(data, toFrontendAlbum);
  },

  upload: async ({ files, eventId, caption }) => {
    const formData = new FormData();
    (files || []).forEach((file) => formData.append('files', file));
    if (eventId) formData.append('eventId', eventId);
    if (caption) formData.append('caption', caption);

    const { data } = await apiClient.post(API_ENDPOINTS.GALLERY.BASE, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });

    const response = data?.data ?? data;
    return response ? toFrontendMedia(response) : null;
  },

  remove: async (mediaId) => {
    await apiClient.delete(`${API_ENDPOINTS.GALLERY.BASE}/${mediaId}`);
    return { success: true };
  }
};
