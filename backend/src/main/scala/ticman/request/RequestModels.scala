package ticman.request

import scalasql.Table
import ticman.db.TypeMappers.given
import upickle.default.*
import java.util.UUID

// --- ScalaSql table classes + converters ---

case class RequestRow[T[_]](
    id: T[UUID],
    collectionId: T[UUID],
    name: T[String],
    method: T[String],
    urlTemplate: T[String],
    headers: T[Map[String, String]],
    body: T[Option[String]],
)

object RequestRow extends Table[RequestRow]:
  override def tableName: String = "requests"

  def toDomain(row: RequestRow[[T] =>> T]): HttpRequest =
    HttpRequest(row.id, row.collectionId, row.name, row.method, row.urlTemplate, row.headers, row.body)

case class ResponseHistoryRow[T[_]](
    id: T[UUID],
    requestId: T[UUID],
    statusCode: T[Int],
    headers: T[Map[String, String]],
    body: T[Option[String]],
    durationMs: T[Long],
    executedAt: T[String],
)

object ResponseHistoryRow extends Table[ResponseHistoryRow]:
  override def tableName: String = "response_history"

  def toDomain(row: ResponseHistoryRow[[T] =>> T]): HistoryResponse =
    HistoryResponse(row.id.toString, row.statusCode, row.headers, row.body, row.durationMs, row.executedAt)

// --- Domain models ---

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

case class HistoryResponse(
    id: String,
    statusCode: Int,
    headers: Map[String, String],
    body: Option[String],
    durationMs: Long,
    executedAt: String,
) derives ReadWriter
