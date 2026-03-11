package ticman.db

import scalasql.TypeMapper
import upickle.default.*
import org.postgresql.util.PGobject
import java.sql.{ResultSet, PreparedStatement, JDBCType}

// UUID TypeMapper is built into PostgresDialect as UuidType — do NOT redefine it here.

object TypeMappers:

  given jsonbMapMapper: TypeMapper[Map[String, String]] = new TypeMapper[Map[String, String]]:
    def jdbcType: JDBCType = JDBCType.OTHER
    def get(r: ResultSet, idx: Int): Map[String, String] =
      read[Map[String, String]](r.getString(idx))
    def put(r: PreparedStatement, idx: Int, v: Map[String, String]): Unit =
      val obj = PGobject()
      obj.setType("jsonb")
      obj.setValue(write(v))
      r.setObject(idx, obj)
