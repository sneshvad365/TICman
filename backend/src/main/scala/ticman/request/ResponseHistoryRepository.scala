package ticman.request

import scalasql.DbClient
import scalasql.PostgresDialect.*
import ticman.db.TypeMappers.given
import ticman.{RequestId, HistoryId, StatusCode, DurationMs, given}

trait ResponseHistoryRepository:
  def save(
      requestId: RequestId,
      statusCode: StatusCode,
      headers: Map[String, String],
      body: Option[String],
      durationMs: DurationMs,
  ): Unit
  def findByRequestId(requestId: RequestId, limit: Int = 20): Seq[HistoryResponse]
  def delete(id: HistoryId): Unit

class PostgresResponseHistoryRepository(db: DbClient.DataSource) extends ResponseHistoryRepository:

  override def save(
      requestId: RequestId,
      statusCode: StatusCode,
      headers: Map[String, String],
      body: Option[String],
      durationMs: DurationMs,
  ): Unit =
    db.transaction { implicit tx =>
      tx.run(
        ResponseHistoryRow.insert.columns(
          _.requestId  := requestId,
          _.statusCode := statusCode,
          _.headers    := headers,
          _.body       := body,
          _.durationMs := durationMs,
        )
      )
    }

  override def findByRequestId(requestId: RequestId, limit: Int = 20): Seq[HistoryResponse] =
    db.transaction { implicit tx =>
      tx.run(ResponseHistoryRow.select.filter(_.requestId === requestId))
        .sortBy(_.executedAt)
        .reverse
        .take(limit)
        .map(ResponseHistoryRow.toDomain)
    }

  override def delete(id: HistoryId): Unit =
    db.transaction { implicit tx =>
      tx.run(ResponseHistoryRow.delete(_.id === id))
    }
