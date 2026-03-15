import { useMemo } from 'react';
import { Card } from '@components/common/data/Card';
import { Badge } from '@components/common/data/Badge';
import { Button } from '@components/common/forms/Button';
import { Spinner } from '@components/common/feedback/Spinner';
import {
  useAssociations,
  useSuspendAssociation,
  useActivateAssociation,
  useArchiveAssociation
} from '@hooks/useAssociations';

const AssociationsTab = () => {
  const { data: associations, isLoading } = useAssociations();
  const suspendMutation = useSuspendAssociation();
  const activateMutation = useActivateAssociation();
  const archiveMutation = useArchiveAssociation();

  const list = useMemo(() => associations || [], [associations]);

  if (isLoading) return <Spinner size="lg" />;

  const normalizeStatus = (status) => {
    if (!status) return status;
    return String(status).toLowerCase();
  };

  const getStatusBadge = (status) => {
    const s = normalizeStatus(status);

    const variants = {
      active: 'success',
      inactive: 'warning',
      suspended: 'danger',
      archived: 'secondary'
    };

    const labels = {
      active: 'Active',
      inactive: 'Inactive',
      suspended: 'Suspendue',
      archived: 'Archivée'
    };

    return <Badge variant={variants[s] || 'default'}>{labels[s] || status}</Badge>;
  };

  return (
    <div className="space-y-6">
      <Card>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                  Association
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                  Slug
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
                  Statut
                </th>
                <th className="px-6 py-3 text-right text-xs font-medium uppercase tracking-wider text-gray-500">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200 bg-white">
              {list.map((assoc) => {
                const status = normalizeStatus(assoc.status);
                const isActive = status === 'active';
                const isArchived = status === 'archived';

                const handleSuspend = () => {
                  if (!window.confirm(`Suspendre l'association "${assoc.name}" ?`)) return;
                  suspendMutation.mutate(assoc.id);
                };

                const handleActivate = () => {
                  if (!window.confirm(`Activer l'association "${assoc.name}" ?`)) return;
                  activateMutation.mutate(assoc.id);
                };

                const handleArchive = () => {
                  if (!window.confirm(`Archiver l'association "${assoc.name}" ?`)) return;
                  archiveMutation.mutate(assoc.id);
                };

                return (
                  <tr key={assoc.id} className="hover:bg-gray-50">
                    <td className="whitespace-nowrap px-6 py-4">
                      <div>
                        <p className="font-medium text-gray-900">{assoc.name}</p>
                      </div>
                    </td>
                    <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500">
                      {assoc.slug || '-'}
                    </td>
                    <td className="whitespace-nowrap px-6 py-4 text-sm">
                      {getStatusBadge(assoc.status)}
                    </td>
                    <td className="whitespace-nowrap px-6 py-4 text-right text-sm">
                      <div className="flex justify-end gap-2">
                        {isActive ? (
                          <Button
                            variant="danger"
                            className="text-xs"
                            onClick={handleSuspend}
                            disabled={suspendMutation.isPending}
                          >
                            Suspendre
                          </Button>
                        ) : (
                          <Button
                            variant="success"
                            className="text-xs"
                            onClick={handleActivate}
                            disabled={activateMutation.isPending}
                          >
                            Activer
                          </Button>
                        )}

                        {!isArchived && (
                          <Button
                            variant="secondary"
                            className="text-xs"
                            onClick={handleArchive}
                            disabled={archiveMutation.isPending}
                          >
                            Archiver
                          </Button>
                        )}
                      </div>
                    </td>
                  </tr>
                );
              })}

              {list.length === 0 && (
                <tr>
                  <td colSpan={4} className="px-6 py-10 text-center text-sm text-gray-500">
                    Aucune association
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

export default AssociationsTab;
