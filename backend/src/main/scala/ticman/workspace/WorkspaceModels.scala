package ticman.workspace

import scalasql.Table
import ticman.db.TypeMappers.given
import upickle.default.*
import java.util.UUID

// --- ScalaSql table class + converter ---

case class WorkspaceRow[T[_]](
    id: T[UUID],
    name: T[String],
)

object WorkspaceRow extends Table[WorkspaceRow]:
  override def tableName: String = "workspaces"

  def toDomain(row: WorkspaceRow[[T] =>> T]): Workspace =
    Workspace(row.id, row.name)

// --- Domain models ---

case class Workspace(id: UUID, name: String)

case class WorkspaceResponse(id: String, name: String) derives ReadWriter

case class CreateWorkspaceRequest(name: String) derives ReadWriter
