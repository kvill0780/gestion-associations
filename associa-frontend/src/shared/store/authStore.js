import { create } from 'zustand';
import { storage } from '@utils/storage';

export const useAuthStore = create((set) => ({
  user: null,
  token: storage.get('auth_token'),
  refreshToken: storage.get('refresh_token'),
  isAuthenticated: !!storage.get('auth_token'),

  setAuth: (user, token, refreshToken) => {
    storage.set('auth_token', token);
    storage.set('refresh_token', refreshToken);
    set({ user, token, refreshToken, isAuthenticated: true });
  },

  setUser: (user) => set({ user }),

  updateToken: (newToken) => {
    storage.set('auth_token', newToken);
    set({ token: newToken, isAuthenticated: !!newToken });
  },

  updateTokens: (newToken, newRefreshToken) => {
    storage.set('auth_token', newToken);
    storage.set('refresh_token', newRefreshToken);
    set({ token: newToken, refreshToken: newRefreshToken, isAuthenticated: !!newToken });
  },

  logout: () => {
    storage.remove('auth_token');
    storage.remove('refresh_token');
    set({ user: null, token: null, refreshToken: null, isAuthenticated: false });
  }
}));
