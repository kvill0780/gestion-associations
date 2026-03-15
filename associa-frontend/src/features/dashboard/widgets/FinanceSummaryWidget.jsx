import { useState } from 'react';
import { Card } from '@components/common/data/Card';
import { Button } from '@components/common/forms/Button';
import { FinanceChart } from '../components/FinanceChart';
import { formatCurrency } from '@utils/formatters';

/**
 * Widget affichant le résumé financier (graphique + résumé mensuel)
 * Visible pour: finances.view, finances_all
 */
const FinanceSummaryWidget = ({ data }) => {
    const [chartType, setChartType] = useState('line');
    const stats = data || {};
    const finances = stats.finances || {};
    const historySeries = finances.history || finances.monthlySeries || finances.series || [];

    return (
        <Card
            title="Évolution financière"
            actions={
                <div className="flex gap-2">
                    <Button
                        variant={chartType === 'line' ? 'primary' : 'secondary'}
                        size="sm"
                        onClick={() => setChartType('line')}
                    >
                        Ligne
                    </Button>
                    <Button
                        variant={chartType === 'bar' ? 'primary' : 'secondary'}
                        size="sm"
                        onClick={() => setChartType('bar')}
                    >
                        Barres
                    </Button>
                </div>
            }
        >
            <div className="mb-4 grid grid-cols-1 gap-3 md:grid-cols-3">
                <div className="rounded-xl border border-emerald-100 bg-emerald-50 px-3 py-2">
                    <p className="text-xs font-semibold uppercase tracking-wide text-emerald-700">Recettes</p>
                    <p className="mt-1 text-lg font-bold text-emerald-700">
                        {formatCurrency(finances.monthlyIncome || 0)}
                    </p>
                </div>
                <div className="rounded-xl border border-red-100 bg-red-50 px-3 py-2">
                    <p className="text-xs font-semibold uppercase tracking-wide text-red-700">Dépenses</p>
                    <p className="mt-1 text-lg font-bold text-red-700">
                        {formatCurrency(finances.monthlyExpenses || 0)}
                    </p>
                </div>
                <div className="rounded-xl border border-slate-200 bg-slate-50 px-3 py-2">
                    <p className="text-xs font-semibold uppercase tracking-wide text-slate-600">Solde net</p>
                    <p className="mt-1 text-lg font-bold text-slate-900">
                        {formatCurrency(finances.netMonthly || 0)}
                    </p>
                </div>
            </div>
            <div className="-mx-2">
                <FinanceChart data={historySeries} type={chartType} />
            </div>
        </Card>
    );
};

export default FinanceSummaryWidget;
