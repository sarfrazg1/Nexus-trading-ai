# NEXUS TRADING AI 📈🤖

[![Build and Release Debug APK](https://github.com/sarfrazg1/Nexus-trading-ai/actions/workflows/build-apk.yml/badge.svg)](https://github.com/sarfrazg1/Nexus-trading-ai/actions/workflows/build-apk.yml)
[![Platform](https://img.shields.io/badge/Platform-Android%207.0%2B%20%28API%2024%E2%80%9336%29-brightgreen.svg)](https://android.com)
[![Language](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)](https://kotlinlang.org)
[![UI](https://img.shields.io/badge/Jetpack%20Compose-M3-blue.svg)](https://developer.android.com/jetpack/compose)
[![Tests](https://img.shields.io/badge/Tests-50%2F50%20Passing-success.svg)](https://github.com/sarfrazg1/Nexus-trading-ai)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

A production-grade, institutional Android trading platform built with **Kotlin** and **Jetpack Compose**. **NEXUS TRADING AI** combines verified live multi-asset market data, an algorithmic technical indicators engine, Smart Money Concepts (SMC) structure detection, multi-factor trade signal generation, a 4-language localized trading academy, and a **Hybrid AI Assistant (NEXUS AI)** combining cloud Gemini AI logic with an offline rule-based financial analysis engine.

---

## 🚀 Key Modules & Capabilities

### 1. 📊 Live Market Data & Multi-Asset Feed
- **Asset Classes:** Cryptocurrency (BTC, ETH, SOL, etc.), Forex (EUR/USD, GBP/USD, etc.), Commodities (Gold, Silver, WTI Oil, Brent Crude, Natural Gas), and Global Indices.
- **Data Providers:** Real, verified endpoints via Binance REST/WebSocket streams, Yahoo Finance, and CoinGecko.
- **Zero Data Fabrication:** Clear distinction between `LIVE`, `CACHED`, and `UNAVAILABLE` statuses. Stale data is never disguised as live.
- **Timeframes:** 12 supported candle intervals (1m, 3m, 5m, 15m, 30m, 1h, 2h, 4h, 6h, 12h, 1D, 1W).

### 2. 📐 Advanced Technical Indicators Engine
Calculated mathematically from authentic historical candle data:
- **Momentum:** Wilder's Smoothed RSI (14) with overbought/oversold detection, MACD (12, 26, 9) signal line & histogram, Stochastic RSI.
- **Trend:** Exponential Moving Averages (EMA 9, 20, 50, 100, 200), SMA, Volume-Weighted Average Price (VWAP).
- **Volatility & Range:** Average True Range (ATR 14), Bollinger Bands (20 periods, 2.0 standard deviations).

### 3. 🏦 Smart Money Concepts (SMC) & Liquidity Analysis
- **Market Structure:** Automated detection of Swing Highs / Swing Lows and structural breaks.
- **BOS & CHoCH:** Break of Structure (trend continuation) and Change of Character (early trend reversal).
- **Order Blocks (OB):** Institutional supply and demand order blocks with volume validation.
- **Fair Value Gaps (FVG):** Imbalance tracking across 3-candle patterns with real-time `UNFILLED`, `PARTIALLY_FILLED`, and `FILLED` tracking.
- **Liquidity Sweeps:** Buy-side and Sell-side liquidity sweep identification (stop-hunts).
- **Fibonacci Levels:** Automated retracement (0.236, 0.382, 0.50, 0.618 Golden Pocket, 0.786) and extension targets (1.272, 1.618).

### 4. ⚡ AI Trading Signal Engine
- **Decisions:** Strict, non-forced outputs: `LONG`, `SHORT`, or `NO_TRADE`.
- **Dynamic Risk Sizing:** Calculates precise Entry Zones, structural Stop Loss (below key invalidation levels), and 3 Take-Profit tiers (TP1 Conservative, TP2 Balanced, TP3 Runner).
- **Minimum 1:2.0 R:R Ratio:** Trade ideas below 1:2 Risk-to-Reward are filtered out.
- **Transparent Weighted Confidence (0–100):**
  - Trend Alignment: 25%
  - Momentum: 20%
  - SMC Structure (BOS/CHoCH/OBs): 25%
  - Liquidity / Volatility: 15%
  - Multi-Timeframe Confirmation: 15%

### 5. 🤖 NEXUS AI — Hybrid Trading Assistant
- **Cloud AI:** Powered by Firebase AI Logic (`gemini-3.8-flash`) with zero hardcoded API keys in client code.
- **Offline Rule-Based Analysis Engine:** 100% deterministic local analysis engine that functions without internet. Evaluates real live metrics and clearly labels responses: `📊 [Offline Rule-Based Analysis (No Fake AI)]`.
- **Market Context Builder:** Automatically injects verified live asset prices, RSI, EMAs, S/R, and news into conversational context.
- **Specialized Modules:**
  - *Timeframe Advisor:* Compares 1m/5m/15m/1h/4h noise vs. structural bias.
  - *Macro & CPI Reasoning:* Explains institutional news dynamics (e.g., sell-the-news, liquidity grabs, Core CPI vs. Headline CPI, DXY impact).
  - *Risk Management Guardrails:* Enforces 1–2% capital risk and max 3x–5x leverage recommendations.

### 6. 🌐 4-Language System & Nastaleeq Urdu Typography
- **Languages Supported:**
  1. **English:** Institutional financial and Wall Street terminology.
  2. **Roman Urdu:** Everyday conversational Pakistani trading phrasing.
  3. **Standard Urdu (اردو رسم الخط):** Authentic Urdu script with proper Right-to-Left (RTL) layout direction.
  4. **Bilingual (English + Urdu):** Dual-format cards with English technical metrics and Urdu conceptual explanations.
- **Typography:** Bundled **Noto Nastaliq Urdu** font (SIL Open Font License) with customizable font sizing in Settings (`Small`, `Medium`, `Large`).

### 7. 📰 Financial News Analyzer & Market Scanner
- **News Feeds:** Real RSS financial feeds with automatic asset impact detection (`Bullish`, `Bearish`, `Neutral`) and impact severity (`High`, `Medium`, `Low`).
- **Macroeconomic Glossary:** In-depth Roman Urdu and Standard Urdu explanations for CPI, PPI, PCE, Fed Rate, NFP, GDP, DXY, Treasury Yields, and ETF Flows.
- **Scanner Filters:** Quick-scan market condition filters (RSI Overbought/Oversold, Bullish/Bearish CHoCH, Volume Surge).

### 8. ⚙️ Performance & Data Controls
- **Low Performance Device Mode:** Tailored for Android devices with 2GB–4GB RAM (reduces animations, closes idle WebSockets, caps cached historical candles).
- **Data Saving Mode:** Suspends background polling; provides manual on-demand refreshes with visible data timestamps.
- **Theme Engine:** Dark Mode, Light Mode, and System Default adhering strictly to Material 3 contrast standards.

---

## 🛠️ Architecture & Tech Stack

- **Architecture:** MVVM + Clean Architecture with Single-Source-of-Truth Unidirectional Data Flow (UDF).
- **UI Framework:** 100% Jetpack Compose with Material Design 3 (M3).
- **Asynchronous Operations:** Kotlin Coroutines & StateFlow / SharedFlow.
- **Typography:** Custom local Font Families (`Noto Nastaliq Urdu` via Android Font Resource).
- **Local Persistence:** Android `SharedPreferences` for user settings, custom watchlists, and offline chat threads.
- **Build System:** Gradle Kotlin DSL (`build.gradle.kts`) with Version Catalog (`gradle/libs.versions.toml`).

---

## 🧪 Testing & Verification

The project includes an extensive local JVM unit test suite running across Robolectric and standard JUnit4 runners.

```bash
# Run all unit tests
gradle testDebugUnitTest
```

**Results:**
- **50 total unit tests** across Market Data, Technical Indicators, SMC calculations, AI Signal Engine, Learning Center, Settings/Performance, News Analyzer, and NEXUS AI Assistant.
- **0 Failures — 100% Pass Rate**.

---

## 📦 Building the APK

### Debug APK (Ready for Device Installation)
```bash
gradle assembleDebug
```
Output path: `app/build/outputs/apk/debug/app-debug.apk` (Size: ~24 MB)

### GitHub Actions CI/CD
Every push to the `main` branch automatically triggers `.github/workflows/build-apk.yml` to:
1. Set up Java 17 and Gradle.
2. Build the debug APK (`:app:assembleDebug`).
3. Verify file integrity and output size.
4. Upload artifacts and publish a GitHub Release with `app-debug.apk`.

---

## 📄 License
This project is licensed under the Apache License 2.0. The bundled Noto Nastaliq Urdu font is licensed under the SIL Open Font License (OFL) v1.1.
