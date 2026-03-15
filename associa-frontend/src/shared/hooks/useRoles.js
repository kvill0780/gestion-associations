import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { rolesService } from '@api/services/roles.service';
import toast from 'react-hot-toast';
import { toApiError } from '@api/http/toApiError';

export const useRoles = (params) => {
  return useQuery({
    queryKey: ['roles', params || {}],
    queryFn: () => rolesService.getAll(params)
  });
};

export const usePermissionsGrouped = (options = {}) => {
  return useQuery({
    queryKey: ['permissions-grouped'],
    queryFn: rolesService.getPermissionsGrouped,
    ...options
  });
};

export const useCreateRole = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: rolesService.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['roles'] });
      toast.success('Rôle créé');
    },
    onError: (error) => {
      const apiError = toApiError(error);
      toast.error(apiError.message || 'Erreur lors de la création');
    }
  });
};

export const useUpdateRole = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ roleId, roleData }) => rolesService.update(roleId, roleData),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['roles'] });
      toast.success('Rôle mis à jour');
    },
    onError: (error) => {
      const apiError = toApiError(error);
      toast.error(apiError.message || 'Erreur lors de la mise à jour');
    }
  });
};
