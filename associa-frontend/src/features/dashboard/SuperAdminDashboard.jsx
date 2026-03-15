import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import {
  BuildingOfficeIcon,
  UsersIcon,
  CheckCircleIcon,
  ExclamationTriangleIcon
} from '@heroicons/react/24/outline';
import { Card } from '@components/common/data/Card';
import { Badge } from '@components/common/data/Badge';
import { Button } from '@components/common/forms/Button';
import { PageHeader } from '@components/common/data/PageHeader';
import { Spinner } from '@components/common/feedback/Spinner';
import { formatRelativeTime } from '@utils/formatters';
import { associationsService } from '@api/services/associations.service';
import { auditService } from '@api/services/audit.service';

const toArray = (payload) => {
  if (Array.isArray(payload)) return payload;
  if (Array.isArray(payload?.content)) return payload.content;
  if (Array.isArray(payload?.data?.content)) return payload.data.content;
  if (Array.isArray(payload?.data)) return payload.data;
  return [];
};

const toNumber = (value) => {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
};

const normalizeStatus = (status) => String(status || 'UNKNOWN').toUpperCase();

const statusToVariant = (status) => {
  if (status === 'ACTIVE') return 'success';
  if (status === 'SUSPENDED') return 'danger';
  if (status === 'INACTIVE' || status === 'ARCHIVED') return 'warning';
  return 'default';
};

const SuperAdminDashboard = () => {
  const [showAllAssociations, setShowAllAssociations] = useState(false);

  const { data: dashboardData, isLoading, refetch, isFetching } = useQuery({
    queryKey: ['super-admin-dashboard'],
    queryFn: async () => {
      const [associationsRes, auditRes] = await Promise.all([
        associationsService.getAll(),
        auditService.getLogs({ size: 20 })
      ]);

      const associations = toArray(associationsRes);
      const recentActivities = toArray(auditRes).map((log) => ({
        id: log.id,
        action: log.action || 'UNKNOWN',
        module: log.entityType || '-',
        user: log.userName || log.userEmail || (log.userId != null ? `User#${log.userId}` : 'Systeme'),
        association: log.associationId != null ? `Association#${log.associationId}` : '-',
        createdAt: log.createdAt
      }));

      const totalUsers = associations.reduce((sum, assoc) => sum + toNumber(assoc?.totalMembersCount), 0);
      const activeAssociations = associations.filter((assoc) => normalizeStatus(assoc?.status) === 'ACTIVE').length;
      const suspendedAssociations = associations.filter((assoc) => normalizeStatus(assoc?.status) === 'SUSPENDED').length;
      const associationsWithoutContact = associations.filter((assoc) => !assoc?.contactEmail).length;
      const associationsWithoutMembers = associations.filter((assoc) => toNumber(assoc?.totalMembersCount) === 0).length;

      return {
        associations,
        kpis: {
          totalAssociations: associations.length,
          activeAssociations,
          suspendedAssociations,
          totalUsers
        },
        recentActivities,
        alerts: {
          associationsWithoutContact,
          associationsWithoutMembers
        }
      };
    }
  });

  if (isLoading) return <Spinner size="lg" />;

  const visibleAssociations = showAllAssociations
    ? dashboardData.associations
    : dashboardData.associations.slice(0, 10);

  const statsCards = [
    {
      label: 'Associations',
      value: dashboardData.kpis.totalAssociations,
      subtitle: `${dashboardData.kpis.activeAssociations} actives`,
      icon: BuildingOfficeIcon,
      tone: 'blue'
    },
    {
      label: 'Actives',
      value: dashboardData.kpis.activeAssociations,
      subtitle: 'Associations operationnelles',
      icon: CheckCircleIcon,
      tone: 'green'
    },
    {
      label: 'Suspendues',
      value: dashboardData.kpis.suspendedAssociations,
      subtitle: 'A traiter en priorite',
      icon: ExclamationTriangleIcon,
      tone: 'amber'
    },
    {
      label: 'Utilisateurs',
      value: dashboardData.kpis.totalUsers,
      subtitle: 'Tous tenants confondus',
      icon: UsersIcon,
      tone: 'slate'
    }
  ];

  const toneClasses = {
    blue: 'bg-primary-100 text-primary-700',
    green: 'bg-emerald-100 text-emerald-700',
    amber: 'bg-amber-100 text-amber-700',
    slate: 'bg-slate-100 text-slate-700'
  };

  const riskIndicators = [];
  if (dashboardData.kpis.suspendedAssociations > 0) {
    riskIndicators.push(`${dashboardData.kpis.suspendedAssociations} association(s) suspendue(s) a traiter.`);
  }
  if (dashboardData.kpis.totalAssociations === 0) {
    riskIndicators.push('Aucune association enregistree.');
  }
  if (dashboardData.alerts.associationsWithoutContact > 0) {
    riskIndicators.push(`${dashboardData.alerts.associationsWithoutContact} association(s) sans email de contact.`);
  }
  if (dashboardData.alerts.associationsWithoutMembers > 0) {
    riskIndicators.push(`${dashboardData.alerts.associationsWithoutMembers} association(s) sans membre actif.`);
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Dashboard système"
        subtitle="Operations quotidiennes multi-associations"
        actions={(
          <Button variant="secondary" size="sm" onClick={() => refetch()} disabled={isFetching}>
            {isFetching ? 'Actualisation...' : 'Actualiser'}
          </Button>
        )}
      />

      <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4">
        {statsCards.map((card) => (
          <Card key={card.label}>
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-semibold text-slate-500">{card.label}</p>
                <p className="mt-2 text-3xl font-extrabold tracking-tight text-slate-900">{card.value}</p>
                <p className="mt-1 text-xs text-slate-500">{card.subtitle}</p>
              </div>
              <div className={`rounded-xl p-3 ${toneClasses[card.tone]}`}>
                <card.icon className="h-8 w-8" />
              </div>
            </div>
          </Card>
        ))}
      </div>

      <Card title="Points d'attention">
        {riskIndicators.length > 0 ? (
          <div className="space-y-2">
            {riskIndicators.map((item) => (
              <div key={item} className="flex items-start gap-2 rounded-xl border border-amber-200 bg-amber-50 px-3 py-2">
                <ExclamationTriangleIcon className="mt-0.5 h-4 w-4 text-amber-700" />
                <p className="text-sm font-medium text-amber-800">{item}</p>
              </div>
            ))}
          </div>
        ) : (
          <div className="rounded-xl border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm font-medium text-emerald-800">
            Aucun point critique detecte.
          </div>
        )}
      </Card>

      <Card title="Activite systeme recente">
        <div className="max-h-96 space-y-3 overflow-y-auto">
          {dashboardData.recentActivities.slice(0, 12).map((activity) => (
            <div key={activity.id} className="rounded-xl border border-slate-200 bg-slate-50 px-3 py-2">
              <div className="mb-1 flex items-center gap-2">
                <Badge variant="default">{activity.module}</Badge>
                <span className="text-xs font-semibold uppercase text-slate-500">{activity.action}</span>
              </div>
              <p className="text-sm font-medium text-slate-900">{activity.user}</p>
              <p className="text-xs text-slate-500">
                {activity.association} • {formatRelativeTime(activity.createdAt)}
              </p>
            </div>
          ))}
          {dashboardData.recentActivities.length === 0 && (
            <p className="py-6 text-sm text-slate-500">Aucune activite systeme recente.</p>
          )}
        </div>
      </Card>

      <Card
        title="Associations"
        actions={dashboardData.associations.length > 10 ? (
          <Button
            variant="secondary"
            size="sm"
            onClick={() => setShowAllAssociations((prev) => !prev)}
          >
            {showAllAssociations ? 'Voir moins' : `Voir tout (${dashboardData.associations.length})`}
          </Button>
        ) : null}
      >
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-slate-200">
            <thead className="bg-slate-50">
              <tr>
                <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">
                  Association
                </th>
                <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">
                  Membres
                </th>
                <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">
                  Contact
                </th>
                <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">
                  Statut
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200 bg-white">
              {visibleAssociations.map((assoc) => {
                const status = normalizeStatus(assoc.status);

                return (
                  <tr key={assoc.id} className="hover:bg-slate-50">
                    <td className="whitespace-nowrap px-4 py-3">
                      <p className="font-semibold text-slate-900">{assoc.name}</p>
                      <p className="text-xs text-slate-500">{assoc.slug}</p>
                    </td>
                    <td className="whitespace-nowrap px-4 py-3 text-sm text-slate-700">
                      {toNumber(assoc.totalMembersCount)}
                    </td>
                    <td className="whitespace-nowrap px-4 py-3 text-sm text-slate-500">
                      {assoc.contactEmail || '-'}
                    </td>
                    <td className="whitespace-nowrap px-4 py-3 text-sm">
                      <Badge variant={statusToVariant(status)}>{status}</Badge>
                    </td>
                  </tr>
                );
              })}
              {visibleAssociations.length === 0 && (
                <tr>
                  <td colSpan={4} className="px-4 py-8 text-center text-sm text-slate-500">
                    Aucune association disponible.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  );
};

export default SuperAdminDashboard;
