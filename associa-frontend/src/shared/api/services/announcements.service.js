import apiClient from '../client';
import { API_ENDPOINTS } from '@config/api.config';

const normalizeLower = (value) => {
  if (value === null || value === undefined) return value;
  return String(value).toLowerCase();
};

const toFrontendOption = (option) => {
  if (!option) return option;

  return {
    id: option.id,
    option_text: option.optionText ?? option.option_text ?? option.text ?? '',
    votes_count: option.votesCount ?? option.votes_count ?? option.votes ?? 0,
    user_voted: option.userVoted ?? option.user_voted ?? option.hasVoted ?? false
  };
};

const toFrontendAnnouncement = (announcement) => {
  if (!announcement) return announcement;

  const author = announcement.author || announcement.createdBy || announcement.user;
  const authorName =
    announcement.authorName ||
    announcement.author_name ||
    author?.name ||
    author?.fullName ||
    author?.full_name ||
    [author?.firstName, author?.lastName].filter(Boolean).join(' ');

  return {
    id: announcement.id,
    title: announcement.title,
    content: announcement.content,
    priority: normalizeLower(announcement.priority),
    type: normalizeLower(announcement.type),
    poll_question: announcement.pollQuestion ?? announcement.poll_question,
    poll_options: (announcement.pollOptions ?? announcement.poll_options ?? []).map(toFrontendOption),
    allow_multiple_votes:
      announcement.allowMultipleVotes ?? announcement.allow_multiple_votes ?? announcement.allowMultipleVotes,
    likes_count: announcement.likesCount ?? announcement.likes_count ?? 0,
    dislikes_count: announcement.dislikesCount ?? announcement.dislikes_count ?? 0,
    user_reaction: announcement.userReaction ?? announcement.user_reaction ?? null,
    author: authorName ? { name: authorName } : null,
    created_at: announcement.createdAt ?? announcement.created_at,
    updated_at: announcement.updatedAt ?? announcement.updated_at
  };
};

const toBackendAnnouncementPayload = (data = {}) => ({
  title: data.title,
  content: data.content,
  priority: data.priority ? String(data.priority).toUpperCase() : undefined,
  type: data.type ? String(data.type).toUpperCase() : undefined,
  pollQuestion: data.pollQuestion ?? data.poll_question,
  pollOptions: data.pollOptions ?? data.poll_options,
  allowMultipleVotes: data.allowMultipleVotes ?? data.allow_multiple_votes
});

const normalizeListResponse = (data) => {
  const payload = data?.data ?? data;
  const items = Array.isArray(payload) ? payload : [];
  return { data: items.map(toFrontendAnnouncement) };
};

export const announcementsService = {
  getAll: async () => {
    const { data } = await apiClient.get(API_ENDPOINTS.ANNOUNCEMENTS.BASE);
    return normalizeListResponse(data);
  },

  create: async (announcementData) => {
    const payload = toBackendAnnouncementPayload(announcementData);
    const { data } = await apiClient.post(API_ENDPOINTS.ANNOUNCEMENTS.BASE, payload);
    const response = data?.data ?? data;
    return toFrontendAnnouncement(response);
  },

  remove: async (id) => {
    await apiClient.delete(`${API_ENDPOINTS.ANNOUNCEMENTS.BASE}/${id}`);
    return { success: true };
  },

  react: async (announcementId, type) => {
    await apiClient.post(API_ENDPOINTS.ANNOUNCEMENTS.REACT(announcementId), { type });
    return { success: true };
  },

  unreact: async (announcementId) => {
    await apiClient.delete(API_ENDPOINTS.ANNOUNCEMENTS.REACT(announcementId));
    return { success: true };
  },

  vote: async (announcementId, optionId) => {
    await apiClient.post(API_ENDPOINTS.ANNOUNCEMENTS.VOTE(announcementId), {
      optionId
    });
    return { success: true };
  }
};
