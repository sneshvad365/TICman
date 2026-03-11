package ticman.db

import ticman.models.Workspace
import javax.sql.DataSource
import java.util.UUID

trait WorkspaceRepository:
  def findAll(): Seq[Workspace]
  def create(name: String): Workspace

class PostgresWorkspaceRepository(dataSource: DataSource) extends WorkspaceRepository:

  override def findAll(): Seq[Workspace] =
    val conn = dataSource.getConnection()
    try
      val stmt = conn.prepareStatement("SELECT id, name FROM workspaces ORDER BY created_at DESC")
      val rs   = stmt.executeQuery()
      val buf  = scala.collection.mutable.ArrayBuffer[Workspace]()
      while rs.next() do
        buf += Workspace(id = UUID.fromString(rs.getString("id")), name = rs.getString("name"))
      rs.close(); stmt.close()
      buf.toSeq
    finally conn.close()

  override def create(name: String): Workspace =
    val conn = dataSource.getConnection()
    try
      val stmt = conn.prepareStatement("INSERT INTO workspaces (name) VALUES (?) RETURNING id")
      stmt.setString(1, name)
      val rs = stmt.executeQuery()
      rs.next()
      val id = UUID.fromString(rs.getString("id"))
      rs.close(); stmt.close()
      Workspace(id, name)
    finally conn.close()
