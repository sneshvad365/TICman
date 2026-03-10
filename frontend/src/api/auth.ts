import { api } from './index'

export interface AuthResponse {
  accessToken: string
  refreshToken: string
}

export const authApi = {
  register(email: string, displayName: string, password: string) {
    return api.post<AuthResponse>('/api/auth/register', { email, displayName, password })
  },

  login(email: string, password: string) {
    return api.post<AuthResponse>('/api/auth/login', { email, password })
  },
}
