import clsx from 'clsx';

export const cn = (...inputs) => {
  return clsx(inputs);
};

export const getInitials = (firstName, lastName) => {
  return `${firstName?.[0] || ''}${lastName?.[0] || ''}`.toUpperCase();
};

export const truncate = (str, length = 50) => {
  if (!str || str.length <= length) return str;
  return `${str.substring(0, length)}...`;
};

export const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

export const getFullName = (user) => {
  const firstName = user?.firstName || user?.first_name || '';
  const lastName = user?.lastName || user?.last_name || '';
  return `${firstName} ${lastName}`.trim();
};
