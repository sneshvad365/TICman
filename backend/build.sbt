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

    // Dependencies
    libraryDependencies ++= Seq(
      "com.lihaoyi"    %% "cask"      % "0.9.7",
      "com.lihaoyi"    %% "upickle"   % "4.1.0",
      "com.lihaoyi"    %% "requests"  % "0.9.0",
      "com.lihaoyi"    %% "scalasql"  % "0.1.19",
      "org.postgresql"  % "postgresql" % "42.7.7",
      "com.auth0"       % "java-jwt"  % "4.5.0",
      "org.mindrot"     % "jbcrypt"   % "0.4",
      "com.zaxxer"      % "HikariCP"  % "5.1.0",
      "org.slf4j"       % "slf4j-simple" % "2.0.9",
      "org.scalatest"  %% "scalatest"                   % "3.2.19" % Test,
      "org.scalatestplus" %% "mockito-5-12"             % "3.2.19.0" % Test,
    ),

    // Flyway
    flywayUrl      := "jdbc:postgresql://localhost:5433/ticman",
    flywayUser     := "ticman",
    flywayPassword := "ticman",
    flywayLocations := Seq("filesystem:../db/migrations"),
  )
