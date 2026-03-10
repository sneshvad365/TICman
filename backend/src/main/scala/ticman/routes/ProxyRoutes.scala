package ticman.routes

import cask.*
import ticman.services.{ProxyService, JwtService}
import ticman.models.{ProxyRequest, ErrorResponse}
import upickle.default.*

class ProxyRoutes(val proxyService: ProxyService, val jwtService: JwtService)
    extends Routes with AuthMiddleware:

  private val jsonHeader = Seq("Content-Type" -> "application/json")

  @post("/api/proxy")
  def proxy(request: Request): Response[String] =
    authenticated(request) { userId =>
      try
        val req = read[ProxyRequest](request.text())
        proxyService.send(req, userId) match
          case Right(resp) => Response(write(resp), 200, headers = jsonHeader)
          case Left(err)   => Response(write(ErrorResponse(err, "PROXY_ERROR")), 400, headers = jsonHeader)
      catch case e: Exception =>
        Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), 500, headers = jsonHeader)
    }

  initialize()
