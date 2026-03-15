import { Card } from '@components/common/data/Card';
import { CalendarIcon } from '@heroicons/react/24/outline';
import { Button } from '@components/common/forms/Button';
import { useNavigate } from 'react-router-dom';

/**
 * Widget affichant les événements à venir
 * Visible pour: events.view, events_all
 */
const UpcomingEventsWidget = ({ data }) => {
    const navigate = useNavigate();
    const stats = data || {};
    const upcomingCount = stats.events?.totalUpcoming || 0;

    return (
        <Card title="Événements à venir" badge={upcomingCount}>
            <div className="space-y-3">
                {upcomingCount > 0 ? (
                    <div className="text-center py-4">
                        <CalendarIcon className="h-12 w-12 text-purple-500 mx-auto mb-2" />
                        <p className="text-sm text-gray-600 mb-3">
                            {upcomingCount} événement{upcomingCount > 1 ? 's' : ''} à venir
                        </p>
                        <Button
                            variant="secondary"
                            size="sm"
                            onClick={() => navigate('/events')}
                        >
                            Voir tous les événements
                        </Button>
                    </div>
                ) : (
                    <div className="text-center py-4">
                        <CalendarIcon className="h-12 w-12 text-gray-400 mx-auto mb-2" />
                        <p className="text-sm text-gray-600">Aucun événement prévu</p>
                    </div>
                )}
            </div>
        </Card>
    );
};

export default UpcomingEventsWidget;
