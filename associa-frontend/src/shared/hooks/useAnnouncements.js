import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { announcementsService } from '@api/services/announcements.service';
import { toast } from 'react-hot-toast';

export const useAnnouncements = () => {
  return useQuery({
    queryKey: ['announcements'],
    queryFn: announcementsService.getAll
  });
};

export const useCreateAnnouncement = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: announcementsService.create,
    onSuccess: () => {
      queryClient.invalidateQueries(['announcements']);
      toast.success('Annonce créée avec succès');
    },
    onError: () => {
      toast.error('Erreur lors de la création de l\'annonce');
    }
  });
};

export const useDeleteAnnouncement = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: announcementsService.remove,
    onSuccess: () => {
      queryClient.invalidateQueries(['announcements']);
      toast.success('Annonce supprimée');
    },
    onError: () => {
      toast.error('Erreur lors de la suppression');
    }
  });
};
