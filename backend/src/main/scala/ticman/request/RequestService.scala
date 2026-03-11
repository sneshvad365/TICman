package ticman.request

import ticman.{RequestId, CollectionId}

trait RequestService:
  def list(collectionId: CollectionId): Seq[RequestResponse]
  def create(collectionId: CollectionId, req: SaveRequestRequest): RequestResponse
  def update(requestId: RequestId, req: SaveRequestRequest): RequestResponse
  def delete(requestId: RequestId): Unit

class RequestServiceImpl(requestRepo: RequestRepository) extends RequestService:

  override def list(collectionId: CollectionId): Seq[RequestResponse] =
    requestRepo.findByCollectionId(collectionId).map(toResponse)

  override def create(collectionId: CollectionId, req: SaveRequestRequest): RequestResponse =
    toResponse(requestRepo.create(collectionId, req))

  override def update(requestId: RequestId, req: SaveRequestRequest): RequestResponse =
    toResponse(requestRepo.update(requestId, req))

  override def delete(requestId: RequestId): Unit =
    requestRepo.delete(requestId)

  private def toResponse(r: HttpRequest): RequestResponse =
    RequestResponse(r.id, r.collectionId, r.name, r.method, r.urlTemplate, r.headers, r.body)
