import { useMutation, useQueryClient } from '@tanstack/react-query';
import { authService } from '@api/services/auth.service';
import { useAuthStore } from '@store/authStore';
import { toApiError } from '@api/http/toApiError';
import toast from 'react-hot-toast';

export const useUpdateProfile = () => {
  const queryClient = useQueryClient();
  const setUser = useAuthStore((state) => state.setUser);

  return useMutation({
    mutationFn: authService.updateProfile,
    onSuccess: (user) => {
      setUser(user);
      queryClient.invalidateQueries({ queryKey: ['profile'] });
      toast.success('Profil mis à jour');
    },
    onError: (error) => {
      const apiError = toApiError(error);
      toast.error(apiError.message || 'Erreur lors de la mise à jour');
    }
  });
};

export const useChangePassword = () => {
  return useMutation({
    mutationFn: authService.changePassword,
    onSuccess: () => {
      toast.success('Mot de passe mis à jour');
    },
    onError: (error) => {
      const apiError = toApiError(error);
      toast.error(apiError.message || 'Erreur lors du changement de mot de passe');
    }
  });
};
