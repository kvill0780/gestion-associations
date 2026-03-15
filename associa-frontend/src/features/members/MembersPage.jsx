import { useState } from 'react';
import { PlusIcon, MagnifyingGlassIcon } from '@heroicons/react/24/outline';
import { Card } from '@components/common/data/Card';
import { Badge } from '@components/common/data/Badge';
import { PageHeader } from '@components/common/data/PageHeader';
import { SectionHeader } from '@components/common/data/SectionHeader';
import { Button } from '@components/common/forms/Button';
import { Input } from '@components/common/forms/Input';
import { Spinner } from '@components/common/feedback/Spinner';
import { useMembers, useUpdateMemberStatus } from '@hooks/useMembers';
import { usePermissions } from '@hooks/usePermissions';
import { getInitials } from '@utils/helpers';
import { formatDate } from '@utils/formatters';
import MemberDetailModal from './components/MemberDetailModal';
import MemberEditModal from './components/MemberEditModal';

const MembersPage = () => {
  const { data: members, isLoading } = useMembers();
  const updateStatus = useUpdateMemberStatus();
  const { can } = usePermissions();
  const [search, setSearch] = useState('');
  const [selectedMember, setSelectedMember] = useState(null);
  const [editingMember, setEditingMember] = useState(null);

  if (isLoading) return <Spinner size="lg" />;

  const membersList = members || [];
  const filteredMembers = membersList.filter(
    (m) =>
      m.firstName?.toLowerCase().includes(search.toLowerCase()) ||
      m.lastName?.toLowerCase().includes(search.toLowerCase()) ||
      m.email?.toLowerCase().includes(search.toLowerCase())
  );

  const normalizeStatus = (status) => {
    if (!status) return status;
    return String(status).toLowerCase();
  };

  const getStatusBadge = (status) => {
    const normalizedStatus = normalizeStatus(status);
    const variants = {
      active: 'success',
      pending: 'warning',
      inactive: 'danger',
      suspended: 'danger',
      expired: 'warning',
      left: 'secondary'
    };
    const labels = {
      active: 'Actif',
      pending: 'En attente',
      inactive: 'Inactif',
      suspended: 'Suspendu',
      expired: 'Expiré',
      left: 'Parti'
    };
    return <Badge variant={variants[normalizedStatus]}>{labels[normalizedStatus]}</Badge>;
  };

  const handleApprove = (userId) => {
    updateStatus.mutate({ userId, statusData: { action: 'approve' } });
  };

  const canApproveMembers = can('members.approve');
  const canUpdateMembers = can('members.update');
  const canCreateMembers = can('members.create');

  return (
    <div className="space-y-6">
      <PageHeader
        title="Membres"
        subtitle="Gestion des membres de l'association"
        actions={
          canCreateMembers ? (
            <Button>
              <PlusIcon className="mr-2 h-5 w-5" />
              Inviter un membre
            </Button>
          ) : null
        }
      />

      <Card>
        <SectionHeader
          title="Annuaire des membres"
          subtitle="Consultez les profils et les statuts d'adhésion"
          count={`${filteredMembers.length}/${membersList.length}`}
          countVariant="info"
        />

        <div className="mb-4">
          <div className="relative">
            <MagnifyingGlassIcon className="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-gray-400" />
            <Input
              placeholder="Rechercher un membre..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="pl-10"
            />
          </div>
        </div>

        <div className="overflow-x-auto rounded-xl border border-slate-200">
          <table className="min-w-full divide-y divide-slate-200">
            <thead className="bg-slate-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">
                  Membre
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">
                  Statut
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">
                  Rôles
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">
                  Adhésion
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200 bg-white">
              {filteredMembers.map((member) => (
                <tr
                  key={member.id}
                  className="cursor-pointer hover:bg-slate-50"
                  onClick={() => setSelectedMember(member)}
                >
                  <td className="whitespace-nowrap px-6 py-4">
                    <div className="flex items-center">
                      <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary-600 text-white font-medium">
                        {getInitials(member.firstName, member.lastName)}
                      </div>
                      <div className="ml-4">
                        <div className="text-sm font-medium text-gray-900">
                          {member.firstName} {member.lastName}
                        </div>
                        <div className="text-sm text-gray-500">{member.email}</div>
                      </div>
                    </div>
                  </td>
                  <td className="whitespace-nowrap px-6 py-4">{getStatusBadge(member.membershipStatus)}</td>
                  <td className="whitespace-nowrap px-6 py-4">
                    <div className="flex flex-wrap gap-1">
                      {member.roles?.map((role, idx) => (
                        <Badge key={idx} variant="info">
                          {role.name || role}
                        </Badge>
                      ))}
                    </div>
                  </td>
                  <td className="whitespace-nowrap px-6 py-4 text-sm text-slate-500">
                    {member.membershipDate ? formatDate(member.membershipDate) : '-'}
                  </td>
                  <td className="whitespace-nowrap px-6 py-4 text-sm">
                    <div className="flex gap-2">
                      {normalizeStatus(member.membershipStatus) === 'pending' && canApproveMembers && (
                        <Button
                          variant="success"
                          onClick={(e) => {
                            e.stopPropagation();
                            handleApprove(member.id);
                          }}
                          className="text-xs"
                        >
                          Approuver
                        </Button>
                      )}
                      {normalizeStatus(member.membershipStatus) === 'active' && canUpdateMembers && (
                        <Button
                          variant="danger"
                          onClick={(e) => {
                            e.stopPropagation();
                            updateStatus.mutate({ userId: member.id, statusData: { action: 'suspend' } });
                          }}
                          className="text-xs"
                        >
                          Suspendre
                        </Button>
                      )}
                      {normalizeStatus(member.membershipStatus) === 'inactive' && canUpdateMembers && (
                        <Button
                          variant="success"
                          onClick={(e) => {
                            e.stopPropagation();
                            updateStatus.mutate({ userId: member.id, statusData: { action: 'activate' } });
                          }}
                          className="text-xs"
                        >
                          Activer
                        </Button>
                      )}
                      {normalizeStatus(member.membershipStatus) === 'suspended' && canUpdateMembers && (
                        <Button
                          variant="success"
                          onClick={(e) => {
                            e.stopPropagation();
                            updateStatus.mutate({ userId: member.id, statusData: { action: 'activate' } });
                          }}
                          className="text-xs"
                        >
                          Activer
                        </Button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>

      <MemberDetailModal
        member={selectedMember}
        isOpen={!!selectedMember}
        onClose={() => setSelectedMember(null)}
        onEdit={(member) => {
          setSelectedMember(null);
          setEditingMember(member);
        }}
      />

      <MemberEditModal
        member={editingMember}
        isOpen={!!editingMember}
        onClose={() => setEditingMember(null)}
      />
    </div>
  );
};

export default MembersPage;
