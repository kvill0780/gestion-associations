import { useState, useEffect } from 'react';
import { MagnifyingGlassIcon, XMarkIcon } from '@heroicons/react/24/outline';
import { useNavigate } from 'react-router-dom';
import { FEATURES } from '@config/app.config';
import { navigationItems } from '@config/navigation.config';
import { useAuthStore } from '@store/authStore';
import { hasAnyPermission } from '@utils/permissions';

export const SearchModal = ({ isOpen, onClose }) => {
  const [query, setQuery] = useState('');
  const user = useAuthStore((state) => state.user);
  const navigate = useNavigate();

  useEffect(() => {
    const handleEscape = (e) => {
      if (e.key === 'Escape') onClose();
    };
    if (isOpen) {
      document.addEventListener('keydown', handleEscape);
      return () => document.removeEventListener('keydown', handleEscape);
    }
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  const quickLinks = navigationItems
    .map((item) => ({
      name: item.name,
      path: item.href,
      icon: item.icon,
      feature: item.feature,
      permissionsAny: item.permissionsAny
    }))
    .filter((link) => {
      if (link.feature && !FEATURES[link.feature]) {
        return false;
      }
      if (link.permissionsAny?.length) {
        return hasAnyPermission(user, link.permissionsAny);
      }
      return true;
    });

  const filteredLinks = query
    ? quickLinks.filter((link) => link.name.toLowerCase().includes(query.toLowerCase()))
    : quickLinks;

  const handleSelect = (path) => {
    navigate(path);
    onClose();
    setQuery('');
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      <div className="flex min-h-screen items-start justify-center p-4 pt-20">
        <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm" onClick={onClose} />

        <div className="relative w-full max-w-2xl rounded-2xl border border-slate-200 bg-white shadow-2xl">
          <div className="flex items-center border-b border-slate-200 px-4 py-3">
            <MagnifyingGlassIcon className="h-5 w-5 text-slate-400" />
            <input
              type="text"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Aller à..."
              className="flex-1 border-none bg-transparent px-3 py-2 text-sm font-medium text-slate-700 focus:outline-none"
              autoFocus
            />
            <button onClick={onClose} className="rounded-lg p-1 hover:bg-slate-100">
              <XMarkIcon className="h-5 w-5 text-slate-400" />
            </button>
          </div>

          <div className="max-h-96 overflow-y-auto p-2">
            {filteredLinks.length > 0 ? (
              filteredLinks.map((link) => (
                <button
                  key={link.path}
                  onClick={() => handleSelect(link.path)}
                  className="flex w-full items-center space-x-3 rounded-xl px-4 py-3 text-left transition hover:bg-slate-100"
                >
                  <span className="rounded-lg bg-slate-100 p-2 text-slate-600">
                    <link.icon className="h-5 w-5" />
                  </span>
                  <span className="font-semibold text-slate-900">{link.name}</span>
                </button>
              ))
            ) : (
              <div className="py-8 text-center text-slate-500">Aucun résultat</div>
            )}
          </div>

          <div className="border-t border-slate-200 px-4 py-2 text-xs text-slate-500">
            Appuyez sur <kbd className="rounded bg-slate-100 px-2 py-1 font-semibold">ESC</kbd> pour fermer
          </div>
        </div>
      </div>
    </div>
  );
};
