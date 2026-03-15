import apiClient from '../client';
import { API_ENDPOINTS } from '@config/api.config';

const toFrontendTransaction = (tx) => {
  if (!tx) return tx;

  return {
    id: tx.id,
    association_id: tx.associationId ?? tx.association_id,
    user_id: tx.userId ?? tx.user_id,
    contribution_id: tx.contributionId ?? tx.contribution_id,
    recorded_by_id: tx.recordedById ?? tx.recorded_by_id,
    validated_by_id: tx.validatedById ?? tx.validated_by_id,
    type: tx.type ? String(tx.type).toLowerCase() : undefined,
    category: tx.category ? String(tx.category).toLowerCase() : undefined,
    title: tx.title,
    description: tx.description,
    amount: tx.amount,
    transaction_date: tx.transactionDate ?? tx.transaction_date,
    academic_year: tx.academicYear ?? tx.academic_year,
    payment_method: tx.paymentMethod
      ? String(tx.paymentMethod).toLowerCase()
      : tx.payment_method,
    status: tx.status ? String(tx.status).toLowerCase() : undefined,
    notes: tx.notes,
    validated_at: tx.validatedAt ?? tx.validated_at,
    created_at: tx.createdAt ?? tx.created_at,
    updated_at: tx.updatedAt ?? tx.updated_at
  };
};

const mapCategoryToBackend = (category) => {
  const value = String(category || '').toLowerCase();
  const mapping = {
    membership_fee: 'MEMBERSHIP_FEE',
    donation: 'DONATION',
    event_fee: 'EVENT_REVENUE',
    event_revenue: 'EVENT_REVENUE',
    administrative: 'SERVICES',
    office_supplies: 'OFFICE_SUPPLIES',
    equipment: 'EQUIPMENT',
    services: 'SERVICES',
    other: 'OTHER'
  };
  return mapping[value] || 'OTHER';
};

const mapPaymentMethodToBackend = (paymentMethod) => {
  const value = String(paymentMethod || '').toLowerCase();
  const mapping = {
    cash: 'CASH',
    check: 'CHECK',
    bank_transfer: 'TRANSFER',
    transfer: 'TRANSFER',
    mobile_money: 'MOBILE_MONEY'
  };
  return mapping[value] || null;
};

const toBackendTransactionPayload = (transactionData = {}) => ({
  type: String(transactionData.type || 'income').toUpperCase(),
  category: mapCategoryToBackend(transactionData.category),
  title: transactionData.title,
  description: transactionData.description || transactionData.title,
  amount: Number(transactionData.amount),
  transactionDate: transactionData.transactionDate || transactionData.transaction_date,
  paymentMethod: mapPaymentMethodToBackend(transactionData.payment_method),
  academicYear: transactionData.academicYear || transactionData.academic_year,
  notes: transactionData.notes
});

export const transactionsService = {
  getAll: async () => {
    const { data } = await apiClient.get(API_ENDPOINTS.TRANSACTIONS);
    const list = Array.isArray(data) ? data : Array.isArray(data?.data) ? data.data : [];
    return { data: list.map(toFrontendTransaction) };
  },

  create: async (transactionData) => {
    const payload = toBackendTransactionPayload(transactionData);
    const { data } = await apiClient.post(API_ENDPOINTS.TRANSACTIONS, payload);
    return toFrontendTransaction(data);
  },

  approve: async (transactionId) => {
    const { data } = await apiClient.post(API_ENDPOINTS.TRANSACTION_APPROVE(transactionId));
    return toFrontendTransaction(data);
  },

  reject: async (transactionId, reason) => {
    const { data } = await apiClient.post(API_ENDPOINTS.TRANSACTION_REJECT(transactionId), {
      reason
    });
    return toFrontendTransaction(data);
  }
};
