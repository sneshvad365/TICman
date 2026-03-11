package ticman.collection

import java.util.UUID

trait CollectionService:
  def list(workspaceId: UUID): Seq[CollectionResponse]
  def create(workspaceId: UUID, req: CreateCollectionRequest): CollectionResponse
  def delete(collectionId: UUID): Unit

class CollectionServiceImpl(collectionRepo: CollectionRepository) extends CollectionService:

  override def list(workspaceId: UUID): Seq[CollectionResponse] =
    collectionRepo.findByWorkspaceId(workspaceId).map(toResponse)

  override def create(workspaceId: UUID, req: CreateCollectionRequest): CollectionResponse =
    toResponse(collectionRepo.create(workspaceId, req.name.trim, req.readme))

  override def delete(collectionId: UUID): Unit =
    collectionRepo.delete(collectionId)

  private def toResponse(c: Collection): CollectionResponse =
    CollectionResponse(c.id.toString, c.workspaceId.toString, c.name, c.readme)
