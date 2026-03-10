package ticman.db

import ticman.models.WorkspaceWithRole
import javax.sql.DataSource
import java.util.UUID

trait WorkspaceRepository:
  def findByUserId(userId: UUID): Seq[WorkspaceWithRole]
  def create(name: String, ownerId: UUID): WorkspaceWithRole
  def findMemberRole(workspaceId: UUID, userId: UUID): Option[String]

class PostgresWorkspaceRepository(dataSource: DataSource) extends WorkspaceRepository:

  override def findByUserId(userId: UUID): Seq[WorkspaceWithRole] =
    val conn = dataSource.getConnection()
    try
      val stmt = conn.prepareStatement("""
        SELECT w.id, w.name, w.owner_id, wm.role
        FROM workspaces w
        JOIN workspace_members wm ON wm.workspace_id = w.id
        WHERE wm.user_id = ?
        ORDER BY w.created_at DESC
      """)
      stmt.setObject(1, userId)
      val rs   = stmt.executeQuery()
      val buf  = scala.collection.mutable.ArrayBuffer[WorkspaceWithRole]()
      while rs.next() do
        buf += WorkspaceWithRole(
          id      = UUID.fromString(rs.getString("id")),
          name    = rs.getString("name"),
          ownerId = UUID.fromString(rs.getString("owner_id")),
          role    = rs.getString("role"),
        )
      rs.close(); stmt.close()
      buf.toSeq
    finally conn.close()

  override def create(name: String, ownerId: UUID): WorkspaceWithRole =
    val conn = dataSource.getConnection()
    try
      val wsStmt = conn.prepareStatement(
        "INSERT INTO workspaces (name, owner_id) VALUES (?, ?) RETURNING id"
      )
      wsStmt.setString(1, name)
      wsStmt.setObject(2, ownerId)
      val rs          = wsStmt.executeQuery()
      rs.next()
      val workspaceId = UUID.fromString(rs.getString("id"))
      rs.close(); wsStmt.close()

      val memStmt = conn.prepareStatement(
        "INSERT INTO workspace_members (workspace_id, user_id, role) VALUES (?, ?, 'owner')"
      )
      memStmt.setObject(1, workspaceId)
      memStmt.setObject(2, ownerId)
      memStmt.executeUpdate()
      memStmt.close()

      WorkspaceWithRole(workspaceId, name, ownerId, "owner")
    finally conn.close()

  override def findMemberRole(workspaceId: UUID, userId: UUID): Option[String] =
    val conn = dataSource.getConnection()
    try
      val stmt = conn.prepareStatement(
        "SELECT role FROM workspace_members WHERE workspace_id = ? AND user_id = ?"
      )
      stmt.setObject(1, workspaceId)
      stmt.setObject(2, userId)
      val rs = stmt.executeQuery()
      val role = if rs.next() then Some(rs.getString("role")) else None
      rs.close(); stmt.close()
      role
    finally conn.close()
