package ticman

import ticman.db.Database
import ticman.workspace.*
import ticman.collection.*
import ticman.request.*
import ticman.proxy.*

object Main extends cask.Main:

  private val db = Database.fromEnv()

  private val workspaceRepo  = PostgresWorkspaceRepository(db)
  private val collectionRepo = PostgresCollectionRepository(db)
  private val requestRepo    = PostgresRequestRepository(db)
  private val historyRepo    = PostgresResponseHistoryRepository(db)

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
