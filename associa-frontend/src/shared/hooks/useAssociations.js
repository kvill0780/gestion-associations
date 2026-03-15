import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { associationsService } from '@api/services/associations.service';
import toast from 'react-hot-toast';
import { toApiError } from '@api/http/toApiError';

export const useAssociations = (params) => {
  return useQuery({
    queryKey: ['associations', params || {}],
    queryFn: () => associationsService.getAll(params)
  });
};

export const useCreateAssociation = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: associationsService.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['associations'] });
      toast.success('Association créée');
    },
    onError: (error) => {
      const apiError = toApiError(error);
      toast.error(apiError.message || "Erreur lors de la création");
    }
  });
};

export const useUpdateAssociation = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, payload }) => associationsService.update(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['associations'] });
      toast.success('Association mise à jour');
    },
    onError: (error) => {
      const apiError = toApiError(error);
      toast.error(apiError.message || 'Erreur lors de la mise à jour');
    }
  });
};

export const useSuspendAssociation = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: associationsService.suspend,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['associations'] });
      toast.success('Association suspendue');
    },
    onError: (error) => {
      const apiError = toApiError(error);
      toast.error(apiError.message || 'Erreur lors de la suspension');
    }
  });
};

export const useActivateAssociation = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: associationsService.activate,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['associations'] });
      toast.success('Association activée');
    },
    onError: (error) => {
      const apiError = toApiError(error);
      toast.error(apiError.message || 'Erreur lors de l\'activation');
    }
  });
};

export const useArchiveAssociation = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: associationsService.archive,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['associations'] });
      toast.success('Association archivée');
    },
    onError: (error) => {
      const apiError = toApiError(error);
      toast.error(apiError.message || "Erreur lors de l'archivage");
    }
  });
};
