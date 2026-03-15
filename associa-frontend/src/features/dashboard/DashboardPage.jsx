import { useState } from 'react';
import { Link } from 'react-router-dom';
import { PageHeader } from '@components/common/data/PageHeader';
import { SectionHeader } from '@components/common/data/SectionHeader';
import { useDashboard } from '@hooks/useDashboard';
import { usePermissions } from '@hooks/usePermissions';
import { useDashboardWidgets } from '@hooks/useDashboardWidgets';
import SuperAdminDashboard from './SuperAdminDashboard';
import { getDashboardHeader } from './helpers';
import DashboardSkeleton from './components/DashboardSkeleton';

// Import de tous les widgets
import StatsOverview from './widgets/StatsOverview';
import PresidentFocusWidget from './widgets/PresidentFocusWidget';
import PendingMembersWidget from './widgets/PendingMembersWidget';
import FinanceSummaryWidget from './widgets/FinanceSummaryWidget';
import ContributionsWidget from './widgets/ContributionsWidget';
import TransactionsListWidget from './widgets/TransactionsListWidget';
import BudgetWidget from './widgets/BudgetWidget';
import UpcomingEventsWidget from './widgets/UpcomingEventsWidget';
import DocumentsWidget from './widgets/DocumentsWidget';
import QuickActionsWidget from './widgets/QuickActionsWidget';
import ActivityFeedWidget from './widgets/ActivityFeedWidget';
import WelcomeWidget from './widgets/WelcomeWidget';

// Map des composants widgets
const WIDGET_COMPONENTS = {
  StatsOverview,
  PresidentFocusWidget,
  PendingMembersWidget,
  FinanceSummaryWidget,
  ContributionsWidget,
  TransactionsListWidget,
  BudgetWidget,
  UpcomingEventsWidget,
  DocumentsWidget,
  QuickActionsWidget,
  ActivityFeedWidget,
  WelcomeWidget
};

const ACTION_WIDGET_ID = 'QuickActionsWidget';

const DashboardPage = () => {
  const { user, isSuperAdmin } = usePermissions();
  const { data: dashboard, isLoading } = useDashboard();
  const widgets = useDashboardWidgets();
  const [showSecondaryWidgets, setShowSecondaryWidgets] = useState(false);
  const dashboardData = dashboard || {};

  if (isLoading) return <DashboardSkeleton />;

  // Super Admin → Dashboard systeme
  if (isSuperAdmin) {
    return <SuperAdminDashboard />;
  }

  const header = getDashboardHeader(user);
  const actionWidget = [...widgets.hero, ...widgets.core, ...widgets.secondary].find(
    (widget) => widget.id === ACTION_WIDGET_ID
  );

  const withoutActionWidget = (list) => list.filter((widget) => widget.id !== ACTION_WIDGET_ID);
  const heroWidgets = withoutActionWidget(widgets.hero);
  const coreWidgets = withoutActionWidget(widgets.core);
  const secondaryWidgets = withoutActionWidget(widgets.secondary);
  const displayedSecondaryWidgets = showSecondaryWidgets ? secondaryWidgets : [];

  const renderWidgets = (widgetConfigs, sectionName) =>
    widgetConfigs.map((widget) => {
      const WidgetComponent = WIDGET_COMPONENTS[widget.component];
      if (!WidgetComponent) return null;

      const spanClass = widget.span?.[sectionName] || '';
      return (
        <div key={`${sectionName}-${widget.id}`} className={spanClass}>
          <WidgetComponent data={dashboardData} />
        </div>
      );
    });

  const hasNoWidgets =
    widgets.totalCount === 0 ||
    (heroWidgets.length === 0 && coreWidgets.length === 0 && secondaryWidgets.length === 0);

  return (
    <div className="space-y-6">
      <PageHeader title={header.title} subtitle={header.description} />

      {actionWidget && renderWidgets([actionWidget], 'hero')}

      {heroWidgets.length > 0 && (
        <section>
          <SectionHeader
            title="Vue rapide de l'association"
            subtitle="Les indicateurs essentiels pour decider vite."
          />
          <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
            {renderWidgets(heroWidgets, 'hero')}
          </div>
        </section>
      )}

      {coreWidgets.length > 0 && (
        <section>
          <SectionHeader
            title="Situation financiere et activites"
            subtitle="Un point clair sur les finances et les evenements."
          />
          <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
            {renderWidgets(coreWidgets, 'core')}
          </div>
        </section>
      )}

      {secondaryWidgets.length > 0 && (
        <div className="rounded-2xl border border-slate-200 bg-white px-4 py-3 shadow-sm">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <p className="text-sm font-semibold text-slate-900">Vue essentielle</p>
              <p className="text-xs text-slate-500">
                {heroWidgets.length + coreWidgets.length} sections cles affichees
              </p>
            </div>
            <button
              type="button"
              onClick={() => setShowSecondaryWidgets((prev) => !prev)}
              className="rounded-xl border border-slate-200 px-3 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
            >
              {showSecondaryWidgets
                ? 'Masquer les details'
                : `Afficher plus (${secondaryWidgets.length})`}
            </button>
          </div>
        </div>
      )}

      {displayedSecondaryWidgets.length > 0 && (
        <div className="grid grid-cols-1 gap-6 md:grid-cols-2 xl:grid-cols-4">
          {renderWidgets(displayedSecondaryWidgets, 'secondary')}
        </div>
      )}

      {hasNoWidgets && (
        <div className="rounded-2xl border-2 border-dashed border-slate-300 bg-slate-50 py-16 text-center">
          <div className="mx-auto max-w-md">
            <h3 className="text-lg font-semibold text-slate-900">Aucune section disponible</h3>
            <p className="mt-2 text-sm text-slate-600">
              Vos permissions actuelles ne donnent pas acces au cockpit complet.
            </p>
            <div className="mt-5 flex justify-center gap-4 text-sm">
              <Link to="/events" className="font-semibold text-primary-600 hover:text-primary-700">
                Voir les evenements
              </Link>
              <Link to="/announcements" className="font-semibold text-primary-600 hover:text-primary-700">
                Lire les annonces
              </Link>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default DashboardPage;
