package ticman.routes

import ticman.services.AuthError
import ticman.models.ErrorResponse
import upickle.default.*
import cask.Response

object ErrorHandler:
  def authErrorToResponse(err: AuthError): Response[String] =
    err match
      case AuthError.DuplicateEmail =>
        Response(
          write(ErrorResponse("Email already in use", "DUPLICATE_EMAIL")),
          statusCode = 409,
          headers = Seq("Content-Type" -> "application/json"),
        )
      case AuthError.InvalidCredentials =>
        Response(
          write(ErrorResponse("Invalid credentials", "INVALID_CREDENTIALS")),
          statusCode = 401,
          headers = Seq("Content-Type" -> "application/json"),
        )
