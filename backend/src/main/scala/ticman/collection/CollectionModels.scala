package ticman.collection

import scalasql.Table
import ticman.db.TypeMappers.given
import ticman.{CollectionId, WorkspaceId, CollectionName, CollectionReadme, given}
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
    Collection(CollectionId(row.id), WorkspaceId(row.workspaceId), CollectionName(row.name), CollectionReadme(row.readme))

// --- Domain models ---

case class Collection(
    id: CollectionId,
    workspaceId: WorkspaceId,
    name: CollectionName,
    readme: CollectionReadme,
)

case class CollectionResponse(
    id: CollectionId,
    workspaceId: WorkspaceId,
    name: CollectionName,
    readme: CollectionReadme,
) derives ReadWriter

case class CreateCollectionRequest(
    name: CollectionName,
    readme: CollectionReadme = CollectionReadme(""),
) derives ReadWriter
