import { Card } from '@components/common/data/Card';
import { ChartBarIcon } from '@heroicons/react/24/outline';
import { formatCurrency } from '@utils/formatters';

/**
 * Widget affichant le budget et les dépenses
 * Visible pour: finances.approve, finances_all
 */
const BudgetWidget = ({ data }) => {
    const stats = data || {};
    const budget = Number(stats.finances?.budget || 0);
    const spent = Number(stats.finances?.monthlyExpenses || 0);
    const remaining = budget - spent;
    const percentageUsed = budget > 0 ? (spent / budget) * 100 : 0;

    return (
        <Card title="Budget mensuel">
            <div className="space-y-4">
                <div className="flex items-center justify-center">
                    <ChartBarIcon className="h-16 w-16 text-primary-600" />
                </div>

                <div className="space-y-2">
                    <div className="flex justify-between text-sm">
                        <span className="text-gray-600">Budget total</span>
                        <span className="font-semibold text-gray-900">{formatCurrency(budget)}</span>
                    </div>

                    <div className="flex justify-between text-sm">
                        <span className="text-gray-600">Dépensé</span>
                        <span className="font-semibold text-red-600">{formatCurrency(spent)}</span>
                    </div>

                    <div className="w-full bg-gray-200 rounded-full h-2.5">
                        <div
                            className={`h-2.5 rounded-full ${percentageUsed > 90 ? 'bg-red-600' : percentageUsed > 70 ? 'bg-yellow-500' : 'bg-green-600'
                                }`}
                            style={{ width: `${Math.min(percentageUsed, 100)}%` }}
                        ></div>
                    </div>

                    <div className="flex justify-between text-sm pt-2 border-t">
                        <span className="text-gray-600">Restant</span>
                        <span className={`font-bold ${remaining >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                            {formatCurrency(remaining)}
                        </span>
                    </div>

                    {budget <= 0 && (
                        <p className="pt-1 text-xs text-slate-500">
                            Aucun budget mensuel défini pour l'instant.
                        </p>
                    )}
                </div>
            </div>
        </Card>
    );
};

export default BudgetWidget;
