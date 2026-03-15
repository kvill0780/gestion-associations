export const PERMISSIONS = {
  // Membres (format: module.action)
  MEMBERS_VIEW: 'members.view',
  MEMBERS_CREATE: 'members.create',
  MEMBERS_UPDATE: 'members.update',
  MEMBERS_DELETE: 'members.delete',
  MEMBERS_APPROVE: 'members.approve',

  // Finances
  FINANCES_VIEW: 'finances.view',
  FINANCES_CREATE: 'finances.create',
  FINANCES_UPDATE: 'finances.update',
  FINANCES_DELETE: 'finances.delete',
  FINANCES_APPROVE: 'finances.approve',
  FINANCES_EXPORT: 'finances.export',

  // Événements
  EVENTS_VIEW: 'events.view',
  EVENTS_CREATE: 'events.create',
  EVENTS_UPDATE: 'events.update',
  EVENTS_DELETE: 'events.delete',
  EVENTS_MANAGE: 'events.manage',

  // Documents
  DOCUMENTS_VIEW: 'documents.view',
  DOCUMENTS_UPLOAD: 'documents.upload',
  DOCUMENTS_DELETE: 'documents.delete',

  // Messages
  MESSAGES_VIEW: 'messages.view',
  MESSAGES_SEND: 'messages.send',
  MESSAGES_DELETE: 'messages.delete',

  // Annonces
  ANNOUNCEMENTS_VIEW: 'announcements.view',
  ANNOUNCEMENTS_CREATE: 'announcements.create',
  ANNOUNCEMENTS_UPDATE: 'announcements.update',
  ANNOUNCEMENTS_DELETE: 'announcements.delete',

  // Votes
  VOTES_VIEW: 'votes.view',
  VOTES_CREATE: 'votes.create',
  VOTES_MANAGE: 'votes.manage',
  VOTES_CAST: 'votes.cast',

  // Galerie
  GALLERY_VIEW: 'gallery.view',
  GALLERY_UPLOAD: 'gallery.upload',
  GALLERY_DELETE: 'gallery.delete',

  // Administration
  ROLES_MANAGE: 'roles.manage',
  POSTS_MANAGE: 'posts.manage',
  SETTINGS_VIEW: 'settings.view',
  SETTINGS_UPDATE: 'settings.update'
};

export const ROLES = {
  SUPER_ADMIN: 'super_admin',
  PRESIDENT: 'president',
  TREASURER: 'treasurer',
  SECRETARY: 'secretary',
  EVENT_MANAGER: 'event_manager',
  MEMBER: 'member'
};
