package ticman.models

import upickle.default.*
import java.util.UUID

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

