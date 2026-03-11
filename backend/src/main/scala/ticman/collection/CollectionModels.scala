package ticman.collection

import scalasql.Table
import ticman.db.TypeMappers.given
import upickle.default.*
import java.util.UUID

// --- ScalaSql table class + converter ---

case class CollectionRow[T[_]](
    id: T[UUID],
    workspaceId: T[UUID],
    name: T[String],
    readme: T[String],
)

object CollectionRow extends Table[CollectionRow]:
  override def tableName: String = "collections"

  def toDomain(row: CollectionRow[[T] =>> T]): Collection =
    Collection(row.id, row.workspaceId, row.name, row.readme)

// --- Domain models ---

case class Collection(
    id: UUID,
    workspaceId: UUID,
    name: String,
    readme: String,
)

case class CollectionResponse(
    id: String,
    workspaceId: String,
    name: String,
    readme: String,
) derives ReadWriter

case class CreateCollectionRequest(
    name: String,
    readme: String = "",
) derives ReadWriter
