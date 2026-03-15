import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { MobileHeader } from './MobileHeader';
import { DesktopHeader } from './DesktopHeader';
import { SearchModal } from './SearchModal';

const AppLayout = ({ children }) => {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [searchOpen, setSearchOpen] = useState(false);

  return (
    <div className="flex h-screen overflow-hidden bg-slate-100/60">
      {/* Mobile sidebar backdrop */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 z-40 bg-slate-900/40 backdrop-blur-sm lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* Sidebar */}
      <div
        className={`fixed inset-y-0 left-0 z-50 w-72 transform transition-transform duration-300 lg:relative lg:translate-x-0 ${
          sidebarOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <Sidebar onClose={() => setSidebarOpen(false)} />
      </div>

      {/* Main content */}
      <div className="flex flex-1 flex-col overflow-hidden">
        <MobileHeader onMenuClick={() => setSidebarOpen(true)} onSearchClick={() => setSearchOpen(true)} />
        <DesktopHeader onSearchClick={() => setSearchOpen(true)} />
        <main className="flex-1 overflow-y-auto px-4 py-4 sm:px-6">
          <div className="mx-auto w-full max-w-7xl">{children ?? <Outlet />}</div>
        </main>
      </div>

      <SearchModal isOpen={searchOpen} onClose={() => setSearchOpen(false)} />
    </div>
  );
};

export default AppLayout;
