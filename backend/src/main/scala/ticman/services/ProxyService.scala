package ticman.services

import ticman.db.ResponseHistoryRepository
import ticman.models.{ProxyRequest, ProxyResponse}
import upickle.default.*
import java.net.URI
import java.util.UUID

class ProxyService(historyRepo: ResponseHistoryRepository):

  private val TimeoutMs = 30_000

  // SSRF protection — block private/loopback ranges
  private val blockedHosts = List(
    "^localhost$".r,
    "^127\\.".r,
    "^10\\.".r,
    "^172\\.(1[6-9]|2[0-9]|3[01])\\.".r,
    "^192\\.168\\.".r,
    "^0\\.0\\.0\\.0$".r,
    "^::1$".r,
    "^\\[::1\\]$".r,
  )

  def send(req: ProxyRequest): Either[String, ProxyResponse] =
    val host =
      try URI(req.url).getHost
      catch case _ => return Left(s"Invalid URL: ${req.url}")

    if blockedHosts.exists(_.findFirstIn(host.toLowerCase).isDefined) then
      return Left("Requests to private/internal addresses are not allowed")

    val start = System.currentTimeMillis()
    try
      val response = requests.send(req.method.toUpperCase)(
        url            = req.url,
        headers        = req.headers.toSeq,
        data           = req.body.getOrElse(""),
        check          = false,
        connectTimeout = TimeoutMs,
        readTimeout    = TimeoutMs,
      )
      val duration    = System.currentTimeMillis() - start
      val respHeaders = response.headers.view.mapValues(_.mkString(", ")).toMap
      val body        = response.text()
      val bodyOpt     = if body.isEmpty then None else Some(body)

      // Save to history if a requestId was provided
      req.requestId.foreach { rid =>
        try
          historyRepo.save(UUID.fromString(rid), response.statusCode, respHeaders, bodyOpt, duration)
        catch case _ => () // don't fail the proxy call if history save fails
      }

      Right(ProxyResponse(response.statusCode, respHeaders, body, duration))

    catch case e: Exception =>
      Left(s"Request failed: ${e.getMessage}")
