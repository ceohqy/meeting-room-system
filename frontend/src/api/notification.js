import request from './request'

export const notificationApi = {
  list: () => request.get('/notification/list'),
  unreadCount: () => request.get('/notification/unread-count'),
  readAll: () => request.post('/notification/read-all'),
  readOne: (id) => request.post(`/notification/read/${id}`)
}
