# TICman Backend — Coding Style

## Language & Build

- **Scala 3** with `sbt`. Run `sbt compile` to verify; `sbt run` to start on port 8080.
- No Scala 2 syntax: use `end` markers only where needed, prefer `given`/`using` over `implicit`.

---

## Package Layout — Feature-Based

Each domain owns all its files in one folder. No shared `models/`, `services/`, or `routes/` layers.

```
ticman/
├── OpaqueTypes.scala        ← all opaque type defs + AutoOpaque trait
├── CommonModels.scala       ← ErrorResponse only
├── Main.scala
├── db/
│   ├── Database.scala       ← HikariCP + ScalaSql client (shared infra)
│   └── TypeMappers.scala    ← custom TypeMapper givens
├── workspace/
│   ├── WorkspaceModels.scala
│   ├── WorkspaceRepository.scala
│   ├── WorkspaceService.scala
│   └── WorkspaceRoutes.scala
├── collection/   (same pattern)
├── request/      (same pattern + ResponseHistoryRepository, HistoryRoutes)
└── proxy/        (same pattern)
```

Package names match folder names: `ticman.workspace`, `ticman.collection`, etc.

---

## Opaque Types

Every meaningful field in a domain model uses an opaque type — no bare `String`, `Int`, `Long`, or `UUID` in case class fields.

### Definition pattern (`OpaqueTypes.scala`)

```scala
opaque type WorkspaceId <: UUID = UUID
object WorkspaceId extends AutoOpaque[WorkspaceId, UUID]
```

`AutoOpaque` provides:
- `WorkspaceId(uuid)` — smart constructor
- `given ReadWriter[WorkspaceId]` — for uPickle serialization

### Rules

- **ScalaSql `*Row[T[_]]` classes keep primitive types** (`UUID`, `String`, `Int`, `Long`) — ScalaSql needs raw types for its TypeMapper resolution.
- **Domain models use opaque types** everywhere: `case class Workspace(id: WorkspaceId, name: WorkspaceName)`.
- `toDomain` converters in the `Row` companion object wrap primitives: `WorkspaceId(row.id)`, `WorkspaceName(row.name)`.
- **Opaque types are subtypes of their underlying type** (`WorkspaceId <: UUID`), so they pass directly to ScalaSql predicates and `columns(_.col := opaqueVal)` without unwrapping.
- When calling `.trim` or other `String` methods on an opaque `String` subtype, re-wrap the result: `WorkspaceName(req.name.trim)`.
- Never call `.toString` on an ID to pass it to another layer — pass the opaque type directly.

### UUID serialization

`uuidReadWriter` is defined at the top of `OpaqueTypes.scala` in the `ticman` package:
```scala
given uuidReadWriter: ReadWriter[UUID] = readwriter[String].bimap(_.toString, UUID.fromString)
```
Import with `import ticman.{..., given}` in files that need it.

---

## Database — ScalaSql

Library: `scalasql` (lihaoyi), version 0.1.19.

### Client setup

```scala
DbClient.DataSource(
  dataSource = hikariDs,
  config = new Config { override def columnNameMapper(v: String) = camelToSnake(v) },
  listeners = Seq.empty,
)
```

`Config` is a **trait** — instantiate with `new Config { override def ... }`.
The `columnNameMapper` converts camelCase Scala field names to snake_case DB column names automatically.

### Query patterns

```scala
// SELECT
db.transaction { implicit tx => tx.run(WorkspaceRow.select) }
db.transaction { implicit tx => tx.run(SomeRow.select.filter(_.id === id)) }

// INSERT RETURNING
val id = tx.run(WorkspaceRow.insert.columns(_.name := name).returning(_.id)).head

// UPDATE — multiple `.set()` calls chained
tx.run(RequestRow.update(_.id === id).set(_.name := n).set(_.method := m))

// DELETE
tx.run(WorkspaceRow.delete(_.id === id))
```

- Every query runs inside `db.transaction { implicit tx => tx.run(...) }`. There is no bare `db.run(...)`.
- `columns(...)` and `update(pred)` take **separate lambda arguments**, not a tuple.
- ScalaSql has no `UPDATE RETURNING` — fetch the row first, then update, then reconstruct the domain object manually.

### Custom TypeMappers

Only define TypeMappers for types ScalaSql can't handle natively. UUID is handled by `PostgresDialect` — **do not redefine it**.

`TypeMappers.scala` currently has one custom mapper:
- `Map[String, String]` → PostgreSQL `jsonb` via `PGobject` + uPickle round-trip.

Import with `import ticman.db.TypeMappers.given` in every file that queries tables with `jsonb` columns.

---

## Routes (Cask)

- Routes are thin — no business logic. Delegate everything to a service.
- One `class *Routes(val service: *Service) extends cask.Routes` per domain.
- Call `initialize()` at the end of every `Routes` class.
- Always return `Response[String]` with an explicit `Content-Type: application/json` header.
- Use `scala.util.Try` / `match Success / Failure` — never `try/catch` blocks in route handlers.
- Error envelope: `ErrorResponse(ErrorMessage(e.getMessage), ErrorCode("INTERNAL_ERROR"))`.
- HTTP status conventions: `200` list/update, `201` create, `204` delete (empty body), `400` bad request, `500` internal error.
- Path param strings are parsed and wrapped into opaque types at the route layer:
  ```scala
  WorkspaceId(UUID.fromString(id))
  ```

---

## Services

- Traits define the public interface; `*ServiceImpl` classes hold the implementation.
- Services accept and return opaque-typed domain objects — no primitives leak across the boundary.
- Services do not call `.toString` on IDs. Pass opaque types directly to response case classes.
- Input sanitisation (e.g. `.trim`) belongs in the service, not the route or repo.

---

## Repositories

- Traits define the public interface; `Postgres*Repository` classes hold the implementation.
- Trait signatures use opaque types matching the service layer.
- Opaque types that are subtypes of `UUID` or `String` can be passed directly to ScalaSql predicates — no explicit unwrapping needed.

---

## Serialization (uPickle)

- Every model that crosses the HTTP boundary has `derives ReadWriter`.
- `ReadWriter` for opaque types is provided by their companion object (via `AutoOpaque`) — just import `given` from `ticman`.
- `Map[String, String]` fields in request/response models are plain — uPickle handles them natively; the custom TypeMapper is only for the DB layer.

---

## Error Handling

- Routes use `scala.util.Try`:
  ```scala
  Try(service.doSomething(args)) match
    case Success(v) => Response(write(v), 200, headers = jsonHeader)
    case Failure(e) => Response(write(ErrorResponse(ErrorMessage(e.getMessage), ErrorCode("INTERNAL_ERROR"))), 500, headers = jsonHeader)
  ```
- Services and repositories let exceptions propagate — the route catches them.
- Proxy-specific errors use `ErrorCode("PROXY_ERROR")` with status `400`.
- History save failures in `ProxyService` are swallowed silently so they don't fail the proxy call.

---

## SSRF Protection

`ProxyService` blocks requests to private/loopback IP ranges. Any new proxy-adjacent feature must preserve this check. Do not add bypass flags.

---

## Dependency Wiring

All wiring is done manually in `Main.scala` — no DI framework. Instantiation order: repos → services → routes.

---

## What NOT to Do

- Don't use bare `String`, `Int`, `Long`, or `UUID` as domain model field types — always use opaque types.
- Don't call `.toString` on IDs when passing them to services, repos, or responses.
- Don't put `try/catch` blocks in route handlers — use `scala.util.Try`.
- Don't put business logic in routes — delegate to services.
- Don't write raw SQL strings — all queries go through ScalaSql.
- Don't redefine a `TypeMapper[UUID]` — `PostgresDialect` already provides one.
- Don't use `db.run(...)` directly — always wrap in `db.transaction { implicit tx => tx.run(...) }`.
