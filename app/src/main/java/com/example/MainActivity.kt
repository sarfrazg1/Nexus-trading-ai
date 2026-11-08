package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.ConnectionStatus
import com.example.data.model.NavigationTab
import com.example.ui.components.BottomNavBar
import com.example.ui.components.NexusTopBar
import com.example.ui.dialogs.AddAlertDialog
import com.example.ui.dialogs.ApiConfigDialog
import com.example.ui.dialogs.AssetDetailDialog
import com.example.ui.dialogs.NewsDetailDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MarketsScreen
import com.example.ui.screens.NewsScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SignalsScreen
import com.example.ui.screens.TechnicalAnalysisScreen
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NexusTradingTheme
import com.example.ui.viewmodel.TradingViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NexusTradingTheme {
                NexusTradingApp()
            }
        }
    }
}

@Composable
fun NexusTradingApp(
    viewModel: TradingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Trigger user messages in snackbar
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    // Predictive back handler
    BackHandler(
        enabled = uiState.selectedAssetDetail != null ||
                uiState.selectedNewsDetail != null ||
                uiState.showAddAlertDialog ||
                uiState.selectedApiConfigForEdit != null ||
                uiState.currentTab != NavigationTab.DASHBOARD
    ) {
        when {
            uiState.selectedAssetDetail != null -> viewModel.closeAssetDetail()
            uiState.selectedNewsDetail != null -> viewModel.closeNewsDetail()
            uiState.showAddAlertDialog -> viewModel.closeAddAlertDialog()
            uiState.selectedApiConfigForEdit != null -> viewModel.closeApiConfigDialog()
            uiState.currentTab != NavigationTab.DASHBOARD -> viewModel.selectTab(NavigationTab.DASHBOARD)
        }
    }

    val btcStatus = uiState.assets.firstOrNull { it.id == "BTC" }?.dataStatus ?: com.example.data.model.MarketDataStatus.UNAVAILABLE
    val activeAlertsCount = uiState.alerts.count { it.isActive && !it.isTriggered }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = NavyBackground,
        topBar = {
            NexusTopBar(
                isWebSocketLive = uiState.isWebSocketLive,
                btcStatus = btcStatus,
                activeAlertsCount = activeAlertsCount,
                isRefreshing = uiState.isRefreshing,
                onRefreshClick = { viewModel.refreshMarketData() },
                onAddAlertClick = { viewModel.openAddAlertDialog("BTC/USDT") },
                onOpenAnalysisClick = { viewModel.openTechnicalAnalysis("BTC") }
            )
        },
        bottomBar = {
            BottomNavBar(
                currentTab = uiState.currentTab,
                onTabSelected = { tab -> viewModel.selectTab(tab) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(NavyBackground)
        ) {
            when (uiState.currentTab) {
                NavigationTab.DASHBOARD -> {
                    DashboardScreen(
                        assets = uiState.assets,
                        overview = uiState.marketOverview,
                        signals = uiState.signals,
                        alerts = uiState.alerts,
                        apiConfigs = uiState.apiConfigs,
                        onAssetClick = { asset -> viewModel.openAssetDetail(asset) },
                        onConnectFeedClick = { asset ->
                            val feed = uiState.apiConfigs.firstOrNull {
                                if (asset.id == "XAU" || asset.id == "XAG") it.id == "commodities_feed"
                                else if (asset.id == "OIL") it.id == "oil_energy_feed"
                                else it.id == "binance_public"
                            } ?: uiState.apiConfigs.first()
                            viewModel.openApiConfigDialog(feed)
                        },
                        onSetAlertClick = { symbol -> viewModel.openAddAlertDialog(symbol) },
                        onToggleAlert = { id -> viewModel.toggleAlert(id) },
                        onDeleteAlert = { id -> viewModel.deleteAlert(id) },
                        onViewAllSignals = { viewModel.selectTab(NavigationTab.AI_SIGNALS) },
                        onNavigateTab = { tab -> viewModel.selectTab(tab) },
                        onCopySignalLevels = { signal ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText(
                                "Nexus Signal",
                                "${signal.assetSymbol} ${signal.type.label}\nEntry: ${signal.entryPrice}\nTarget: ${signal.targetPrice}\nStop: ${signal.stopLoss}"
                            )
                            clipboard.setPrimaryClip(clip)
                            viewModel.createAlert(signal.assetSymbol, signal.assetName, 0.0, com.example.data.model.AlertDirection.ABOVE, "Copied signal levels")
                        }
                    )
                }

                NavigationTab.MARKETS -> {
                    MarketsScreen(
                        assets = uiState.assets,
                        selectedCategory = uiState.selectedCategory,
                        statusFilter = uiState.statusFilter,
                        searchQuery = uiState.searchQuery,
                        onCategorySelected = { cat -> viewModel.setCategory(cat) },
                        onStatusFilterSelected = { stat -> viewModel.setStatusFilter(stat) },
                        onSearchQueryChange = { query -> viewModel.setSearchQuery(query) },
                        onAssetClick = { asset -> viewModel.openAssetDetail(asset) },
                        onConnectFeedClick = { asset ->
                            val feed = uiState.apiConfigs.firstOrNull {
                                if (asset.id == "XAU" || asset.id == "XAG") it.id == "commodities_feed"
                                else if (asset.id == "WTI" || asset.id == "BRENT" || asset.id == "NATGAS") it.id == "commodities_feed"
                                else it.id == "binance_public"
                            } ?: uiState.apiConfigs.first()
                            viewModel.openApiConfigDialog(feed)
                        },
                        onSetAlertClick = { symbol -> viewModel.openAddAlertDialog(symbol) },
                        onOpenTechnicalAnalysis = { symbol -> viewModel.openTechnicalAnalysis(symbol) }
                    )
                }

                NavigationTab.ANALYSIS -> {
                    TechnicalAnalysisScreen(
                        analysis = uiState.technicalAnalysis,
                        candles = uiState.analysisCandles,
                        isLoading = uiState.isCalculatingAnalysis,
                        selectedSymbol = uiState.analysisAssetSymbol,
                        selectedTimeframe = uiState.analysisTimeframe,
                        selectedCategory = uiState.selectedIndicatorCategory,
                        errorMessage = uiState.analysisError,
                        onSymbolSelected = { sym -> viewModel.setAnalysisAsset(sym) },
                        onTimeframeSelected = { tf -> viewModel.setAnalysisTimeframe(tf) },
                        onCategorySelected = { cat -> viewModel.setIndicatorCategory(cat) },
                        onRefresh = { viewModel.loadTechnicalAnalysis() },
                        onSetAlert = { sym -> viewModel.openAddAlertDialog(sym) },
                        onBackClick = { viewModel.selectTab(NavigationTab.MARKETS) }
                    )
                }

                NavigationTab.AI_SIGNALS -> {
                    SignalsScreen(
                        signals = uiState.signals,
                        signalFilter = uiState.signalFilter,
                        onFilterSelected = { filter -> viewModel.setSignalFilter(filter) },
                        onSetAlertClick = { symbol -> viewModel.openAddAlertDialog(symbol) },
                        onCopyLevelsClick = { signal ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText(
                                "Nexus Signal",
                                "${signal.assetSymbol} ${signal.type.label}\nEntry: ${signal.entryPrice}\nTarget: ${signal.targetPrice}\nStop: ${signal.stopLoss}"
                            )
                            clipboard.setPrimaryClip(clip)
                        }
                    )
                }

                NavigationTab.SCANNER -> {
                    ScannerScreen(
                        scannerItems = uiState.scannerItems,
                        scannerFilter = uiState.scannerFilter,
                        onFilterSelected = { filter -> viewModel.setScannerFilter(filter) },
                        onSetAlertClick = { symbol -> viewModel.openAddAlertDialog(symbol) }
                    )
                }

                NavigationTab.NEWS -> {
                    NewsScreen(
                        news = uiState.news,
                        newsFilter = uiState.newsFilter,
                        onFilterSelected = { filter -> viewModel.setNewsFilter(filter) },
                        onNewsClick = { newsItem -> viewModel.openNewsDetail(newsItem) },
                        onToggleBookmark = { id -> viewModel.toggleNewsBookmark(id) }
                    )
                }

                NavigationTab.SETTINGS -> {
                    SettingsScreen(
                        apiConfigs = uiState.apiConfigs,
                        onConfigureFeed = { config -> viewModel.openApiConfigDialog(config) },
                        onRefreshFeeds = { viewModel.refreshMarketData() }
                    )
                }
            }
        }
    }

    // Modal Dialogs
    if (uiState.showAddAlertDialog) {
        AddAlertDialog(
            defaultSymbol = uiState.defaultAlertAssetSymbol,
            onDismiss = { viewModel.closeAddAlertDialog() },
            onConfirm = { symbol, name, price, direction, note ->
                viewModel.createAlert(symbol, name, price, direction, note)
            }
        )
    }

    uiState.selectedApiConfigForEdit?.let { feedConfig ->
        ApiConfigDialog(
            config = feedConfig,
            onDismiss = { viewModel.closeApiConfigDialog() },
            onConnect = { key -> viewModel.connectApiFeed(feedConfig.id, key) },
            onDisconnect = { viewModel.disconnectApiFeed(feedConfig.id) }
        )
    }

    uiState.selectedAssetDetail?.let { asset ->
        AssetDetailDialog(
            asset = asset,
            onDismiss = { viewModel.closeAssetDetail() },
            onSetAlert = { symbol -> viewModel.openAddAlertDialog(symbol) },
            onConfigureFeed = {
                viewModel.closeAssetDetail()
                val feed = uiState.apiConfigs.firstOrNull {
                    if (asset.id == "XAU" || asset.id == "XAG") it.id == "commodities_feed"
                    else if (asset.id == "OIL") it.id == "oil_energy_feed"
                    else it.id == "binance_public"
                } ?: uiState.apiConfigs.first()
                viewModel.openApiConfigDialog(feed)
            },
            onOpenTechnicalAnalysis = { symbol ->
                viewModel.openTechnicalAnalysis(symbol)
            }
        )
    }

    uiState.selectedNewsDetail?.let { news ->
        NewsDetailDialog(
            news = news,
            onDismiss = { viewModel.closeNewsDetail() },
            onToggleBookmark = { viewModel.toggleNewsBookmark(news.id) }
        )
    }
}
