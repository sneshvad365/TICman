import { api } from './index'

export interface ProxyResponse {
  statusCode: number
  headers: Record<string, string>
  body: string
  durationMs: number
}

export interface HistoryEntry {
  id: string
  statusCode: number
  headers: Record<string, string>
  body: string | null
  durationMs: number
  executedAt: string
}

export const proxyApi = {
  send(payload: {
    method: string
    url: string
    headers: Record<string, string>
    body: string | null
    requestId?: string
  }) {
    return api.post<ProxyResponse>('/api/proxy', payload)
  },

  history(requestId: string) {
    return api.get<HistoryEntry[]>(`/api/requests/${requestId}/history`)
  },

  deleteHistory(id: string) {
    return api.delete<void>(`/api/history/${id}`)
  },
}
