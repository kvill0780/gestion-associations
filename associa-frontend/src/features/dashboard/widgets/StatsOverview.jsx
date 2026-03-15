import { UsersIcon, BanknotesIcon, CalendarIcon, ClockIcon } from '@heroicons/react/24/outline';
import { StatCard } from '@components/common/data/StatCard';
import { formatCurrency } from '@utils/formatters';

/**
 * Widget affichant les statistiques principales (4 cartes)
 * Visible pour: admin_all, members.view, finances.view, events.view
 */
const StatsOverview = ({ data }) => {
  const stats = data || {};
  const members = stats.members || {};
  const finances = stats.finances || {};
  const events = stats.events || {};

  return (
    <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4">
      <StatCard title="Membres actifs" value={members.active || 0} icon={UsersIcon} color="blue" />
      <StatCard
        title="Solde actuel"
        value={formatCurrency(finances.currentBalance || 0)}
        icon={BanknotesIcon}
        color="green"
      />
      <StatCard
        title="Evenements a venir"
        value={events.totalUpcoming || 0}
        icon={CalendarIcon}
        color="purple"
      />
      <StatCard
        title="Demandes d'adhesion"
        value={members.pending || 0}
        icon={ClockIcon}
        color="yellow"
      />
    </div>
  );
};

export default StatsOverview;
