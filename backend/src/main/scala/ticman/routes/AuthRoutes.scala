package ticman.routes

import cask.*
import ticman.services.AuthService
import ticman.models.{RegisterRequest, LoginRequest, ErrorResponse}
import upickle.default.*

class AuthRoutes(authService: AuthService) extends Routes:

  @post("/api/auth/register")
  def register(request: Request): Response[String] =
    try
      val body = read[RegisterRequest](request.text())
      authService.register(body) match
        case Right(resp) =>
          Response(write(resp), statusCode = 201, headers = jsonHeader)
        case Left(err) => ErrorHandler.authErrorToResponse(err)
    catch case e: Exception =>
      Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), statusCode = 500, headers = jsonHeader)

  @post("/api/auth/login")
  def login(request: Request): Response[String] =
    try
      val body = read[LoginRequest](request.text())
      authService.login(body) match
        case Right(resp) =>
          Response(write(resp), statusCode = 200, headers = jsonHeader)
        case Left(err) => ErrorHandler.authErrorToResponse(err)
    catch case e: Exception =>
      Response(write(ErrorResponse(e.getMessage, "INTERNAL_ERROR")), statusCode = 500, headers = jsonHeader)

  private val jsonHeader = Seq("Content-Type" -> "application/json")

  initialize()
