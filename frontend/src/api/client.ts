import axios from 'axios';

export const apiClient = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Interceptor to inject tenant and auth tokens
apiClient.interceptors.request.use(config => {
  const tenantId = localStorage.getItem('agentguard_tenant_id') || 'tenant-corp-alpha';
  const token = localStorage.getItem('agentguard_token');
  config.headers['X-Tenant-ID'] = tenantId;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
