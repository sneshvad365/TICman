package ticman.collection

import scalasql.DbClient
import scalasql.PostgresDialect.*
import ticman.db.TypeMappers.given
import java.util.UUID

trait CollectionRepository:
  def findByWorkspaceId(workspaceId: UUID): Seq[Collection]
  def findById(id: UUID): Option[Collection]
  def create(workspaceId: UUID, name: String, readme: String): Collection
  def delete(id: UUID): Unit

class PostgresCollectionRepository(db: DbClient.DataSource) extends CollectionRepository:

  override def findByWorkspaceId(workspaceId: UUID): Seq[Collection] =
    db.transaction { implicit tx =>
      tx.run(CollectionRow.select.filter(_.workspaceId === workspaceId))
        .map(CollectionRow.toDomain)
    }

  override def findById(id: UUID): Option[Collection] =
    db.transaction { implicit tx =>
      tx.run(CollectionRow.select.filter(_.id === id))
        .map(CollectionRow.toDomain)
        .headOption
    }

  override def create(workspaceId: UUID, name: String, readme: String): Collection =
    db.transaction { implicit tx =>
      val id = tx.run(
        CollectionRow.insert
          .columns(_.workspaceId := workspaceId, _.name := name, _.readme := readme)
          .returning(_.id)
      ).head
      Collection(id, workspaceId, name, readme)
    }

  override def delete(id: UUID): Unit =
    db.transaction { implicit tx =>
      tx.run(CollectionRow.delete(_.id === id))
    }
