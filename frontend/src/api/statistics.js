import request from './request'

export const statisticsApi = {

  overview() {
    return request.get('/statistics/overview')
  },

  trend() {
    return request.get('/statistics/trend')
  },

  trend30() {
    return request.get('/statistics/trend30')
  },

  roomUsage() {
    return request.get('/statistics/roomUsage')
  },

  roomRank() {
    return request.get('/statistics/roomRank')
  },

  hourDist() {
    return request.get('/statistics/hourDist')
  },

  approvalStatus() {
    return request.get('/statistics/approvalStatus')
  }

}