import { useEffect, useMemo, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  BanknotesIcon,
  PlusIcon,
  CalendarDaysIcon,
  ArrowUpTrayIcon,
  DocumentArrowDownIcon,
  CheckCircleIcon
} from '@heroicons/react/24/outline';
import { PageHeader } from '@components/common/data/PageHeader';
import { SectionHeader } from '@components/common/data/SectionHeader';
import { Card } from '@components/common/data/Card';
import { StatCard } from '@components/common/data/StatCard';
import { Badge } from '@components/common/data/Badge';
import { Button } from '@components/common/forms/Button';
import { Input } from '@components/common/forms/Input';
import { RequiredLabel } from '@components/common/forms/RequiredLabel';
import { Modal } from '@components/common/feedback/Modal';
import { Spinner } from '@components/common/feedback/Spinner';
import {
  useContributions,
  useContributionStats,
  useCreateContribution,
  useGenerateContributions,
  useRecordContributionPayment
} from '@hooks/useContributions';
import { useMembers } from '@hooks/useMembers';
import { usePermissions } from '@hooks/usePermissions';
import { formatCurrency, formatDate } from '@utils/formatters';
import { exportContributionsToPDF, exportContributionsToExcel } from '@utils/exportUtils';

const monthOptions = [
  { value: '', label: 'Tous les mois' },
  { value: '1', label: 'Janvier' },
  { value: '2', label: 'Fevrier' },
  { value: '3', label: 'Mars' },
  { value: '4', label: 'Avril' },
  { value: '5', label: 'Mai' },
  { value: '6', label: 'Juin' },
  { value: '7', label: 'Juillet' },
  { value: '8', label: 'Aout' },
  { value: '9', label: 'Septembre' },
  { value: '10', label: 'Octobre' },
  { value: '11', label: 'Novembre' },
  { value: '12', label: 'Decembre' }
];

const createSchema = z.object({
  member_id: z.string().min(1, 'Membre requis'),
  year: z.string().min(4, 'Annee requise'),
  month: z.string().optional(),
  expected_amount: z.string().min(1, 'Montant requis'),
  due_date: z.string().optional()
});

const generateSchema = z.object({
  year: z.string().min(4, 'Annee requise'),
  month: z.string().optional(),
  expected_amount: z.string().optional(),
  due_date: z.string().optional()
});

const paymentSchema = z.object({
  amount: z.string().min(1, 'Montant requis'),
  payment_method: z.string().optional(),
  transaction_date: z.string().optional(),
  notes: z.string().optional()
});

const getStatusBadge = (status) => {
  const normalized = String(status || '').toLowerCase();
  const variants = {
    paid: 'success',
    partial: 'warning',
    late: 'danger',
    late_partial: 'danger',
    pending: 'default',
    waived: 'info'
  };
  const labels = {
    paid: 'Payee',
    partial: 'Partielle',
    late: 'En retard',
    late_partial: 'Retard partiel',
    pending: 'En attente',
    waived: 'Exoneree'
  };
  return <Badge variant={variants[normalized] || 'default'}>{labels[normalized] || status}</Badge>;
};

const ContributionsPage = () => {
  const today = new Date();
  const defaultYear = String(today.getFullYear());
  const defaultMonth = String(today.getMonth() + 1);

  const [filters, setFilters] = useState({ year: defaultYear, month: defaultMonth });
  const [showCreate, setShowCreate] = useState(false);
  const [showGenerate, setShowGenerate] = useState(false);
  const [showPayment, setShowPayment] = useState(false);
  const [selectedContribution, setSelectedContribution] = useState(null);

  const { can } = usePermissions();
  const canCreate = can('finances.create');
  const canExport = can('finances.export');

  const queryFilters = useMemo(() => ({
    year: filters.year,
    month: filters.month ? Number(filters.month) : undefined
  }), [filters]);

  const { data: contributionsData, isLoading } = useContributions(queryFilters);
  const { data: statsData } = useContributionStats(queryFilters);
  const { data: members } = useMembers();

  const createContribution = useCreateContribution();
  const generateContributions = useGenerateContributions();
  const recordPayment = useRecordContributionPayment();

  const {
    register: registerCreate,
    handleSubmit: handleSubmitCreate,
    reset: resetCreate,
    formState: { errors: createErrors }
  } = useForm({
    resolver: zodResolver(createSchema),
    defaultValues: {
      year: defaultYear,
      month: defaultMonth
    }
  });

  const {
    register: registerGenerate,
    handleSubmit: handleSubmitGenerate,
    reset: resetGenerate,
    formState: { errors: generateErrors }
  } = useForm({
    resolver: zodResolver(generateSchema),
    defaultValues: {
      year: defaultYear,
      month: defaultMonth
    }
  });

  const {
    register: registerPayment,
    handleSubmit: handleSubmitPayment,
    reset: resetPayment,
    setValue: setPaymentValue,
    formState: { errors: paymentErrors }
  } = useForm({
    resolver: zodResolver(paymentSchema)
  });

  useEffect(() => {
    if (!selectedContribution) return;
    const remaining = selectedContribution.remaining_amount || 0;
    setPaymentValue('amount', remaining ? String(remaining) : '');
    setPaymentValue('transaction_date', new Date().toISOString().split('T')[0]);
  }, [selectedContribution, setPaymentValue]);

  const contributions = contributionsData?.data || [];
  const stats = statsData || {};

  const handleCreate = (data) => {
    if (!canCreate) return;
    createContribution.mutate(
      {
        member_id: data.member_id,
        year: data.year,
        month: data.month || null,
        expected_amount: data.expected_amount,
        due_date: data.due_date || null,
        type: data.month ? 'monthly' : 'annual'
      },
      {
        onSuccess: () => {
          setShowCreate(false);
          resetCreate();
        }
      }
    );
  };

  const handleGenerate = (data) => {
    if (!canCreate) return;
    generateContributions.mutate(
      {
        year: data.year,
        month: data.month || null,
        expected_amount: data.expected_amount || undefined,
        due_date: data.due_date || undefined,
        type: data.month ? 'monthly' : 'annual'
      },
      {
        onSuccess: () => {
          setShowGenerate(false);
          resetGenerate();
        }
      }
    );
  };

  const handlePayment = (data) => {
    if (!selectedContribution) return;
    recordPayment.mutate(
      {
        contributionId: selectedContribution.id,
        payload: {
          amount: data.amount,
          payment_method: data.payment_method,
          transaction_date: data.transaction_date,
          notes: data.notes
        }
      },
      {
        onSuccess: () => {
          setShowPayment(false);
          setSelectedContribution(null);
          resetPayment();
        }
      }
    );
  };

  if (isLoading) return <Spinner size="lg" />;

  const membersList = members || [];

  return (
    <div className="space-y-6">
      <PageHeader
        title="Cotisations"
        subtitle="Suivi des obligations de paiement"
        actions={
          <>
            {canExport ? (
              <Button variant="secondary" onClick={() => exportContributionsToPDF(contributions)}>
                <DocumentArrowDownIcon className="mr-2 h-5 w-5" />
                PDF
              </Button>
            ) : null}
            {canExport ? (
              <Button variant="secondary" onClick={() => exportContributionsToExcel(contributions)}>
                <DocumentArrowDownIcon className="mr-2 h-5 w-5" />
                Excel
              </Button>
            ) : null}
            {canCreate ? (
              <Button variant="secondary" onClick={() => setShowGenerate(true)}>
                <ArrowUpTrayIcon className="mr-2 h-5 w-5" />
                Generer
              </Button>
            ) : null}
            {canCreate ? (
              <Button onClick={() => setShowCreate(true)}>
                <PlusIcon className="mr-2 h-5 w-5" />
                Nouvelle cotisation
              </Button>
            ) : null}
          </>
        }
      />

      <Card>
        <SectionHeader title="Periode" subtitle="Filtrez les cotisations par mois" />
        <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
          <div>
            <RequiredLabel required>Annee</RequiredLabel>
            <Input
              type="number"
              value={filters.year}
              onChange={(e) => setFilters((prev) => ({ ...prev, year: e.target.value }))}
            />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Mois</label>
            <select
              className="w-full rounded-xl border border-gray-300 bg-white px-3 py-2 text-sm"
              value={filters.month}
              onChange={(e) => setFilters((prev) => ({ ...prev, month: e.target.value }))}
            >
              {monthOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
        </div>
      </Card>

      <div className="grid grid-cols-1 gap-6 md:grid-cols-4">
        <StatCard
          title="A jour"
          value={stats.upToDate ?? 0}
          icon={CheckCircleIcon}
          color="green"
        />
        <StatCard
          title="En retard"
          value={stats.late ?? 0}
          icon={CalendarDaysIcon}
          color="red"
        />
        <StatCard
          title="Partielles"
          value={stats.partial ?? 0}
          icon={BanknotesIcon}
          color="yellow"
        />
        <StatCard
          title="Collecte"
          value={formatCurrency(stats.totalCollected || 0)}
          icon={BanknotesIcon}
          color="blue"
        />
      </div>

      <Card>
        <SectionHeader
          title="Cotisations"
          subtitle="Details par membre"
          count={contributions.length}
          countVariant="info"
        />
        <div className="overflow-x-auto rounded-xl border border-slate-200">
          <table className="min-w-full divide-y divide-slate-200">
            <thead className="bg-slate-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">
                  Membre
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">
                  Periode
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">
                  Du
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">
                  Paye
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">
                  Statut
                </th>
                <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-500">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-200 bg-white">
              {contributions.map((contribution) => (
                <tr key={contribution.id}>
                  <td className="px-6 py-4 text-sm font-medium text-slate-900">
                    {contribution.member_name || '-'}
                  </td>
                  <td className="px-6 py-4 text-sm text-slate-600">
                    {contribution.period_label || '-'}
                  </td>
                  <td className="px-6 py-4 text-sm text-slate-600">
                    {formatCurrency(contribution.expected_amount || 0)}
                  </td>
                  <td className="px-6 py-4 text-sm text-slate-600">
                    <div>{formatCurrency(contribution.paid_amount || 0)}</div>
                    <div className="text-xs text-slate-400">
                      Reste {formatCurrency(contribution.remaining_amount || 0)}
                    </div>
                  </td>
                  <td className="px-6 py-4 text-sm text-slate-600">
                    {getStatusBadge(contribution.status)}
                    <div className="mt-1 text-xs text-slate-400">
                      Echeance {contribution.due_date ? formatDate(contribution.due_date) : '-'}
                    </div>
                  </td>
                  <td className="px-6 py-4 text-sm text-slate-600">
                    {canCreate && (contribution.remaining_amount || 0) > 0 ? (
                      <Button
                        size="sm"
                        variant="secondary"
                        onClick={() => {
                          setSelectedContribution(contribution);
                          setShowPayment(true);
                        }}
                      >
                        Encaisser
                      </Button>
                    ) : (
                      <span className="text-xs text-slate-400">-</span>
                    )}
                  </td>
                </tr>
              ))}
              {contributions.length === 0 ? (
                <tr>
                  <td className="px-6 py-8 text-center text-sm text-slate-500" colSpan={6}>
                    Aucune cotisation pour cette periode
                  </td>
                </tr>
              ) : null}
            </tbody>
          </table>
        </div>
      </Card>

      <Modal
        isOpen={showCreate}
        onClose={() => {
          setShowCreate(false);
          resetCreate();
        }}
        title="Nouvelle cotisation"
      >
        <form onSubmit={handleSubmitCreate(handleCreate)} className="space-y-4">
          <div>
            <RequiredLabel required>Membre</RequiredLabel>
            <select
              className="w-full rounded-xl border border-gray-300 bg-white px-3 py-2 text-sm"
              {...registerCreate('member_id')}
            >
              <option value="">Selectionner un membre</option>
              {membersList.map((member) => (
                <option key={member.id} value={member.id}>
                  {member.firstName} {member.lastName}
                </option>
              ))}
            </select>
            {createErrors.member_id && (
              <p className="mt-1 text-xs text-red-600">{createErrors.member_id.message}</p>
            )}
          </div>
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <div>
              <RequiredLabel required>Annee</RequiredLabel>
              <Input type="number" {...registerCreate('year')} error={createErrors.year?.message} />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">Mois</label>
              <select
                className="w-full rounded-xl border border-gray-300 bg-white px-3 py-2 text-sm"
                {...registerCreate('month')}
              >
                {monthOptions.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </div>
          </div>
          <div>
            <RequiredLabel required>Montant attendu</RequiredLabel>
            <Input type="number" step="0.01" {...registerCreate('expected_amount')} error={createErrors.expected_amount?.message} />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Echeance</label>
            <Input type="date" {...registerCreate('due_date')} />
          </div>
          <div className="flex justify-end gap-3">
            <Button type="button" variant="secondary" onClick={() => setShowCreate(false)}>
              Annuler
            </Button>
            <Button type="submit" disabled={createContribution.isPending}>
              {createContribution.isPending ? 'Creation...' : 'Creer'}
            </Button>
          </div>
        </form>
      </Modal>

      <Modal
        isOpen={showGenerate}
        onClose={() => {
          setShowGenerate(false);
          resetGenerate();
        }}
        title="Generer les cotisations"
      >
        <form onSubmit={handleSubmitGenerate(handleGenerate)} className="space-y-4">
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            <div>
              <RequiredLabel required>Annee</RequiredLabel>
              <Input type="number" {...registerGenerate('year')} error={generateErrors.year?.message} />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">Mois</label>
              <select
                className="w-full rounded-xl border border-gray-300 bg-white px-3 py-2 text-sm"
                {...registerGenerate('month')}
              >
                {monthOptions.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </div>
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Montant attendu</label>
            <Input type="number" step="0.01" {...registerGenerate('expected_amount')} />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Echeance</label>
            <Input type="date" {...registerGenerate('due_date')} />
          </div>
          <div className="flex justify-end gap-3">
            <Button type="button" variant="secondary" onClick={() => setShowGenerate(false)}>
              Annuler
            </Button>
            <Button type="submit" disabled={generateContributions.isPending}>
              {generateContributions.isPending ? 'Generation...' : 'Generer'}
            </Button>
          </div>
        </form>
      </Modal>

      <Modal
        isOpen={showPayment}
        onClose={() => {
          setShowPayment(false);
          setSelectedContribution(null);
          resetPayment();
        }}
        title="Enregistrer un paiement"
      >
        <form onSubmit={handleSubmitPayment(handlePayment)} className="space-y-4">
          <div className="rounded-lg border border-slate-200 bg-slate-50 p-3 text-sm text-slate-600">
            <div className="font-semibold text-slate-900">{selectedContribution?.member_name}</div>
            <div>{selectedContribution?.period_label}</div>
            <div>Reste: {formatCurrency(selectedContribution?.remaining_amount || 0)}</div>
          </div>
          <div>
            <RequiredLabel required>Montant</RequiredLabel>
            <Input type="number" step="0.01" {...registerPayment('amount')} error={paymentErrors.amount?.message} />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Moyen de paiement</label>
            <select
              className="w-full rounded-xl border border-gray-300 bg-white px-3 py-2 text-sm"
              {...registerPayment('payment_method')}
            >
              <option value="">Selectionner</option>
              <option value="cash">Especes</option>
              <option value="mobile_money">Mobile Money</option>
              <option value="transfer">Virement</option>
              <option value="check">Cheque</option>
            </select>
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Date</label>
            <Input type="date" {...registerPayment('transaction_date')} />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Notes</label>
            <Input {...registerPayment('notes')} />
          </div>
          <div className="flex justify-end gap-3">
            <Button
              type="button"
              variant="secondary"
              onClick={() => {
                setShowPayment(false);
                setSelectedContribution(null);
                resetPayment();
              }}
            >
              Annuler
            </Button>
            <Button type="submit" disabled={recordPayment.isPending}>
              {recordPayment.isPending ? 'Enregistrement...' : 'Enregistrer'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default ContributionsPage;
