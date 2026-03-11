package ticman.proxy

import cask.*
import ticman.{ErrorResponse, ErrorMessage, ErrorCode, given}
import upickle.default.*
import scala.util.{Try, Success, Failure}

class ProxyRoutes(val proxyService: ProxyService) extends Routes:

  private val jsonHeader = Seq("Content-Type" -> "application/json")

  @post("/api/proxy")
  def proxy(request: Request): Response[String] =
    Try(read[ProxyRequest](request.text())) match
      case Failure(e) => Response(write(ErrorResponse(ErrorMessage(e.getMessage), ErrorCode("INTERNAL_ERROR"))), 500, headers = jsonHeader)
      case Success(req) =>
        proxyService.send(req) match
          case Right(resp) => Response(write(resp), 200, headers = jsonHeader)
          case Left(err)   => Response(write(ErrorResponse(ErrorMessage(err), ErrorCode("PROXY_ERROR"))), 400, headers = jsonHeader)

  initialize()
