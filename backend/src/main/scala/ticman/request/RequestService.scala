package ticman.request

import java.util.UUID

trait RequestService:
  def list(collectionId: UUID): Seq[RequestResponse]
  def create(collectionId: UUID, req: SaveRequestRequest): RequestResponse
  def update(requestId: UUID, req: SaveRequestRequest): RequestResponse
  def delete(requestId: UUID): Unit

class RequestServiceImpl(requestRepo: RequestRepository) extends RequestService:

  override def list(collectionId: UUID): Seq[RequestResponse] =
    requestRepo.findByCollectionId(collectionId).map(toResponse)

  override def create(collectionId: UUID, req: SaveRequestRequest): RequestResponse =
    toResponse(requestRepo.create(collectionId, req))

  override def update(requestId: UUID, req: SaveRequestRequest): RequestResponse =
    toResponse(requestRepo.update(requestId, req))

  override def delete(requestId: UUID): Unit =
    requestRepo.delete(requestId)

  private def toResponse(r: HttpRequest): RequestResponse =
    RequestResponse(r.id.toString, r.collectionId.toString, r.name, r.method, r.urlTemplate, r.headers, r.body)
