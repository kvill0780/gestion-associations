import { useParams, useNavigate } from 'react-router-dom';
import { CheckCircleIcon, ExclamationTriangleIcon } from '@heroicons/react/24/solid';
import { Card } from '@components/common/data/Card';
import { Button } from '@components/common/forms/Button';
import { Spinner } from '@components/common/feedback/Spinner';
import { useQuery } from '@tanstack/react-query';
import { eventsService } from '@api/services/events.service';
import { toApiError } from '@api/http/toApiError';
import { useEventCheckIn, useEventParticipants } from '@hooks/useEventParticipation';
import { useAuthStore } from '@store/authStore';

const EventCheckInPage = () => {
  const { eventId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuthStore();

  const { data: event, isLoading: eventLoading } = useQuery({
    queryKey: ['event', eventId],
    queryFn: () => eventsService.getById(eventId),
    enabled: !!eventId,
    retry: false
  });

  const { data: participants, isLoading: participantsLoading } = useEventParticipants(eventId);
  const checkInMutation = useEventCheckIn(eventId);

  if (eventLoading || participantsLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Spinner size="lg" />
      </div>
    );
  }

  const participantList = participants || [];
  const existingParticipant = participantList.find((participant) => participant.user_id === user?.id);
  const checkedIn =
    checkInMutation.data?.status === 'attended' || existingParticipant?.status === 'attended';

  const checkInError = checkInMutation.isError
    ? toApiError(checkInMutation.error).message || 'Impossible d’enregistrer la présence.'
    : null;

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 p-4">
      <Card className="max-w-md text-center">
        <div className="p-8">
          {checkedIn ? (
            <>
              <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-green-100">
                <CheckCircleIcon className="h-10 w-10 text-green-600" />
              </div>
              <h2 className="mb-2 text-2xl font-bold text-gray-900">Présence enregistrée</h2>
              <p className="mb-4 text-gray-600">
                Votre présence est déjà enregistrée pour <strong>{event?.title}</strong>.
              </p>
            </>
          ) : (
            <>
              <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-amber-100">
                <ExclamationTriangleIcon className="h-10 w-10 text-amber-600" />
              </div>
              <h2 className="mb-2 text-2xl font-bold text-gray-900">Check-in événement</h2>
              <p className="mb-4 text-gray-600">
                Enregistrez votre présence pour <strong>{event?.title}</strong>.
              </p>
              <Button
                onClick={() => checkInMutation.mutate()}
                disabled={checkInMutation.isPending}
                className="mb-3 w-full"
              >
                {checkInMutation.isPending ? 'Enregistrement...' : 'Je confirme ma présence'}
              </Button>
              {checkInError ? <p className="mb-3 text-sm text-red-600">{checkInError}</p> : null}
            </>
          )}

          <Button variant="secondary" onClick={() => navigate('/events')} className="w-full">
            Retour aux événements
          </Button>
        </div>
      </Card>
    </div>
  );
};

export default EventCheckInPage;
