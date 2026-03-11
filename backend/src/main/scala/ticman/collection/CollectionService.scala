package ticman.collection

import ticman.{CollectionId, WorkspaceId, CollectionName}

trait CollectionService:
  def list(workspaceId: WorkspaceId): Seq[CollectionResponse]
  def create(workspaceId: WorkspaceId, req: CreateCollectionRequest): CollectionResponse
  def delete(collectionId: CollectionId): Unit

class CollectionServiceImpl(collectionRepo: CollectionRepository) extends CollectionService:

  override def list(workspaceId: WorkspaceId): Seq[CollectionResponse] =
    collectionRepo.findByWorkspaceId(workspaceId).map(toResponse)

  override def create(workspaceId: WorkspaceId, req: CreateCollectionRequest): CollectionResponse =
    toResponse(collectionRepo.create(workspaceId, CollectionName(req.name.trim), req.readme))

  override def delete(collectionId: CollectionId): Unit =
    collectionRepo.delete(collectionId)

  private def toResponse(c: Collection): CollectionResponse =
    CollectionResponse(c.id, c.workspaceId, c.name, c.readme)
