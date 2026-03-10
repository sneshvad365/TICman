package ticman.db

import ticman.models.Collection
import javax.sql.DataSource
import java.util.UUID

trait CollectionRepository:
  def findByWorkspaceId(workspaceId: UUID): Seq[Collection]
  def findById(id: UUID): Option[Collection]
  def create(workspaceId: UUID, name: String, readme: String): Collection
  def delete(id: UUID): Unit

class PostgresCollectionRepository(dataSource: DataSource) extends CollectionRepository:

  override def findByWorkspaceId(workspaceId: UUID): Seq[Collection] =
    val conn = dataSource.getConnection()
    try
      val stmt = conn.prepareStatement(
        "SELECT id, workspace_id, name, readme FROM collections WHERE workspace_id = ? ORDER BY created_at ASC"
      )
      stmt.setObject(1, workspaceId)
      val rs  = stmt.executeQuery()
      val buf = scala.collection.mutable.ArrayBuffer[Collection]()
      while rs.next() do
        buf += rowToCollection(rs)
      rs.close(); stmt.close()
      buf.toSeq
    finally conn.close()

  override def findById(id: UUID): Option[Collection] =
    val conn = dataSource.getConnection()
    try
      val stmt = conn.prepareStatement(
        "SELECT id, workspace_id, name, readme FROM collections WHERE id = ?"
      )
      stmt.setObject(1, id)
      val rs     = stmt.executeQuery()
      val result = if rs.next() then Some(rowToCollection(rs)) else None
      rs.close(); stmt.close()
      result
    finally conn.close()

  override def create(workspaceId: UUID, name: String, readme: String): Collection =
    val conn = dataSource.getConnection()
    try
      val stmt = conn.prepareStatement(
        "INSERT INTO collections (workspace_id, name, readme) VALUES (?, ?, ?) RETURNING id"
      )
      stmt.setObject(1, workspaceId)
      stmt.setString(2, name)
      stmt.setString(3, readme)
      val rs = stmt.executeQuery()
      rs.next()
      val id = UUID.fromString(rs.getString("id"))
      rs.close(); stmt.close()
      Collection(id, workspaceId, name, readme)
    finally conn.close()

  override def delete(id: UUID): Unit =
    val conn = dataSource.getConnection()
    try
      val stmt = conn.prepareStatement("DELETE FROM collections WHERE id = ?")
      stmt.setObject(1, id)
      stmt.executeUpdate()
      stmt.close()
    finally conn.close()

  private def rowToCollection(rs: java.sql.ResultSet): Collection =
    Collection(
      id          = UUID.fromString(rs.getString("id")),
      workspaceId = UUID.fromString(rs.getString("workspace_id")),
      name        = rs.getString("name"),
      readme      = rs.getString("readme"),
    )
