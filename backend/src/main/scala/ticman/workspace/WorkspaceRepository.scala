package ticman.workspace

import scalasql.DbClient
import scalasql.PostgresDialect.*
import ticman.db.TypeMappers.given
import java.util.UUID

trait WorkspaceRepository:
  def findAll(): Seq[Workspace]
  def create(name: String): Workspace
  def delete(id: UUID): Unit

class PostgresWorkspaceRepository(db: DbClient.DataSource) extends WorkspaceRepository:

  override def findAll(): Seq[Workspace] =
    db.transaction { implicit tx =>
      tx.run(WorkspaceRow.select).map(WorkspaceRow.toDomain)
    }

  override def create(name: String): Workspace =
    db.transaction { implicit tx =>
      val id = tx.run(
        WorkspaceRow.insert.columns(_.name := name).returning(_.id)
      ).head
      Workspace(id, name)
    }

  override def delete(id: UUID): Unit =
    db.transaction { implicit tx =>
      tx.run(WorkspaceRow.delete(_.id === id))
    }
