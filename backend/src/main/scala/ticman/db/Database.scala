package ticman.db

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import org.flywaydb.core.Flyway
import scalasql.DbClient
import scalasql.core.Config
import scalasql.PostgresDialect.given

object Database:
  private val url      = "jdbc:postgresql://localhost:5433/ticman"
  private val user     = "ticman"
  private val password = "ticman"

  def fromEnv(): DbClient.DataSource =
    migrate()

    val hikariConfig = HikariConfig()
    hikariConfig.setJdbcUrl(url)
    hikariConfig.setUsername(user)
    hikariConfig.setPassword(password)
    hikariConfig.setMaximumPoolSize(10)
    val ds = HikariDataSource(hikariConfig)

    DbClient.DataSource(
      dataSource = ds,
      config = new Config { override def columnNameMapper(v: String) = camelToSnake(v) },
      listeners = Seq.empty,
    )

  private def migrate(): Unit =
    Flyway.configure()
      .dataSource(url, user, password)
      .locations("classpath:db/migration")
      .load()
      .migrate()

  private def camelToSnake(s: String): String =
    "[A-Z]".r.replaceAllIn(s, m => s"_${m.group(0).toLowerCase}")
