import { api } from './index'

export interface WorkspaceResponse {
  id: string
  name: string
}

export const workspacesApi = {
  list() {
    return api.get<WorkspaceResponse[]>('/api/workspaces')
  },
  create(name: string) {
    return api.post<WorkspaceResponse>('/api/workspaces', { name })
  },
}
