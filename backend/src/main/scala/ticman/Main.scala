package ticman

import ticman.db.*
import ticman.services.*
import ticman.routes.*

object Main extends cask.Main:

  private val dataSource = Database.fromEnv()

  private val workspaceRepo  = PostgresWorkspaceRepository(dataSource)
  private val collectionRepo = PostgresCollectionRepository(dataSource)
  private val requestRepo    = PostgresRequestRepository(dataSource)
  private val historyRepo    = PostgresResponseHistoryRepository(dataSource)

  private val workspaceService  = WorkspaceServiceImpl(workspaceRepo)
  private val collectionService = CollectionServiceImpl(collectionRepo)
  private val requestService    = RequestServiceImpl(requestRepo)
  private val proxyService      = ProxyService(historyRepo)

  override val allRoutes: Seq[cask.Routes] = Seq(
    WorkspaceRoutes(workspaceService),
    CollectionRoutes(collectionService),
    RequestRoutes(requestService),
    ProxyRoutes(proxyService),
    HistoryRoutes(historyRepo),
  )
