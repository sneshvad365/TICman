package ticman

import upickle.default.{ReadWriter, readwriter}
import java.util.UUID

given uuidReadWriter: ReadWriter[UUID] = readwriter[String].bimap(_.toString, UUID.fromString)

trait AutoOpaque[A, B](using rw: ReadWriter[B]):
  given ReadWriter[A] = rw.bimap(a => a.asInstanceOf[B], b => b.asInstanceOf[A])
  def apply(b: B): A = b.asInstanceOf[A]

opaque type WorkspaceId <: UUID = UUID
object WorkspaceId extends AutoOpaque[WorkspaceId, UUID]

opaque type CollectionId <: UUID = UUID
object CollectionId extends AutoOpaque[CollectionId, UUID]

opaque type RequestId <: UUID = UUID
object RequestId extends AutoOpaque[RequestId, UUID]

opaque type HistoryId <: UUID = UUID
object HistoryId extends AutoOpaque[HistoryId, UUID]

opaque type WorkspaceName <: String = String
object WorkspaceName extends AutoOpaque[WorkspaceName, String]

opaque type CollectionName <: String = String
object CollectionName extends AutoOpaque[CollectionName, String]

opaque type CollectionReadme <: String = String
object CollectionReadme extends AutoOpaque[CollectionReadme, String]

opaque type RequestName <: String = String
object RequestName extends AutoOpaque[RequestName, String]

opaque type HttpMethod <: String = String
object HttpMethod extends AutoOpaque[HttpMethod, String]

opaque type UrlTemplate <: String = String
object UrlTemplate extends AutoOpaque[UrlTemplate, String]

opaque type RequestBody <: String = String
object RequestBody extends AutoOpaque[RequestBody, String]

opaque type ResponseBody <: String = String
object ResponseBody extends AutoOpaque[ResponseBody, String]

opaque type ProxyUrl <: String = String
object ProxyUrl extends AutoOpaque[ProxyUrl, String]

opaque type StatusCode <: Int = Int
object StatusCode extends AutoOpaque[StatusCode, Int]

opaque type DurationMs <: Long = Long
object DurationMs extends AutoOpaque[DurationMs, Long]

opaque type ExecutedAt <: String = String
object ExecutedAt extends AutoOpaque[ExecutedAt, String]

opaque type ErrorMessage <: String = String
object ErrorMessage extends AutoOpaque[ErrorMessage, String]

opaque type ErrorCode <: String = String
object ErrorCode extends AutoOpaque[ErrorCode, String]
