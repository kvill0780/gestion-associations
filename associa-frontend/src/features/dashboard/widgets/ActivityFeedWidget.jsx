import { Card } from '@components/common/data/Card';
import { Badge } from '@components/common/data/Badge';
import { formatRelativeTime } from '@utils/formatters';

/**
 * Widget affichant les activités récentes
 * Visible pour: tous les utilisateurs (permissions: null)
 */
const ActivityFeedWidget = ({ data }) => {
    const stats = data || {};
    const activities = stats.recentActivities || [];

    return (
        <Card title="Activités récentes">
            <div className="space-y-3">
                {activities.length > 0 ? (
                    activities.slice(0, 5).map((activity, idx) => (
                    <div key={idx} className="flex items-start justify-between border-b pb-3 last:border-0">
                        <div className="min-w-0">
                            <p className="truncate text-sm font-medium text-gray-900">{activity.description}</p>
                            <p className="text-xs text-gray-500">
                                {activity.userName || 'Système'} • {formatRelativeTime(activity.createdAt)}
                            </p>
                        </div>
                        {activity.entityType && (
                            <Badge variant="default" className="ml-3">
                                {activity.entityType}
                            </Badge>
                        )}
                    </div>
                    ))
                ) : (
                    <p className="text-sm text-gray-500">Aucune activité récente</p>
                )}
            </div>
        </Card>
    );
};

export default ActivityFeedWidget;
