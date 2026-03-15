import { useAuthStore } from '@store/authStore';
import { hasPermission, hasAnyPermission, hasAllPermissions, isAdmin, isSuperAdmin, isMember } from '@utils/permissions';

/**
 * Hook pour gérer les permissions de l'utilisateur
 * Utilise la structure backend: user.permissions = ["members.view", "finances_all", ...]
 */
export const usePermissions = () => {
  const user = useAuthStore((state) => state.user);

  return {
    user,
    isAdmin: isAdmin(user),
    isSuperAdmin: isSuperAdmin(user),
    isMember: isMember(user),

    // Vérifier une permission spécifique
    can: (permission) => hasPermission(user, permission),

    // Vérifier si l'utilisateur a AU MOINS UNE des permissions (OR)
    canAny: (permissions) => hasAnyPermission(user, permissions),

    // Vérifier si l'utilisateur a TOUTES les permissions (AND)
    canAll: (permissions) => hasAllPermissions(user, permissions),

    // Alias pour compatibilité
    hasPermission: (permission) => hasPermission(user, permission),
    hasAnyPermission: (permissions) => hasAnyPermission(user, permissions),
    hasAllPermissions: (permissions) => hasAllPermissions(user, permissions),
  };
};
