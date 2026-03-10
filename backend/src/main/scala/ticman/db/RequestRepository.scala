package ticman.db

import ticman.models.{HttpRequest, SaveRequestRequest}
import upickle.default.*
import javax.sql.DataSource
import java.util.UUID

trait RequestRepository:
  def findByCollectionId(collectionId: UUID): Seq[HttpRequest]
  def findById(id: UUID): Option[HttpRequest]
  def create(collectionId: UUID, createdBy: UUID, req: SaveRequestRequest): HttpRequest
  def update(id: UUID, req: SaveRequestRequest): HttpRequest
  def delete(id: UUID): Unit

class PostgresRequestRepository(dataSource: DataSource) extends RequestRepository:

  override def findByCollectionId(collectionId: UUID): Seq[HttpRequest] =
    val conn = dataSource.getConnection()
    try
      val stmt = conn.prepareStatement(
        "SELECT id, collection_id, created_by, name, method, url_template, headers::text, body FROM requests WHERE collection_id = ? ORDER BY created_at ASC"
      )
      stmt.setObject(1, collectionId)
      val rs  = stmt.executeQuery()
      val buf = scala.collection.mutable.ArrayBuffer[HttpRequest]()
      while rs.next() do buf += rowToRequest(rs)
      rs.close(); stmt.close()
      buf.toSeq
    finally conn.close()

  override def findById(id: UUID): Option[HttpRequest] =
    val conn = dataSource.getConnection()
    try
      val stmt = conn.prepareStatement(
        "SELECT id, collection_id, created_by, name, method, url_template, headers::text, body FROM requests WHERE id = ?"
      )
      stmt.setObject(1, id)
      val rs     = stmt.executeQuery()
      val result = if rs.next() then Some(rowToRequest(rs)) else None
      rs.close(); stmt.close()
      result
    finally conn.close()

  override def create(collectionId: UUID, createdBy: UUID, req: SaveRequestRequest): HttpRequest =
    val conn = dataSource.getConnection()
    try
      val stmt = conn.prepareStatement(
        "INSERT INTO requests (collection_id, created_by, name, method, url_template, headers, body) VALUES (?, ?, ?, ?, ?, ?::jsonb, ?) RETURNING id"
      )
      stmt.setObject(1, collectionId)
      stmt.setObject(2, createdBy)
      stmt.setString(3, req.name)
      stmt.setString(4, req.method)
      stmt.setString(5, req.urlTemplate)
      stmt.setString(6, write(req.headers))
      stmt.setString(7, req.body.orNull)
      val rs = stmt.executeQuery()
      rs.next()
      val id = UUID.fromString(rs.getString("id"))
      rs.close(); stmt.close()
      HttpRequest(id, collectionId, createdBy, req.name, req.method, req.urlTemplate, req.headers, req.body)
    finally conn.close()

  override def update(id: UUID, req: SaveRequestRequest): HttpRequest =
    val conn = dataSource.getConnection()
    try
      val stmt = conn.prepareStatement(
        "UPDATE requests SET name=?, method=?, url_template=?, headers=?::jsonb, body=?, updated_at=now() WHERE id=? RETURNING collection_id, created_by"
      )
      stmt.setString(1, req.name)
      stmt.setString(2, req.method)
      stmt.setString(3, req.urlTemplate)
      stmt.setString(4, write(req.headers))
      stmt.setString(5, req.body.orNull)
      stmt.setObject(6, id)
      val rs = stmt.executeQuery()
      rs.next()
      val collectionId = UUID.fromString(rs.getString("collection_id"))
      val createdBy    = UUID.fromString(rs.getString("created_by"))
      rs.close(); stmt.close()
      HttpRequest(id, collectionId, createdBy, req.name, req.method, req.urlTemplate, req.headers, req.body)
    finally conn.close()

  override def delete(id: UUID): Unit =
    val conn = dataSource.getConnection()
    try
      val stmt = conn.prepareStatement("DELETE FROM requests WHERE id = ?")
      stmt.setObject(1, id)
      stmt.executeUpdate()
      stmt.close()
    finally conn.close()

  private def rowToRequest(rs: java.sql.ResultSet): HttpRequest =
    HttpRequest(
      id           = UUID.fromString(rs.getString("id")),
      collectionId = UUID.fromString(rs.getString("collection_id")),
      createdBy    = UUID.fromString(rs.getString("created_by")),
      name         = rs.getString("name"),
      method       = rs.getString("method"),
      urlTemplate  = rs.getString("url_template"),
      headers      = read[Map[String, String]](rs.getString("headers")),
      body         = Option(rs.getString("body")),
    )
