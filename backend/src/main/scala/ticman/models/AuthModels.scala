package ticman.models

import upickle.default.*

case class RegisterRequest(
    email: String,
    displayName: String,
    password: String,
) derives ReadWriter

case class LoginRequest(
    email: String,
    password: String,
) derives ReadWriter

case class AuthResponse(
    accessToken: String,
    refreshToken: String,
) derives ReadWriter

case class ErrorResponse(
    error: String,
    code: String,
) derives ReadWriter
