package ticman.services

import ticman.db.{RequestRepository, CollectionRepository, WorkspaceRepository}
import ticman.models.{HttpRequest, RequestResponse, SaveRequestRequest, PermissionError}
import java.util.UUID

trait RequestService:
  def list(collectionId: UUID, userId: UUID): Either[PermissionError, Seq[RequestResponse]]
  def create(collectionId: UUID, req: SaveRequestRequest, userId: UUID): Either[PermissionError, RequestResponse]
  def update(requestId: UUID, req: SaveRequestRequest, userId: UUID): Either[PermissionError, RequestResponse]
  def delete(requestId: UUID, userId: UUID): Either[PermissionError, Unit]

class RequestServiceImpl(
    requestRepo: RequestRepository,
    collectionRepo: CollectionRepository,
    workspaceRepo: WorkspaceRepository,
) extends RequestService:

  override def list(collectionId: UUID, userId: UUID): Either[PermissionError, Seq[RequestResponse]] =
    withMembership(collectionId, userId, requireEditor = false) { _ =>
      requestRepo.findByCollectionId(collectionId).map(toResponse)
    }

  override def create(collectionId: UUID, req: SaveRequestRequest, userId: UUID): Either[PermissionError, RequestResponse] =
    withMembership(collectionId, userId, requireEditor = true) { _ =>
      toResponse(requestRepo.create(collectionId, userId, req))
    }

  override def update(requestId: UUID, req: SaveRequestRequest, userId: UUID): Either[PermissionError, RequestResponse] =
    requestRepo.findById(requestId) match
      case None    => Left(PermissionError.NotMember)
      case Some(r) =>
        withMembership(r.collectionId, userId, requireEditor = true) { _ =>
          toResponse(requestRepo.update(requestId, req))
        }

  override def delete(requestId: UUID, userId: UUID): Either[PermissionError, Unit] =
    requestRepo.findById(requestId) match
      case None    => Left(PermissionError.NotMember)
      case Some(r) =>
        withMembership(r.collectionId, userId, requireEditor = true) { _ =>
          requestRepo.delete(requestId)
        }

  private def withMembership[A](collectionId: UUID, userId: UUID, requireEditor: Boolean)(
      f: String => A
  ): Either[PermissionError, A] =
    collectionRepo.findById(collectionId) match
      case None => Left(PermissionError.NotMember)
      case Some(col) =>
        workspaceRepo.findMemberRole(col.workspaceId, userId) match
          case None                              => Left(PermissionError.NotMember)
          case Some("viewer") if requireEditor   => Left(PermissionError.Forbidden)
          case Some(role)                        => Right(f(role))

  private def toResponse(r: HttpRequest): RequestResponse =
    RequestResponse(r.id.toString, r.collectionId.toString, r.name, r.method, r.urlTemplate, r.headers, r.body)
