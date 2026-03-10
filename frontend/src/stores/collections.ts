import { defineStore } from 'pinia'
import { collectionsApi, type CollectionResponse } from 'src/api/collections'

export const useCollectionsStore = defineStore('collections', {
  state: () => ({
    collections: [] as CollectionResponse[],
    currentWorkspaceId: null as string | null,
    loading: false,
  }),

  actions: {
    async fetchForWorkspace(workspaceId: string) {
      this.loading = true
      this.currentWorkspaceId = workspaceId
      try {
        this.collections = await collectionsApi.list(workspaceId)
      } finally {
        this.loading = false
      }
    },

    async create(workspaceId: string, name: string, readme = ''): Promise<CollectionResponse> {
      const col = await collectionsApi.create(workspaceId, name, readme)
      this.collections.push(col)
      return col
    },

    async delete(id: string) {
      await collectionsApi.delete(id)
      this.collections = this.collections.filter(c => c.id !== id)
    },
  },
})
