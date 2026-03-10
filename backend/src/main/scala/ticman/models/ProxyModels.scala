package ticman.models

import upickle.default.*

case class ProxyRequest(
    method: String,
    url: String,
    headers: Map[String, String] = Map.empty,
    body: Option[String] = None,
    requestId: Option[String] = None,
) derives ReadWriter

case class ProxyResponse(
    statusCode: Int,
    headers: Map[String, String],
    body: String,
    durationMs: Long,
) derives ReadWriter

case class HistoryResponse(
    id: String,
    statusCode: Int,
    headers: Map[String, String],
    body: Option[String],
    durationMs: Long,
    executedAt: String,
) derives ReadWriter
