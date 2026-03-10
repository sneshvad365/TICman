import { api } from './index'

export interface CollectionResponse {
  id: string
  workspaceId: string
  name: string
  readme: string
}

export const collectionsApi = {
  list(workspaceId: string) {
    return api.get<CollectionResponse[]>(`/api/workspaces/${workspaceId}/collections`)
  },
  create(workspaceId: string, name: string, readme = '') {
    return api.post<CollectionResponse>(`/api/workspaces/${workspaceId}/collections`, { name, readme })
  },
  delete(id: string) {
    return api.delete<void>(`/api/collections/${id}`)
  },
}
