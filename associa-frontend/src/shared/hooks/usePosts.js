import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { postsService } from '@api/services/posts.service';
import toast from 'react-hot-toast';
import { toApiError } from '@api/http/toApiError';

export const usePosts = (params) => {
  return useQuery({
    queryKey: ['posts', params || {}],
    queryFn: () => postsService.getAll(params)
  });
};

export const useCreatePost = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: postsService.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['posts'] });
      toast.success('Poste créé avec succès');
    },
    onError: (error) => {
      const apiError = toApiError(error);
      toast.error(apiError.message || 'Erreur lors de la création');
    }
  });
};

export const useUpdatePost = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }) => postsService.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['posts'] });
      toast.success('Poste modifié avec succès');
    },
    onError: (error) => {
      const apiError = toApiError(error);
      toast.error(apiError.message || 'Erreur lors de la modification');
    }
  });
};

export const useDeletePost = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: postsService.remove,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['posts'] });
      toast.success('Poste supprimé avec succès');
    },
    onError: (error) => {
      const apiError = toApiError(error);
      toast.error(apiError.message || 'Erreur lors de la suppression');
    }
  });
};
