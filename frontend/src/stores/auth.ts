import { defineStore } from 'pinia'
import { authApi } from 'src/api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: localStorage.getItem('accessToken') ?? null as string | null,
    refreshToken: localStorage.getItem('refreshToken') ?? null as string | null,
  }),

  getters: {
    isLoggedIn: (state) => !!state.accessToken,
  },

  actions: {
    async login(email: string, password: string) {
      const res = await authApi.login(email, password)
      this._saveTokens(res.accessToken, res.refreshToken)
    },

    async register(email: string, displayName: string, password: string) {
      const res = await authApi.register(email, displayName, password)
      this._saveTokens(res.accessToken, res.refreshToken)
    },

    logout() {
      this.accessToken = null
      this.refreshToken = null
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
    },

    _saveTokens(accessToken: string, refreshToken: string) {
      this.accessToken = accessToken
      this.refreshToken = refreshToken
      localStorage.setItem('accessToken', accessToken)
      localStorage.setItem('refreshToken', refreshToken)
    },
  },
})
