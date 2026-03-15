import { Card } from '@components/common/data/Card';
import { HandRaisedIcon } from '@heroicons/react/24/outline';
import { Link } from 'react-router-dom';
import { useAuthStore } from '@store/authStore';

/**
 * Widget de bienvenue pour les membres simples
 * Visible pour: tous (permissions: null)
 */
const WelcomeWidget = ({ data: _data }) => {
    const user = useAuthStore((state) => state.user);

    return (
        <Card>
            <div className="text-center py-8">
                <HandRaisedIcon className="h-16 w-16 text-primary-600 mx-auto mb-4" />
                <h3 className="text-2xl font-bold text-gray-900 mb-2">
                    Bienvenue{user?.firstName ? `, ${user.firstName}` : ''} !
                </h3>
                <p className="text-gray-600 mb-4">
                    Vous êtes membre de l'association. Consultez les événements à venir et les annonces.
                </p>
                <div className="flex justify-center space-x-4">
                    <Link
                        to="/events"
                        className="text-primary-600 hover:text-primary-700 font-medium text-sm"
                    >
                        Voir les événements →
                    </Link>
                    <Link
                        to="/announcements"
                        className="text-primary-600 hover:text-primary-700 font-medium text-sm"
                    >
                        Lire les annonces →
                    </Link>
                </div>
            </div>
        </Card>
    );
};

export default WelcomeWidget;
