package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.indicators.IndicatorCategory
import com.example.data.indicators.OverallAnalysisResult
import com.example.data.model.AlertDirection
import com.example.data.model.ApiFeedConfig
import com.example.data.model.AssetCategory
import com.example.data.model.Candle
import com.example.data.model.CandleTimeframe
import com.example.data.model.MarketAlert
import com.example.data.model.MarketAsset
import com.example.data.model.MarketDataStatus
import com.example.data.model.MarketNews
import com.example.data.model.MarketOverview
import com.example.data.model.NavigationTab
import com.example.data.model.ScannerItem
import com.example.data.model.Signal
import com.example.data.model.Timeframe
import com.example.data.repository.TradingRepository
import com.example.data.smc.RomanUrduSMCGlossary
import com.example.data.smc.SmcAnalysisResult
import com.example.data.smc.SmcGlossaryItem
import com.example.data.signal.AiTradingSignal
import com.example.data.signal.RomanUrduSignalGlossary
import com.example.data.signal.SignalDecision
import com.example.data.learning.LearningContentDatabase
import com.example.data.learning.LearningLanguageMode
import com.example.data.learning.LearningPreferencesRepository
import com.example.data.learning.LessonCategory
import com.example.data.learning.LessonItem
import com.example.data.settings.AppSettingsRepository
import com.example.data.settings.AppThemeMode
import com.example.data.settings.RefreshRatePreference
import android.content.Context
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AnalysisMode(val title: String) {
    SMC_FIBONACCI("Smart Money & Fib"),
    INDICATORS("22 Indicators")
}

enum class SmcCategoryTab(val label: String) {
    ALL("All SMC"),
    STRUCTURE("Structure"),
    ORDER_BLOCKS("Order Blocks"),
    FVG("Fair Value Gaps"),
    LIQUIDITY("Liquidity & Stops"),
    FIBONACCI_SR("Fib & S/R"),
    MULTI_TIMEFRAME("Multi-Timeframe")
}

data class TradingUiState(
    val currentTab: NavigationTab = NavigationTab.DASHBOARD,
    val assets: List<MarketAsset> = emptyList(),
    val marketOverview: MarketOverview = MarketOverview(),
    val signals: List<Signal> = emptyList(),
    val alerts: List<MarketAlert> = emptyList(),
    val news: List<MarketNews> = emptyList(),
    val scannerItems: List<ScannerItem> = emptyList(),
    val apiConfigs: List<ApiFeedConfig> = emptyList(),
    val isRefreshing: Boolean = false,
    val isWebSocketLive: Boolean = false,
    val selectedTimeframe: Timeframe = Timeframe._24H,
    val selectedCategory: AssetCategory = AssetCategory.ALL,
    val statusFilter: String = "ALL",
    val searchQuery: String = "",
    val signalFilter: String = "ALL",
    val scannerFilter: String = "ALL",
    val newsFilter: String = "ALL",
    val selectedAssetId: String? = null,
    val selectedNewsDetail: MarketNews? = null,
    val showAddAlertDialog: Boolean = false,
    val defaultAlertAssetSymbol: String = "BTC/USDT",
    val selectedApiConfigForEdit: ApiFeedConfig? = null,
    val userMessage: String? = null,

    // Technical Analysis State
    val analysisMode: AnalysisMode = AnalysisMode.SMC_FIBONACCI,
    val technicalAnalysis: OverallAnalysisResult? = null,
    val smcAnalysis: SmcAnalysisResult? = null,
    val selectedSmcTab: SmcCategoryTab = SmcCategoryTab.ALL,
    val selectedGlossaryTerm: SmcGlossaryItem? = null,
    val isCalculatingAnalysis: Boolean = false,
    val analysisAssetSymbol: String = "BTC",
    val analysisTimeframe: CandleTimeframe = CandleTimeframe.H1,
    val selectedIndicatorCategory: IndicatorCategory = IndicatorCategory.ALL,
    val analysisCandles: List<Candle> = emptyList(),
    val analysisError: String? = null,

    // AI Signal Engine State
    val currentAiSignal: AiTradingSignal? = null,
    val aiSignalsList: List<AiTradingSignal> = emptyList(),
    val isAnalyzingSignal: Boolean = false,
    val selectedSignalAssetSymbol: String = "BTC",
    val selectedSignalTimeframe: CandleTimeframe = CandleTimeframe.H1,
    val isSignalExpanded: Boolean = false,
    val signalAnalysisError: String? = null,

    // Learning Center & Academy State
    val learningLanguageMode: LearningLanguageMode = LearningLanguageMode.BILINGUAL,
    val selectedLessonCategory: LessonCategory? = null,
    val learningSearchQuery: String = "",
    val bookmarkedLessonIds: Set<String> = emptySet(),
    val completedLessonIds: Set<String> = emptySet(),
    val recentlyViewedLessonIds: List<String> = emptyList(),
    val selectedLessonForDetail: LessonItem? = null,

    // Task 7: Theme, Settings & Performance Controls
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val isDataSavingMode: Boolean = false,
    val isLowPerformanceMode: Boolean = false,
    val defaultChartTimeframe: CandleTimeframe = CandleTimeframe.H1,
    val defaultWatchlistCategory: AssetCategory = AssetCategory.ALL,
    val alertNotificationsEnabled: Boolean = true,
    val soundEffectsEnabled: Boolean = false,
    val refreshRate: RefreshRatePreference = RefreshRatePreference.FAST_15S,
    val lastCacheClearedTimestamp: Long? = null,
    val lastMarketDataRefreshTimestamp: Long = 0L,

    // Task 8: News Analyzer, Scanner & Notification States
    val selectedEconomicTerm: com.example.data.news.EconomicTerm? = null,
    val scannerTimeframe: CandleTimeframe = CandleTimeframe.H1,
    val priceAlertsEnabled: Boolean = true,
    val longSignalsEnabled: Boolean = true,
    val shortSignalsEnabled: Boolean = true,
    val newsAlertsEnabled: Boolean = true,
    val macroEventsEnabled: Boolean = true,
    val watchlistUpdatesEnabled: Boolean = true,

    // Task 10: NEXUS AI Hybrid Trading Assistant
    val aiMessages: List<com.example.data.ai.NexusChatMessage> = emptyList(),
    val aiConversations: List<com.example.data.ai.NexusConversation> = emptyList(),
    val currentAiConversationId: String = "default_session",
    val isAiLoading: Boolean = false,
    val isOnlineAiAvailable: Boolean = false,
    val urduFontSize: com.example.data.settings.UrduFontSize = com.example.data.settings.UrduFontSize.MEDIUM,

    // Binance USDT-M Futures Auto-Scanner State
    val futuresScannerState: com.example.data.scanner.ScannerEngineState = com.example.data.scanner.ScannerEngineState(),
    val futuresWatchlist: List<com.example.data.scanner.FuturesWatchlistPair> = emptyList(),
    val scannerScreenMode: Int = 0
) {
    val selectedAssetDetail: MarketAsset?
        get() = selectedAssetId?.let { id -> assets.firstOrNull { it.id == id } }
}

class TradingViewModel(
    private val repository: TradingRepository = TradingRepository()
) : ViewModel() {

    private val _currentTab = MutableStateFlow(NavigationTab.DASHBOARD)
    private val _selectedTimeframe = MutableStateFlow(Timeframe._24H)
    private val _selectedCategory = MutableStateFlow(AssetCategory.ALL)
    private val _statusFilter = MutableStateFlow("ALL")
    private val _searchQuery = MutableStateFlow("")
    private val _signalFilter = MutableStateFlow("ALL")
    private val _scannerFilter = MutableStateFlow("ALL")
    private val _newsFilter = MutableStateFlow("ALL")
    private val _selectedAssetId = MutableStateFlow<String?>(null)
    private val _selectedNewsDetail = MutableStateFlow<MarketNews?>(null)
    private val _showAddAlertDialog = MutableStateFlow(false)
    private val _defaultAlertAssetSymbol = MutableStateFlow("BTC/USDT")
    private val _selectedApiConfigForEdit = MutableStateFlow<ApiFeedConfig?>(null)
    private val _userMessage = MutableStateFlow<String?>(null)

    // Technical Indicators & SMC Engine State
    private val _analysisMode = MutableStateFlow(AnalysisMode.SMC_FIBONACCI)
    private val _technicalAnalysis = MutableStateFlow<OverallAnalysisResult?>(null)
    private val _smcAnalysis = MutableStateFlow<SmcAnalysisResult?>(null)
    private val _selectedSmcTab = MutableStateFlow(SmcCategoryTab.ALL)
    private val _selectedGlossaryTerm = MutableStateFlow<SmcGlossaryItem?>(null)
    private val _isCalculatingAnalysis = MutableStateFlow(false)
    private val _analysisAssetSymbol = MutableStateFlow("BTC")
    private val _analysisTimeframe = MutableStateFlow(CandleTimeframe.H1)
    private val _selectedIndicatorCategory = MutableStateFlow(IndicatorCategory.ALL)
    private val _analysisCandles = MutableStateFlow<List<Candle>>(emptyList())
    private val _analysisError = MutableStateFlow<String?>(null)

    // AI Signal Engine State
    private val _currentAiSignal = MutableStateFlow<AiTradingSignal?>(null)
    private val _aiSignalsList = MutableStateFlow<List<AiTradingSignal>>(emptyList())
    private val _isAnalyzingSignal = MutableStateFlow(false)
    private val _selectedSignalAssetSymbol = MutableStateFlow("BTC")
    private val _selectedSignalTimeframe = MutableStateFlow(CandleTimeframe.H1)
    private val _isSignalExpanded = MutableStateFlow(false)
    private val _signalAnalysisError = MutableStateFlow<String?>(null)

    // Learning Center & App Settings State
    private val learningPrefsRepo = LearningPreferencesRepository()
    private val appSettingsRepo = AppSettingsRepository()
    private var nexusAiRepo = com.example.data.ai.NexusAiRepository()
    val futuresScannerRepo = com.example.data.scanner.FuturesScannerRepository.getInstance(notificationManager = repository.notificationManager)
    private val _scannerScreenMode = MutableStateFlow(0)

    private val _selectedLessonCategory = MutableStateFlow<LessonCategory?>(null)
    private val _learningSearchQuery = MutableStateFlow("")
    private val _selectedLessonForDetail = MutableStateFlow<LessonItem?>(null)
    private val _selectedEconomicTerm = MutableStateFlow<com.example.data.news.EconomicTerm?>(null)
    private val _scannerTimeframe = MutableStateFlow(CandleTimeframe.H1)
    private val _learningUpdate = MutableStateFlow(0)

    // Task 10: NEXUS AI State
    private val _aiConversations = MutableStateFlow<List<com.example.data.ai.NexusConversation>>(listOf(com.example.data.ai.NexusConversation(id = "default_session", title = "Market Analysis")))
    private val _currentAiConversationId = MutableStateFlow("default_session")
    private val _aiMessages = MutableStateFlow<List<com.example.data.ai.NexusChatMessage>>(emptyList())
    private val _isAiLoading = MutableStateFlow(false)

    val uiState: StateFlow<TradingUiState> = combine(
        combine(
            combine(_currentTab, _learningUpdate) { tab, _ -> tab },
            repository.assets,
            repository.marketOverview,
            repository.signals,
            repository.alerts
        ) { tab, assets, overview, signals, alerts ->
            TabAssetsOverviewSignalsAlerts(tab, assets, overview, signals, alerts)
        },
        combine(
            repository.news,
            repository.scannerItems,
            repository.apiConfigs,
            repository.isRefreshing,
            repository.isWebSocketLive
        ) { news, scanner, apiConfigs, isRefreshing, wsLive ->
            NewsScannerApiRefreshingWs(news, scanner, apiConfigs, isRefreshing, wsLive)
        },
        combine(
            _selectedCategory,
            _statusFilter,
            _searchQuery,
            _signalFilter,
            _selectedTimeframe
        ) { category, statFilter, search, sigFilter, tf ->
            CategoryStatusSearchFilters(category, statFilter, search, sigFilter, tf)
        },
        combine(
            _selectedAssetId,
            _selectedNewsDetail,
            _showAddAlertDialog,
            _defaultAlertAssetSymbol,
            _selectedApiConfigForEdit
        ) { assetId, newsDetail, addAlert, defaultSymbol, apiConfigEdit ->
            DialogStates(assetId, newsDetail, addAlert, defaultSymbol, apiConfigEdit)
        },
        combine(
            _technicalAnalysis,
            _isCalculatingAnalysis,
            _analysisAssetSymbol,
            _analysisTimeframe,
            _selectedIndicatorCategory
        ) { analysis, calculating, symbol, timeframe, category ->
            AnalysisCoreState(analysis, calculating, symbol, timeframe, category)
        }
    ) { group1, group2, group3, group4, group5 ->
        TradingUiState(
            currentTab = group1.tab,
            assets = group1.assets,
            marketOverview = group1.overview,
            signals = group1.signals,
            alerts = group1.alerts,
            news = group2.news,
            scannerItems = group2.scanner,
            apiConfigs = group2.apiConfigs,
            isRefreshing = group2.isRefreshing,
            isWebSocketLive = group2.wsLive,
            selectedTimeframe = group3.timeframe,
            selectedCategory = group3.category,
            statusFilter = group3.statFilter,
            searchQuery = group3.search,
            signalFilter = group3.sigFilter,
            scannerFilter = _scannerFilter.value,
            newsFilter = _newsFilter.value,
            selectedAssetId = group4.assetId,
            selectedNewsDetail = group4.newsDetail,
            showAddAlertDialog = group4.addAlert,
            defaultAlertAssetSymbol = group4.defaultSymbol,
            selectedApiConfigForEdit = group4.apiConfigEdit,
            userMessage = _userMessage.value,
            analysisMode = _analysisMode.value,
            technicalAnalysis = group5.analysis,
            smcAnalysis = _smcAnalysis.value,
            selectedSmcTab = _selectedSmcTab.value,
            selectedGlossaryTerm = _selectedGlossaryTerm.value,
            isCalculatingAnalysis = group5.isCalculating,
            analysisAssetSymbol = group5.symbol,
            analysisTimeframe = group5.timeframe,
            selectedIndicatorCategory = group5.category,
            analysisCandles = _analysisCandles.value,
            analysisError = _analysisError.value,
            currentAiSignal = _currentAiSignal.value,
            aiSignalsList = if (_aiSignalsList.value.isNotEmpty()) _aiSignalsList.value else repository.aiSignals.value,
            isAnalyzingSignal = _isAnalyzingSignal.value,
            selectedSignalAssetSymbol = _selectedSignalAssetSymbol.value,
            selectedSignalTimeframe = _selectedSignalTimeframe.value,
            isSignalExpanded = _isSignalExpanded.value,
            learningLanguageMode = appSettingsRepo.languageMode.value,
            selectedLessonCategory = _selectedLessonCategory.value,
            learningSearchQuery = _learningSearchQuery.value,
            bookmarkedLessonIds = learningPrefsRepo.bookmarkedIds.value,
            completedLessonIds = learningPrefsRepo.completedIds.value,
            recentlyViewedLessonIds = learningPrefsRepo.recentlyViewedIds.value,
            selectedLessonForDetail = _selectedLessonForDetail.value,
            themeMode = appSettingsRepo.themeMode.value,
            isDataSavingMode = appSettingsRepo.isDataSavingMode.value,
            isLowPerformanceMode = appSettingsRepo.isLowPerformanceMode.value,
            defaultChartTimeframe = appSettingsRepo.defaultTimeframe.value,
            defaultWatchlistCategory = appSettingsRepo.defaultWatchlistCategory.value,
            alertNotificationsEnabled = appSettingsRepo.alertNotificationsEnabled.value,
            soundEffectsEnabled = appSettingsRepo.soundEffectsEnabled.value,
            refreshRate = RefreshRatePreference.fromSeconds(appSettingsRepo.refreshIntervalSeconds.value),
            lastCacheClearedTimestamp = appSettingsRepo.lastCacheClearedTimestamp.value,
            lastMarketDataRefreshTimestamp = appSettingsRepo.lastDataRefreshTimestamp.value,
            selectedEconomicTerm = _selectedEconomicTerm.value,
            scannerTimeframe = _scannerTimeframe.value,
            priceAlertsEnabled = repository.notificationManager.isPriceAlertsEnabled(),
            longSignalsEnabled = repository.notificationManager.isLongSignalsEnabled(),
            shortSignalsEnabled = repository.notificationManager.isShortSignalsEnabled(),
            newsAlertsEnabled = repository.notificationManager.isNewsAlertsEnabled(),
            macroEventsEnabled = repository.notificationManager.isMacroEventsEnabled(),
            watchlistUpdatesEnabled = repository.notificationManager.isWatchlistUpdatesEnabled(),
            aiMessages = _aiMessages.value,
            aiConversations = _aiConversations.value,
            currentAiConversationId = _currentAiConversationId.value,
            isAiLoading = _isAiLoading.value,
            isOnlineAiAvailable = nexusAiRepo.isOnlineAiAvailable(),
            urduFontSize = appSettingsRepo.urduFontSize.value,
            futuresScannerState = futuresScannerRepo.scannerState.value,
            futuresWatchlist = futuresScannerRepo.watchlist.value,
            scannerScreenMode = _scannerScreenMode.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TradingUiState()
    )

    init {
        refreshMarketData()
        loadTechnicalAnalysis("BTC", CandleTimeframe.H1)
        loadAiSignal("BTC", CandleTimeframe.H1)
        refreshAllAiSignals()

        viewModelScope.launch {
            futuresScannerRepo.scannerState.collect {
                _learningUpdate.value++
            }
        }
        viewModelScope.launch {
            futuresScannerRepo.watchlist.collect {
                _learningUpdate.value++
            }
        }
    }

    fun selectTab(tab: NavigationTab) {
        _currentTab.value = tab
        if (tab == NavigationTab.ANALYSIS && _technicalAnalysis.value == null) {
            loadTechnicalAnalysis(_analysisAssetSymbol.value, _analysisTimeframe.value)
        }
        if (tab == NavigationTab.AI_SIGNALS && _currentAiSignal.value == null) {
            loadAiSignal(_selectedSignalAssetSymbol.value, _selectedSignalTimeframe.value)
        }
    }

    fun refreshMarketData() {
        viewModelScope.launch {
            repository.refreshData()
            _userMessage.value = "Market data updated"
        }
    }

    fun loadTechnicalAnalysis(
        symbol: String = _analysisAssetSymbol.value,
        timeframe: CandleTimeframe = _analysisTimeframe.value
    ) {
        _analysisAssetSymbol.value = symbol
        _analysisTimeframe.value = timeframe
        _isCalculatingAnalysis.value = true
        _analysisError.value = null

        viewModelScope.launch {
            try {
                val assetName = repository.assets.value.firstOrNull {
                    it.symbol.equals(symbol, ignoreCase = true) || it.id.equals(symbol, ignoreCase = true)
                }?.name ?: symbol

                val candles = repository.getCandles(symbol, timeframe)
                _analysisCandles.value = candles

                val techDeferred = async { repository.calculateTechnicalAnalysis(symbol, assetName, timeframe) }
                val smcDeferred = async { repository.calculateSmcAnalysis(symbol, assetName, timeframe) }

                _technicalAnalysis.value = techDeferred.await()
                _smcAnalysis.value = smcDeferred.await()
            } catch (e: Exception) {
                _analysisError.value = e.message ?: "Technical calculation error"
            } finally {
                _isCalculatingAnalysis.value = false
            }
        }
    }

    fun setAnalysisMode(mode: AnalysisMode) {
        _analysisMode.value = mode
    }

    fun setSmcTab(tab: SmcCategoryTab) {
        _selectedSmcTab.value = tab
    }

    fun openGlossaryTerm(termKey: String) {
        _selectedGlossaryTerm.value = RomanUrduSignalGlossary.getTerm(termKey) ?: RomanUrduSMCGlossary.getItem(termKey)
    }

    fun closeGlossaryDialog() {
        _selectedGlossaryTerm.value = null
    }

    fun loadAiSignal(
        symbol: String = _selectedSignalAssetSymbol.value,
        timeframe: CandleTimeframe = _selectedSignalTimeframe.value
    ) {
        _selectedSignalAssetSymbol.value = symbol
        _selectedSignalTimeframe.value = timeframe
        _isAnalyzingSignal.value = true
        _signalAnalysisError.value = null

        viewModelScope.launch {
            try {
                val assetName = repository.assets.value.firstOrNull {
                    it.symbol.equals(symbol, ignoreCase = true) || it.id.equals(symbol, ignoreCase = true)
                }?.name ?: symbol

                val signal = repository.calculateAiSignal(symbol, assetName, timeframe)
                _currentAiSignal.value = signal
            } catch (e: Exception) {
                _signalAnalysisError.value = e.message ?: "Failed to generate AI signal"
            } finally {
                _isAnalyzingSignal.value = false
            }
        }
    }

    fun refreshAllAiSignals() {
        viewModelScope.launch {
            try {
                val list = repository.refreshAiSignals()
                if (list.isNotEmpty()) {
                    _aiSignalsList.value = list
                }
            } catch (e: Exception) {
                // Keep current signals if refresh fails
            }
        }
    }

    fun setSignalAsset(symbol: String) {
        _selectedSignalAssetSymbol.value = symbol
        loadAiSignal(symbol, _selectedSignalTimeframe.value)
    }

    fun setSignalTimeframe(timeframe: CandleTimeframe) {
        _selectedSignalTimeframe.value = timeframe
        loadAiSignal(_selectedSignalAssetSymbol.value, timeframe)
    }

    fun toggleSignalExpanded() {
        _isSignalExpanded.value = !_isSignalExpanded.value
    }

    fun setAnalysisTimeframe(timeframe: CandleTimeframe) {
        _analysisTimeframe.value = timeframe
        loadTechnicalAnalysis(_analysisAssetSymbol.value, timeframe)
    }

    fun setAnalysisAsset(symbol: String) {
        _analysisAssetSymbol.value = symbol
        loadTechnicalAnalysis(symbol, _analysisTimeframe.value)
    }

    fun setIndicatorCategory(category: IndicatorCategory) {
        _selectedIndicatorCategory.value = category
    }

    fun openTechnicalAnalysis(symbol: String = "BTC") {
        _analysisAssetSymbol.value = symbol
        _currentTab.value = NavigationTab.ANALYSIS
        loadTechnicalAnalysis(symbol, _analysisTimeframe.value)
    }

    fun setTimeframe(timeframe: Timeframe) {
        _selectedTimeframe.value = timeframe
    }

    fun setCategory(category: AssetCategory) {
        _selectedCategory.value = category
    }

    fun setStatusFilter(filter: String) {
        _statusFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSignalFilter(filter: String) {
        _signalFilter.value = filter
    }

    fun setScannerFilter(filter: String) {
        _scannerFilter.value = filter
    }

    fun setNewsFilter(filter: String) {
        _newsFilter.value = filter
    }

    fun openAssetDetail(asset: MarketAsset) {
        _selectedAssetId.value = asset.id
    }

    fun closeAssetDetail() {
        _selectedAssetId.value = null
    }

    fun openNewsDetail(news: MarketNews) {
        _selectedNewsDetail.value = news
    }

    fun closeNewsDetail() {
        _selectedNewsDetail.value = null
    }

    fun openAddAlertDialog(defaultSymbol: String = "BTC/USDT") {
        _defaultAlertAssetSymbol.value = defaultSymbol
        _showAddAlertDialog.value = true
    }

    fun closeAddAlertDialog() {
        _showAddAlertDialog.value = false
    }

    fun openApiConfigDialog(config: ApiFeedConfig) {
        _selectedApiConfigForEdit.value = config
    }

    fun closeApiConfigDialog() {
        _selectedApiConfigForEdit.value = null
    }

    fun createAlert(symbol: String, assetName: String, targetPrice: Double, direction: AlertDirection, note: String) {
        repository.createAlert(symbol, assetName, targetPrice, direction, note)
        _showAddAlertDialog.value = false
        _userMessage.value = "Alert created for $symbol at $$targetPrice"
    }

    fun toggleAlert(alertId: String) {
        repository.toggleAlert(alertId)
    }

    fun deleteAlert(alertId: String) {
        repository.deleteAlert(alertId)
        _userMessage.value = "Alert deleted"
    }

    fun toggleNewsBookmark(newsId: String) {
        repository.toggleNewsBookmark(newsId)
    }

    fun toggleFavorite(assetId: String) {
        repository.toggleFavorite(assetId)
    }

    fun openEconomicGlossary(term: com.example.data.news.EconomicTerm) {
        _selectedEconomicTerm.value = term
    }

    fun dismissEconomicGlossary() {
        _selectedEconomicTerm.value = null
    }

    fun refreshNews() {
        viewModelScope.launch {
            repository.refreshNews(forceRefresh = true)
            _userMessage.value = "Market dispatches updated"
        }
    }

    fun setScannerTimeframe(timeframe: CandleTimeframe) {
        _scannerTimeframe.value = timeframe
        refreshScanner(timeframe)
    }

    fun refreshScanner(timeframe: CandleTimeframe = _scannerTimeframe.value) {
        viewModelScope.launch {
            repository.refreshScanner(timeframe)
        }
    }

    fun inspectAssetInAnalysis(assetId: String, symbol: String) {
        val cleanSym = symbol.replace("/USDT", "").replace("/USD", "").replace("/", "")
        _analysisAssetSymbol.value = cleanSym
        _currentTab.value = NavigationTab.ANALYSIS
        loadTechnicalAnalysis(cleanSym, _analysisTimeframe.value)
    }

    fun setPriceAlertsEnabled(enabled: Boolean) {
        repository.notificationManager.setPriceAlertsEnabled(enabled)
    }

    fun setLongSignalsEnabled(enabled: Boolean) {
        repository.notificationManager.setLongSignalsEnabled(enabled)
    }

    fun setShortSignalsEnabled(enabled: Boolean) {
        repository.notificationManager.setShortSignalsEnabled(enabled)
    }

    fun setNewsAlertsEnabled(enabled: Boolean) {
        repository.notificationManager.setNewsAlertsEnabled(enabled)
    }

    fun setMacroEventsEnabled(enabled: Boolean) {
        repository.notificationManager.setMacroEventsEnabled(enabled)
    }

    fun setWatchlistUpdatesEnabled(enabled: Boolean) {
        repository.notificationManager.setWatchlistUpdatesEnabled(enabled)
    }

    fun connectApiFeed(feedId: String, apiKey: String) {
        repository.connectApiFeed(feedId, apiKey)
        _selectedApiConfigForEdit.value = null
        _userMessage.value = "API feed connected successfully"
    }

    fun disconnectApiFeed(feedId: String) {
        repository.disconnectApiFeed(feedId)
        _selectedApiConfigForEdit.value = null
        _userMessage.value = "API feed disconnected"
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun bindLearningContext(context: Context) {
        learningPrefsRepo.bindContext(context)
        appSettingsRepo.bindContext(context)
        nexusAiRepo = com.example.data.ai.NexusAiRepository(context)
        _aiConversations.value = nexusAiRepo.loadConversations()
        val activeId = _aiConversations.value.firstOrNull()?.id ?: "default_session"
        _currentAiConversationId.value = activeId
        _aiMessages.value = nexusAiRepo.loadMessages(activeId)
        repository.setDataSavingMode(appSettingsRepo.isDataSavingMode.value)
        repository.setLowPerformanceMode(appSettingsRepo.isLowPerformanceMode.value)
        repository.setRefreshIntervalSeconds(appSettingsRepo.refreshIntervalSeconds.value)
        _learningUpdate.value++
    }

    fun setUrduFontSize(size: com.example.data.settings.UrduFontSize) {
        appSettingsRepo.setUrduFontSize(size)
        _learningUpdate.value++
    }

    // ==========================================
    // TASK 10: NEXUS AI Methods
    // ==========================================

    fun sendAiMessage(query: String) {
        if (query.isBlank() || _isAiLoading.value) return
        val currentId = _currentAiConversationId.value
        val userMsg = com.example.data.ai.NexusChatMessage(
            conversationId = currentId,
            sender = com.example.data.ai.MessageSender.USER,
            text = query.trim()
        )
        val updatedList = _aiMessages.value + userMsg
        _aiMessages.value = updatedList
        _isAiLoading.value = true

        viewModelScope.launch {
            try {
                // Find currently active or selected asset
                val activeAsset = _selectedAssetId.value?.let { id ->
                    repository.assets.value.firstOrNull { it.id == id }
                } ?: repository.assets.value.firstOrNull { it.id == "btc" }
                ?: repository.assets.value.firstOrNull()

                val matchedFuturesSignal = futuresScannerRepo.scannerState.value.activeSignals.firstOrNull {
                    query.contains(it.symbol, ignoreCase = true) || query.contains(it.baseAsset, ignoreCase = true)
                } ?: futuresScannerRepo.scannerState.value.activeSignals.firstOrNull()

                val contextSnapshot = com.example.data.ai.MarketContextBuilder.buildSnapshot(
                    asset = activeAsset,
                    candles = _analysisCandles.value,
                    timeframe = _analysisTimeframe.value,
                    technicalAnalysis = _technicalAnalysis.value,
                    smcAnalysis = _smcAnalysis.value,
                    signal = _currentAiSignal.value,
                    newsList = repository.news.value,
                    futuresSignal = matchedFuturesSignal
                )

                val responseMsg = nexusAiRepo.generateResponse(
                    userPrompt = query,
                    contextSnapshot = contextSnapshot,
                    languageMode = appSettingsRepo.languageMode.value,
                    recentHistory = _aiMessages.value.takeLast(6)
                ).copy(conversationId = currentId)

                val finalizedList = _aiMessages.value + responseMsg
                _aiMessages.value = finalizedList
                nexusAiRepo.saveMessages(currentId, finalizedList)
            } catch (e: Exception) {
                val errorMsg = com.example.data.ai.NexusChatMessage(
                    conversationId = currentId,
                    sender = com.example.data.ai.MessageSender.ASSISTANT,
                    text = "System error during analysis. Falling back to offline rule-based engine.",
                    isOfflineEngine = true
                )
                _aiMessages.value = _aiMessages.value + errorMsg
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun newAiChat() {
        val newId = "chat_" + System.currentTimeMillis()
        val newConv = com.example.data.ai.NexusConversation(
            id = newId,
            title = "Analysis ${java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.US).format(java.util.Date())}"
        )
        val updatedConvs = listOf(newConv) + _aiConversations.value
        _aiConversations.value = updatedConvs
        _currentAiConversationId.value = newId
        _aiMessages.value = emptyList()
        nexusAiRepo.saveConversations(updatedConvs)
    }

    fun clearAiChat() {
        val currentId = _currentAiConversationId.value
        _aiMessages.value = emptyList()
        nexusAiRepo.saveMessages(currentId, emptyList())
        _userMessage.value = "Chat conversation cleared"
    }

    fun deleteAiConversation(id: String) {
        val updated = _aiConversations.value.filter { it.id != id }
        val finalConvs = if (updated.isEmpty()) listOf(com.example.data.ai.NexusConversation(id = "default_session", title = "Market Analysis")) else updated
        _aiConversations.value = finalConvs
        nexusAiRepo.saveConversations(finalConvs)
        nexusAiRepo.saveMessages(id, emptyList())
        val nextId = finalConvs.first().id
        _currentAiConversationId.value = nextId
        _aiMessages.value = nexusAiRepo.loadMessages(nextId)
    }

    fun selectAiConversation(id: String) {
        _currentAiConversationId.value = id
        _aiMessages.value = nexusAiRepo.loadMessages(id)
    }

    fun attachCurrentMarketToAi() {
        val activeAsset = _selectedAssetId.value?.let { id ->
            repository.assets.value.firstOrNull { it.id == id }
        } ?: repository.assets.value.firstOrNull { it.id == "btc" }
        val sym = activeAsset?.symbol ?: "BTCUSDT"
        sendAiMessage("Analyze current market condition, trend, and SMC levels for $sym")
    }

    fun setThemeMode(mode: AppThemeMode) {
        appSettingsRepo.setThemeMode(mode)
        _learningUpdate.value++
    }

    fun setAppLanguageMode(mode: LearningLanguageMode) {
        appSettingsRepo.setLanguageMode(mode)
        learningPrefsRepo.setLanguageMode(mode)
        _learningUpdate.value++
    }

    fun setLearningLanguageMode(mode: LearningLanguageMode) {
        setAppLanguageMode(mode)
    }

    fun setDataSavingMode(enabled: Boolean) {
        appSettingsRepo.setDataSavingMode(enabled)
        repository.setDataSavingMode(enabled)
        _learningUpdate.value++
        _userMessage.value = if (enabled) "Data Saving Mode Enabled" else "Data Saving Mode Disabled"
    }

    fun setLowPerformanceMode(enabled: Boolean) {
        appSettingsRepo.setLowPerformanceMode(enabled)
        repository.setLowPerformanceMode(enabled)
        _learningUpdate.value++
        _userMessage.value = if (enabled) "Low Performance Device Mode Enabled" else "Low Performance Device Mode Disabled"
    }

    fun setDefaultChartTimeframe(timeframe: CandleTimeframe) {
        appSettingsRepo.setDefaultTimeframe(timeframe)
        _analysisTimeframe.value = timeframe
        _selectedSignalTimeframe.value = timeframe
        _learningUpdate.value++
    }

    fun setDefaultWatchlistCategory(category: AssetCategory) {
        appSettingsRepo.setDefaultWatchlistCategory(category)
        _selectedCategory.value = category
        _learningUpdate.value++
    }

    fun setAlertNotificationsEnabled(enabled: Boolean) {
        appSettingsRepo.setAlertNotificationsEnabled(enabled)
        _learningUpdate.value++
    }

    fun setSoundEffectsEnabled(enabled: Boolean) {
        appSettingsRepo.setSoundEffectsEnabled(enabled)
        _learningUpdate.value++
    }

    fun setRefreshRate(rate: RefreshRatePreference) {
        appSettingsRepo.setRefreshIntervalSeconds(rate.seconds)
        repository.setRefreshIntervalSeconds(rate.seconds)
        _learningUpdate.value++
    }

    fun clearTemporaryCache() {
        val repoPurged = repository.clearCache()
        appSettingsRepo.clearTemporaryCache()
        _learningUpdate.value++
        _userMessage.value = "Cache cleared: $repoPurged buffers freed"
    }

    fun setLearningSearchQuery(query: String) {
        _learningSearchQuery.value = query
        _learningUpdate.value++
    }

    fun selectLessonCategory(category: LessonCategory?) {
        _selectedLessonCategory.value = category
        _learningUpdate.value++
    }

    fun openLessonDetail(lesson: LessonItem) {
        learningPrefsRepo.recordLessonViewed(lesson.id)
        _selectedLessonForDetail.value = lesson
        _learningUpdate.value++
    }

    fun closeLessonDetail() {
        _selectedLessonForDetail.value = null
        _learningUpdate.value++
    }

    fun toggleLessonBookmark(lessonId: String) {
        learningPrefsRepo.toggleBookmark(lessonId)
        _learningUpdate.value++
    }

    fun toggleLessonCompleted(lessonId: String) {
        learningPrefsRepo.toggleCompleted(lessonId)
        _learningUpdate.value++
    }

    fun openPreviousLesson() {
        val current = _selectedLessonForDetail.value ?: return
        val all: List<LessonItem> = LearningContentDatabase.allLessons
        val index = all.indexOfFirst { it.id == current.id }
        if (index > 0) {
            openLessonDetail(all[index - 1])
        } else if (all.isNotEmpty()) {
            openLessonDetail(all.last())
        }
    }

    fun openNextLesson() {
        val current = _selectedLessonForDetail.value ?: return
        val all: List<LessonItem> = LearningContentDatabase.allLessons
        val index = all.indexOfFirst { it.id == current.id }
        if (index >= 0 && index < all.size - 1) {
            openLessonDetail(all[index + 1])
        } else if (all.isNotEmpty()) {
            openLessonDetail(all.first())
        }
    }

    fun navigateToLessonInAcademy(lessonId: String) {
        val lesson = LearningContentDatabase.findLesson(lessonId)
        if (lesson != null) {
            _selectedLessonForDetail.value = lesson
            learningPrefsRepo.recordLessonViewed(lesson.id)
            _selectedGlossaryTerm.value = null
            _currentTab.value = NavigationTab.LEARNING
            _learningUpdate.value++
        }
    }

    fun setScannerScreenMode(mode: Int) {
        _scannerScreenMode.value = mode
        _learningUpdate.value++
    }

    fun startFuturesScanner(context: android.content.Context) {
        com.example.data.scanner.BinanceFuturesScannerService.startService(context)
        _userMessage.value = "Binance Futures Scanner Started"
    }

    fun stopFuturesScanner(context: android.content.Context) {
        com.example.data.scanner.BinanceFuturesScannerService.stopService(context)
        _userMessage.value = "Binance Futures Scanner Stopped"
    }

    fun toggleFuturesWatchlistPair(symbol: String) {
        futuresScannerRepo.togglePairScanning(symbol)
        _learningUpdate.value++
    }

    fun addCustomFuturesPair(symbol: String) {
        futuresScannerRepo.addCustomPair(symbol)
        _learningUpdate.value++
    }

    fun removeFuturesWatchlistPair(symbol: String) {
        futuresScannerRepo.removePair(symbol)
        _learningUpdate.value++
    }

    fun setFuturesScannerInterval(seconds: Int) {
        futuresScannerRepo.setScanInterval(seconds)
        _learningUpdate.value++
    }

    fun setFuturesScannerPaperTrading(enabled: Boolean) {
        futuresScannerRepo.setPaperTradingEnabled(enabled)
        _learningUpdate.value++
    }

    fun explainFuturesSignalWithAi(signal: com.example.data.scanner.FuturesScanSignal) {
        val query = "Please analyze and explain the current Binance USDT-M Futures signal for ${signal.symbol} ${signal.direction.name}. Quality score is ${signal.qualityScore}% (${signal.qualityRating}). Break down the 4H, 1H, 15M, and 5M timeframe confluences, entry zones, risk parameters, and invalidation rules."
        selectTab(NavigationTab.NEXUS_AI)
        sendAiMessage(query)
    }

    fun simulateFuturesPaperTrade(signal: com.example.data.scanner.FuturesScanSignal) {
        _userMessage.value = "Paper trade tracked: ${signal.symbol} ${signal.direction.name} @ ${signal.entryZoneFormatted} (TP2: ${signal.tp2Formatted}, SL: ${signal.stopLossFormatted})"
    }

    private data class TabAssetsOverviewSignalsAlerts(
        val tab: NavigationTab,
        val assets: List<MarketAsset>,
        val overview: MarketOverview,
        val signals: List<Signal>,
        val alerts: List<MarketAlert>
    )

    private data class NewsScannerApiRefreshingWs(
        val news: List<MarketNews>,
        val scanner: List<ScannerItem>,
        val apiConfigs: List<ApiFeedConfig>,
        val isRefreshing: Boolean,
        val wsLive: Boolean
    )

    private data class CategoryStatusSearchFilters(
        val category: AssetCategory,
        val statFilter: String,
        val search: String,
        val sigFilter: String,
        val timeframe: Timeframe
    )

    private data class DialogStates(
        val assetId: String?,
        val newsDetail: MarketNews?,
        val addAlert: Boolean,
        val defaultSymbol: String,
        val apiConfigEdit: ApiFeedConfig?
    )

    private data class AnalysisCoreState(
        val analysis: OverallAnalysisResult?,
        val isCalculating: Boolean,
        val symbol: String,
        val timeframe: CandleTimeframe,
        val category: IndicatorCategory
    )
}
