import { useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';
import { QRCodeSVG } from 'qrcode.react';
import { CheckCircleIcon, QrCodeIcon } from '@heroicons/react/24/outline';
import { Card } from '@components/common/data/Card';
import { Badge } from '@components/common/data/Badge';
import { Button } from '@components/common/forms/Button';
import { Input } from '@components/common/forms/Input';
import { Spinner } from '@components/common/feedback/Spinner';
import { useQuery } from '@tanstack/react-query';
import { eventsService } from '@api/services/events.service';
import {
  useEventAttendanceSummary,
  useEventCheckIn,
  useEventParticipants,
  useRegisterEventParticipant,
  useUpdateEventParticipantStatus
} from '@hooks/useEventParticipation';
import { useMembers } from '@hooks/useMembers';
import { usePermissions } from '@hooks/usePermissions';
import { formatDateTime } from '@utils/formatters';

const STATUS_META = {
  registered: { label: 'Inscrit', variant: 'info' },
  attended: { label: 'Présent', variant: 'success' },
  absent: { label: 'Absent', variant: 'danger' },
  cancelled: { label: 'Annulé', variant: 'default' }
};

const EventAttendancePage = () => {
  const { eventId } = useParams();
  const [showQR, setShowQR] = useState(false);
  const [selectedUserId, setSelectedUserId] = useState('');
  const [manualNotes, setManualNotes] = useState('');

  const { can } = usePermissions();
  const canManage = can('events.manage');

  const { data: event, isLoading: eventLoading } = useQuery({
    queryKey: ['event', eventId],
    queryFn: () => eventsService.getById(eventId),
    enabled: !!eventId
  });

  const { data: participants, isLoading: participantsLoading } = useEventParticipants(eventId);
  const { data: summary, isLoading: summaryLoading } = useEventAttendanceSummary(eventId);
  const { data: members } = useMembers();

  const registerParticipant = useRegisterEventParticipant(eventId);
  const checkInMutation = useEventCheckIn(eventId);
  const updateStatusMutation = useUpdateEventParticipantStatus(eventId);

  const qrCodeUrl = `${window.location.origin}/events/${eventId}/checkin`;
  const participantsList = useMemo(() => participants || [], [participants]);

  const loading = eventLoading || participantsLoading || summaryLoading;

  const availableMembers = useMemo(() => {
    const rawMembers = Array.isArray(members) ? members : [];
    const participantIds = new Set(participantsList.map((participant) => participant.user_id));

    return rawMembers
      .map((member) => ({
        id: member.id ?? member.user_id,
        firstName: member.firstName ?? member.first_name ?? '',
        lastName: member.lastName ?? member.last_name ?? '',
        email: member.email ?? '',
        membershipStatus: (member.membershipStatus ?? member.membership_status ?? '').toString().toLowerCase()
      }))
      .filter(
        (member) =>
          member.id && member.membershipStatus === 'active' && !participantIds.has(member.id)
      );
  }, [members, participantsList]);

  const cards = useMemo(
    () => [
      { label: 'Total', value: summary?.total_participants ?? 0 },
      { label: 'Inscrits', value: summary?.registered_count ?? 0 },
      { label: 'Présents', value: summary?.attended_count ?? 0 },
      { label: 'Absents', value: summary?.absent_count ?? 0 }
    ],
    [summary]
  );

  const handleManualRegister = (e) => {
    e.preventDefault();
    const parsedId = Number(selectedUserId);
    if (!Number.isFinite(parsedId) || parsedId <= 0) {
      return;
    }

    registerParticipant.mutate(
      { userId: parsedId, notes: manualNotes || undefined },
      {
        onSuccess: () => {
          setSelectedUserId('');
          setManualNotes('');
        }
      }
    );
  };

  if (loading) return <Spinner size="lg" />;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Gestion des présences</h2>
          <p className="text-gray-600">{event?.title}</p>
        </div>
        <div className="flex gap-2">
          <Button variant="secondary" onClick={() => setShowQR((v) => !v)}>
            <QrCodeIcon className="mr-2 h-5 w-5" />
            QR Code
          </Button>
          <Button onClick={() => checkInMutation.mutate()} disabled={checkInMutation.isPending}>
            <CheckCircleIcon className="mr-2 h-5 w-5" />
            Je suis présent
          </Button>
        </div>
      </div>

      {showQR && (
        <Card>
          <div className="flex flex-col items-center p-6">
            <h3 className="mb-4 text-lg font-semibold">QR Code Check-in</h3>
            <QRCodeSVG value={qrCodeUrl} size={256} level="H" />
            <p className="mt-4 text-center text-sm text-gray-600">
              Scannez ce code pour ouvrir la page de check-in de cet événement.
            </p>
          </div>
        </Card>
      )}

      <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
        {cards.map((card) => (
          <Card key={card.label}>
            <p className="text-sm text-gray-500">{card.label}</p>
            <p className="mt-1 text-2xl font-semibold text-gray-900">{card.value}</p>
          </Card>
        ))}
      </div>

      <Card>
        <div className="grid grid-cols-1 gap-4 text-sm text-gray-700 md:grid-cols-3">
          <div>
            <p className="text-gray-500">Capacité max</p>
            <p className="font-medium">{summary?.max_participants ?? 'Illimitée'}</p>
          </div>
          <div>
            <p className="text-gray-500">Places disponibles</p>
            <p className="font-medium">{summary?.available_slots ?? 'N/A'}</p>
          </div>
          <div>
            <p className="text-gray-500">Statut événement</p>
            <p className="font-medium capitalize">{event?.status || '-'}</p>
          </div>
        </div>
      </Card>

      {canManage && (
        <Card title="Inscription manuelle">
          <form onSubmit={handleManualRegister} className="grid grid-cols-1 gap-3 md:grid-cols-3">
            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">Membre</label>
              <select
                value={selectedUserId}
                onChange={(e) => setSelectedUserId(e.target.value)}
                className="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500"
              >
                <option value="">Sélectionner un membre actif</option>
                {availableMembers.map((member) => (
                  <option key={member.id} value={member.id}>
                    {member.firstName} {member.lastName} ({member.email})
                  </option>
                ))}
              </select>
            </div>
            <Input
              label="Notes"
              value={manualNotes}
              onChange={(e) => setManualNotes(e.target.value)}
              placeholder="Optionnel"
            />
            <div className="flex items-end">
              <Button
                type="submit"
                className="w-full"
                disabled={registerParticipant.isPending || !selectedUserId}
              >
                Inscrire
              </Button>
            </div>
          </form>
          <p className="mt-2 text-xs text-gray-500">
            {availableMembers.length > 0
              ? 'Seuls les membres actifs non encore inscrits sont proposés.'
              : 'Aucun membre actif disponible pour inscription.'}
          </p>
        </Card>
      )}

      <Card title="Participants">
        {participantsList.length === 0 ? (
          <p className="py-6 text-center text-sm text-gray-500">Aucun participant enregistré.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wide text-gray-500">Membre</th>
                  <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wide text-gray-500">Statut</th>
                  <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wide text-gray-500">Inscription</th>
                  {canManage ? (
                    <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wide text-gray-500">Actions</th>
                  ) : null}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 bg-white">
                {participantsList.map((participant) => {
                  const statusMeta = STATUS_META[participant.status] || {
                    label: participant.status,
                    variant: 'default'
                  };

                  return (
                    <tr key={`${participant.event_id}-${participant.user_id}`}>
                      <td className="px-4 py-3">
                        <p className="text-sm font-medium text-gray-900">{participant.user_full_name}</p>
                        <p className="text-xs text-gray-500">{participant.user_email}</p>
                      </td>
                      <td className="px-4 py-3">
                        <Badge variant={statusMeta.variant}>{statusMeta.label}</Badge>
                      </td>
                      <td className="px-4 py-3 text-sm text-gray-600">
                        {participant.registered_at ? formatDateTime(participant.registered_at) : '-'}
                      </td>
                      {canManage ? (
                        <td className="px-4 py-3">
                          <div className="flex flex-wrap gap-2">
                            <Button
                              variant="secondary"
                              className="text-xs"
                              onClick={() =>
                                updateStatusMutation.mutate({
                                  userId: participant.user_id,
                                  status: 'REGISTERED'
                                })
                              }
                            >
                              Inscrit
                            </Button>
                            <Button
                              className="text-xs"
                              onClick={() =>
                                updateStatusMutation.mutate({
                                  userId: participant.user_id,
                                  status: 'ATTENDED'
                                })
                              }
                            >
                              Présent
                            </Button>
                            <Button
                              variant="danger"
                              className="text-xs"
                              onClick={() =>
                                updateStatusMutation.mutate({
                                  userId: participant.user_id,
                                  status: 'ABSENT'
                                })
                              }
                            >
                              Absent
                            </Button>
                          </div>
                        </td>
                      ) : null}
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  );
};

export default EventAttendancePage;
