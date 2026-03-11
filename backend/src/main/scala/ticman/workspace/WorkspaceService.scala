package ticman.workspace

import java.util.UUID

trait WorkspaceService:
  def list(): Seq[WorkspaceResponse]
  def create(req: CreateWorkspaceRequest): WorkspaceResponse
  def delete(id: UUID): Unit

class WorkspaceServiceImpl(workspaceRepo: WorkspaceRepository) extends WorkspaceService:

  override def list(): Seq[WorkspaceResponse] =
    workspaceRepo.findAll().map(ws => WorkspaceResponse(ws.id.toString, ws.name))

  override def create(req: CreateWorkspaceRequest): WorkspaceResponse =
    val ws = workspaceRepo.create(req.name.trim)
    WorkspaceResponse(ws.id.toString, ws.name)

  override def delete(id: UUID): Unit =
    workspaceRepo.delete(id)
