import request from './request'

export const equipmentApi = {
  list: (params) => request.get('/equipment/list', { params }),
  add: (data) => request.post('/equipment/add', data),
  update: (data) => request.put('/equipment/update', data),
  delete: (id) => request.delete(`/equipment/delete/${id}`)
}
