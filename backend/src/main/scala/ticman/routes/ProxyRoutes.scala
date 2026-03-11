package ticman.routes

import cask.*
import ticman.services.ProxyService
import ticman.models.{ProxyRequest, ErrorResponse}
import upickle.default.*

class ProxyRoutes(val proxyService: ProxyService) extends Routes:

  private val jsonHeader = Seq("Content-Type" -> "application/json")

  @post("/api/proxy")
  def proxy(request: Request): Response[String] =
    try
      val req = read[ProxyRequest](request.text())
      proxyService.send(req) match
        case Right(resp) => Response(write(resp), 200, headers = jsonHeader)
        case Left(err)   => Response(write(ErrorResponse(err, "PROXY_ERROR")), 400, headers = jsonHeader)
    catch case e: Exception =>
      Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), 500, headers = jsonHeader)

  initialize()
