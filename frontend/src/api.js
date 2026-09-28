import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

export const api = axios.create({
  baseURL: API_BASE_URL,
});

export const getLoanApplications = async (borrowerId) => {
  const params = borrowerId ? { borrowerId } : {};
  const response = await api.get('/loan-applications', { params });
  return response.data;
};

export const submitLoanApplication = async (application) => {
  const response = await api.post('/loan-applications', application);
  return response.data;
};

export const submitCollateral = async (applicationId, payload) => {
  const response = await api.post(`/loan-applications/${applicationId}/collateral`, payload);
  return response.data;
};

export const getDecisionTrail = async (applicationId) => {
  const response = await api.get(`/loan-applications/${applicationId}/decision-trail`);
  return response.data;
};

export const loginUser = async (id, password) => {
  const response = await api.post('/users/login', { id, password });
  return response.data;
};

export const registerUser = async (payload) => {
  const response = await api.post('/users/register', payload);
  return response.data;
};

export const updateProfile = async (id, payload) => {
  const response = await api.put(`/users/${id}`, payload);
  return response.data;
};

export const requestNominee = async (applicationId, nomineeId) => {
  const response = await api.post(`/loan-applications/${applicationId}/nominee`, { nomineeId });
  return response.data;
};

export const getNomineeRequests = async (nomineeId) => {
  const response = await api.get(`/users/${nomineeId}/nominee-requests`);
  return response.data;
};

export const acceptNominee = async (requestId) => {
  const response = await api.post(`/loan-applications/nominee-requests/${requestId}/accept`);
  return response.data;
};
