import { defineStore } from 'pinia'
import { workspacesApi, type WorkspaceResponse } from 'src/api/workspaces'

export const useWorkspacesStore = defineStore('workspaces', {
  state: () => ({
    workspaces: [] as WorkspaceResponse[],
    loading: false,
  }),

  actions: {
    async fetchAll() {
      this.loading = true
      try {
        this.workspaces = await workspacesApi.list()
      } finally {
        this.loading = false
      }
    },

    async create(name: string): Promise<WorkspaceResponse> {
      const workspace = await workspacesApi.create(name)
      this.workspaces.unshift(workspace)
      return workspace
    },

    async delete(id: string) {
      await workspacesApi.delete(id)
      this.workspaces = this.workspaces.filter(w => w.id !== id)
    },
  },
})
