import apiClient from '../client';
import { API_ENDPOINTS } from '@config/api.config';

const toFrontendPayment = (payment) => {
  if (!payment) return payment;
  return {
    id: payment.id,
    amount: payment.amount,
    transaction_date: payment.transactionDate ?? payment.transaction_date,
    status: payment.status ? String(payment.status).toLowerCase() : undefined,
    payment_method: payment.paymentMethod
      ? String(payment.paymentMethod).toLowerCase()
      : payment.payment_method,
    notes: payment.notes
  };
};

const toFrontendContribution = (contribution) => {
  if (!contribution) return contribution;

  return {
    id: contribution.id,
    association_id: contribution.associationId ?? contribution.association_id,
    member_id: contribution.memberId ?? contribution.member_id,
    member_name: contribution.memberName ?? contribution.member_name,
    year: contribution.year,
    month: contribution.month,
    period_label: contribution.periodLabel ?? contribution.period_label,
    type: contribution.type ? String(contribution.type).toLowerCase() : undefined,
    expected_amount: contribution.expectedAmount ?? contribution.expected_amount,
    paid_amount: contribution.paidAmount ?? contribution.paid_amount,
    remaining_amount: contribution.remainingAmount ?? contribution.remaining_amount,
    status: contribution.status ? String(contribution.status).toLowerCase() : undefined,
    due_date: contribution.dueDate ?? contribution.due_date,
    first_payment_date: contribution.firstPaymentDate ?? contribution.first_payment_date,
    last_payment_date: contribution.lastPaymentDate ?? contribution.last_payment_date,
    waived: contribution.waived,
    waived_reason: contribution.waivedReason ?? contribution.waived_reason,
    notes: contribution.notes,
    payments: Array.isArray(contribution.payments)
      ? contribution.payments.map(toFrontendPayment)
      : [],
    created_at: contribution.createdAt ?? contribution.created_at,
    updated_at: contribution.updatedAt ?? contribution.updated_at
  };
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

const toNumber = (value) => {
  if (value === undefined || value === null || value === '') return undefined;
  return Number(value);
};

const toBackendContributionPayload = (payload = {}) => ({
  memberId: payload.memberId ?? payload.member_id,
  year: payload.year ? Number(payload.year) : undefined,
  month: payload.month ? Number(payload.month) : null,
  type: payload.type ? String(payload.type).toUpperCase() : undefined,
  expectedAmount: toNumber(payload.expectedAmount ?? payload.expected_amount),
  dueDate: payload.dueDate ?? payload.due_date,
  waived: payload.waived ?? false,
  waivedReason: payload.waivedReason ?? payload.waived_reason,
  notes: payload.notes
});

const toBackendGeneratePayload = (payload = {}) => ({
  year: payload.year ? Number(payload.year) : undefined,
  month: payload.month ? Number(payload.month) : null,
  type: payload.type ? String(payload.type).toUpperCase() : undefined,
  expectedAmount: toNumber(payload.expectedAmount ?? payload.expected_amount),
  dueDate: payload.dueDate ?? payload.due_date
});

const toBackendPaymentPayload = (payload = {}) => ({
  amount: payload.amount ? Number(payload.amount) : undefined,
  paymentMethod: mapPaymentMethodToBackend(payload.payment_method || payload.paymentMethod),
  transactionDate: payload.transactionDate ?? payload.transaction_date,
  notes: payload.notes
});

export const contributionsService = {
  getAll: async (filters = {}) => {
    const params = {};
    if (filters.year) params.year = filters.year;
    if (filters.month) params.month = filters.month;

    const { data } = await apiClient.get(API_ENDPOINTS.CONTRIBUTIONS, { params });
    const list = Array.isArray(data) ? data : Array.isArray(data?.data) ? data.data : [];
    return { data: list.map(toFrontendContribution) };
  },

  getStats: async (filters = {}) => {
    const params = {};
    if (filters.year) params.year = filters.year;
    if (filters.month) params.month = filters.month;

    const { data } = await apiClient.get(API_ENDPOINTS.CONTRIBUTION_STATS, { params });
    return data;
  },

  create: async (payload) => {
    const request = toBackendContributionPayload(payload);
    const { data } = await apiClient.post(API_ENDPOINTS.CONTRIBUTIONS, request);
    return toFrontendContribution(data);
  },

  generate: async (payload) => {
    const request = toBackendGeneratePayload(payload);
    const { data } = await apiClient.post(API_ENDPOINTS.CONTRIBUTION_GENERATE, request);
    const list = Array.isArray(data) ? data : Array.isArray(data?.data) ? data.data : [];
    return list.map(toFrontendContribution);
  },

  recordPayment: async ({ contributionId, payload }) => {
    const request = toBackendPaymentPayload(payload);
    const { data } = await apiClient.post(API_ENDPOINTS.CONTRIBUTION_PAYMENTS(contributionId), request);
    return toFrontendContribution(data);
  }
};
