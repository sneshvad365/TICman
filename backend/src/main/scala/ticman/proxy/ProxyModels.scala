package ticman.proxy

import ticman.{RequestId, HttpMethod, ProxyUrl, RequestBody, ResponseBody, StatusCode, DurationMs, given}
import upickle.default.*

case class ProxyRequest(
    method: HttpMethod,
    url: ProxyUrl,
    headers: Map[String, String] = Map.empty,
    body: Option[RequestBody] = None,
    requestId: Option[RequestId] = None,
) derives ReadWriter

case class ProxyResponse(
    statusCode: StatusCode,
    headers: Map[String, String],
    body: ResponseBody,
    durationMs: DurationMs,
) derives ReadWriter
