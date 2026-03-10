package ticman.db

import ticman.models.User
import scalasql.{DbClient, Sc}
import scalasql.dialects.PostgresDialect
import scalasql.PostgresDialect.*
import javax.sql.DataSource
import java.util.UUID
import java.time.OffsetDateTime

trait UserRepository:
  def findByEmail(email: String): Option[User]
  def create(email: String, displayName: String, hashedPassword: String): User

class PostgresUserRepository(dataSource: DataSource) extends UserRepository:

  private given scalasql.core.DialectConfig = PostgresDialect

  private val db = new DbClient.DataSource(dataSource)

  override def findByEmail(email: String): Option[User] =
    db.transaction { implicit db =>
      db.run(
        UserRow.select.filter(_.email === email)
      ).headOption.map(rowToUser)
    }

  // ScalaSql sends UUID columns as varchar, so we use raw JDBC for INSERT
  // and let Postgres generate id and created_at from their column defaults.
  override def create(email: String, displayName: String, hashedPassword: String): User =
    val conn = dataSource.getConnection()
    try
      val stmt = conn.prepareStatement(
        "INSERT INTO users (email, display_name, hashed_password) VALUES (?, ?, ?)"
      )
      stmt.setString(1, email)
      stmt.setString(2, displayName)
      stmt.setString(3, hashedPassword)
      stmt.executeUpdate()
      stmt.close()
    finally
      conn.close()

    db.transaction { implicit db =>
      rowToUser(db.run(UserRow.select.filter(_.email === email)).head)
    }

  private def rowToUser(row: UserRow[Sc]): User =
    User(
      id             = UUID.fromString(row.id),
      email          = row.email,
      displayName    = row.displayName,
      hashedPassword = row.hashedPassword,
      createdAt      = OffsetDateTime.now(), // createdAt is internal, never sent to client
    )
