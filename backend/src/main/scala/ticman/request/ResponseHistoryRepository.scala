package ticman.request

import scalasql.DbClient
import scalasql.PostgresDialect.*
import ticman.db.TypeMappers.given
import java.util.UUID

trait ResponseHistoryRepository:
  def save(
      requestId: UUID,
      statusCode: Int,
      headers: Map[String, String],
      body: Option[String],
      durationMs: Long,
  ): Unit
  def findByRequestId(requestId: UUID, limit: Int = 20): Seq[HistoryResponse]

class PostgresResponseHistoryRepository(db: DbClient.DataSource) extends ResponseHistoryRepository:

  override def save(
      requestId: UUID,
      statusCode: Int,
      headers: Map[String, String],
      body: Option[String],
      durationMs: Long,
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

  override def findByRequestId(requestId: UUID, limit: Int = 20): Seq[HistoryResponse] =
    db.transaction { implicit tx =>
      tx.run(ResponseHistoryRow.select.filter(_.requestId === requestId))
        .sortBy(_.executedAt)
        .reverse
        .take(limit)
        .map(ResponseHistoryRow.toDomain)
    }
