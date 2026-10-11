const RAW_BASE_URL = import.meta.env.VITE_API_URL || '';
const API_BASE = RAW_BASE_URL.replace(/\/$/, '');

async function request(endpoint, options = {}) {
  const url = `${API_BASE}${endpoint}`;
  const defaultHeaders = {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  };

  const response = await fetch(url, {
    ...options,
    headers: {
      ...defaultHeaders,
      ...options.headers,
    },
  });

  if (!response.ok) {
    let errorDetail = 'Erro ao processar requisição';
    try {
      const errJson = await response.json();
      errorDetail = errJson.message || errJson.error || JSON.stringify(errJson);
    } catch {
      errorDetail = `Status ${response.status}: ${response.statusText}`;
    }
    const error = new Error(errorDetail);
    error.status = response.status;
    throw error;
  }

  // Handle 204 No Content
  if (response.status === 204) return null;
  return response.json();
}

export const api = {
  // Single Charge Operations
  async createCharge(payload) {
    return request('/api/v1/charges', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },

  async listCharges(page = 0, size = 50) {
    return request(`/api/v1/charges?page=${page}&size=${size}`);
  },

  async getChargeById(id) {
    return request(`/api/v1/charges/${id}`);
  },

  async getChargeTimeline(id) {
    return request(`/api/v1/charges/${id}/timeline`);
  },

  async cancelCharge(id) {
    return request(`/api/v1/charges/${id}/cancel`, {
      method: 'POST',
    });
  },

  async settleCharge(id) {
    return request(`/api/v1/charges/${id}/settle`, {
      method: 'POST',
    });
  },

  // Batch Charge Operations
  async createBatchCharges(payload) {
    return request('/api/v1/charges/batch', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },

  // Webhooks
  async listWebhooks(page = 0, size = 50) {
    return request(`/api/v1/webhooks?page=${page}&size=${size}`);
  },

  async getWebhookById(id) {
    return request(`/api/v1/webhooks/${id}`);
  },

  async replayWebhook(id) {
    return request(`/api/v1/webhooks/${id}/replay`, {
      method: 'POST',
    });
  },

  // Metrics & Latency
  async getMetrics() {
    return request('/api/v1/metrics');
  },

  async pingLatency() {
    const start = performance.now();
    try {
      await request('/api/v1/metrics');
      const duration = Math.round(performance.now() - start);
      return { latency: duration, online: true };
    } catch (err) {
      return { latency: null, online: false, error: err.message };
    }
  },

  // Export CSV Helper
  getExportCsvUrl() {
    return `${API_BASE}/api/v1/charges/export`;
  },
};
