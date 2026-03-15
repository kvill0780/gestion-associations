export const API_CONFIG = {
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json'
  }
};

/**
 * API Endpoints for Spring Boot Backend
 * 
 * Convention: baseURL = http://localhost:8080/api
 * Endpoints start with / (e.g., /auth/login)
 * Final URL: baseURL + endpoint = http://localhost:8080/api/auth/login
 */
export const API_ENDPOINTS = {
  // ========== Auth ==========
  LOGIN: '/auth/login',
  LOGOUT: '/auth/logout',
  REGISTER: '/auth/register',
  REFRESH: '/auth/refresh',
  ME: '/auth/me',
  ME_PASSWORD: '/auth/me/password',
  FORGOT_PASSWORD: '/auth/forgot-password',
  RESET_PASSWORD: '/auth/reset-password',

  // ========== Users / Members ==========
  USERS: '/members/users',
  USER_DETAIL: (id) => `/members/users/${id}`,
  USER_ACTIVATE: (id) => `/members/users/${id}/activate`,
  USER_APPROVE: (id) => `/members/users/${id}/approve`,
  USER_SUSPEND: (id) => `/members/users/${id}/suspend`,
  USERS_BY_ASSOCIATION: (associationId) => `/members/users/association/${associationId}`,

  // ========== Roles ==========
  ROLES: '/members/roles',
  ROLE_DETAIL: (id) => `/members/roles/${id}`,
  ROLE_BY_ASSOCIATION: (associationId) => `/members/roles/association/${associationId}`,
  ROLE_ASSIGN: '/members/roles/assign',
  ROLE_REVOKE: '/members/roles/revoke',

  // ========== Mandates ==========
  MANDATES: '/members/mandates',
  MANDATE_DETAIL: (id) => `/members/mandates/${id}`,
  MANDATE_ASSIGN: '/members/mandates/assign-post',
  MANDATE_REVOKE: (id) => `/members/mandates/${id}/revoke`,
  MANDATE_EXTEND: (id) => `/members/mandates/${id}/extend`,
  MANDATE_BY_USER: (userId) => `/members/mandates/user/${userId}`,
  MANDATE_CURRENT: (associationId) => `/members/mandates/association/${associationId}/current`,

  // ========== Posts ==========
  POSTS: '/members/posts',
  POST_DETAIL: (id) => `/members/posts/${id}`,
  POST_SUGGESTED_ROLES: (id) => `/members/posts/${id}/suggested-roles`,
  POST_CURRENT_HOLDERS: (id) => `/members/posts/${id}/current-holders`,
  POST_STATS: (id) => `/members/posts/${id}/stats`,
  POST_LINK_ROLE: (postId, roleId) => `/members/posts/${postId}/roles/${roleId}`,
  POST_UNLINK_ROLE: (postId, roleId) => `/members/posts/${postId}/roles/${roleId}`,
  POST_ACTIVATE: (id) => `/members/posts/${id}/activate`,
  POST_DEACTIVATE: (id) => `/members/posts/${id}/deactivate`,

  // ========== Events ==========
  EVENTS: '/events',
  EVENT_DETAIL: (id) => `/events/${id}`,
  EVENT_STATUS: (id) => `/events/${id}/status`,
  EVENT_PARTICIPANTS: (id) => `/events/${id}/participants`,
  EVENT_ATTENDANCE_SUMMARY: (id) => `/events/${id}/attendance-summary`,
  EVENT_REGISTER_PARTICIPANT: (id) => `/events/${id}/participants/register`,
  EVENT_CHECKIN: (id) => `/events/${id}/check-in`,
  EVENT_PARTICIPANT_STATUS: (eventId, userId) => `/events/${eventId}/participants/${userId}/status`,

  // ========== Finance / Transactions ==========
  TRANSACTIONS: '/finance/transactions',
  TRANSACTION_APPROVE: (id) => `/finance/transactions/${id}/approve`,
  TRANSACTION_REJECT: (id) => `/finance/transactions/${id}/reject`,
  CONTRIBUTIONS: '/finance/contributions',
  CONTRIBUTION_DETAIL: (id) => `/finance/contributions/${id}`,
  CONTRIBUTION_STATS: '/finance/contributions/stats',
  CONTRIBUTION_GENERATE: '/finance/contributions/generate',
  CONTRIBUTION_PAYMENTS: (id) => `/finance/contributions/${id}/payments`,

  // ========== Associations ==========
  ASSOCIATIONS: '/system/associations',
  ASSOCIATION_DETAIL: (id) => `/system/associations/${id}`,
  ASSOCIATION_BY_SLUG: (slug) => `/system/associations/slug/${slug}`,
  ASSOCIATION_STATS: (id) => `/system/associations/${id}/stats`,
  ASSOCIATION_EXECUTIVE_BOARD: (id) => `/system/associations/${id}/executive-board`,
  ASSOCIATION_ACTIVE_MEMBERS: (id) => `/system/associations/${id}/active-members`,
  ASSOCIATION_SUSPEND: (id) => `/system/associations/${id}/suspend`,
  ASSOCIATION_ACTIVATE: (id) => `/system/associations/${id}/activate`,
  ASSOCIATION_ARCHIVE: (id) => `/system/associations/${id}/archive`,

  // ========== Audit / System ==========
  AUDIT_LOGS: '/system/audit/logs',
  AUDIT_SEARCH: '/system/audit/logs/search',

  // ========== Permissions (Core) ==========
  PERMISSIONS_ALL: '/core/permissions/all',
  PERMISSIONS_GROUPED: '/core/permissions/grouped',

  // ========== Dashboard (if implemented) ==========
  DASHBOARD: '/dashboard',
  SYSTEM_STATS: '/dashboard/system-stats',

  // ========== Modules en cours d'implémentation ==========
  ANNOUNCEMENTS: {
    BASE: '/announcements',
    REACT: (id) => `/announcements/${id}/react`,
    VOTE: (id) => `/announcements/${id}/vote`
  },
  DOCUMENTS: '/documents',
  FILE_UPLOAD: '/documents/upload',
  MESSAGES: {
    BASE: '/messages',
    CONVERSATION: (userId) => `/messages/conversation/${userId}`,
    UNREAD_COUNT: '/messages/unread-count'
  },
  VOTES: {
    BASE: '/votes',
    CAST: (id) => `/votes/${id}/cast`,
    PUBLISH: (id) => `/votes/${id}/publish`,
    CLOSE: (id) => `/votes/${id}/close`,
    RESULTS: (id) => `/votes/${id}/results`
  },
  GALLERY: {
    BASE: '/media',
    ALBUMS: '/media/albums'
  }
};
