package ticman.models

import java.time.OffsetDateTime
import java.util.UUID

// Internal domain model — never serialized to client
case class User(
    id: UUID,
    email: String,
    displayName: String,
    hashedPassword: String,
    createdAt: OffsetDateTime,
)
