package ticman.db

import scalasql.Table

case class UserRow[T[_]](
    id: T[String],
    email: T[String],
    displayName: T[String],
    hashedPassword: T[String],
    createdAt: T[String],
)

object UserRow extends Table[UserRow]:
  override def tableName: String = "users"
