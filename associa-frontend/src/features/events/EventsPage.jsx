import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  PlusIcon,
  CalendarIcon,
  MapPinIcon,
  UsersIcon,
  ClipboardDocumentCheckIcon,
  QrCodeIcon
} from '@heroicons/react/24/outline';
import { useNavigate } from 'react-router-dom';
import QRScanner from '@components/common/QRScanner';
import { Card } from '@components/common/data/Card';
import { Badge } from '@components/common/data/Badge';
import { PageHeader } from '@components/common/data/PageHeader';
import { SectionHeader } from '@components/common/data/SectionHeader';
import { Button } from '@components/common/forms/Button';
import { Input } from '@components/common/forms/Input';
import { RequiredLabel } from '@components/common/forms/RequiredLabel';
import { Modal } from '@components/common/feedback/Modal';
import { Spinner } from '@components/common/feedback/Spinner';
import { useEvents, useCreateEvent, useChangeEventStatus } from '@hooks/useEvents';
import { usePermissions } from '@hooks/usePermissions';
import { formatDate, formatDateTime } from '@utils/formatters';
import { EventCalendar } from './components/EventCalendar';

const eventSchema = z.object({
  title: z.string().min(3, 'Minimum 3 caractères'),
  description: z.string().min(10, 'Minimum 10 caractères'),
  location: z.string().min(3, 'Lieu requis'),
  start_date: z.string(),
  end_date: z.string(),
  max_participants: z.string().optional()
});

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

const EventsPage = () => {
  const { data: events, isLoading } = useEvents();
  const createEvent = useCreateEvent();
  const changeEventStatus = useChangeEventStatus();
  const { can } = usePermissions();
  const canManageEvents = can('events.manage');

  const navigate = useNavigate();
  const [showModal, setShowModal] = useState(false);
  const [showScanner, setShowScanner] = useState(false);
  const [viewMode, setViewMode] = useState('grid');

  const handleQRScan = (url) => {
    setShowScanner(false);
    const match = url.match(/\/events\/(\d+)\/checkin/);
    if (match) {
      navigate(`/events/${match[1]}/checkin`);
    }
  };

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors }
  } = useForm({
    resolver: zodResolver(eventSchema)
  });

  const onSubmit = (data) => {
    if (!canManageEvents) {
      return;
    }

    createEvent.mutate(data, {
      onSuccess: () => {
        setShowModal(false);
        reset();
      }
    });
  };

  if (isLoading) return <Spinner size="lg" />;

  const eventsList = events?.data || [];
  const upcomingEvents = eventsList.filter((eventItem) => new Date(eventItem.start_date) > new Date());
  const pastEvents = eventsList.filter((eventItem) => new Date(eventItem.start_date) <= new Date());

  return (
    <div className="space-y-6">
      <PageHeader
        title="Événements"
        subtitle="Gestion des événements de l'association"
        actions={
          <>
            <Button variant="secondary" onClick={() => setShowScanner(true)}>
              <QrCodeIcon className="mr-2 h-5 w-5" />
              Scanner
            </Button>
            <Button
              variant={viewMode === 'grid' ? 'primary' : 'secondary'}
              onClick={() => setViewMode('grid')}
            >
              Grille
            </Button>
            <Button
              variant={viewMode === 'calendar' ? 'primary' : 'secondary'}
              onClick={() => setViewMode('calendar')}
            >
              <CalendarIcon className="mr-2 h-5 w-5" />
              Calendrier
            </Button>
            {canManageEvents ? (
              <Button onClick={() => setShowModal(true)}>
                <PlusIcon className="mr-2 h-5 w-5" />
                Créer
              </Button>
            ) : null}
          </>
        }
      />

      {viewMode === 'calendar' ? (
        <EventCalendar events={eventsList} />
      ) : (
        <>
          <div>
            <SectionHeader
              title="À venir"
              subtitle="Événements planifiés dans les prochains jours"
              count={upcomingEvents.length}
              countVariant="info"
            />
            <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
              {upcomingEvents.map((event) => {
                const statusActions = canManageEvents ? STATUS_ACTIONS[event.status] || [] : [];

                return (
                  <Card key={event.id} className="transition-shadow hover:shadow-lg">
                    <div className="space-y-3">
                      <div className="flex items-start justify-between">
                        <h4 className="text-lg font-semibold text-gray-900">{event.title}</h4>
                        <Badge variant="info">{event.status}</Badge>
                      </div>

                      <p className="line-clamp-2 text-sm text-gray-600">{event.description}</p>

                      <div className="space-y-2 text-sm text-gray-500">
                        <div className="flex items-center">
                          <CalendarIcon className="mr-2 h-4 w-4" />
                          {formatDateTime(event.start_date)}
                        </div>
                        <div className="flex items-center">
                          <MapPinIcon className="mr-2 h-4 w-4" />
                          {event.location}
                        </div>
                        {event.max_participants ? (
                          <div className="flex items-center">
                            <UsersIcon className="mr-2 h-4 w-4" />
                            {event.participants_count || 0} / {event.max_participants}
                          </div>
                        ) : null}
                      </div>

                      <div className="space-y-2">
                        {statusActions.length > 0 ? (
                          <div className="flex flex-wrap gap-2">
                            {statusActions.map((action) => (
                              <Button
                                key={`${event.id}-${action.status}`}
                                variant={action.variant}
                                onClick={() =>
                                  changeEventStatus.mutate({ eventId: event.id, status: action.status })
                                }
                                disabled={changeEventStatus.isPending}
                                className="text-sm"
                              >
                                {action.label}
                              </Button>
                            ))}
                          </div>
                        ) : null}

                        <div className="flex gap-2">
                          <Button
                            variant="secondary"
                            className="flex-1 text-sm"
                            onClick={() => navigate("/events/" + event.id)}
                          >
                            Détails
                          </Button>
                          <Button
                            variant="primary"
                            onClick={() => navigate(`/events/${event.id}/attendance`)}
                            className="flex-1 text-sm"
                          >
                            <ClipboardDocumentCheckIcon className="mr-1 h-4 w-4" />
                            Présences
                          </Button>
                        </div>
                      </div>
                    </div>
                  </Card>
                );
              })}
            </div>
            {upcomingEvents.length === 0 ? (
              <div className="rounded-xl border border-dashed border-slate-300 bg-slate-50 py-10 text-center text-slate-500">
                Aucun événement à venir
              </div>
            ) : null}
          </div>

          {pastEvents.length > 0 ? (
            <div>
              <SectionHeader
                title="Passés"
                subtitle="Historique des événements terminés"
                count={pastEvents.length}
              />
              <div className="space-y-3">
                {pastEvents.map((event) => (
                  <Card key={event.id} className="opacity-75">
                    <div className="flex items-center justify-between">
                      <div>
                        <h4 className="font-medium text-gray-900">{event.title}</h4>
                        <p className="text-sm text-gray-500">{formatDate(event.start_date)}</p>
                      </div>
                      <Badge variant="default">{event.participants_count || 0} participants</Badge>
                    </div>
                  </Card>
                ))}
              </div>
            </div>
          ) : null}
        </>
      )}

      <Modal isOpen={showModal} onClose={() => setShowModal(false)} title="Créer un événement">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Input label="Titre" required error={errors.title?.message} {...register('title')} />

          <div>
            <RequiredLabel required>Description</RequiredLabel>
            <textarea
              className="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500"
              rows={3}
              required
              {...register('description')}
            />
            {errors.description ? (
              <p className="mt-1 text-sm text-red-600">{errors.description.message}</p>
            ) : null}
          </div>

          <Input label="Lieu" required error={errors.location?.message} {...register('location')} />

          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Date de début"
              required
              type="datetime-local"
              error={errors.start_date?.message}
              {...register('start_date')}
            />
            <Input
              label="Date de fin"
              required
              type="datetime-local"
              error={errors.end_date?.message}
              {...register('end_date')}
            />
          </div>

          <Input
            label="Nombre max de participants (optionnel)"
            type="number"
            {...register('max_participants')}
          />

          <div className="flex justify-end space-x-3">
            <Button type="button" variant="secondary" onClick={() => setShowModal(false)}>
              Annuler
            </Button>
            <Button type="submit" disabled={createEvent.isPending || !canManageEvents}>
              {createEvent.isPending ? 'Création...' : 'Créer'}
            </Button>
          </div>
        </form>
      </Modal>

      {showScanner ? <QRScanner onScan={handleQRScan} onClose={() => setShowScanner(false)} /> : null}
    </div>
  );
};

export default EventsPage;
