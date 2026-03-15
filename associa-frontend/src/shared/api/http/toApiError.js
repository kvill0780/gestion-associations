export const toApiError = (error) => {
  const status = error?.response?.status;

  const responseData = error?.response?.data;
  const message =
    responseData?.message ||
    responseData?.error?.message ||
    error?.message ||
    'Erreur réseau';

  return {
    status,
    message,
    data: responseData,
    raw: error
  };
};
