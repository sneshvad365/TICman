package ticman.routes

import cask.*
import ticman.services.RequestService
import ticman.models.{SaveRequestRequest, ErrorResponse}
import upickle.default.*
import java.util.UUID

class RequestRoutes(val requestService: RequestService) extends Routes:

  private val jsonHeader = Seq("Content-Type" -> "application/json")

  @get("/api/collections/:collectionId/requests")
  def list(collectionId: String, request: Request): Response[String] =
    try Response(write(requestService.list(UUID.fromString(collectionId))), 200, headers = jsonHeader)
    catch case e: Exception =>
      Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), 500, headers = jsonHeader)

  @post("/api/collections/:collectionId/requests")
  def create(collectionId: String, request: Request): Response[String] =
    try
      val req = read[SaveRequestRequest](request.text())
      Response(write(requestService.create(UUID.fromString(collectionId), req)), 201, headers = jsonHeader)
    catch case e: Exception =>
      Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), 500, headers = jsonHeader)

  @put("/api/requests/:id")
  def update(id: String, request: Request): Response[String] =
    try
      val req = read[SaveRequestRequest](request.text())
      Response(write(requestService.update(UUID.fromString(id), req)), 200, headers = jsonHeader)
    catch case e: Exception =>
      Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), 500, headers = jsonHeader)

  @delete("/api/requests/:id")
  def deleteRequest(id: String, request: Request): Response[String] =
    try
      requestService.delete(UUID.fromString(id))
      Response("", 204, headers = jsonHeader)
    catch case e: Exception =>
      Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), 500, headers = jsonHeader)

  initialize()
