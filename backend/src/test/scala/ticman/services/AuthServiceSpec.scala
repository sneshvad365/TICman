package ticman.services

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.*

import ticman.db.UserRepository
import ticman.models.{RegisterRequest, LoginRequest, User}
import org.mindrot.jbcrypt.BCrypt
import java.util.UUID
import java.time.OffsetDateTime

class AuthServiceSpec extends AnyFlatSpec with Matchers with MockitoSugar with EitherValues:

  // ---- helpers ----

  private val jwtSecret        = "test-access-secret-long-enough"
  private val jwtRefreshSecret = "test-refresh-secret-long-enough"

  private def makeJwt()  = JwtServiceImpl(jwtSecret, jwtRefreshSecret)
  private def makeRepo() = mock[UserRepository]

  private def makeUser(email: String = "alice@example.com"): User =
    User(
      id             = UUID.randomUUID(),
      email          = email,
      displayName    = "Alice",
      hashedPassword = BCrypt.hashpw("hunter2", BCrypt.gensalt(4)),
      createdAt      = OffsetDateTime.now(),
    )

  // ---- AuthService tests ----

  "AuthService.register" should "return tokens on success" in:
    val repo    = makeRepo()
    val jwt     = makeJwt()
    val service = AuthServiceImpl(repo, jwt)
    val user    = makeUser()

    when(repo.findByEmail("alice@example.com")).thenReturn(None)
    when(repo.create(anyString(), anyString(), anyString())).thenReturn(user)

    val result = service.register(RegisterRequest("alice@example.com", "Alice", "hunter2"))
    result.isRight shouldBe true
    result.value.accessToken  should not be empty
    result.value.refreshToken should not be empty

  it should "return Left(DuplicateEmail) for a duplicate email" in:
    val repo    = makeRepo()
    val jwt     = makeJwt()
    val service = AuthServiceImpl(repo, jwt)
    val user    = makeUser()

    when(repo.findByEmail("alice@example.com")).thenReturn(Some(user))

    val result = service.register(RegisterRequest("alice@example.com", "Alice", "hunter2"))
    result shouldBe Left(AuthError.DuplicateEmail)

  "AuthService.login" should "return tokens on success" in:
    val repo    = makeRepo()
    val jwt     = makeJwt()
    val service = AuthServiceImpl(repo, jwt)
    val user    = makeUser()

    when(repo.findByEmail("alice@example.com")).thenReturn(Some(user))

    val result = service.login(LoginRequest("alice@example.com", "hunter2"))
    result.isRight shouldBe true
    result.value.accessToken  should not be empty
    result.value.refreshToken should not be empty

  it should "return Left(InvalidCredentials) for wrong password" in:
    val repo    = makeRepo()
    val jwt     = makeJwt()
    val service = AuthServiceImpl(repo, jwt)
    val user    = makeUser()

    when(repo.findByEmail("alice@example.com")).thenReturn(Some(user))

    val result = service.login(LoginRequest("alice@example.com", "wrongpassword"))
    result shouldBe Left(AuthError.InvalidCredentials)

  it should "return Left(InvalidCredentials) for unknown email" in:
    val repo    = makeRepo()
    val jwt     = makeJwt()
    val service = AuthServiceImpl(repo, jwt)

    when(repo.findByEmail("nobody@example.com")).thenReturn(None)

    val result = service.login(LoginRequest("nobody@example.com", "hunter2"))
    result shouldBe Left(AuthError.InvalidCredentials)

  // ---- JwtService tests ----

  "JwtService" should "round-trip a userId through access token" in:
    val jwt    = makeJwt()
    val userId = UUID.randomUUID()
    val token  = jwt.generateAccessToken(userId)
    jwt.validateAccessToken(token).value shouldBe userId

  it should "reject a refresh token when validating as access token" in:
    val jwt    = makeJwt()
    val userId = UUID.randomUUID()
    val refresh = jwt.generateRefreshToken(userId)
    jwt.validateAccessToken(refresh).isLeft shouldBe true
