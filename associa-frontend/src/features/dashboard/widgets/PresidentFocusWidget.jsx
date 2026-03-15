import { Card } from '@components/common/data/Card';
import { Button } from '@components/common/forms/Button';
import { formatCurrency } from '@utils/formatters';
import { useNavigate } from 'react-router-dom';
import { usePermissions } from '@hooks/usePermissions';

const statusStyles = {
  ok: 'bg-emerald-50 text-emerald-700 border-emerald-200',
  attention: 'bg-amber-50 text-amber-800 border-amber-200',
  warning: 'bg-amber-50 text-amber-800 border-amber-200',
  danger: 'bg-red-50 text-red-700 border-red-200',
  info: 'bg-slate-50 text-slate-700 border-slate-200'
};

const PresidentFocusWidget = ({ data }) => {
  const navigate = useNavigate();
  const { canAny } = usePermissions();
  const membersPending = data?.members?.pending ?? 0;
  const upcomingEvents = data?.events?.totalUpcoming ?? 0;
  const netMonthly = data?.finances?.netMonthly ?? 0;
  const currentBalance = data?.finances?.currentBalance ?? 0;

  const items = [
    {
      title: "Demandes d'adhesion",
      description:
        membersPending > 0
          ? `${membersPending} demande${membersPending > 1 ? 's' : ''} en attente`
          : 'Aucune demande en attente',
      actionLabel: membersPending > 0 ? 'Traiter' : 'Voir membres',
      action: () => navigate('/members?filter=pending'),
      showAction: canAny(['members.approve', 'members_all', 'admin_all']),
      status: membersPending > 0 ? 'attention' : 'ok'
    },
    {
      title: 'Evenements',
      description:
        upcomingEvents > 0
          ? `${upcomingEvents} evenement${upcomingEvents > 1 ? 's' : ''} planifie(s)`
          : 'Aucun evenement planifie',
      actionLabel: upcomingEvents > 0 ? 'Voir calendrier' : 'Creer un evenement',
      action: () => navigate(upcomingEvents > 0 ? '/events' : '/events?action=add'),
      showAction: canAny(['events.view', 'events.manage', 'events_all', 'admin_all']),
      status: upcomingEvents > 0 ? 'info' : 'warning'
    },
    {
      title: 'Situation financiere',
      description: `Solde net ${formatCurrency(netMonthly)}`,
      sub: `Solde actuel ${formatCurrency(currentBalance)}`,
      actionLabel: 'Voir finances',
      action: () => navigate('/transactions'),
      showAction: canAny(['finances.view', 'finances.approve', 'finances_all', 'admin_all']),
      status: netMonthly < 0 ? 'danger' : 'ok'
    }
  ];

  return (
    <Card title="Priorites aujourd'hui">
      <div className="space-y-3">
        {items.map((item) => (
          <div
            key={item.title}
            className={`flex flex-col gap-3 rounded-xl border px-4 py-3 sm:flex-row sm:items-center sm:justify-between ${
              statusStyles[item.status] || statusStyles.info
            }`}
          >
            <div>
              <p className="text-sm font-semibold text-slate-900">{item.title}</p>
              <p className="text-sm text-slate-700">{item.description}</p>
              {item.sub && <p className="text-xs text-slate-500">{item.sub}</p>}
            </div>
            {item.showAction && (
              <Button variant="secondary" size="sm" onClick={item.action}>
                {item.actionLabel}
              </Button>
            )}
          </div>
        ))}
      </div>
    </Card>
  );
};

export default PresidentFocusWidget;
