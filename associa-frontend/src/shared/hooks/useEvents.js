import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { eventsService } from '@api/services/events.service';
import { toApiError } from '@api/http/toApiError';
import toast from 'react-hot-toast';

export const useEvents = () => {
  return useQuery({
    queryKey: ['events'],
    queryFn: eventsService.getAll
  });
};

export const useCreateEvent = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: eventsService.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['events'] });
      toast.success('Événement créé');
    },
    onError: () => {
      toast.error('Erreur lors de la création');
    }
  });
};

export const useDeleteEvent = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: eventsService.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['events'] });
      toast.success('Événement supprimé');
    },
    onError: () => {
      toast.error('Erreur lors de la suppression');
    }
  });
};

export const useChangeEventStatus = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ eventId, status }) => eventsService.changeStatus(eventId, status),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['events'] });
      const statusLabel = String(variables?.status || '').toUpperCase();
      toast.success(`Statut mis à jour: ${statusLabel}`);
    },
    onError: (error) => {
      toast.error(toApiError(error).message || 'Erreur lors du changement de statut');
    }
  });
};

export const usePublishEvent = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (eventId) => eventsService.publish(eventId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['events'] });
      toast.success('Événement publié');
    },
    onError: (error) => {
      toast.error(toApiError(error).message || 'Erreur lors de la publication');
    }
  });
};
