import { api } from './index'

export interface RequestResponse {
  id: string
  collectionId: string
  name: string
  method: string
  urlTemplate: string
  headers: Record<string, string>
  body: string | null
}

export interface SaveRequestPayload {
  name: string
  method: string
  urlTemplate: string
  headers: Record<string, string>
  body: string | null
}

export const requestsApi = {
  list(collectionId: string) {
    return api.get<RequestResponse[]>(`/api/collections/${collectionId}/requests`)
  },
  create(collectionId: string, payload: SaveRequestPayload) {
    return api.post<RequestResponse>(`/api/collections/${collectionId}/requests`, payload)
  },
  update(id: string, payload: SaveRequestPayload) {
    return api.put<RequestResponse>(`/api/requests/${id}`, payload)
  },
  delete(id: string) {
    return api.delete<void>(`/api/requests/${id}`)
  },
}
