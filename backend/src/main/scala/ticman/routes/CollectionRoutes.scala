package ticman.routes

import cask.*
import ticman.services.{CollectionService, JwtService}
import ticman.models.{CreateCollectionRequest, ErrorResponse, PermissionError}
import upickle.default.*
import java.util.UUID

class CollectionRoutes(val collectionService: CollectionService, val jwtService: JwtService)
    extends Routes with AuthMiddleware:

  private val jsonHeader = Seq("Content-Type" -> "application/json")

  @get("/api/workspaces/:workspaceId/collections")
  def list(workspaceId: String, request: Request): Response[String] =
    authenticated(request) { userId =>
      try
        collectionService.list(UUID.fromString(workspaceId), userId) match
          case Right(cols) => Response(write(cols), 200, headers = jsonHeader)
          case Left(err)   => permissionResponse(err)
      catch case e: Exception =>
        Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), 500, headers = jsonHeader)
    }

  @post("/api/workspaces/:workspaceId/collections")
  def create(workspaceId: String, request: Request): Response[String] =
    authenticated(request) { userId =>
      try
        val req = read[CreateCollectionRequest](request.text())
        collectionService.create(UUID.fromString(workspaceId), req, userId) match
          case Right(col) => Response(write(col), 201, headers = jsonHeader)
          case Left(err)  => permissionResponse(err)
      catch case e: Exception =>
        Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), 500, headers = jsonHeader)
    }

  @delete("/api/collections/:id")
  def deleteCollection(id: String, request: Request): Response[String] =
    authenticated(request) { userId =>
      try
        collectionService.delete(UUID.fromString(id), userId) match
          case Right(_)  => Response("", 204, headers = jsonHeader)
          case Left(err) => permissionResponse(err)
      catch case e: Exception =>
        Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), 500, headers = jsonHeader)
    }

  private def permissionResponse(err: PermissionError): Response[String] =
    err match
      case PermissionError.NotMember => Response(write(ErrorResponse("Not a workspace member", "FORBIDDEN")), 403, headers = jsonHeader)
      case PermissionError.Forbidden => Response(write(ErrorResponse("Insufficient permissions", "FORBIDDEN")), 403, headers = jsonHeader)

  initialize()
