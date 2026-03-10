package ticman.services

import ticman.db.{CollectionRepository, WorkspaceRepository}
import ticman.models.{Collection, CollectionResponse, CreateCollectionRequest, PermissionError}
import java.util.UUID

trait CollectionService:
  def list(workspaceId: UUID, userId: UUID): Either[PermissionError, Seq[CollectionResponse]]
  def create(workspaceId: UUID, req: CreateCollectionRequest, userId: UUID): Either[PermissionError, CollectionResponse]
  def delete(collectionId: UUID, userId: UUID): Either[PermissionError, Unit]

class CollectionServiceImpl(
    collectionRepo: CollectionRepository,
    workspaceRepo: WorkspaceRepository,
) extends CollectionService:

  override def list(workspaceId: UUID, userId: UUID): Either[PermissionError, Seq[CollectionResponse]] =
    workspaceRepo.findMemberRole(workspaceId, userId) match
      case None    => Left(PermissionError.NotMember)
      case Some(_) => Right(collectionRepo.findByWorkspaceId(workspaceId).map(toResponse))

  override def create(workspaceId: UUID, req: CreateCollectionRequest, userId: UUID): Either[PermissionError, CollectionResponse] =
    workspaceRepo.findMemberRole(workspaceId, userId) match
      case None                          => Left(PermissionError.NotMember)
      case Some("viewer")                => Left(PermissionError.Forbidden)
      case Some(_) =>
        Right(toResponse(collectionRepo.create(workspaceId, req.name.trim, req.readme)))

  override def delete(collectionId: UUID, userId: UUID): Either[PermissionError, Unit] =
    collectionRepo.findById(collectionId) match
      case None => Left(PermissionError.NotMember)
      case Some(col) =>
        workspaceRepo.findMemberRole(col.workspaceId, userId) match
          case None           => Left(PermissionError.NotMember)
          case Some("viewer") => Left(PermissionError.Forbidden)
          case Some(_)        => Right(collectionRepo.delete(collectionId))

  private def toResponse(c: Collection): CollectionResponse =
    CollectionResponse(c.id.toString, c.workspaceId.toString, c.name, c.readme)
