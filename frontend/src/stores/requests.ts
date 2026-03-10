import { defineStore } from 'pinia'
import { requestsApi, type RequestResponse, type SaveRequestPayload } from 'src/api/requests'

export const useRequestsStore = defineStore('requests', {
  state: () => ({
    requests: [] as RequestResponse[],
    currentCollectionId: null as string | null,
    loading: false,
  }),

  actions: {
    async fetchForCollection(collectionId: string) {
      this.loading = true
      this.currentCollectionId = collectionId
      try {
        this.requests = await requestsApi.list(collectionId)
      } finally {
        this.loading = false
      }
    },

    async create(collectionId: string, payload: SaveRequestPayload): Promise<RequestResponse> {
      const req = await requestsApi.create(collectionId, payload)
      this.requests.push(req)
      return req
    },

    async update(id: string, payload: SaveRequestPayload): Promise<RequestResponse> {
      const req = await requestsApi.update(id, payload)
      const idx = this.requests.findIndex(r => r.id === id)
      if (idx !== -1) this.requests[idx] = req
      return req
    },

    async delete(id: string) {
      await requestsApi.delete(id)
      this.requests = this.requests.filter(r => r.id !== id)
    },
  },
})
