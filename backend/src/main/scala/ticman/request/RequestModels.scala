package ticman.request

import scalasql.Table
import ticman.db.TypeMappers.given
import ticman.{RequestId, CollectionId, HistoryId, RequestName, HttpMethod, UrlTemplate, RequestBody, ResponseBody, StatusCode, DurationMs, ExecutedAt, given}
import upickle.default.*
import java.util.UUID
import java.time.Instant

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
    HttpRequest(
      RequestId(row.id),
      CollectionId(row.collectionId),
      RequestName(row.name),
      HttpMethod(row.method),
      UrlTemplate(row.urlTemplate),
      row.headers,
      row.body.map(RequestBody(_)),
    )

case class ResponseHistoryRow[T[_]](
    id: T[UUID],
    requestId: T[UUID],
    statusCode: T[Int],
    headers: T[Map[String, String]],
    body: T[Option[String]],
    durationMs: T[Long],
    executedAt: T[Instant],
)

object ResponseHistoryRow extends Table[ResponseHistoryRow]:
  override def tableName: String = "response_history"

  def toDomain(row: ResponseHistoryRow[[T] =>> T]): HistoryResponse =
    HistoryResponse(
      HistoryId(row.id),
      StatusCode(row.statusCode),
      row.headers,
      row.body.map(ResponseBody(_)),
      DurationMs(row.durationMs),
      ExecutedAt(row.executedAt.toString),
    )

// --- Domain models ---

case class HttpRequest(
    id: RequestId,
    collectionId: CollectionId,
    name: RequestName,
    method: HttpMethod,
    urlTemplate: UrlTemplate,
    headers: Map[String, String],
    body: Option[RequestBody],
)

case class RequestResponse(
    id: RequestId,
    collectionId: CollectionId,
    name: RequestName,
    method: HttpMethod,
    urlTemplate: UrlTemplate,
    headers: Map[String, String],
    body: Option[RequestBody],
) derives ReadWriter

case class SaveRequestRequest(
    name: RequestName,
    method: HttpMethod,
    urlTemplate: UrlTemplate,
    headers: Map[String, String] = Map.empty,
    body: Option[RequestBody] = None,
) derives ReadWriter

case class HistoryResponse(
    id: HistoryId,
    statusCode: StatusCode,
    headers: Map[String, String],
    body: Option[ResponseBody],
    durationMs: DurationMs,
    executedAt: ExecutedAt,
) derives ReadWriter
