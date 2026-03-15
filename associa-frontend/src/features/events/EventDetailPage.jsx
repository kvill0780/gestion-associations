import { useNavigate, useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { CalendarIcon, MapPinIcon, UsersIcon, ClockIcon } from '@heroicons/react/24/outline';
import { Card } from '@components/common/data/Card';
import { Badge } from '@components/common/data/Badge';
import { PageHeader } from '@components/common/data/PageHeader';
import { Button } from '@components/common/forms/Button';
import { Spinner } from '@components/common/feedback/Spinner';
import { eventsService } from '@api/services/events.service';
import { useChangeEventStatus } from '@hooks/useEvents';
import { usePermissions } from '@hooks/usePermissions';
import { formatDateTime } from '@utils/formatters';

const STATUS_LABELS = {
  draft: 'Brouillon',
  published: 'Publié',
  in_progress: 'En cours',
  completed: 'Terminé',
  cancelled: 'Annulé'
};

const STATUS_ACTIONS = {
  draft: [{ label: 'Publier', status: 'PUBLISHED', variant: 'warning' }],
  published: [
    { label: 'Démarrer', status: 'IN_PROGRESS', variant: 'primary' },
    { label: 'Annuler', status: 'CANCELLED', variant: 'danger' }
  ],
  in_progress: [
    { label: 'Terminer', status: 'COMPLETED', variant: 'primary' },
    { label: 'Annuler', status: 'CANCELLED', variant: 'danger' }
  ]
};

const EventDetailPage = () => {
  const { eventId } = useParams();
  const navigate = useNavigate();
  const { can } = usePermissions();
  const canManageEvents = can('events.manage');

  const { data: event, isLoading } = useQuery({
    queryKey: ['event', eventId],
    queryFn: () => eventsService.getById(eventId),
    enabled: !!eventId
  });

  const changeEventStatus = useChangeEventStatus();

  if (isLoading) {
    return <Spinner size="lg" />;
  }

  if (!event) {
    return (
      <Card>
        <p className="text-sm text-gray-600">Événement introuvable.</p>
        <div className="mt-4">
          <Button variant="secondary" onClick={() => navigate('/events')}>
            Retour aux événements
          </Button>
        </div>
      </Card>
    );
  }

  const statusActions = canManageEvents ? STATUS_ACTIONS[event.status] || [] : [];

  return (
    <div className="space-y-6">
      <PageHeader
        title={event.title}
        subtitle={event.type ? `Type: ${event.type}` : 'Détails de l’événement'}
        actions={
          <div className="flex gap-2">
            <Button variant="secondary" onClick={() => navigate('/events')}>
              Retour
            </Button>
            <Button onClick={() => navigate(`/events/${event.id}/attendance`)}>Présences</Button>
          </div>
        }
      />

      <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <div className="flex items-center gap-2 text-sm text-gray-600">
            <ClockIcon className="h-4 w-4" />
            Statut
          </div>
          <div className="mt-2">
            <Badge variant="info">{STATUS_LABELS[event.status] || event.status}</Badge>
          </div>
        </Card>

        <Card>
          <div className="flex items-center gap-2 text-sm text-gray-600">
            <CalendarIcon className="h-4 w-4" />
            Début
          </div>
          <p className="mt-2 text-sm font-medium text-gray-900">{formatDateTime(event.start_date)}</p>
        </Card>

        <Card>
          <div className="flex items-center gap-2 text-sm text-gray-600">
            <CalendarIcon className="h-4 w-4" />
            Fin
          </div>
          <p className="mt-2 text-sm font-medium text-gray-900">{formatDateTime(event.end_date)}</p>
        </Card>

        <Card>
          <div className="flex items-center gap-2 text-sm text-gray-600">
            <UsersIcon className="h-4 w-4" />
            Capacité
          </div>
          <p className="mt-2 text-sm font-medium text-gray-900">
            {event.max_participants ?? 'Illimitée'}
          </p>
        </Card>
      </div>

      <Card title="Informations">
        <div className="space-y-3">
          <div className="flex items-center gap-2 text-sm text-gray-700">
            <MapPinIcon className="h-4 w-4" />
            <span>{event.location || 'Lieu non défini'}</span>
          </div>

          <div>
            <p className="mb-1 text-sm font-medium text-gray-700">Description</p>
            <p className="text-sm text-gray-600">{event.description || 'Aucune description'}</p>
          </div>
        </div>
      </Card>

      {statusActions.length > 0 ? (
        <Card title="Changer le statut">
          <div className="flex flex-wrap gap-2">
            {statusActions.map((action) => (
              <Button
                key={`${event.id}-${action.status}`}
                variant={action.variant}
                onClick={() => changeEventStatus.mutate({ eventId: event.id, status: action.status })}
                disabled={changeEventStatus.isPending}
              >
                {action.label}
              </Button>
            ))}
          </div>
        </Card>
      ) : null}
    </div>
  );
};

export default EventDetailPage;
