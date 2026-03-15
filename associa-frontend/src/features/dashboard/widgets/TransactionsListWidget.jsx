import { Card } from '@components/common/data/Card';
import { ArrowTrendingUpIcon, ArrowTrendingDownIcon } from '@heroicons/react/24/outline';
import { formatCurrency, formatRelativeTime } from '@utils/formatters';

/**
 * Widget affichant la liste des transactions récentes
 * Visible pour: finances.view, finances_all
 */
const TransactionsListWidget = ({ data }) => {
    const stats = data || {};
    const transactions = stats.finances?.recentTransactions || [];

    return (
        <Card title="Transactions récentes">
            <div className="space-y-3">
                {transactions.length > 0 ? (
                    transactions.slice(0, 5).map((transaction, idx) => (
                        <div key={idx} className="flex items-center justify-between border-b pb-3 last:border-0">
                            <div className="flex items-center space-x-3">
                                {transaction.type === 'INCOME' ? (
                                    <ArrowTrendingUpIcon className="h-5 w-5 text-green-600" />
                                ) : (
                                    <ArrowTrendingDownIcon className="h-5 w-5 text-red-600" />
                                )}
                                <div>
                                    <p className="text-sm font-medium text-gray-900">{transaction.description || 'Transaction'}</p>
                                    <p className="text-xs text-gray-500">{formatRelativeTime(transaction.createdAt || transaction.date)}</p>
                                </div>
                            </div>
                            <span className={`text-sm font-semibold ${transaction.type === 'INCOME' ? 'text-green-600' : 'text-red-600'
                                }`}>
                                {transaction.type === 'INCOME' ? '+' : '-'}{formatCurrency(transaction.amount || 0)}
                            </span>
                        </div>
                    ))
                ) : (
                    <p className="text-sm text-gray-500 text-center py-4">Aucune transaction récente</p>
                )}
            </div>
        </Card>
    );
};

export default TransactionsListWidget;
