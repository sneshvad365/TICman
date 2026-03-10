package ticman.db

import ticman.models.HistoryResponse
import upickle.default.*
import javax.sql.DataSource
import java.util.UUID

trait ResponseHistoryRepository:
  def save(
      requestId: UUID,
      userId: UUID,
      statusCode: Int,
      headers: Map[String, String],
      body: Option[String],
      durationMs: Long,
  ): Unit
  def findByRequestId(requestId: UUID, limit: Int = 20): Seq[HistoryResponse]

class PostgresResponseHistoryRepository(dataSource: DataSource) extends ResponseHistoryRepository:

  override def save(
      requestId: UUID,
      userId: UUID,
      statusCode: Int,
      headers: Map[String, String],
      body: Option[String],
      durationMs: Long,
  ): Unit =
    val conn = dataSource.getConnection()
    try
      val stmt = conn.prepareStatement(
        "INSERT INTO response_history (request_id, user_id, status_code, headers, body, duration_ms) VALUES (?, ?, ?, ?::jsonb, ?, ?)"
      )
      stmt.setObject(1, requestId)
      stmt.setObject(2, userId)
      stmt.setInt(3, statusCode)
      stmt.setString(4, write(headers))
      stmt.setString(5, body.orNull)
      stmt.setLong(6, durationMs)
      stmt.executeUpdate()
      stmt.close()
    finally conn.close()

  override def findByRequestId(requestId: UUID, limit: Int = 20): Seq[HistoryResponse] =
    val conn = dataSource.getConnection()
    try
      val stmt = conn.prepareStatement(
        "SELECT id, status_code, headers::text, body, duration_ms, executed_at FROM response_history WHERE request_id = ? ORDER BY executed_at DESC LIMIT ?"
      )
      stmt.setObject(1, requestId)
      stmt.setInt(2, limit)
      val rs  = stmt.executeQuery()
      val buf = scala.collection.mutable.ArrayBuffer[HistoryResponse]()
      while rs.next() do
        buf += HistoryResponse(
          id          = rs.getString("id"),
          statusCode  = rs.getInt("status_code"),
          headers     = read[Map[String, String]](rs.getString("headers")),
          body        = Option(rs.getString("body")),
          durationMs  = rs.getLong("duration_ms"),
          executedAt  = rs.getString("executed_at"),
        )
      rs.close(); stmt.close()
      buf.toSeq
    finally conn.close()
