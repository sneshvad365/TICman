package ticman.workspace

import scalasql.DbClient
import scalasql.PostgresDialect.*
import ticman.db.TypeMappers.given
import ticman.{WorkspaceId, WorkspaceName, given}

trait WorkspaceRepository:
  def findAll(): Seq[Workspace]
  def create(name: WorkspaceName): Workspace
  def delete(id: WorkspaceId): Unit

class PostgresWorkspaceRepository(db: DbClient.DataSource) extends WorkspaceRepository:

  override def findAll(): Seq[Workspace] =
    db.transaction { implicit tx =>
      tx.run(WorkspaceRow.select).map(WorkspaceRow.toDomain)
    }

  override def create(name: WorkspaceName): Workspace =
    db.transaction { implicit tx =>
      val id = tx.run(
        WorkspaceRow.insert.columns(_.name := name).returning(_.id)
      ).head
      Workspace(WorkspaceId(id), name)
    }

  override def delete(id: WorkspaceId): Unit =
    db.transaction { implicit tx =>
      tx.run(WorkspaceRow.delete(_.id === id))
    }
