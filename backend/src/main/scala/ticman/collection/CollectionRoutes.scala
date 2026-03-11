package ticman.collection

import cask.*
import ticman.ErrorResponse
import upickle.default.*
import scala.util.{Try, Success, Failure}
import java.util.UUID

class CollectionRoutes(val collectionService: CollectionService) extends Routes:

  private val jsonHeader = Seq("Content-Type" -> "application/json")

  @get("/api/workspaces/:workspaceId/collections")
  def list(workspaceId: String, request: Request): Response[String] =
    Try(collectionService.list(UUID.fromString(workspaceId))) match
      case Success(cs) => Response(write(cs), 200, headers = jsonHeader)
      case Failure(e)  => Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), 500, headers = jsonHeader)

  @post("/api/workspaces/:workspaceId/collections")
  def create(workspaceId: String, request: Request): Response[String] =
    Try {
      val req = read[CreateCollectionRequest](request.text())
      collectionService.create(UUID.fromString(workspaceId), req)
    } match
      case Success(c) => Response(write(c), 201, headers = jsonHeader)
      case Failure(e) => Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), 500, headers = jsonHeader)

  @delete("/api/collections/:id")
  def deleteCollection(id: String, request: Request): Response[String] =
    Try(collectionService.delete(UUID.fromString(id))) match
      case Success(_) => Response("", 204, headers = jsonHeader)
      case Failure(e) => Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), 500, headers = jsonHeader)

  initialize()
