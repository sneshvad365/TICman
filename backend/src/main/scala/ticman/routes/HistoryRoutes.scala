package ticman.routes

import cask.*
import ticman.db.ResponseHistoryRepository
import ticman.services.JwtService
import ticman.models.ErrorResponse
import upickle.default.*
import java.util.UUID

class HistoryRoutes(val historyRepo: ResponseHistoryRepository, val jwtService: JwtService)
    extends Routes with AuthMiddleware:

  private val jsonHeader = Seq("Content-Type" -> "application/json")

  @get("/api/requests/:requestId/history")
  def history(requestId: String, request: Request): Response[String] =
    authenticated(request) { _ =>
      try
        val entries = historyRepo.findByRequestId(UUID.fromString(requestId))
        Response(write(entries), 200, headers = jsonHeader)
      catch case e: Exception =>
        Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), 500, headers = jsonHeader)
    }

  initialize()
