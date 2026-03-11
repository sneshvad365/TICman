package ticman

import upickle.default.*

case class ErrorResponse(error: ErrorMessage, code: ErrorCode) derives ReadWriter
