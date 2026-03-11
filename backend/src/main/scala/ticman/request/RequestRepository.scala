package ticman.request

import scalasql.DbClient
import scalasql.PostgresDialect.*
import ticman.db.TypeMappers.given
import ticman.{RequestId, CollectionId, given}

trait RequestRepository:
  def findByCollectionId(collectionId: CollectionId): Seq[HttpRequest]
  def findById(id: RequestId): Option[HttpRequest]
  def create(collectionId: CollectionId, req: SaveRequestRequest): HttpRequest
  def update(id: RequestId, req: SaveRequestRequest): HttpRequest
  def delete(id: RequestId): Unit

class PostgresRequestRepository(db: DbClient.DataSource) extends RequestRepository:

  override def findByCollectionId(collectionId: CollectionId): Seq[HttpRequest] =
    db.transaction { implicit tx =>
      tx.run(RequestRow.select.filter(_.collectionId === collectionId))
        .map(RequestRow.toDomain)
    }

  override def findById(id: RequestId): Option[HttpRequest] =
    db.transaction { implicit tx =>
      tx.run(RequestRow.select.filter(_.id === id))
        .map(RequestRow.toDomain)
        .headOption
    }

  override def create(collectionId: CollectionId, req: SaveRequestRequest): HttpRequest =
    db.transaction { implicit tx =>
      val id = tx.run(
        RequestRow.insert
          .columns(
            _.collectionId  := collectionId,
            _.name          := req.name,
            _.method        := req.method,
            _.urlTemplate   := req.urlTemplate,
            _.headers       := req.headers,
            _.body          := req.body,
          )
          .returning(_.id)
      ).head
      HttpRequest(RequestId(id), collectionId, req.name, req.method, req.urlTemplate, req.headers, req.body)
    }

  override def update(id: RequestId, req: SaveRequestRequest): HttpRequest =
    db.transaction { implicit tx =>
      val existing = tx.run(RequestRow.select.filter(_.id === id)).head
      tx.run(
        RequestRow.update(_.id === id)
          .set(_.name        := req.name)
          .set(_.method      := req.method)
          .set(_.urlTemplate := req.urlTemplate)
          .set(_.headers     := req.headers)
          .set(_.body        := req.body)
      )
      HttpRequest(id, CollectionId(existing.collectionId), req.name, req.method, req.urlTemplate, req.headers, req.body)
    }

  override def delete(id: RequestId): Unit =
    db.transaction { implicit tx =>
      tx.run(RequestRow.delete(_.id === id))
    }
