package ticman.routes

import cask.*
import ticman.services.{RequestService, JwtService}
import ticman.models.{SaveRequestRequest, ErrorResponse, PermissionError}
import upickle.default.*
import java.util.UUID

class RequestRoutes(val requestService: RequestService, val jwtService: JwtService)
    extends Routes with AuthMiddleware:

  private val jsonHeader = Seq("Content-Type" -> "application/json")

  @get("/api/collections/:collectionId/requests")
  def list(collectionId: String, request: Request): Response[String] =
    authenticated(request) { userId =>
      try
        requestService.list(UUID.fromString(collectionId), userId) match
          case Right(reqs) => Response(write(reqs), 200, headers = jsonHeader)
          case Left(err)   => permissionResponse(err)
      catch case e: Exception =>
        Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), 500, headers = jsonHeader)
    }

  @post("/api/collections/:collectionId/requests")
  def create(collectionId: String, request: Request): Response[String] =
    authenticated(request) { userId =>
      try
        val req = read[SaveRequestRequest](request.text())
        requestService.create(UUID.fromString(collectionId), req, userId) match
          case Right(r)  => Response(write(r), 201, headers = jsonHeader)
          case Left(err) => permissionResponse(err)
      catch case e: Exception =>
        Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), 500, headers = jsonHeader)
    }

  @put("/api/requests/:id")
  def update(id: String, request: Request): Response[String] =
    authenticated(request) { userId =>
      try
        val req = read[SaveRequestRequest](request.text())
        requestService.update(UUID.fromString(id), req, userId) match
          case Right(r)  => Response(write(r), 200, headers = jsonHeader)
          case Left(err) => permissionResponse(err)
      catch case e: Exception =>
        Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), 500, headers = jsonHeader)
    }

  @delete("/api/requests/:id")
  def deleteRequest(id: String, request: Request): Response[String] =
    authenticated(request) { userId =>
      try
        requestService.delete(UUID.fromString(id), userId) match
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
