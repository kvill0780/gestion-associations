import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { eventsService } from '@api/services/events.service';
import { toApiError } from '@api/http/toApiError';
import toast from 'react-hot-toast';

export const useEventParticipants = (eventId) =>
  useQuery({
    queryKey: ['events', eventId, 'participants'],
    queryFn: () => eventsService.getParticipants(eventId),
    enabled: !!eventId
  });

export const useEventAttendanceSummary = (eventId) =>
  useQuery({
    queryKey: ['events', eventId, 'attendance-summary'],
    queryFn: () => eventsService.getAttendanceSummary(eventId),
    enabled: !!eventId
  });

export const useRegisterEventParticipant = (eventId) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ userId, notes }) => eventsService.registerParticipant(eventId, userId, notes),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['events', eventId, 'participants'] });
      queryClient.invalidateQueries({ queryKey: ['events', eventId, 'attendance-summary'] });
      toast.success('Participant inscrit');
    },
    onError: (error) => {
      toast.error(toApiError(error).message || 'Erreur lors de l’inscription');
    }
  });
};

export const useUpdateEventParticipantStatus = (eventId) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ userId, status, notes }) =>
      eventsService.updateParticipantStatus(eventId, userId, status, notes),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['events', eventId, 'participants'] });
      queryClient.invalidateQueries({ queryKey: ['events', eventId, 'attendance-summary'] });
      toast.success('Statut mis à jour');
    },
    onError: (error) => {
      toast.error(toApiError(error).message || 'Erreur lors de la mise à jour');
    }
  });
};

export const useEventCheckIn = (eventId) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => eventsService.checkIn(eventId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['events', eventId, 'participants'] });
      queryClient.invalidateQueries({ queryKey: ['events', eventId, 'attendance-summary'] });
      toast.success('Présence enregistrée');
    },
    onError: (error) => {
      toast.error(toApiError(error).message || 'Impossible d’enregistrer la présence');
    }
  });
};
