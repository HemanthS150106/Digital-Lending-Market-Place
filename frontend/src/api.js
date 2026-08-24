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

export const getDecisionTrail = async (applicationId) => {
  const response = await api.get(`/loan-applications/${applicationId}/decision-trail`);
  return response.data;
};
