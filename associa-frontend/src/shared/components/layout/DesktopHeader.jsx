import { MagnifyingGlassIcon, BellIcon } from '@heroicons/react/24/outline';
import { useLocation } from 'react-router-dom';
import { navigationItems } from '@config/navigation.config';

const pageLabelByPath = navigationItems.reduce((acc, item) => {
  acc[item.href] = item.name;
  return acc;
}, {});

const resolvePageTitle = (pathname) => {
  if (pageLabelByPath[pathname]) {
    return pageLabelByPath[pathname];
  }

  const matchingItem = navigationItems
    .filter((item) => pathname.startsWith(`${item.href}/`))
    .sort((a, b) => b.href.length - a.href.length)[0];

  if (matchingItem) {
    return matchingItem.name;
  }

  return 'Associa';
};

export const DesktopHeader = ({ onSearchClick }) => {
  const location = useLocation();
  const pageTitle = resolvePageTitle(location.pathname);

  return (
    <header className="hidden border-b border-slate-200 bg-white/80 px-6 py-4 backdrop-blur lg:block">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wider text-slate-400">Associa</p>
          <h1 className="text-xl font-bold text-slate-900">{pageTitle}</h1>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={onSearchClick}
            className="rounded-xl border border-slate-200 bg-white p-2.5 text-slate-500 transition hover:border-slate-300 hover:text-slate-800"
          >
            <MagnifyingGlassIcon className="h-5 w-5" />
          </button>
          <button className="relative rounded-xl border border-slate-200 bg-white p-2.5 text-slate-500 transition hover:border-slate-300 hover:text-slate-800">
            <BellIcon className="h-5 w-5" />
            <span className="absolute right-2 top-2 h-2 w-2 rounded-full bg-emerald-500" />
          </button>
        </div>
      </div>
    </header>
  );
};
