package ticman.workspace

import scalasql.Table
import ticman.db.TypeMappers.given
import ticman.{WorkspaceId, WorkspaceName, given}
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
    Workspace(WorkspaceId(row.id), WorkspaceName(row.name))

// --- Domain models ---

case class Workspace(id: WorkspaceId, name: WorkspaceName)

case class WorkspaceResponse(id: WorkspaceId, name: WorkspaceName) derives ReadWriter

case class CreateWorkspaceRequest(name: WorkspaceName) derives ReadWriter
