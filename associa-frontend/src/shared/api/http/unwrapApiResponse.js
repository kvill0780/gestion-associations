export class ApiResponseError extends Error {
  constructor(message, payload) {
    super(message);
    this.name = 'ApiResponseError';
    this.payload = payload;
  }
}

/**
 * Spring Boot responses follow ApiResponse<T>: { success, message, data }
 */
export const unwrapApiResponse = (responseData) => {
  if (!responseData || typeof responseData !== 'object') {
    return responseData;
  }

  if (Object.prototype.hasOwnProperty.call(responseData, 'success')) {
    if (responseData.success === false) {
      throw new ApiResponseError(responseData.message || 'Erreur API', responseData);
    }
    return responseData.data;
  }

  return responseData;
};
