package ticman.request

import cask.*
import ticman.{ErrorResponse, ErrorMessage, ErrorCode, RequestId, given}
import upickle.default.*
import scala.util.{Try, Success, Failure}
import java.util.UUID

class HistoryRoutes(val historyRepo: ResponseHistoryRepository) extends Routes:

  private val jsonHeader = Seq("Content-Type" -> "application/json")

  @get("/api/requests/:requestId/history")
  def history(requestId: String, request: Request): Response[String] =
    Try(historyRepo.findByRequestId(RequestId(UUID.fromString(requestId)))) match
      case Success(entries) => Response(write(entries), 200, headers = jsonHeader)
      case Failure(e)       => Response(write(ErrorResponse(ErrorMessage(e.getMessage), ErrorCode("INTERNAL_ERROR"))), 500, headers = jsonHeader)

  initialize()
