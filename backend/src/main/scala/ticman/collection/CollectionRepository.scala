package ticman.collection

import scalasql.DbClient
import scalasql.PostgresDialect.*
import ticman.db.TypeMappers.given
import ticman.{CollectionId, WorkspaceId, CollectionName, CollectionReadme, given}

trait CollectionRepository:
  def findByWorkspaceId(workspaceId: WorkspaceId): Seq[Collection]
  def findById(id: CollectionId): Option[Collection]
  def create(workspaceId: WorkspaceId, name: CollectionName, readme: CollectionReadme): Collection
  def delete(id: CollectionId): Unit

class PostgresCollectionRepository(db: DbClient.DataSource) extends CollectionRepository:

  override def findByWorkspaceId(workspaceId: WorkspaceId): Seq[Collection] =
    db.transaction { implicit tx =>
      tx.run(CollectionRow.select.filter(_.workspaceId === workspaceId))
        .map(CollectionRow.toDomain)
    }

  override def findById(id: CollectionId): Option[Collection] =
    db.transaction { implicit tx =>
      tx.run(CollectionRow.select.filter(_.id === id))
        .map(CollectionRow.toDomain)
        .headOption
    }

  override def create(workspaceId: WorkspaceId, name: CollectionName, readme: CollectionReadme): Collection =
    db.transaction { implicit tx =>
      val id = tx.run(
        CollectionRow.insert
          .columns(_.workspaceId := workspaceId, _.name := name, _.readme := readme)
          .returning(_.id)
      ).head
      Collection(CollectionId(id), workspaceId, name, readme)
    }

  override def delete(id: CollectionId): Unit =
    db.transaction { implicit tx =>
      tx.run(CollectionRow.delete(_.id === id))
    }
