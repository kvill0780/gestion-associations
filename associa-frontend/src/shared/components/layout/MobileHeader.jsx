import { Bars3Icon, MagnifyingGlassIcon, BellIcon } from '@heroicons/react/24/outline';
import LanguageSwitcher from '@components/common/LanguageSwitcher';

export const MobileHeader = ({ onMenuClick, onSearchClick }) => {
  return (
    <header className="border-b border-slate-200 bg-white/95 px-4 py-3 backdrop-blur lg:hidden">
      <div className="flex items-center justify-between">
        <button onClick={onMenuClick} className="rounded-xl p-2.5 hover:bg-slate-100">
          <Bars3Icon className="h-6 w-6 text-slate-700" />
        </button>

        <h1 className="text-lg font-extrabold tracking-tight text-slate-900">Associa</h1>

        <div className="flex items-center space-x-1">
          <LanguageSwitcher />
          <button onClick={onSearchClick} className="rounded-xl p-2.5 hover:bg-slate-100">
            <MagnifyingGlassIcon className="h-5 w-5 text-slate-600" />
          </button>
          <button className="relative rounded-xl p-2.5 hover:bg-slate-100">
            <BellIcon className="h-5 w-5 text-slate-600" />
            <span className="absolute right-2 top-2 h-2 w-2 rounded-full bg-emerald-500" />
          </button>
        </div>
      </div>
    </header>
  );
};
