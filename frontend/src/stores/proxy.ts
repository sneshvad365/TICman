import { defineStore } from 'pinia'
import { proxyApi, type ProxyResponse, type HistoryEntry } from 'src/api/proxy'
import type { RequestResponse } from 'src/api/requests'

export const useProxyStore = defineStore('proxy', {
  state: () => ({
    response: null as ProxyResponse | null,
    history: [] as HistoryEntry[],
    sending: false,
    loadingHistory: false,
    error: null as string | null,
  }),

  actions: {
    async send(request: RequestResponse) {
      this.sending = true
      this.error = null
      this.response = null
      try {
        this.response = await proxyApi.send({
          method: request.method,
          url: request.urlTemplate,
          headers: request.headers,
          body: request.body ?? null,
          requestId: request.id,
        })
        // Refresh history after sending
        await this.fetchHistory(request.id)
      } catch (e) {
        this.error = e instanceof Error ? e.message : 'Request failed'
      } finally {
        this.sending = false
      }
    },

    async fetchHistory(requestId: string) {
      this.loadingHistory = true
      try {
        this.history = await proxyApi.history(requestId)
      } finally {
        this.loadingHistory = false
      }
    },

    async deleteHistory(id: string) {
      await proxyApi.deleteHistory(id)
      this.history = this.history.filter(e => e.id !== id)
    },

    clear() {
      this.response = null
      this.history = []
      this.error = null
    },
  },
})
