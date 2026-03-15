/**
 * Système de permissions aligné avec le backend Spring Boot
 * 
 * Backend structure:
 * user.permissions = ["members.view", "finances.approve", "finances_all", ...]
 * user.isSuperAdmin = true/false
 * 
 * Permissions format: "module.action" (e.g., "members.view", "finances.create")
 * Macros: "module_all" (e.g., "finances_all" → toutes les permissions finances)
 * Special: "admin_all" → toutes les permissions sauf super_admin
 */

/**
 * Vérifie si l'utilisateur a une permission spécifique
 * @param {Object} user - Objet utilisateur avec permissions array
 * @param {string} permission - Permission à vérifier (e.g., "members.view")
 * @returns {boolean}
 */
export const hasPermission = (user, permission) => {
  if (!user) return false;

  // Super admin a toutes les permissions
  if (user.isSuperAdmin) return true;

  const userPermissions = user.permissions || [];

  // admin_all donne toutes les permissions (sauf super_admin)
  if (userPermissions.includes('admin_all')) {
    return permission !== 'super_admin';
  }

  // Vérifier les macros par module (finances_all → finances.*)
  const [module] = permission.split('.');
  if (module && userPermissions.includes(`${module}_all`)) {
    return true;
  }

  // Vérifier la permission exacte
  return userPermissions.includes(permission);
};

/**
 * Vérifie si l'utilisateur a un rôle spécifique (par nom)
 * @param {Object} user - Objet utilisateur
 * @param {string} roleName - Nom du rôle (e.g., "Président", "Trésorier")
 * @returns {boolean}
 */
export const hasRole = (user, roleName) => {
  if (!user || !user.roles) return false;
  return user.roles.includes(roleName);
};

/**
 * Vérifie si l'utilisateur est super admin
 */
export const isSuperAdmin = (user) => {
  return user?.isSuperAdmin === true;
};

/**
 * Vérifie si l'utilisateur est admin (a admin_all)
 */
export const isAdmin = (user) => {
  return hasPermission(user, 'admin_all');
};

/**
 * Vérifie si l'utilisateur est membre simple (pas admin)
 */
export const isMember = (user) => {
  return user && !isAdmin(user) && !isSuperAdmin(user);
};

/**
 * Vérifie si l'utilisateur a AU MOINS UNE des permissions
 * @param {Object} user
 * @param {string[]} permissions - Array de permissions
 * @returns {boolean}
 */
export const hasAnyPermission = (user, permissions) => {
  if (!user) return false;
  if (user.isSuperAdmin) return true;

  return permissions.some(permission => hasPermission(user, permission));
};

/**
 * Vérifie si l'utilisateur a TOUTES les permissions
 * @param {Object} user
 * @param {string[]} permissions - Array de permissions
 * @returns {boolean}
 */
export const hasAllPermissions = (user, permissions) => {
  if (!user) return false;
  if (user.isSuperAdmin) return true;

  return permissions.every(permission => hasPermission(user, permission));
};

/**
 * Helpers de permissions par module (pour compatibilité)
 * Utilise les vraies permissions backend
 */
export const can = {
  // Membres
  viewMembers: (user) => hasPermission(user, 'members.view'),
  createMember: (user) => hasPermission(user, 'members.create'),
  updateMember: (user) => hasPermission(user, 'members.update'),
  deleteMember: (user) => hasPermission(user, 'members.delete'),
  approveMembers: (user) => hasPermission(user, 'members.approve'),

  // Finances
  viewFinances: (user) => hasPermission(user, 'finances.view'),
  createTransaction: (user) => hasPermission(user, 'finances.create'),
  updateTransaction: (user) => hasPermission(user, 'finances.update'),
  deleteTransaction: (user) => hasPermission(user, 'finances.delete'),
  approveTransaction: (user) => hasPermission(user, 'finances.approve'),
  exportFinances: (user) => hasPermission(user, 'finances.export'),

  // Événements
  viewEvents: (user) => hasPermission(user, 'events.view'),
  createEvent: (user) => hasPermission(user, 'events.create'),
  updateEvent: (user) => hasPermission(user, 'events.update'),
  deleteEvent: (user) => hasPermission(user, 'events.delete'),
  manageEvents: (user) => hasPermission(user, 'events.manage'),

  // Documents
  viewDocuments: (user) => hasPermission(user, 'documents.view'),
  uploadDocument: (user) => hasPermission(user, 'documents.upload'),
  deleteDocument: (user) => hasPermission(user, 'documents.delete'),
  shareDocument: (user) => hasPermission(user, 'documents.share'),

  // Messages
  viewMessages: (user) => hasPermission(user, 'messages.view'),
  sendMessage: (user) => hasPermission(user, 'messages.send'),
  deleteMessage: (user) => hasPermission(user, 'messages.delete'),

  // Annonces
  viewAnnouncements: (user) => hasPermission(user, 'announcements.view'),
  createAnnouncement: (user) => hasPermission(user, 'announcements.create'),
  updateAnnouncement: (user) => hasPermission(user, 'announcements.update'),
  deleteAnnouncement: (user) => hasPermission(user, 'announcements.delete'),

  // Votes
  viewVotes: (user) => hasPermission(user, 'votes.view'),
  createVote: (user) => hasPermission(user, 'votes.create'),
  manageVote: (user) => hasPermission(user, 'votes.manage'),
  castVote: (user) => hasPermission(user, 'votes.cast'),

  // Galerie
  viewGallery: (user) => hasPermission(user, 'gallery.view'),
  uploadMedia: (user) => hasPermission(user, 'gallery.upload'),
  deleteMedia: (user) => hasPermission(user, 'gallery.delete'),

  // Administration
  manageRoles: (user) => hasPermission(user, 'roles.manage'),
  managePosts: (user) => hasPermission(user, 'posts.manage'),
  viewSettings: (user) => hasPermission(user, 'settings.view'),
  updateSettings: (user) => hasPermission(user, 'settings.update'),

  // Associations (Super Admin uniquement)
  manageAssociations: (user) => isSuperAdmin(user),
};
