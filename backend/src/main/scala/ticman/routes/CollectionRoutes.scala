package ticman.routes

import cask.*
import ticman.services.CollectionService
import ticman.models.{CreateCollectionRequest, ErrorResponse}
import upickle.default.*
import java.util.UUID

class CollectionRoutes(val collectionService: CollectionService) extends Routes:

  private val jsonHeader = Seq("Content-Type" -> "application/json")

  @get("/api/workspaces/:workspaceId/collections")
  def list(workspaceId: String, request: Request): Response[String] =
    try Response(write(collectionService.list(UUID.fromString(workspaceId))), 200, headers = jsonHeader)
    catch case e: Exception =>
      Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), 500, headers = jsonHeader)

  @post("/api/workspaces/:workspaceId/collections")
  def create(workspaceId: String, request: Request): Response[String] =
    try
      val req = read[CreateCollectionRequest](request.text())
      Response(write(collectionService.create(UUID.fromString(workspaceId), req)), 201, headers = jsonHeader)
    catch case e: Exception =>
      Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), 500, headers = jsonHeader)

  @delete("/api/collections/:id")
  def deleteCollection(id: String, request: Request): Response[String] =
    try
      collectionService.delete(UUID.fromString(id))
      Response("", 204, headers = jsonHeader)
    catch case e: Exception =>
      Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), 500, headers = jsonHeader)

  initialize()
