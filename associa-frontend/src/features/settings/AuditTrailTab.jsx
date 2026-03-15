import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { ClockIcon, UserIcon, FunnelIcon } from '@heroicons/react/24/outline';
import { Card } from '@components/common/data/Card';
import { Badge } from '@components/common/data/Badge';
import { Spinner } from '@components/common/feedback/Spinner';
import { formatDateTime } from '@utils/formatters';
import { auditService } from '@api/services/audit.service';

const AuditTrailTab = () => {
  const [filters, setFilters] = useState({
    module: '',
    action: '',
    severity: '',
    associationId: '',
    userId: ''
  });

  const [page, setPage] = useState(0);
  const [size, setSize] = useState(50);

  const normalizeAction = (action) => {
    if (!action) return action;
    return String(action).toLowerCase();
  };

  const { data: logs, isLoading } = useQuery({
    queryKey: ['activity-logs', filters, page, size],
    queryFn: async () => {
      const params = {
        ...(filters.action ? { action: filters.action } : {}),
        ...(filters.module ? { entityType: filters.module } : {})
        ,...(filters.severity ? { severity: filters.severity } : {})
        ,...(filters.associationId ? { associationId: Number(filters.associationId) } : {})
        ,...(filters.userId ? { userId: Number(filters.userId) } : {})
        ,page
        ,size
      };

      return auditService.searchLogs(params);
    }
  });

  const { data: stats } = useQuery({
    queryKey: ['activity-stats'],
    queryFn: async () => {
      return auditService.getStats();
    }
  });

  const actionColors = {
    create: 'success',
    update: 'warning',
    delete: 'danger',
    login: 'info',
    logout: 'default'
  };

  const moduleLabels = {
    events: 'Événements',
    transactions: 'Finances',
    members: 'Membres',
    documents: 'Documents',
    announcements: 'Annonces',
    messages: 'Messages'
  };

  if (isLoading) return <Spinner size="lg" />;

  const logsList = logs?.content || [];
  const totalPages = logs?.totalPages ?? 1;

  return (
    <div className="space-y-6">
      {/* Statistics */}
      {stats && (
        <div className="grid grid-cols-1 gap-6 md:grid-cols-3">
          <Card>
            <div className="text-center">
              <p className="text-sm text-gray-600">Total d'activités</p>
              <p className="mt-2 text-3xl font-bold text-gray-900">{stats.totalLogs}</p>
            </div>
          </Card>
          <Card>
            <div className="text-center">
              <p className="text-sm text-gray-600">Aujourd'hui</p>
              <p className="mt-2 text-3xl font-bold text-primary-600">{stats.logsLast24h}</p>
            </div>
          </Card>
          <Card>
            <div className="text-center">
              <p className="text-sm text-gray-600">Modules actifs</p>
              <p className="mt-2 text-3xl font-bold text-gray-900">-</p>
            </div>
          </Card>
        </div>
      )}

      {/* Filters */}
      <Card>
        <div className="flex items-center gap-4">
          <FunnelIcon className="h-5 w-5 text-gray-400" />
          <select
            value={filters.module}
            onChange={(e) => {
              setPage(0);
              setFilters({ ...filters, module: e.target.value });
            }}
            className="rounded-md border border-gray-300 px-3 py-2"
          >
            <option value="">Tous les modules</option>
            <option value="events">Événements</option>
            <option value="transactions">Finances</option>
            <option value="members">Membres</option>
            <option value="documents">Documents</option>
            <option value="announcements">Annonces</option>
          </select>
          <select
            value={filters.action}
            onChange={(e) => {
              setPage(0);
              setFilters({ ...filters, action: e.target.value });
            }}
            className="rounded-md border border-gray-300 px-3 py-2"
          >
            <option value="">Toutes les actions</option>
            <option value="create">Création</option>
            <option value="update">Modification</option>
            <option value="delete">Suppression</option>
            <option value="login">Connexion</option>
          </select>

          <select
            value={filters.severity}
            onChange={(e) => {
              setPage(0);
              setFilters({ ...filters, severity: e.target.value });
            }}
            className="rounded-md border border-gray-300 px-3 py-2"
          >
            <option value="">Toutes les sévérités</option>
            <option value="INFO">INFO</option>
            <option value="WARNING">WARNING</option>
            <option value="ERROR">ERROR</option>
            <option value="CRITICAL">CRITICAL</option>
          </select>

          <input
            value={filters.associationId}
            onChange={(e) => {
              setPage(0);
              setFilters({ ...filters, associationId: e.target.value });
            }}
            placeholder="AssociationId"
            className="w-32 rounded-md border border-gray-300 px-3 py-2"
          />

          <input
            value={filters.userId}
            onChange={(e) => {
              setPage(0);
              setFilters({ ...filters, userId: e.target.value });
            }}
            placeholder="UserId"
            className="w-24 rounded-md border border-gray-300 px-3 py-2"
          />

          <select
            value={String(size)}
            onChange={(e) => {
              setPage(0);
              setSize(Number(e.target.value));
            }}
            className="rounded-md border border-gray-300 px-3 py-2"
          >
            <option value="20">20</option>
            <option value="50">50</option>
            <option value="100">100</option>
          </select>
        </div>
      </Card>

      {/* Activity Logs */}
      <Card title="Historique des activités">
        <div className="mb-4 flex items-center justify-between">
          <div className="text-sm text-gray-500">
            Page {page + 1} / {totalPages}
          </div>
          <div className="flex gap-2">
            <button
              className="rounded-md border px-3 py-1 text-sm disabled:opacity-50"
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              disabled={page <= 0}
              type="button"
            >
              Précédent
            </button>
            <button
              className="rounded-md border px-3 py-1 text-sm disabled:opacity-50"
              onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
              disabled={page >= totalPages - 1}
              type="button"
            >
              Suivant
            </button>
          </div>
        </div>
        <div className="space-y-3">
          {logsList.map((log) => (
            <div
              key={log.id}
              className="flex items-start gap-4 rounded-lg border p-4 hover:bg-gray-50"
            >
              <div className="flex h-10 w-10 items-center justify-center rounded-full bg-gray-100">
                <ClockIcon className="h-5 w-5 text-gray-600" />
              </div>
              <div className="flex-1">
                <div className="flex items-center gap-2 mb-1">
                  <Badge variant={actionColors[normalizeAction(log.action)] || 'default'}>
                    {normalizeAction(log.action)}
                  </Badge>
                  <Badge variant="info">{moduleLabels[log.entityType] || log.entityType}</Badge>
                </div>
                <div className="flex items-center gap-2 text-sm text-gray-600">
                  <UserIcon className="h-4 w-4" />
                  <span className="font-medium">{log.userName || log.userEmail || 'Système'}</span>
                  <span>•</span>
                  <span>{formatDateTime(log.createdAt)}</span>
                </div>
              </div>
            </div>
          ))}
          {logsList.length === 0 && (
            <p className="py-8 text-center text-gray-500">Aucune activité trouvée</p>
          )}
        </div>
      </Card>
    </div>
  );
};

export default AuditTrailTab;
