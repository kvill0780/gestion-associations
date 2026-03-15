import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { membersService } from '@api/services/members.service';
import toast from 'react-hot-toast';
import { toApiError } from '@api/http/toApiError';

export const useMembers = () => {
  return useQuery({
    queryKey: ['members'],
    queryFn: membersService.getAll
  });
};

export const useUpdateMemberRole = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ userId, roleData }) => membersService.updateRole(userId, roleData),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['members'] });
      toast.success('Rôle mis à jour');
    },
    onError: (error) => {
      const apiError = toApiError(error);
      toast.error(apiError.message || 'Erreur lors de la mise à jour');
    }
  });
};

export const useUpdateMemberStatus = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ userId, statusData }) => membersService.updateStatus(userId, statusData),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['members'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      toast.success('Statut mis à jour');
    },
    onError: (error) => {
      const apiError = toApiError(error);
      toast.error(apiError.message || 'Erreur lors de la mise à jour');
    }
  });
};

export const useUpdateMember = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ userId, userData }) => membersService.update(userId, userData),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['members'] });
      toast.success('Membre mis à jour');
    },
    onError: (error) => {
      const apiError = toApiError(error);
      toast.error(apiError.message || 'Erreur lors de la mise à jour');
    }
  });
};
