import { NavLink } from 'react-router-dom';
import { ArrowRightOnRectangleIcon } from '@heroicons/react/24/outline';
import { useAuth } from '@hooks/useAuth';
import { APP_CONFIG, FEATURES } from '@config/app.config';
import { navigationItems } from '@config/navigation.config';
import { hasAnyPermission } from '@utils/permissions';

export const Sidebar = ({ onClose }) => {
  const { user, logout } = useAuth();
  const initials = `${user?.firstName?.[0] || ''}${user?.lastName?.[0] || ''}`.toUpperCase();
  const visibleNavigationItems = navigationItems.filter((item) => {
    if (item.feature && !FEATURES[item.feature]) {
      return false;
    }
    if (item.permissionsAny?.length) {
      return hasAnyPermission(user, item.permissionsAny);
    }
    return true;
  });

  return (
    <aside className="flex h-screen w-72 flex-col border-r border-slate-200 bg-white/95 backdrop-blur">
      {/* Logo */}
      <div className="flex h-20 items-center border-b border-slate-200 px-6">
        <div className="mr-3 flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-primary-600 to-primary-700 text-sm font-bold text-white shadow-md">
          AS
        </div>
        <div>
          <h1 className="text-lg font-extrabold tracking-tight text-slate-900">{APP_CONFIG.name}</h1>
          <p className="text-xs font-medium text-slate-500">Workspace</p>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 space-y-1.5 px-4 py-5">
        {visibleNavigationItems.map((item) => (
          <NavLink
            key={item.name}
            to={item.href}
            onClick={onClose}
            className={({ isActive }) =>
              `group flex items-center rounded-xl px-3 py-2.5 text-sm font-semibold transition ${
                isActive
                  ? 'bg-primary-50 text-primary-700 ring-1 ring-primary-100'
                  : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
              }`
            }
          >
            <item.icon className="mr-3 h-5 w-5 shrink-0" />
            {item.name}
          </NavLink>
        ))}
      </nav>

      {/* User section */}
      <div className="border-t border-slate-200 p-4">
        <div className="mb-4 rounded-xl bg-slate-50 p-3">
          <div className="flex items-center">
            <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary-600 text-sm font-semibold text-white">
              {initials}
            </div>
            <div className="ml-3 min-w-0">
              <p className="truncate text-sm font-semibold text-slate-900">
                {user?.firstName} {user?.lastName}
              </p>
              <p className="truncate text-xs text-slate-500">{user?.email}</p>
            </div>
          </div>
        </div>
        <button
          onClick={logout}
          className="flex w-full items-center rounded-xl px-3 py-2.5 text-sm font-semibold text-slate-600 transition hover:bg-slate-100 hover:text-slate-900"
        >
          <ArrowRightOnRectangleIcon className="mr-3 h-5 w-5" />
          Déconnexion
        </button>
      </div>
    </aside>
  );
};
