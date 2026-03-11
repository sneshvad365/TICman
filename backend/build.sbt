import FlywayPlugin.autoImport._

val scala3Version = "3.6.2"

lazy val root = project
  .in(file("."))
  .enablePlugins(FlywayPlugin)
  .settings(
    name         := "ticman-backend",
    version      := "0.1.0",
    scalaVersion := scala3Version,

    // Run settings
    Compile / mainClass := Some("ticman.Main"),
    fork                := true,
    javaOptions += "-Djava.util.logging.config.file=src/main/resources/logging.properties",

    // Dependencies
    libraryDependencies ++= Seq(
      "com.lihaoyi"    %% "cask"      % "0.9.7",
      "com.lihaoyi"    %% "upickle"   % "4.1.0",
      "com.lihaoyi"    %% "requests"  % "0.9.0",
      "com.lihaoyi"    %% "scalasql"  % "0.1.19",
      "org.postgresql"  % "postgresql" % "42.7.7",
      "com.zaxxer"      % "HikariCP"  % "5.1.0",
      "org.slf4j"       % "slf4j-simple" % "2.0.9",
      "org.scalatest"  %% "scalatest" % "3.2.19" % Test,
      "org.flywaydb"    % "flyway-core" % "9.22.3",
    ),

    // Flyway sbt plugin (for sbt flywayMigrate convenience)
    flywayUrl      := "jdbc:postgresql://localhost:5433/ticman",
    flywayUser     := "ticman",
    flywayPassword := "ticman",
    flywayLocations := Seq("filesystem:src/main/resources/db/migration"),
  )
