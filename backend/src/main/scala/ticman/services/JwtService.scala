package ticman.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import java.util.{Date, UUID}

trait JwtService:
  def generateAccessToken(userId: UUID): String
  def generateRefreshToken(userId: UUID): String
  def validateAccessToken(token: String): Either[String, UUID]
  def validateRefreshToken(token: String): Either[String, UUID]

class JwtServiceImpl(accessSecret: String, refreshSecret: String) extends JwtService:

  private val AccessType  = "access"
  private val RefreshType = "refresh"
  private val AccessTtlMs  = 15L * 60 * 1000       // 15 minutes
  private val RefreshTtlMs = 7L * 24 * 60 * 60 * 1000 // 7 days

  private val accessAlgorithm  = Algorithm.HMAC256(accessSecret)
  private val refreshAlgorithm = Algorithm.HMAC256(refreshSecret)

  override def generateAccessToken(userId: UUID): String =
    val now = System.currentTimeMillis()
    JWT.create()
      .withSubject(userId.toString)
      .withClaim("type", AccessType)
      .withIssuedAt(new Date(now))
      .withExpiresAt(new Date(now + AccessTtlMs))
      .sign(accessAlgorithm)

  override def generateRefreshToken(userId: UUID): String =
    val now = System.currentTimeMillis()
    JWT.create()
      .withSubject(userId.toString)
      .withClaim("type", RefreshType)
      .withIssuedAt(new Date(now))
      .withExpiresAt(new Date(now + RefreshTtlMs))
      .sign(refreshAlgorithm)

  override def validateAccessToken(token: String): Either[String, UUID] =
    try
      val decoded = JWT.require(accessAlgorithm).build().verify(token)
      val tokenType = decoded.getClaim("type").asString()
      if tokenType != AccessType then Left("Invalid token type")
      else Right(UUID.fromString(decoded.getSubject))
    catch
      case e: JWTVerificationException => Left(e.getMessage)
      case e: IllegalArgumentException => Left(e.getMessage)

  override def validateRefreshToken(token: String): Either[String, UUID] =
    try
      val decoded = JWT.require(refreshAlgorithm).build().verify(token)
      val tokenType = decoded.getClaim("type").asString()
      if tokenType != RefreshType then Left("Invalid token type")
      else Right(UUID.fromString(decoded.getSubject))
    catch
      case e: JWTVerificationException => Left(e.getMessage)
      case e: IllegalArgumentException => Left(e.getMessage)
