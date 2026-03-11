package ticman.request

import cask.*
import ticman.ErrorResponse
import upickle.default.*
import scala.util.{Try, Success, Failure}
import java.util.UUID

class RequestRoutes(val requestService: RequestService) extends Routes:

  private val jsonHeader = Seq("Content-Type" -> "application/json")

  @get("/api/collections/:collectionId/requests")
  def list(collectionId: String, request: Request): Response[String] =
    Try(requestService.list(UUID.fromString(collectionId))) match
      case Success(rs) => Response(write(rs), 200, headers = jsonHeader)
      case Failure(e)  => Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), 500, headers = jsonHeader)

  @post("/api/collections/:collectionId/requests")
  def create(collectionId: String, request: Request): Response[String] =
    Try {
      val req = read[SaveRequestRequest](request.text())
      requestService.create(UUID.fromString(collectionId), req)
    } match
      case Success(r) => Response(write(r), 201, headers = jsonHeader)
      case Failure(e) => Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), 500, headers = jsonHeader)

  @put("/api/requests/:id")
  def update(id: String, request: Request): Response[String] =
    Try {
      val req = read[SaveRequestRequest](request.text())
      requestService.update(UUID.fromString(id), req)
    } match
      case Success(r) => Response(write(r), 200, headers = jsonHeader)
      case Failure(e) => Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), 500, headers = jsonHeader)

  @delete("/api/requests/:id")
  def deleteRequest(id: String, request: Request): Response[String] =
    Try(requestService.delete(UUID.fromString(id))) match
      case Success(_) => Response("", 204, headers = jsonHeader)
      case Failure(e) => Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), 500, headers = jsonHeader)

  initialize()
