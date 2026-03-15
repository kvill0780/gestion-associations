import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  PlusIcon,
  ArrowUpIcon,
  ArrowDownIcon,
  CheckIcon,
  DocumentArrowDownIcon,
  BanknotesIcon
} from '@heroicons/react/24/outline';
import { Card } from '@components/common/data/Card';
import { PageHeader } from '@components/common/data/PageHeader';
import { SectionHeader } from '@components/common/data/SectionHeader';
import { StatCard } from '@components/common/data/StatCard';
import { Button } from '@components/common/forms/Button';
import { Input } from '@components/common/forms/Input';
import { RequiredLabel } from '@components/common/forms/RequiredLabel';
import { Modal } from '@components/common/feedback/Modal';
import { Spinner } from '@components/common/feedback/Spinner';
import { useTransactions, useCreateTransaction, useApproveTransaction } from '@hooks/useTransactions';
import { usePermissions } from '@hooks/usePermissions';
import { formatCurrency, formatDate } from '@utils/formatters';
import { exportTransactionsToPDF, exportTransactionsToExcel } from '@utils/exportUtils';

const transactionSchema = z.object({
  type: z.enum(['income', 'expense']),
  category: z.string().min(1, 'Catégorie requise'),
  title: z.string().min(3, 'Minimum 3 caractères'),
  amount: z.string().min(1, 'Montant requis'),
  description: z.string().optional(),
  payment_method: z.string().optional(),
  transaction_date: z.string().min(1, 'Date requise')
});

const TransactionsPage = () => {
  const { data: transactions, isLoading } = useTransactions();
  const createTransaction = useCreateTransaction();
  const approveTransaction = useApproveTransaction();
  const { can } = usePermissions();

  const canCreateTransaction = can('finances.create');
  const canApproveTransaction = can('finances.approve');
  const canExportTransactions = can('finances.export');

  const [showModal, setShowModal] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors }
  } = useForm({
    resolver: zodResolver(transactionSchema),
    defaultValues: {
      type: 'income',
      transaction_date: new Date().toISOString().split('T')[0]
    }
  });

  const onSubmit = (data) => {
    if (!canCreateTransaction) return;

    createTransaction.mutate(
      { ...data, amount: parseFloat(data.amount) },
      {
        onSuccess: () => {
          setShowModal(false);
          reset();
        }
      }
    );
  };

  if (isLoading) return <Spinner size="lg" />;

  const transactionsList = transactions?.data || [];
  const pendingTransactions = transactionsList.filter((t) => t.status === 'pending');
  const approvedTransactions = transactionsList.filter((t) => t.status === 'approved');

  const totalIncome = approvedTransactions
    .filter((t) => t.type === 'income')
    .reduce((sum, t) => sum + parseFloat(t.amount), 0);

  const totalExpense = approvedTransactions
    .filter((t) => t.type === 'expense')
    .reduce((sum, t) => sum + parseFloat(t.amount), 0);

  const balance = totalIncome - totalExpense;

  return (
    <div className="space-y-6">
      <PageHeader
        title="Finances"
        subtitle="Gestion des transactions financières"
        actions={
          <>
            {canExportTransactions ? (
              <Button variant="secondary" onClick={() => exportTransactionsToPDF(transactionsList)}>
                <DocumentArrowDownIcon className="mr-2 h-5 w-5" />
                PDF
              </Button>
            ) : null}
            {canExportTransactions ? (
              <Button variant="secondary" onClick={() => exportTransactionsToExcel(transactionsList)}>
                <DocumentArrowDownIcon className="mr-2 h-5 w-5" />
                Excel
              </Button>
            ) : null}
            {canCreateTransaction ? (
              <Button onClick={() => setShowModal(true)}>
                <PlusIcon className="mr-2 h-5 w-5" />
                Nouvelle transaction
              </Button>
            ) : null}
          </>
        }
      />

      <div className="grid grid-cols-1 gap-6 md:grid-cols-3">
        <StatCard title="Recettes" value={formatCurrency(totalIncome)} icon={ArrowUpIcon} color="green" />
        <StatCard title="Dépenses" value={formatCurrency(totalExpense)} icon={ArrowDownIcon} color="red" />
        <StatCard
          title="Solde"
          value={formatCurrency(balance)}
          icon={BanknotesIcon}
          color={balance >= 0 ? 'blue' : 'red'}
        />
      </div>

      {pendingTransactions.length > 0 ? (
        <Card>
          <SectionHeader
            title="Transactions en attente"
            subtitle="Validez les écritures avant publication"
            count={pendingTransactions.length}
            countVariant="warning"
          />
          <div className="space-y-3">
            {pendingTransactions.map((transaction) => (
              <div
                key={transaction.id}
                className="flex items-center justify-between rounded-lg border border-yellow-200 bg-yellow-50 p-4"
              >
                <div className="flex-1">
                  <div className="flex items-center">
                    <div
                      className={`mr-3 rounded-full p-2 ${
                        transaction.type === 'income' ? 'bg-green-100' : 'bg-red-100'
                      }`}
                    >
                      {transaction.type === 'income' ? (
                        <ArrowUpIcon className="h-4 w-4 text-green-600" />
                      ) : (
                        <ArrowDownIcon className="h-4 w-4 text-red-600" />
                      )}
                    </div>
                    <div>
                      <p className="font-medium text-gray-900">{transaction.description || transaction.title}</p>
                      <p className="text-sm text-gray-500">
                        {transaction.category} • {formatDate(transaction.transaction_date)}
                      </p>
                    </div>
                  </div>
                </div>
                <div className="flex items-center space-x-3">
                  <p
                    className={`text-lg font-bold ${
                      transaction.type === 'income' ? 'text-green-600' : 'text-red-600'
                    }`}
                  >
                    {formatCurrency(transaction.amount)}
                  </p>
                  {canApproveTransaction ? (
                    <Button
                      variant="success"
                      onClick={() => approveTransaction.mutate(transaction.id)}
                      className="text-xs"
                    >
                      <CheckIcon className="h-4 w-4" />
                    </Button>
                  ) : null}
                </div>
              </div>
            ))}
          </div>
        </Card>
      ) : null}

      <Card>
        <SectionHeader
          title="Transactions récentes"
          subtitle="Dernières écritures validées"
          count={Math.min(approvedTransactions.length, 10)}
          countVariant="success"
        />
        <div className="space-y-3">
          {approvedTransactions.slice(0, 10).map((transaction) => (
            <div key={transaction.id} className="flex items-center justify-between border-b pb-3 last:border-0">
              <div className="flex items-center">
                <div
                  className={`mr-3 rounded-full p-2 ${
                    transaction.type === 'income' ? 'bg-green-100' : 'bg-red-100'
                  }`}
                >
                  {transaction.type === 'income' ? (
                    <ArrowUpIcon className="h-4 w-4 text-green-600" />
                  ) : (
                    <ArrowDownIcon className="h-4 w-4 text-red-600" />
                  )}
                </div>
                <div>
                  <p className="font-medium text-gray-900">{transaction.description || transaction.title}</p>
                  <p className="text-sm text-gray-500">
                    {transaction.category} • {formatDate(transaction.transaction_date)}
                  </p>
                </div>
              </div>
              <p
                className={`text-lg font-bold ${
                  transaction.type === 'income' ? 'text-green-600' : 'text-red-600'
                }`}
              >
                {transaction.type === 'income' ? '+' : '-'}
                {formatCurrency(transaction.amount)}
              </p>
            </div>
          ))}
        </div>
      </Card>

      {canCreateTransaction ? (
        <Modal isOpen={showModal} onClose={() => setShowModal(false)} title="Nouvelle transaction">
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div>
              <RequiredLabel required>Type</RequiredLabel>
              <select
                className="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500"
                {...register('type')}
                required
              >
                <option value="income">Recette</option>
                <option value="expense">Dépense</option>
              </select>
            </div>

            <div>
              <RequiredLabel required>Catégorie</RequiredLabel>
              <select
                className="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500"
                {...register('category')}
                required
              >
                <option value="">Sélectionner...</option>
                <option value="membership_fee">Cotisation</option>
                <option value="donation">Don</option>
                <option value="event_fee">Frais événement</option>
                <option value="administrative">Frais administratifs</option>
                <option value="equipment">Équipement</option>
                <option value="other">Autre</option>
              </select>
              {errors.category ? <p className="mt-1 text-sm text-red-600">{errors.category.message}</p> : null}
            </div>

            <Input
              label="Montant (FCFA)"
              type="number"
              step="0.01"
              required
              error={errors.amount?.message}
              {...register('amount')}
            />

            <Input label="Titre" required error={errors.title?.message} {...register('title')} />

            <div>
              <RequiredLabel>Description (optionnel)</RequiredLabel>
              <textarea
                className="w-full rounded-md border border-gray-300 px-3 py-2"
                rows={2}
                {...register('description')}
              />
            </div>

            <Input
              label="Date"
              type="date"
              required
              error={errors.transaction_date?.message}
              {...register('transaction_date')}
            />

            <div>
              <RequiredLabel>Méthode de paiement</RequiredLabel>
              <select
                className="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500"
                {...register('payment_method')}
              >
                <option value="">Sélectionner...</option>
                <option value="cash">Espèces</option>
                <option value="mobile_money">Mobile Money</option>
                <option value="bank_transfer">Virement bancaire</option>
                <option value="check">Chèque</option>
              </select>
            </div>

            <div className="flex justify-end space-x-3">
              <Button type="button" variant="secondary" onClick={() => setShowModal(false)}>
                Annuler
              </Button>
              <Button type="submit" disabled={createTransaction.isPending}>
                {createTransaction.isPending ? 'Création...' : 'Créer'}
              </Button>
            </div>
          </form>
        </Modal>
      ) : null}
    </div>
  );
};

export default TransactionsPage;
