import { formatDate, formatDateTime, formatCurrency } from './formatters';

let pdfDepsPromise;
let excelDepsPromise;

const loadPdfDeps = async () => {
  if (!pdfDepsPromise) {
    pdfDepsPromise = Promise.all([import('jspdf'), import('jspdf-autotable')]);
  }

  const [jspdfModule, autoTableModule] = await pdfDepsPromise;
  const jsPDF = jspdfModule.jsPDF || jspdfModule.default;
  const autoTable = autoTableModule.default || autoTableModule.autoTable;

  if (!jsPDF || !autoTable) {
    throw new Error('Impossible de charger les dépendances PDF');
  }

  return { jsPDF, autoTable };
};

const loadExcelDeps = async () => {
  if (!excelDepsPromise) {
    excelDepsPromise = import('xlsx-js-style');
  }

  return excelDepsPromise;
};

export const exportToPDF = async (title, headers, data, filename = 'rapport.pdf') => {
  const { jsPDF, autoTable } = await loadPdfDeps();
  const doc = new jsPDF();

  doc.setFontSize(18);
  doc.text(title, 14, 22);

  doc.setFontSize(11);
  doc.text(`Généré le ${formatDate(new Date())}`, 14, 30);

  autoTable(doc, {
    head: [headers],
    body: data,
    startY: 35,
    styles: { fontSize: 10 },
    headStyles: { fillColor: [59, 130, 246] }
  });

  doc.save(filename);
};

export const exportToExcel = async (title, headers, data, filename = 'rapport.xlsx') => {
  const XLSX = await loadExcelDeps();
  const ws = XLSX.utils.aoa_to_sheet([headers, ...data]);
  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, title);
  XLSX.writeFile(wb, filename);
};

const resolveTransactionDateTime = (transaction) => {
  return (
    transaction.created_at ||
    transaction.createdAt ||
    transaction.transaction_date ||
    transaction.transactionDate
  );
};

export const exportTransactionsToPDF = async (transactions) => {
  const headers = ['Date et heure', 'Type', 'Catégorie', 'Montant', 'Statut'];
  const data = transactions.map(t => [
    formatDateTime(resolveTransactionDateTime(t)),
    t.type === 'income' ? 'Recette' : 'Dépense',
    t.category,
    formatCurrency(t.amount),
    t.status === 'approved' ? 'Approuvé' : 'En attente'
  ]);

  await exportToPDF('Rapport des Transactions', headers, data, 'transactions.pdf');
};

export const exportTransactionsToExcel = async (transactions) => {
  const headers = ['Date et heure', 'Type', 'Catégorie', 'Montant', 'Statut'];
  const data = transactions.map(t => [
    formatDateTime(resolveTransactionDateTime(t)),
    t.type === 'income' ? 'Recette' : 'Dépense',
    t.category,
    formatCurrency(t.amount),
    t.status === 'approved' ? 'Approuvé' : 'En attente'
  ]);

  await exportToExcel('Transactions', headers, data, 'transactions.xlsx');
};

export const exportMembersToPDF = async (members) => {
  const headers = ['Nom', 'Email', 'Téléphone', 'Statut', 'Date d\'adhésion'];
  const data = members.map(m => [
    m.fullName || `${m.firstName || ''} ${m.lastName || ''}`.trim(),
    m.email,
    m.whatsapp || 'N/A',
    m.membershipStatus || '-',
    formatDate(m.membershipDate || m.createdAt)
  ]);

  await exportToPDF('Liste des Membres', headers, data, 'membres.pdf');
};

export const exportMembersToExcel = async (members) => {
  const headers = ['Nom', 'Email', 'Téléphone', 'Statut', 'Date d\'adhésion'];
  const data = members.map(m => [
    m.fullName || `${m.firstName || ''} ${m.lastName || ''}`.trim(),
    m.email,
    m.whatsapp || 'N/A',
    m.membershipStatus || '-',
    formatDate(m.membershipDate || m.createdAt)
  ]);

  await exportToExcel('Membres', headers, data, 'membres.xlsx');
};

const toNumber = (value) => {
  if (value === null || value === undefined || value === '') return 0;
  const parsed = Number(value);
  return Number.isNaN(parsed) ? 0 : parsed;
};

const resolveContributionMemberName = (contribution) => {
  if (!contribution) return '-';
  if (contribution.member_name) return contribution.member_name;
  if (contribution.memberName) return contribution.memberName;
  if (contribution.member?.fullName) return contribution.member.fullName;
  const firstName = contribution.member?.firstName || '';
  const lastName = contribution.member?.lastName || '';
  const fullName = `${firstName} ${lastName}`.trim();
  return fullName || '-';
};

const resolveContributionPeriod = (contribution) => {
  return contribution?.period_label || contribution?.periodLabel || '-';
};

const resolveContributionStatusLabel = (status) => {
  const normalized = String(status || '').toLowerCase();
  const labels = {
    paid: 'Payee',
    partial: 'Partielle',
    late: 'En retard',
    late_partial: 'Retard partiel',
    pending: 'En attente',
    waived: 'Exoneree'
  };
  return labels[normalized] || status || '-';
};

export const exportContributionsToPDF = async (contributions) => {
  const headers = ['Membre', 'Periode', 'Du', 'Paye', 'Reste', 'Statut', 'Echeance'];
  const data = contributions.map((contribution) => {
    const expected = toNumber(contribution.expected_amount ?? contribution.expectedAmount);
    const paid = toNumber(contribution.paid_amount ?? contribution.paidAmount);
    const remaining = toNumber(
      contribution.remaining_amount ?? contribution.remainingAmount ?? Math.max(expected - paid, 0)
    );
    const dueDate = contribution.due_date || contribution.dueDate;

    return [
      resolveContributionMemberName(contribution),
      resolveContributionPeriod(contribution),
      formatCurrency(expected),
      formatCurrency(paid),
      formatCurrency(remaining),
      resolveContributionStatusLabel(contribution.status),
      dueDate ? formatDate(dueDate) : '-'
    ];
  });

  await exportToPDF('Rapport des Cotisations', headers, data, 'cotisations.pdf');
};

export const exportContributionsToExcel = async (contributions) => {
  const headers = ['Membre', 'Periode', 'Du', 'Paye', 'Reste', 'Statut', 'Echeance'];
  const data = contributions.map((contribution) => {
    const expected = toNumber(contribution.expected_amount ?? contribution.expectedAmount);
    const paid = toNumber(contribution.paid_amount ?? contribution.paidAmount);
    const remaining = toNumber(
      contribution.remaining_amount ?? contribution.remainingAmount ?? Math.max(expected - paid, 0)
    );
    const dueDate = contribution.due_date || contribution.dueDate;

    return [
      resolveContributionMemberName(contribution),
      resolveContributionPeriod(contribution),
      formatCurrency(expected),
      formatCurrency(paid),
      formatCurrency(remaining),
      resolveContributionStatusLabel(contribution.status),
      dueDate ? formatDate(dueDate) : '-'
    ];
  });

  await exportToExcel('Cotisations', headers, data, 'cotisations.xlsx');
};
