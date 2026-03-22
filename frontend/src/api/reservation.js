import request from './request'

export const reservationApi = {
  list: (params) => request.get('/reservation/list', { params }),
  add: (data) => request.post('/reservation/add', data),
  cancel: (id) => request.delete(`/reservation/delete/${id}`)
}