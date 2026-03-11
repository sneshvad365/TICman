package ticman.models

import upickle.default.*
import java.util.UUID

case class Workspace(id: UUID, name: String)

case class WorkspaceResponse(id: String, name: String) derives ReadWriter

case class CreateWorkspaceRequest(name: String) derives ReadWriter
