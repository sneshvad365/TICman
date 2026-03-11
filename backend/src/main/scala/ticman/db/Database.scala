package ticman.db

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import scalasql.DbClient
import scalasql.core.Config
import scalasql.PostgresDialect.given

object Database:
  def fromEnv(): DbClient.DataSource =
    val url      = sys.env.getOrElse("DATABASE_URL", "jdbc:postgresql://localhost:5433/ticman")
    val user     = sys.env.getOrElse("DATABASE_USER", "ticman")
    val password = sys.env.getOrElse("DATABASE_PASSWORD", "ticman")

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

  private def camelToSnake(s: String): String =
    "[A-Z]".r.replaceAllIn(s, m => s"_${m.group(0).toLowerCase}")
