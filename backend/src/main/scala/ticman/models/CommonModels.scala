package ticman.models

import upickle.default.*

case class ErrorResponse(error: String, code: String) derives ReadWriter
