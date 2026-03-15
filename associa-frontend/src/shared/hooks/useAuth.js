import { useMutation, useQueryClient } from '@tanstack/react-query';
import { authService } from '@api/services/auth.service';
import { useAuthStore } from '@store/authStore';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { toApiError } from '@api/http/toApiError';

export const useAuth = () => {
  const { user, setAuth, setUser, logout: logoutStore } = useAuthStore();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const loginMutation = useMutation({
    mutationFn: authService.login,
    onSuccess: async (response) => {
      // response = { accessToken, refreshToken, user }
      const { accessToken, refreshToken, user } = response;

      setAuth(user, accessToken, refreshToken);
      try {
        const profile = await authService.getProfile();
        setUser(profile);
        queryClient.setQueryData(['profile'], profile);
      } catch (_error) {
        // On garde l'utilisateur basique si le profil complet échoue
      }

      // Redirection selon le type d'utilisateur :
      // - Super Admin → /system/dashboard (gère TOUTES les associations)
      // - Utilisateurs d'association → /dashboard (widgets adaptatifs par permissions)
      navigate(user?.isSuperAdmin ? '/system/dashboard' : '/dashboard');
      toast.success('Connexion réussie');
    },
    onError: (error) => {
      const apiError = toApiError(error);
      toast.error(apiError.message || 'Erreur de connexion');
    }
  });

  const logoutMutation = useMutation({
    mutationFn: () => {
      const refreshToken = authService.getStoredRefreshToken();
      if (!refreshToken) {
        return Promise.resolve();
      }
      return authService.logout(refreshToken);
    },
    onSuccess: () => {
      logoutStore();
      queryClient.clear();
      toast.success('Déconnexion réussie');
      navigate('/login');
    },
    onError: (error) => {
      logoutStore();
      queryClient.clear();
      const apiError = toApiError(error);
      toast.error(apiError.message || 'Erreur de déconnexion');
      navigate('/login');
    }
  });

  return {
    user,
    login: loginMutation.mutate,
    logout: logoutMutation.mutate,
    isLoggingIn: loginMutation.isPending,
    isLoggingOut: logoutMutation.isPending
  };
};
