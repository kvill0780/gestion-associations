import apiClient from '../client';
import { API_ENDPOINTS } from '@config/api.config';

const toFrontendEvent = (event) => {
  if (!event) return event;

  return {
    id: event.id,
    association_id: event.associationId ?? event.association_id,
    title: event.title,
    description: event.description,
    type: event.type ? String(event.type).toLowerCase() : undefined,
    status: event.status ? String(event.status).toLowerCase() : undefined,
    start_date: event.startDate ?? event.start_date,
    end_date: event.endDate ?? event.end_date,
    location: event.location,
    is_online: event.isOnline ?? event.is_online ?? false,
    meeting_link: event.meetingLink ?? event.meeting_link,
    max_participants: event.maxParticipants ?? event.max_participants,
    created_by_name: event.createdByName ?? event.created_by_name,
    created_at: event.createdAt ?? event.created_at,
    updated_at: event.updatedAt ?? event.updated_at
  };
};

const toBackendEventPayload = (eventData = {}) => ({
  title: eventData.title,
  description: eventData.description,
  type: (eventData.type || 'MEETING').toUpperCase(),
  status: eventData.status ? String(eventData.status).toUpperCase() : undefined,
  startDate: eventData.startDate || eventData.start_date,
  endDate: eventData.endDate || eventData.end_date,
  location: eventData.location,
  isOnline: Boolean(eventData.isOnline ?? eventData.is_online),
  meetingLink: eventData.meetingLink || eventData.meeting_link || undefined,
  maxParticipants: (() => {
    const rawValue =
      eventData.maxParticipants != null
        ? eventData.maxParticipants
        : eventData.max_participants != null
          ? eventData.max_participants
          : null;

    if (rawValue == null || rawValue === '') {
      return undefined;
    }

    const parsed = Number(rawValue);
    return Number.isFinite(parsed) ? parsed : undefined;
  })()
});

const toFrontendParticipant = (participant) => {
  if (!participant) return participant;

  return {
    event_id: participant.eventId ?? participant.event_id,
    user_id: participant.userId ?? participant.user_id,
    user_full_name: participant.userFullName ?? participant.user_full_name,
    user_email: participant.userEmail ?? participant.user_email,
    user_whatsapp: participant.userWhatsapp ?? participant.user_whatsapp,
    status: participant.status ? String(participant.status).toLowerCase() : 'registered',
    notes: participant.notes,
    registered_at: participant.registeredAt ?? participant.registered_at,
    updated_at: participant.updatedAt ?? participant.updated_at
  };
};

const toFrontendAttendanceSummary = (summary) => {
  if (!summary) return summary;

  return {
    event_id: summary.eventId ?? summary.event_id,
    max_participants: summary.maxParticipants ?? summary.max_participants,
    available_slots: summary.availableSlots ?? summary.available_slots,
    total_participants: summary.totalParticipants ?? summary.total_participants ?? 0,
    registered_count: summary.registeredCount ?? summary.registered_count ?? 0,
    attended_count: summary.attendedCount ?? summary.attended_count ?? 0,
    absent_count: summary.absentCount ?? summary.absent_count ?? 0,
    cancelled_count: summary.cancelledCount ?? summary.cancelled_count ?? 0
  };
};

export const eventsService = {
  getAll: async () => {
    const { data } = await apiClient.get(API_ENDPOINTS.EVENTS);
    const items = Array.isArray(data?.content)
      ? data.content
      : Array.isArray(data?.data)
        ? data.data
        : Array.isArray(data)
          ? data
          : [];

    return {
      data: items.map(toFrontendEvent),
      pagination: Array.isArray(data?.content)
        ? {
            totalElements: data.totalElements,
            totalPages: data.totalPages,
            pageNumber: data.number
          }
        : null
    };
  },

  getById: async (eventId) => {
    const { data } = await apiClient.get(API_ENDPOINTS.EVENT_DETAIL(eventId));
    return toFrontendEvent(data);
  },

  create: async (eventData) => {
    const payload = toBackendEventPayload(eventData);
    const { data } = await apiClient.post(API_ENDPOINTS.EVENTS, payload);
    return toFrontendEvent(data);
  },

  update: async (eventId, eventData) => {
    const payload = toBackendEventPayload(eventData);
    const { data } = await apiClient.put(API_ENDPOINTS.EVENT_DETAIL(eventId), payload);
    return toFrontendEvent(data);
  },

  delete: async (eventId) => {
    await apiClient.delete(API_ENDPOINTS.EVENT_DETAIL(eventId));
    return { success: true };
  },

  changeStatus: async (eventId, status) => {
    const { data } = await apiClient.patch(API_ENDPOINTS.EVENT_STATUS(eventId), null, {
      params: { status: String(status || 'DRAFT').toUpperCase() }
    });
    return toFrontendEvent(data);
  },

  publish: async (eventId) => {
    return eventsService.changeStatus(eventId, 'PUBLISHED');
  },

  getParticipants: async (eventId) => {
    const { data } = await apiClient.get(API_ENDPOINTS.EVENT_PARTICIPANTS(eventId));
    return Array.isArray(data) ? data.map(toFrontendParticipant) : [];
  },

  getAttendanceSummary: async (eventId) => {
    const { data } = await apiClient.get(API_ENDPOINTS.EVENT_ATTENDANCE_SUMMARY(eventId));
    return toFrontendAttendanceSummary(data);
  },

  registerParticipant: async (eventId, userId, notes) => {
    const { data } = await apiClient.post(API_ENDPOINTS.EVENT_REGISTER_PARTICIPANT(eventId), {
      userId,
      notes
    });
    return toFrontendParticipant(data);
  },

  updateParticipantStatus: async (eventId, userId, status, notes) => {
    const { data } = await apiClient.patch(API_ENDPOINTS.EVENT_PARTICIPANT_STATUS(eventId, userId), {
      status: String(status || 'REGISTERED').toUpperCase(),
      notes
    });
    return toFrontendParticipant(data);
  },

  checkIn: async (eventId) => {
    const { data } = await apiClient.post(API_ENDPOINTS.EVENT_CHECKIN(eventId));
    return toFrontendParticipant(data);
  }
};
