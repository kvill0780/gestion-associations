export const APP_CONFIG = {
  name: import.meta.env.VITE_APP_NAME || 'Associa',
  version: import.meta.env.VITE_APP_VERSION || '1.0.0',
  locale: 'fr-FR',
  currency: 'XOF',
  dateFormat: 'dd/MM/yyyy',
  timeFormat: 'HH:mm'
};

const parseFlag = (value, fallback = false) => {
  if (value === undefined || value === null) return fallback;
  return String(value).toLowerCase() === 'true';
};

export const FEATURES = {
  announcements: parseFlag(import.meta.env.VITE_FEATURE_ANNOUNCEMENTS, false),
  documents: parseFlag(import.meta.env.VITE_FEATURE_DOCUMENTS, false),
  messages: parseFlag(import.meta.env.VITE_FEATURE_MESSAGES, false),
  gallery: parseFlag(import.meta.env.VITE_FEATURE_GALLERY, false),
  votes: parseFlag(import.meta.env.VITE_FEATURE_VOTES, false)
};
