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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
    val technicalAnalysis: OverallAnalysisResult? = null,
    val isCalculatingAnalysis: Boolean = false,
    val analysisAssetSymbol: String = "BTC",
    val analysisTimeframe: CandleTimeframe = CandleTimeframe.H1,
    val selectedIndicatorCategory: IndicatorCategory = IndicatorCategory.ALL,
    val analysisCandles: List<Candle> = emptyList(),
    val analysisError: String? = null
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

    // Technical Indicators Engine State
    private val _technicalAnalysis = MutableStateFlow<OverallAnalysisResult?>(null)
    private val _isCalculatingAnalysis = MutableStateFlow(false)
    private val _analysisAssetSymbol = MutableStateFlow("BTC")
    private val _analysisTimeframe = MutableStateFlow(CandleTimeframe.H1)
    private val _selectedIndicatorCategory = MutableStateFlow(IndicatorCategory.ALL)
    private val _analysisCandles = MutableStateFlow<List<Candle>>(emptyList())
    private val _analysisError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<TradingUiState> = combine(
        combine(
            _currentTab,
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
            technicalAnalysis = group5.analysis,
            isCalculatingAnalysis = group5.isCalculating,
            analysisAssetSymbol = group5.symbol,
            analysisTimeframe = group5.timeframe,
            selectedIndicatorCategory = group5.category,
            analysisCandles = _analysisCandles.value,
            analysisError = _analysisError.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TradingUiState()
    )

    init {
        refreshMarketData()
        loadTechnicalAnalysis("BTC", CandleTimeframe.H1)
    }

    fun selectTab(tab: NavigationTab) {
        _currentTab.value = tab
        if (tab == NavigationTab.ANALYSIS && _technicalAnalysis.value == null) {
            loadTechnicalAnalysis(_analysisAssetSymbol.value, _analysisTimeframe.value)
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

                val result = repository.calculateTechnicalAnalysis(symbol, assetName, timeframe)
                _technicalAnalysis.value = result
            } catch (e: Exception) {
                _analysisError.value = e.message ?: "Technical indicators calculation error"
            } finally {
                _isCalculatingAnalysis.value = false
            }
        }
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
