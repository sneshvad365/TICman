package ticman.workspace

import cask.*
import ticman.ErrorResponse
import upickle.default.*
import scala.util.{Try, Success, Failure}
import java.util.UUID

class WorkspaceRoutes(val workspaceService: WorkspaceService) extends Routes:

  private val jsonHeader = Seq("Content-Type" -> "application/json")

  @get("/api/workspaces")
  def list(request: Request): Response[String] =
    Try(workspaceService.list()) match
      case Success(ws) => Response(write(ws), 200, headers = jsonHeader)
      case Failure(e)  => Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), 500, headers = jsonHeader)

  @post("/api/workspaces")
  def create(request: Request): Response[String] =
    Try {
      val req = read[CreateWorkspaceRequest](request.text())
      workspaceService.create(req)
    } match
      case Success(workspace) => Response(write(workspace), 201, headers = jsonHeader)
      case Failure(e)         => Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), 500, headers = jsonHeader)

  @delete("/api/workspaces/:id")
  def deleteWorkspace(id: String, request: Request): Response[String] =
    Try(workspaceService.delete(UUID.fromString(id))) match
      case Success(_) => Response("", 204, headers = jsonHeader)
      case Failure(e) => Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), 500, headers = jsonHeader)

  initialize()
