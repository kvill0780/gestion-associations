import { Button } from '@components/common/forms/Button';
import {
  UserPlusIcon,
  BanknotesIcon,
  CalendarIcon,
  CheckCircleIcon
} from '@heroicons/react/24/outline';
import { useNavigate } from 'react-router-dom';
import { usePermissions } from '@hooks/usePermissions';

/**
 * Barre d'actions prioritaires compacte
 */
const QuickActionsWidget = () => {
  const navigate = useNavigate();
  const { canAny } = usePermissions();
  const iconColors = {
    blue: 'bg-primary-100 text-primary-700',
    green: 'bg-emerald-100 text-emerald-700',
    purple: 'bg-indigo-100 text-indigo-700'
  };

  const actions = [
    {
      label: 'Valider membres',
      icon: CheckCircleIcon,
      color: 'blue',
      onClick: () => navigate('/members?status=pending'),
      permissionsAny: ['members.approve', 'members_all', 'admin_all']
    },
    {
      label: 'Creer evenement',
      icon: CalendarIcon,
      color: 'purple',
      onClick: () => navigate('/events?action=add'),
      permissionsAny: ['events.create', 'events.manage', 'events_all', 'admin_all']
    },
    {
      label: 'Ajouter depense',
      icon: BanknotesIcon,
      color: 'green',
      onClick: () => navigate('/transactions?action=add'),
      permissionsAny: ['finances.create', 'finances.approve', 'finances_all', 'admin_all']
    },
    {
      label: 'Ajouter membre',
      icon: UserPlusIcon,
      color: 'blue',
      onClick: () => navigate('/members?action=add'),
      permissionsAny: ['members.create', 'members_all', 'admin_all']
    }
  ];

  const visibleActions = actions.filter((action) => {
    if (!action.permissionsAny || action.permissionsAny.length === 0) return true;
    return canAny(action.permissionsAny);
  });

  if (visibleActions.length === 0) return null;

  return (
    <div className="flex flex-wrap gap-2">
      {visibleActions.slice(0, 3).map((action, idx) => (
        <Button
          key={idx}
          variant="secondary"
          size="sm"
          onClick={action.onClick}
          className="flex items-center gap-2 px-3 py-2"
        >
          <span className={`rounded-lg p-2 ${iconColors[action.color] || iconColors.blue}`}>
            <action.icon className="h-5 w-5" />
          </span>
          <span className="text-sm font-semibold">{action.label}</span>
        </Button>
      ))}
    </div>
  );
};

export default QuickActionsWidget;
