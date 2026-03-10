package ticman.routes

import cask.*
import ticman.services.{WorkspaceService, JwtService}
import ticman.models.{CreateWorkspaceRequest, ErrorResponse}
import upickle.default.*

class WorkspaceRoutes(val workspaceService: WorkspaceService, val jwtService: JwtService)
    extends Routes with AuthMiddleware:

  private val jsonHeader = Seq("Content-Type" -> "application/json")

  @get("/api/workspaces")
  def list(request: Request): Response[String] =
    authenticated(request) { userId =>
      try
        val workspaces = workspaceService.list(userId)
        Response(write(workspaces), 200, headers = jsonHeader)
      catch case e: Exception =>
        Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), 500, headers = jsonHeader)
    }

  @post("/api/workspaces")
  def create(request: Request): Response[String] =
    authenticated(request) { userId =>
      try
        val req       = read[CreateWorkspaceRequest](request.text())
        val workspace = workspaceService.create(req, userId)
        Response(write(workspace), 201, headers = jsonHeader)
      catch case e: Exception =>
        Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), 500, headers = jsonHeader)
    }

  initialize()
