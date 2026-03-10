package ticman.routes

import cask.{Request, Response}
import ticman.services.JwtService
import ticman.models.ErrorResponse
import upickle.default.*
import java.util.UUID

trait AuthMiddleware:
  def jwtService: JwtService

  private val jsonHeader = Seq("Content-Type" -> "application/json")

  def authenticated(request: Request)(handler: UUID => Response[String]): Response[String] =
    request.headers.get("authorization").flatMap(_.headOption) match
      case None =>
        Response(write(ErrorResponse("Missing token", "UNAUTHORIZED")), 401, headers = jsonHeader)
      case Some(header) if !header.startsWith("Bearer ") =>
        Response(write(ErrorResponse("Invalid token format", "UNAUTHORIZED")), 401, headers = jsonHeader)
      case Some(header) =>
        jwtService.validateAccessToken(header.stripPrefix("Bearer ")) match
          case Left(_) =>
            Response(write(ErrorResponse("Invalid or expired token", "UNAUTHORIZED")), 401, headers = jsonHeader)
          case Right(userId) =>
            handler(userId)
