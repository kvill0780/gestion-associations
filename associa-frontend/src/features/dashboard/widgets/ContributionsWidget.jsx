import { BanknotesIcon, ClockIcon, CheckCircleIcon } from '@heroicons/react/24/outline';
import { Card } from '@components/common/data/Card';
import { Button } from '@components/common/forms/Button';
import { useContributionStats } from '@hooks/useContributions';
import { formatCurrency } from '@utils/formatters';
import { useNavigate } from 'react-router-dom';
import { Spinner } from '@components/common/feedback/Spinner';

/**
 * Widget cotisations (resume du mois)
 * Visible pour: finances.view, finances.approve, finances_all
 */
const ContributionsWidget = () => {
  const navigate = useNavigate();
  const today = new Date();
  const filters = {
    year: today.getFullYear(),
    month: today.getMonth() + 1
  };

  const { data, isLoading } = useContributionStats(filters);
  const stats = data || {};

  return (
    <Card title="Cotisations" badge={stats.totalMembers ?? 0}>
      {isLoading ? (
        <div className="flex items-center justify-center py-6">
          <Spinner size="md" />
        </div>
      ) : (
        <div className="space-y-4">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div className="flex items-center gap-3 rounded-xl border border-slate-200 bg-slate-50 px-4 py-3">
              <div className="rounded-lg bg-emerald-100 p-2 text-emerald-700">
                <CheckCircleIcon className="h-5 w-5" />
              </div>
              <div>
                <p className="text-xs font-semibold text-slate-500">A jour</p>
                <p className="text-lg font-bold text-slate-900">{stats.upToDate ?? 0}</p>
              </div>
            </div>
            <div className="flex items-center gap-3 rounded-xl border border-slate-200 bg-slate-50 px-4 py-3">
              <div className="rounded-lg bg-red-100 p-2 text-red-600">
                <ClockIcon className="h-5 w-5" />
              </div>
              <div>
                <p className="text-xs font-semibold text-slate-500">En retard</p>
                <p className="text-lg font-bold text-slate-900">{stats.late ?? 0}</p>
              </div>
            </div>
          </div>

          <div className="rounded-xl border border-slate-200 px-4 py-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="rounded-lg bg-primary-100 p-2 text-primary-700">
                  <BanknotesIcon className="h-5 w-5" />
                </div>
                <div>
                  <p className="text-xs font-semibold text-slate-500">Collecte du mois</p>
                  <p className="text-lg font-bold text-slate-900">
                    {formatCurrency(stats.totalCollected || 0)}
                  </p>
                </div>
              </div>
              <Button variant="secondary" size="sm" onClick={() => navigate('/contributions')}>
                Voir
              </Button>
            </div>
          </div>
        </div>
      )}
    </Card>
  );
};

export default ContributionsWidget;
