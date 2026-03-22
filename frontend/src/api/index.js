// ===== Auth API =====
import request from './request'

export const authApi = {
  login: (data) => request.post('/auth/login', data),
  logout: () => request.post('/auth/logout'),
  getInfo: () => request.get('/auth/info'),
}

// ===== Room API =====
export const roomApi = {
  list: (params) => request.get('/room/list', { params }),
  add: (data) => request.post('/room/add', data),
  update: (data) => request.put('/room/update', data),
  delete: (id) => request.delete(`/room/delete/${id}`),
  detail: (id) => request.get(`/room/detail/${id}`),
}

// ===== Reservation API =====
export const reservationApi = {
  list: (params) => request.get('/reservation/list', { params }),
  add: (data) => request.post('/reservation/add', data),
  cancel: (id) => request.delete(`/reservation/delete/${id}`),
  detail: (id) => request.get(`/reservation/detail/${id}`),
}

// ===== Equipment API =====
export const equipmentApi = {
  list: (params) => request.get('/equipment/list', { params }),
  add: (data) => request.post('/equipment/add', data),
  update: (data) => request.put('/equipment/update', data),
  delete: (id) => request.delete(`/equipment/delete/${id}`),
}

// ===== Approval API =====
export const approvalApi = {
  list: (params) => request.get('/approval/list', { params }),
  pass: (id, remark) => request.post('/approval/pass', { id, remark }),
  reject: (id, remark) => request.post('/approval/reject', { id, remark }),
}

// ===== Statistics API =====
export const statisticsApi = {
  overview: () => request.get('/statistics/overview'),
  trend: (params) => request.get('/statistics/trend', { params }),
  roomUsage: () => request.get('/statistics/roomUsage'),
  hotRooms: () => request.get('/statistics/hotRooms'),
}
