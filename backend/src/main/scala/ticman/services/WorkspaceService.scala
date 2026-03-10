package ticman.services

import ticman.db.WorkspaceRepository
import ticman.models.{CreateWorkspaceRequest, WorkspaceResponse}
import java.util.UUID

trait WorkspaceService:
  def list(userId: UUID): Seq[WorkspaceResponse]
  def create(req: CreateWorkspaceRequest, userId: UUID): WorkspaceResponse

class WorkspaceServiceImpl(workspaceRepo: WorkspaceRepository) extends WorkspaceService:

  override def list(userId: UUID): Seq[WorkspaceResponse] =
    workspaceRepo.findByUserId(userId).map(toResponse)

  override def create(req: CreateWorkspaceRequest, userId: UUID): WorkspaceResponse =
    val ws = workspaceRepo.create(req.name.trim, userId)
    toResponse(ws)

  private def toResponse(ws: ticman.models.WorkspaceWithRole): WorkspaceResponse =
    WorkspaceResponse(ws.id.toString, ws.name, ws.role)
