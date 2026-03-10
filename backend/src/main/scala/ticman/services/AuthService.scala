package ticman.services

import ticman.db.UserRepository
import ticman.models.{AuthResponse, RegisterRequest, LoginRequest}
import org.mindrot.jbcrypt.BCrypt
import java.util.UUID

enum AuthError:
  case DuplicateEmail
  case InvalidCredentials

trait AuthService:
  def register(req: RegisterRequest): Either[AuthError, AuthResponse]
  def login(req: LoginRequest): Either[AuthError, AuthResponse]

class AuthServiceImpl(
    userRepo: UserRepository,
    jwtService: JwtService,
) extends AuthService:

  private val BcryptWorkFactor = 12

  override def register(req: RegisterRequest): Either[AuthError, AuthResponse] =
    userRepo.findByEmail(req.email) match
      case Some(_) => Left(AuthError.DuplicateEmail)
      case None =>
        val hashed = BCrypt.hashpw(req.password, BCrypt.gensalt(BcryptWorkFactor))
        val user   = userRepo.create(req.email, req.displayName, hashed)
        Right(tokensFor(user.id))

  override def login(req: LoginRequest): Either[AuthError, AuthResponse] =
    userRepo.findByEmail(req.email) match
      case None => Left(AuthError.InvalidCredentials)
      case Some(user) =>
        if BCrypt.checkpw(req.password, user.hashedPassword) then Right(tokensFor(user.id))
        else Left(AuthError.InvalidCredentials)

  private def tokensFor(userId: UUID): AuthResponse =
    AuthResponse(
      accessToken  = jwtService.generateAccessToken(userId),
      refreshToken = jwtService.generateRefreshToken(userId),
    )
