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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ui.dialogs.AddAlertDialog
import com.example.ui.dialogs.ApiConfigDialog
import com.example.ui.dialogs.AssetDetailDialog
import com.example.ui.dialogs.EconomicGlossaryDialog
import com.example.ui.dialogs.NewsDetailDialog
import com.example.ui.dialogs.RomanUrduGlossaryDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LearningCenterScreen
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
            val viewModel: TradingViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            NexusTradingTheme(themeMode = uiState.themeMode) {
                NexusTradingApp(viewModel = viewModel)
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

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        viewModel.bindLearningContext(context)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val isGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!isGranted) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Trigger user messages in snackbar
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    // Predictive back handler
    BackHandler(
        enabled = uiState.selectedGlossaryTerm != null ||
                uiState.selectedEconomicTerm != null ||
                uiState.selectedLessonForDetail != null ||
                uiState.selectedAssetDetail != null ||
                uiState.selectedNewsDetail != null ||
                uiState.showAddAlertDialog ||
                uiState.selectedApiConfigForEdit != null ||
                uiState.currentTab != NavigationTab.DASHBOARD
    ) {
        when {
            uiState.selectedGlossaryTerm != null -> viewModel.closeGlossaryDialog()
            uiState.selectedEconomicTerm != null -> viewModel.dismissEconomicGlossary()
            uiState.selectedLessonForDetail != null -> viewModel.closeLessonDetail()
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
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        topBar = {
            NexusTopBar(
                isWebSocketLive = uiState.isWebSocketLive,
                btcStatus = btcStatus,
                activeAlertsCount = activeAlertsCount,
                isRefreshing = uiState.isRefreshing,
                isLowPerformanceMode = uiState.isLowPerformanceMode,
                onRefreshClick = { viewModel.refreshMarketData() },
                onAddAlertClick = { viewModel.openAddAlertDialog("BTC/USDT") },
                onOpenAnalysisClick = { viewModel.openTechnicalAnalysis("BTC") },
                onOpenLearningClick = { viewModel.selectTab(NavigationTab.LEARNING) }
            )
        },
        bottomBar = {
            BottomNavBar(
                currentTab = uiState.currentTab,
                onTabSelected = { tab -> viewModel.selectTab(tab) },
                languageMode = uiState.learningLanguageMode
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
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
                        },
                        onToggleFavorite = { id -> viewModel.toggleFavorite(id) }
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
                        onOpenTechnicalAnalysis = { symbol -> viewModel.openTechnicalAnalysis(symbol) },
                        onToggleFavorite = { id -> viewModel.toggleFavorite(id) }
                    )
                }

                NavigationTab.ANALYSIS -> {
                    TechnicalAnalysisScreen(
                        analysis = uiState.technicalAnalysis,
                        smcAnalysis = uiState.smcAnalysis,
                        candles = uiState.analysisCandles,
                        isLoading = uiState.isCalculatingAnalysis,
                        selectedSymbol = uiState.analysisAssetSymbol,
                        selectedTimeframe = uiState.analysisTimeframe,
                        selectedCategory = uiState.selectedIndicatorCategory,
                        analysisMode = uiState.analysisMode,
                        selectedSmcTab = uiState.selectedSmcTab,
                        errorMessage = uiState.analysisError,
                        onSymbolSelected = { sym -> viewModel.setAnalysisAsset(sym) },
                        onTimeframeSelected = { tf -> viewModel.setAnalysisTimeframe(tf) },
                        onCategorySelected = { cat -> viewModel.setIndicatorCategory(cat) },
                        onAnalysisModeChange = { mode -> viewModel.setAnalysisMode(mode) },
                        onSmcTabChange = { tab -> viewModel.setSmcTab(tab) },
                        onInfoClick = { term -> viewModel.openGlossaryTerm(term) },
                        onRefresh = { viewModel.loadTechnicalAnalysis() },
                        onSetAlert = { sym -> viewModel.openAddAlertDialog(sym) },
                        onBackClick = { viewModel.selectTab(NavigationTab.MARKETS) }
                    )
                }

                NavigationTab.AI_SIGNALS -> {
                    SignalsScreen(
                        currentAiSignal = uiState.currentAiSignal,
                        isAnalyzing = uiState.isAnalyzingSignal,
                        selectedSymbol = uiState.selectedSignalAssetSymbol,
                        selectedTimeframe = uiState.selectedSignalTimeframe,
                        isExpanded = uiState.isSignalExpanded,
                        onAssetSelected = { sym -> viewModel.setSignalAsset(sym) },
                        onTimeframeSelected = { tf -> viewModel.setSignalTimeframe(tf) },
                        onAnalyzeClick = { viewModel.loadAiSignal() },
                        onToggleExpanded = { viewModel.toggleSignalExpanded() },
                        onInfoClick = { term -> viewModel.openGlossaryTerm(term) },
                        aiSignalsList = uiState.aiSignalsList,
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

                NavigationTab.NEXUS_AI -> {
                    com.example.ui.screens.NexusAiScreen(
                        messages = uiState.aiMessages,
                        conversations = uiState.aiConversations,
                        currentConversationId = uiState.currentAiConversationId,
                        isLoading = uiState.isAiLoading,
                        isOnlineAiAvailable = uiState.isOnlineAiAvailable,
                        currentAssetSymbol = uiState.analysisAssetSymbol,
                        currentAssetPrice = uiState.selectedAssetDetail?.currentPrice ?: 95000.0,
                        languageMode = uiState.learningLanguageMode,
                        urduFontSize = uiState.urduFontSize,
                        onSendMessage = { query -> viewModel.sendAiMessage(query) },
                        onNewChat = { viewModel.newAiChat() },
                        onClearChat = { viewModel.clearAiChat() },
                        onDeleteConversation = { id -> viewModel.deleteAiConversation(id) },
                        onSelectConversation = { id -> viewModel.selectAiConversation(id) },
                        onAttachCurrentMarket = { viewModel.attachCurrentMarketToAi() }
                    )
                }

                NavigationTab.LEARNING -> {
                    LearningCenterScreen(
                        languageMode = uiState.learningLanguageMode,
                        selectedCategory = uiState.selectedLessonCategory,
                        searchQuery = uiState.learningSearchQuery,
                        bookmarkedLessonIds = uiState.bookmarkedLessonIds,
                        completedLessonIds = uiState.completedLessonIds,
                        recentlyViewedLessonIds = uiState.recentlyViewedLessonIds,
                        selectedLesson = uiState.selectedLessonForDetail,
                        onLanguageModeChange = { mode -> viewModel.setLearningLanguageMode(mode) },
                        onCategorySelect = { cat -> viewModel.selectLessonCategory(cat) },
                        onSearchQueryChange = { query -> viewModel.setLearningSearchQuery(query) },
                        onSelectLesson = { lesson -> viewModel.openLessonDetail(lesson) },
                        onCloseLessonDetail = { viewModel.closeLessonDetail() },
                        onToggleBookmark = { id -> viewModel.toggleLessonBookmark(id) },
                        onToggleCompleted = { id -> viewModel.toggleLessonCompleted(id) },
                        onPreviousLesson = { viewModel.openPreviousLesson() },
                        onNextLesson = { viewModel.openNextLesson() }
                    )
                }

                NavigationTab.SCANNER -> {
                    ScannerScreen(
                        scannerItems = uiState.scannerItems,
                        scannerFilter = uiState.scannerFilter,
                        onFilterSelected = { filter -> viewModel.setScannerFilter(filter) },
                        onSetAlertClick = { symbol -> viewModel.openAddAlertDialog(symbol) },
                        selectedTimeframe = uiState.scannerTimeframe,
                        onTimeframeSelected = { tf -> viewModel.setScannerTimeframe(tf) },
                        onInspectAsset = { assetId, symbol -> viewModel.inspectAssetInAnalysis(assetId, symbol) },
                        onRefreshScanner = { viewModel.refreshScanner() },
                        futuresScannerState = uiState.futuresScannerState,
                        futuresWatchlist = uiState.futuresWatchlist,
                        scannerScreenMode = uiState.scannerScreenMode,
                        onModeSelected = { mode -> viewModel.setScannerScreenMode(mode) },
                        onStartFuturesScanner = { viewModel.startFuturesScanner(context) },
                        onStopFuturesScanner = { viewModel.stopFuturesScanner(context) },
                        onToggleFuturesWatchlist = { symbol, _ -> viewModel.toggleFuturesWatchlistPair(symbol) },
                        onAddCustomFuturesPair = { raw -> viewModel.addCustomFuturesPair(raw) },
                        onRemoveFuturesPair = { symbol -> viewModel.removeFuturesWatchlistPair(symbol) },
                        onIntervalSelected = { sec -> viewModel.setFuturesScannerInterval(sec) },
                        onTogglePaperTrading = { enabled -> viewModel.setFuturesScannerPaperTrading(enabled) },
                        onExplainSignalWithAi = { signal -> viewModel.explainFuturesSignalWithAi(signal) },
                        onSimulatePaperTrade = { signal -> viewModel.simulateFuturesPaperTrade(signal) }
                    )
                }

                NavigationTab.NEWS -> {
                    NewsScreen(
                        news = uiState.news,
                        newsFilter = uiState.newsFilter,
                        onFilterSelected = { filter -> viewModel.setNewsFilter(filter) },
                        onNewsClick = { newsItem -> viewModel.openNewsDetail(newsItem) },
                        onToggleBookmark = { id -> viewModel.toggleNewsBookmark(id) },
                        onRefreshNews = { viewModel.refreshNews() },
                        onOpenGlossaryTerm = { term -> viewModel.openEconomicGlossary(term) }
                    )
                }

                NavigationTab.SETTINGS -> {
                    val settingsState = com.example.data.settings.AppSettingsState(
                        themeMode = uiState.themeMode,
                        languageMode = uiState.learningLanguageMode,
                        urduFontSize = uiState.urduFontSize,
                        isDataSavingMode = uiState.isDataSavingMode,
                        isLowPerformanceMode = uiState.isLowPerformanceMode,
                        defaultTimeframe = uiState.defaultChartTimeframe,
                        defaultWatchlistCategory = uiState.defaultWatchlistCategory,
                        alertNotificationsEnabled = uiState.alertNotificationsEnabled,
                        soundEffectsEnabled = uiState.soundEffectsEnabled,
                        refreshRate = uiState.refreshRate,
                        lastCacheClearedTimestamp = uiState.lastCacheClearedTimestamp,
                        lastMarketRefreshTimestamp = uiState.lastMarketDataRefreshTimestamp
                    )

                    SettingsScreen(
                        settingsState = settingsState,
                        apiConfigs = uiState.apiConfigs,
                        onThemeSelect = { mode -> viewModel.setThemeMode(mode) },
                        onLanguageSelect = { lang -> viewModel.setAppLanguageMode(lang) },
                        onDataSavingToggle = { enabled -> viewModel.setDataSavingMode(enabled) },
                        onLowPerformanceToggle = { enabled -> viewModel.setLowPerformanceMode(enabled) },
                        onDefaultTimeframeSelect = { tf -> viewModel.setDefaultChartTimeframe(tf) },
                        onDefaultWatchlistSelect = { cat -> viewModel.setDefaultWatchlistCategory(cat) },
                        onRefreshRateSelect = { rate -> viewModel.setRefreshRate(rate) },
                        onAlertsToggle = { enabled -> viewModel.setAlertNotificationsEnabled(enabled) },
                        onSoundEffectsToggle = { enabled -> viewModel.setSoundEffectsEnabled(enabled) },
                        onClearCacheClick = { viewModel.clearTemporaryCache() },
                        onConfigureFeed = { config -> viewModel.openApiConfigDialog(config) },
                        onRefreshFeeds = { viewModel.refreshMarketData() },
                        onUrduFontSizeSelect = { size -> viewModel.setUrduFontSize(size) }
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
            },
            onToggleFavorite = { viewModel.toggleFavorite(asset.id) }
        )
    }

    uiState.selectedNewsDetail?.let { news ->
        NewsDetailDialog(
            news = news,
            onDismiss = { viewModel.closeNewsDetail() },
            onToggleBookmark = { viewModel.toggleNewsBookmark(news.id) },
            onOpenGlossaryTerm = { term -> viewModel.openEconomicGlossary(term) }
        )
    }

    uiState.selectedGlossaryTerm?.let { item ->
        RomanUrduGlossaryDialog(
            item = item,
            initialLanguageMode = uiState.learningLanguageMode,
            onOpenFullLesson = { lessonId -> viewModel.navigateToLessonInAcademy(lessonId) },
            onDismiss = { viewModel.closeGlossaryDialog() }
        )
    }

    uiState.selectedEconomicTerm?.let { term ->
        EconomicGlossaryDialog(
            term = term,
            currentLanguageMode = uiState.learningLanguageMode,
            onDismiss = { viewModel.dismissEconomicGlossary() }
        )
    }
}
