package ticman.collection

import cask.*
import ticman.{ErrorResponse, ErrorMessage, ErrorCode, CollectionId, WorkspaceId, given}
import upickle.default.*
import scala.util.{Try, Success, Failure}
import java.util.UUID

class CollectionRoutes(val collectionService: CollectionService) extends Routes:

  private val jsonHeader = Seq("Content-Type" -> "application/json")

  @get("/api/workspaces/:workspaceId/collections")
  def list(workspaceId: String, request: Request): Response[String] =
    Try(collectionService.list(WorkspaceId(UUID.fromString(workspaceId)))) match
      case Success(cs) => Response(write(cs), 200, headers = jsonHeader)
      case Failure(e)  => Response(write(ErrorResponse(ErrorMessage(e.getMessage), ErrorCode("INTERNAL_ERROR"))), 500, headers = jsonHeader)

  @post("/api/workspaces/:workspaceId/collections")
  def create(workspaceId: String, request: Request): Response[String] =
    Try {
      val req = read[CreateCollectionRequest](request.text())
      collectionService.create(WorkspaceId(UUID.fromString(workspaceId)), req)
    } match
      case Success(c) => Response(write(c), 201, headers = jsonHeader)
      case Failure(e) => Response(write(ErrorResponse(ErrorMessage(e.getMessage), ErrorCode("INTERNAL_ERROR"))), 500, headers = jsonHeader)

  @delete("/api/collections/:id")
  def deleteCollection(id: String, request: Request): Response[String] =
    Try(collectionService.delete(CollectionId(UUID.fromString(id)))) match
      case Success(_) => Response("", 204, headers = jsonHeader)
      case Failure(e) => Response(write(ErrorResponse(ErrorMessage(e.getMessage), ErrorCode("INTERNAL_ERROR"))), 500, headers = jsonHeader)

  initialize()
