import request from './request'

export const roomApi = {
  list: (params) => request.get('/room/list', { params }),
  add: (data) => request.post('/room/add', data),
  update: (data) => request.put('/room/update', data),
  delete: (id) => request.delete(`/room/delete/${id}`)
}
