import apiClient from '../client';
import { API_ENDPOINTS } from '@config/api.config';

const toFrontendUser = (user) => {
  if (!user) return null;
  const name =
    user.name ||
    user.fullName ||
    user.full_name ||
    [user.firstName, user.lastName].filter(Boolean).join(' ') ||
    user.email;
  return {
    id: user.id,
    name,
    email: user.email
  };
};

const toFrontendMessage = (message) => {
  if (!message) return message;
  const senderId = message.senderId ?? message.sender_id ?? message.sender?.id;
  const receiverId = message.receiverId ?? message.receiver_id ?? message.receiver?.id;

  return {
    id: message.id,
    sender_id: senderId,
    receiver_id: receiverId,
    content: message.content,
    created_at: message.createdAt ?? message.created_at,
    read_at: message.readAt ?? message.read_at
  };
};

const toFrontendConversation = (conversation) => {
  if (!conversation) return conversation;
  const lastMessage = conversation.lastMessage ?? conversation.last_message;

  return {
    user: toFrontendUser(conversation.user ?? conversation.otherUser ?? conversation.participant),
    last_message: lastMessage
      ? {
          content: lastMessage.content ?? lastMessage.text,
          created_at: lastMessage.createdAt ?? lastMessage.created_at
        }
      : null,
    unread_count: conversation.unreadCount ?? conversation.unread_count ?? 0
  };
};

const normalizeListResponse = (data, mapper) => {
  const payload = data?.data ?? data;
  const items = Array.isArray(payload) ? payload : [];
  return { data: items.map(mapper) };
};

const toBackendMessagePayload = (messageData = {}) => ({
  receiverId: messageData.receiverId ?? messageData.receiver_id,
  content: messageData.content
});

export const messagesService = {
  getConversations: async () => {
    const { data } = await apiClient.get(API_ENDPOINTS.MESSAGES.BASE);
    return normalizeListResponse(data, toFrontendConversation);
  },

  getConversation: async (userId) => {
    const { data } = await apiClient.get(API_ENDPOINTS.MESSAGES.CONVERSATION(userId));
    return normalizeListResponse(data, toFrontendMessage);
  },

  sendMessage: async (messageData) => {
    const payload = toBackendMessagePayload(messageData);
    const { data } = await apiClient.post(API_ENDPOINTS.MESSAGES.BASE, payload);
    const response = data?.data ?? data;
    return toFrontendMessage(response);
  },

  getUnreadCount: async () => {
    const { data } = await apiClient.get(API_ENDPOINTS.MESSAGES.UNREAD_COUNT);
    return data?.data ?? data;
  }
};
