package ticman.routes

import cask.*
import ticman.services.WorkspaceService
import ticman.models.{CreateWorkspaceRequest, ErrorResponse}
import upickle.default.*

class WorkspaceRoutes(val workspaceService: WorkspaceService) extends Routes:

  private val jsonHeader = Seq("Content-Type" -> "application/json")

  @get("/api/workspaces")
  def list(request: Request): Response[String] =
    try Response(write(workspaceService.list()), 200, headers = jsonHeader)
    catch case e: Exception =>
      Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), 500, headers = jsonHeader)

  @post("/api/workspaces")
  def create(request: Request): Response[String] =
    try
      val req       = read[CreateWorkspaceRequest](request.text())
      val workspace = workspaceService.create(req)
      Response(write(workspace), 201, headers = jsonHeader)
    catch case e: Exception =>
      Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), 500, headers = jsonHeader)

  initialize()
