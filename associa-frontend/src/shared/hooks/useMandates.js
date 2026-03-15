import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { mandatesService } from '@api/services/mandates.service';
import { postsService } from '@api/services/posts.service';
import { useAuthStore } from '@store/authStore';
import toast from 'react-hot-toast';
import { toApiError } from '@api/http/toApiError';

export const useCurrentMandates = () => {
  const associationId = useAuthStore((state) => state.user?.associationId);

  return useQuery({
    queryKey: ['mandates', 'current', associationId],
    queryFn: () => mandatesService.getCurrentByAssociation(associationId),
    enabled: !!associationId
  });
};

export const useRevokeMandate = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ mandateId, endDate, reason }) =>
      mandatesService.revoke(mandateId, { endDate, reason }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['mandates'] });
      queryClient.invalidateQueries({ queryKey: ['mandates', 'current'] });
      toast.success('Mandat révoqué');
    },
    onError: (error) => {
      const apiError = toApiError(error);
      toast.error(apiError.message || 'Erreur lors de la révocation');
    }
  });
};

export const useExtendMandate = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ mandateId, newEndDate }) => mandatesService.extend(mandateId, newEndDate),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['mandates'] });
      queryClient.invalidateQueries({ queryKey: ['mandates', 'current'] });
      toast.success('Mandat prolongé');
    },
    onError: (error) => {
      const apiError = toApiError(error);
      toast.error(apiError.message || 'Erreur lors de la prolongation');
    }
  });
};

// Hook pour l'endpoint atomique assign-post
export const useAssignPost = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (postData) => {
      const payload = {
        userId: postData.user_id,
        postId: postData.post_id,
        startDate: postData.start_date,
        endDate: postData.end_date || null,
        assignRole: postData.assign_role,
        notes: postData.notes
      };

      return mandatesService.assignPost(payload);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['mandates'] });
      queryClient.invalidateQueries({ queryKey: ['mandates', 'current'] });
      queryClient.invalidateQueries({ queryKey: ['members'] });
      toast.success('Poste assigné avec succès (rôle automatique)');
    },
    onError: (error) => {
      const apiError = toApiError(error);
      toast.error(apiError.message || 'Erreur lors de l\'assignation');
    }
  });
};

// Hook pour récupérer le rôle suggéré pour un poste
export const useGetSuggestedRole = (postId) => {
  return useQuery({
    queryKey: ['suggested-role', postId],
    queryFn: async () => {
      const roles = await postsService.getSuggestedRoles(postId);
      return roles?.find((role) => role.isDefault === true) || roles?.[0] || null;
    },
    enabled: !!postId,
    staleTime: 5 * 60 * 1000 // Cache 5 minutes
  });
};
