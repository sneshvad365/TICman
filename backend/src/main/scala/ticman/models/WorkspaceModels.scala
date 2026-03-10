package ticman.models

import upickle.default.*
import java.util.UUID

case class WorkspaceWithRole(
    id: UUID,
    name: String,
    ownerId: UUID,
    role: String,
)

case class WorkspaceResponse(
    id: String,
    name: String,
    role: String,
) derives ReadWriter

case class CreateWorkspaceRequest(name: String) derives ReadWriter
