package ticman.db

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import javax.sql.DataSource

object Database:
  def fromEnv(): DataSource =
    val url      = sys.env.getOrElse("DATABASE_URL", "jdbc:postgresql://localhost:5433/ticman")
    val user     = sys.env.getOrElse("DATABASE_USER", "ticman")
    val password = sys.env.getOrElse("DATABASE_PASSWORD", "ticman")

    val config = HikariConfig()
    config.setJdbcUrl(url)
    config.setUsername(user)
    config.setPassword(password)
    config.setMaximumPoolSize(10)
    HikariDataSource(config)
