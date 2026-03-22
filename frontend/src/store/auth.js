import { defineStore } from 'pinia'
import { authApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || null,
    user: JSON.parse(localStorage.getItem('user') || 'null'),
    loading: false
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    userName: (state) => state.user?.name || state.user?.username || '用户'
  },
  actions: {
    async login(credentials) {
      this.loading = true
      try {
        const res = await authApi.login(credentials)
        const token = res.data?.token || res.token
        const user = res.data?.user || res.user || { username: credentials.username }
        this.token = token
        this.user = user
        localStorage.setItem('token', token)
        localStorage.setItem('user', JSON.stringify(user))
        return true
      } finally {
        this.loading = false
      }
    },
    async logout() {
      try { await authApi.logout() } catch {}
      this.token = null
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
