package ticman.services

import ticman.db.WorkspaceRepository
import ticman.models.{CreateWorkspaceRequest, WorkspaceResponse}

trait WorkspaceService:
  def list(): Seq[WorkspaceResponse]
  def create(req: CreateWorkspaceRequest): WorkspaceResponse

class WorkspaceServiceImpl(workspaceRepo: WorkspaceRepository) extends WorkspaceService:

  override def list(): Seq[WorkspaceResponse] =
    workspaceRepo.findAll().map(ws => WorkspaceResponse(ws.id.toString, ws.name))

  override def create(req: CreateWorkspaceRequest): WorkspaceResponse =
    val ws = workspaceRepo.create(req.name.trim)
    WorkspaceResponse(ws.id.toString, ws.name)
