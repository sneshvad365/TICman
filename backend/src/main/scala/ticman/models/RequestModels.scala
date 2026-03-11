package ticman.models

import upickle.default.*
import java.util.UUID

case class HttpRequest(
    id: UUID,
    collectionId: UUID,
    name: String,
    method: String,
    urlTemplate: String,
    headers: Map[String, String],
    body: Option[String],
)

case class RequestResponse(
    id: String,
    collectionId: String,
    name: String,
    method: String,
    urlTemplate: String,
    headers: Map[String, String],
    body: Option[String],
) derives ReadWriter

case class SaveRequestRequest(
    name: String,
    method: String,
    urlTemplate: String,
    headers: Map[String, String] = Map.empty,
    body: Option[String] = None,
) derives ReadWriter
