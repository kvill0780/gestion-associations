import { useEffect, useMemo, useState } from 'react';
import { ShieldCheckIcon, BriefcaseIcon, ClockIcon, RectangleStackIcon, BuildingOfficeIcon } from '@heroicons/react/24/outline';
import { PageHeader } from '@components/common/data/PageHeader';
import { Card } from '@components/common/data/Card';
import RolesTab from './RolesTab';
import PostsTab from './PostsTab';
import MandatesTab from './MandatesTab';
import AuditTrailTab from './AuditTrailTab';
import AssociationsTab from './AssociationsTab';
import { usePermissions } from '@hooks/usePermissions';

const SettingsPage = () => {
  const { can, canAny, isSuperAdmin } = usePermissions();
  const canAccessSettings = isSuperAdmin || canAny(['roles.manage', 'posts.manage', 'settings.view', 'settings.update']);
  const tabs = useMemo(() => {
    const availableTabs = [];

    if (can('roles.manage')) {
      availableTabs.push({ id: 'roles', name: 'Rôles', icon: ShieldCheckIcon });
    }
    if (can('posts.manage')) {
      availableTabs.push({ id: 'posts', name: 'Postes', icon: RectangleStackIcon });
      availableTabs.push({ id: 'mandates', name: 'Mandats', icon: BriefcaseIcon });
    }
    if (canAny(['settings.view', 'settings.update'])) {
      availableTabs.push({ id: 'audit', name: 'Audit trail', icon: ClockIcon });
    }
    if (isSuperAdmin) {
      availableTabs.push({ id: 'associations', name: 'Associations', icon: BuildingOfficeIcon });
    }

    return availableTabs;
  }, [can, canAny, isSuperAdmin]);
  const [activeTab, setActiveTab] = useState('');

  useEffect(() => {
    if (!tabs.length) {
      setActiveTab('');
      return;
    }
    if (!activeTab || !tabs.some((tab) => tab.id === activeTab)) {
      setActiveTab(tabs[0].id);
    }
  }, [activeTab, tabs]);

  if (!canAccessSettings) {
    return (
      <div className="space-y-6">
        <PageHeader title="Paramètres" subtitle="Administration de l'application" />
        <Card>
          <p className="text-sm text-gray-600">
            Cet espace est réservé aux administrateurs de l'association.
          </p>
        </Card>
      </div>
    );
  }

  if (!tabs.length) {
    return (
      <div className="space-y-6">
        <PageHeader title="Paramètres" subtitle="Administration de l'application" />
        <Card>
          <p className="text-sm text-gray-600">
            Aucun module de paramétrage n'est disponible pour votre rôle actuel.
          </p>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <PageHeader title="Paramètres" subtitle="Administration de l'application" />

      <div className="rounded-2xl border border-slate-200 bg-white p-2 shadow-sm">
        <nav className="flex flex-wrap gap-2">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`flex items-center rounded-xl px-3 py-2.5 text-sm font-semibold transition ${
                activeTab === tab.id
                  ? 'bg-primary-50 text-primary-700 ring-1 ring-primary-100'
                  : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
              }`}
            >
              <tab.icon className="mr-2 h-5 w-5" />
              {tab.name}
            </button>
          ))}
        </nav>
      </div>

      <div>
        {activeTab === 'roles' && <RolesTab />}
        {activeTab === 'posts' && <PostsTab />}
        {activeTab === 'mandates' && <MandatesTab />}
        {activeTab === 'associations' && <AssociationsTab />}
        {activeTab === 'audit' && <AuditTrailTab />}
      </div>
    </div>
  );
};

export default SettingsPage;
