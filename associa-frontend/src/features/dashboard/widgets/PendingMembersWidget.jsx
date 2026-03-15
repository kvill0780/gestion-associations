import { Card } from '@components/common/data/Card';
import { Button } from '@components/common/forms/Button';
import { UserPlusIcon, CheckCircleIcon } from '@heroicons/react/24/outline';
import { useNavigate } from 'react-router-dom';

/**
 * Widget pour approuver les membres en attente
 * Visible pour: members.approve, admin_all
 */
const PendingMembersWidget = ({ data }) => {
    const navigate = useNavigate();
    const stats = data || {};
    const pendingCount = stats.members?.pending || 0;

    return (
        <Card title="Membres en attente" badge={pendingCount > 0 ? pendingCount : null}>
            <div className="space-y-3">
                {pendingCount > 0 ? (
                    <div className="text-center py-4">
                        <UserPlusIcon className="h-12 w-12 text-gray-400 mx-auto mb-2" />
                        <p className="text-sm text-gray-600 mb-3">
                            {pendingCount} membre{pendingCount > 1 ? 's' : ''} en attente d'approbation
                        </p>
                        <Button
                            variant="primary"
                            size="sm"
                            onClick={() => navigate('/members?filter=pending')}
                        >
                            <CheckCircleIcon className="h-4 w-4 mr-1" />
                            Traiter les demandes
                        </Button>
                    </div>
                ) : (
                    <div className="text-center py-4">
                        <CheckCircleIcon className="h-12 w-12 text-green-500 mx-auto mb-2" />
                        <p className="text-sm text-gray-600">Aucun membre en attente</p>
                    </div>
                )}
            </div>
        </Card>
    );
};

export default PendingMembersWidget;
