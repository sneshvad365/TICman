package ticman.workspace

import ticman.{WorkspaceId, WorkspaceName}

trait WorkspaceService:
  def list(): Seq[WorkspaceResponse]
  def create(req: CreateWorkspaceRequest): WorkspaceResponse
  def delete(id: WorkspaceId): Unit

class WorkspaceServiceImpl(workspaceRepo: WorkspaceRepository) extends WorkspaceService:

  override def list(): Seq[WorkspaceResponse] =
    workspaceRepo.findAll().map(ws => WorkspaceResponse(ws.id, ws.name))

  override def create(req: CreateWorkspaceRequest): WorkspaceResponse =
    val ws = workspaceRepo.create(WorkspaceName(req.name.trim))
    WorkspaceResponse(ws.id, ws.name)

  override def delete(id: WorkspaceId): Unit =
    workspaceRepo.delete(id)
