package ticman

import ticman.db.*
import ticman.services.*
import ticman.routes.*

object Main extends cask.Main:

  private val dataSource = Database.fromEnv()

  private val userRepo       = PostgresUserRepository(dataSource)
  private val workspaceRepo  = PostgresWorkspaceRepository(dataSource)
  private val collectionRepo = PostgresCollectionRepository(dataSource)
  private val requestRepo    = PostgresRequestRepository(dataSource)
  private val historyRepo    = PostgresResponseHistoryRepository(dataSource)

  private val jwtService = JwtServiceImpl(
    accessSecret  = sys.env.getOrElse("JWT_SECRET", "change-me-in-production"),
    refreshSecret = sys.env.getOrElse("JWT_REFRESH_SECRET", "change-me-refresh-in-production"),
  )

  private val authService       = AuthServiceImpl(userRepo, jwtService)
  private val workspaceService  = WorkspaceServiceImpl(workspaceRepo)
  private val collectionService = CollectionServiceImpl(collectionRepo, workspaceRepo)
  private val requestService    = RequestServiceImpl(requestRepo, collectionRepo, workspaceRepo)
  private val proxyService      = ProxyService(historyRepo)

  override val allRoutes: Seq[cask.Routes] = Seq(
    AuthRoutes(authService),
    WorkspaceRoutes(workspaceService, jwtService),
    CollectionRoutes(collectionService, jwtService),
    RequestRoutes(requestService, jwtService),
    ProxyRoutes(proxyService, jwtService),
    HistoryRoutes(historyRepo, jwtService),
  )
