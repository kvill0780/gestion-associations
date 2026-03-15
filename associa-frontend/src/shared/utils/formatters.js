import { format, parseISO, formatDistanceToNow } from 'date-fns';
import { fr } from 'date-fns/locale';

export const formatDate = (date, formatStr = 'dd/MM/yyyy') => {
  if (!date) return '';
  try {
    const parsedDate = typeof date === 'string' ? parseISO(date) : date;
    return format(parsedDate, formatStr, { locale: fr });
  } catch {
    return '';
  }
};

export const formatDateTime = (date) => {
  if (!date) return '';
  return formatDate(date, 'dd/MM/yyyy à HH:mm');
};

export const formatRelativeTime = (date) => {
  if (!date) return '';
  try {
    const parsedDate = typeof date === 'string' ? parseISO(date) : date;
    return formatDistanceToNow(parsedDate, { addSuffix: true, locale: fr });
  } catch {
    return '';
  }
};

export const formatCurrency = (amount) => {
  if (amount === null || amount === undefined) return '0 FCFA';
  return `${new Intl.NumberFormat('fr-FR').format(amount)} FCFA`;
};

export const formatNumber = (number) => {
  if (number === null || number === undefined) return '';
  return new Intl.NumberFormat('fr-FR').format(number);
};

export const formatPhone = (phone) => {
  if (!phone) return '';
  return phone.replace(/(\d{2})(\d{2})(\d{2})(\d{2})(\d{2})/, '$1 $2 $3 $4 $5');
};
