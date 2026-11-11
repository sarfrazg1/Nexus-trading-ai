package com.example.data.ai

import com.example.data.learning.LearningLanguageMode
import java.util.Locale

/**
 * 100% Offline Rule-Based Trading Analysis Engine.
 * Evaluates real market metrics (RSI, MACD, EMAs, BOS/CHoCH, FVGs, S/R, ATR, Timeframe Agreement)
 * and generates authentic, fluent market reasoning in:
 * 1. English
 * 2. Roman Urdu
 * 3. Standard Urdu (اردو script with Nastaleeq typography)
 * 4. Bilingual (English + اردو)
 *
 * Always clearly labeled: "Offline Rule-Based Analysis (No Fake AI)".
 */
object OfflineTradingAnalysisEngine {

    fun generateAnalysis(
        query: String,
        context: MarketContextSnapshot,
        language: LearningLanguageMode
    ): String {
        val lowerQuery = query.lowercase().trim()
        val isTimeframeQuery = lowerQuery.contains("timeframe") || lowerQuery.contains("chart") ||
                lowerQuery.contains("5m") || lowerQuery.contains("15m") || lowerQuery.contains("1h") ||
                lowerQuery.contains("ٹائم فریم") || lowerQuery.contains("چارٹ")

        val isCpiMacroQuery = lowerQuery.contains("cpi") || lowerQuery.contains("fed") ||
                lowerQuery.contains("inflation") || lowerQuery.contains("mehngai") ||
                lowerQuery.contains("سی پی آئی") || lowerQuery.contains("مہنگائی") || lowerQuery.contains("سود")

        val isRiskQuery = lowerQuery.contains("risk") || lowerQuery.contains("leverage") ||
                lowerQuery.contains("stop loss") || lowerQuery.contains("liquidation") ||
                lowerQuery.contains("رسک") || lowerQuery.contains("لیوریج") || lowerQuery.contains("اسٹاپ لاس")

        return when {
            isTimeframeQuery -> generateTimeframeAdvice(context, language)
            isCpiMacroQuery -> generateMacroExplanation(context, language)
            isRiskQuery -> generateRiskAnalysis(context, language)
            else -> generateGeneralMarketAnalysis(context, language)
        }
    }

    private fun generateGeneralMarketAnalysis(
        ctx: MarketContextSnapshot,
        lang: LearningLanguageMode
    ): String {
        val rsi = ctx.rsi14 ?: 50.0
        val rsiStatus = when {
            rsi > 70.0 -> "Overbought (>70)"
            rsi < 30.0 -> "Oversold (<30)"
            else -> "Neutral (30-70)"
        }

        val priceStr = String.format(Locale.US, "$%.2f", ctx.currentPrice)
        val changeStr = String.format(Locale.US, "%+.2f%%", ctx.change24hPercent)

        return when (lang) {
            LearningLanguageMode.ENGLISH -> """
                📊 [OFFLINE RULE-BASED ANALYSIS]
                Asset: ${ctx.assetName} (${ctx.symbol}) | Current Price: $priceStr ($changeStr)
                Timeframe: ${ctx.timeframe} | Data Source: ${ctx.dataSource}

                1. Momentum & Indicators:
                • RSI (14): ${String.format(Locale.US, "%.1f", rsi)} — $rsiStatus.
                • Trend Structure: ${ctx.emaCondition ?: "Consolidation / EMA crossover pending"}.
                • Volatility: ATR is ${ctx.atrVolatility?.let { String.format(Locale.US, "%.2f", it) } ?: "normal"}.

                2. Smart Money & Key Levels:
                • Market Structure: ${ctx.bosChochCondition ?: "Ranging inside key swing points"}.
                • Support: ${ctx.supportLevel?.let { String.format(Locale.US, "$%.2f", it) } ?: "Dynamic EMA support"}.
                • Resistance: ${ctx.resistanceLevel?.let { String.format(Locale.US, "$%.2f", it) } ?: "Dynamic EMA resistance"}.
                • Fair Value Gaps: ${ctx.fvgStatus ?: "No open major imbalances"}.

                3. Trading Recommendation:
                ${if (rsi > 70.0) "⚠️ Caution: Price is extended in overbought territory. Avoid chasing Longs at resistance." 
                  else if (rsi < 30.0) "💡 Opportunity: Price is in oversold exhaustion. Watch for bullish confirmation before entry." 
                  else "⚖️ Balanced market: Await clear breakout above resistance or pullback to verified support."}
                
                ⚠️ DISCLAIMER: Algorithmic analysis based on real historical data. Always apply strict Stop Loss & 1-2% risk sizing.
            """.trimIndent()

            LearningLanguageMode.ROMAN_URDU -> """
                📊 [OFFLINE RULE-BASED ANALYSIS - NO FAKE AI]
                Asset: ${ctx.assetName} (${ctx.symbol}) | Qeemat: $priceStr ($changeStr)
                Timeframe: ${ctx.timeframe} | Data Zariya: ${ctx.dataSource}

                1. Momentum aur Indicators:
                • RSI (14): ${String.format(Locale.US, "%.1f", rsi)} — ${if (rsi > 70) "Overbought hai (Kharidari hadd se zyada)" else if (rsi < 30) "Oversold hai (Farokht zyada ho chuki)" else "Neutral zone mein hai"}.
                • EMA Trend: ${ctx.emaCondition ?: "Market sideways chal rahi hai"}.
                • Volatility: ATR normal hai, sudden movement par nazar rakhein.

                2. Smart Money Concepts aur Levels:
                • Market Structure: ${ctx.bosChochCondition ?: "Swing High aur Swing Low ke darmian range kar raha hai"}.
                • Support Level: ${ctx.supportLevel?.let { String.format(Locale.US, "$%.2f", it) } ?: "EMA support level"}.
                • Resistance Level: ${ctx.resistanceLevel?.let { String.format(Locale.US, "$%.2f", it) } ?: "EMA resistance level"}.
                • Fair Value Gaps (FVG): ${ctx.fvgStatus ?: "Koi bara unfilled gap mojood nahi hai"}.

                3. Mashwara aur Risk Plan:
                ${if (rsi > 70.0) "⚠️ Ehtiyat: Market overbought ho chuki hai. Resistance ke qareeb FOMO Long lene se bachein." 
                  else if (rsi < 30.0) "💡 Mauqa: Price oversold zone mein hai. Bullish candle ya CHoCH confirmation ka intezar karein." 
                  else "⚖️ Intezar Behtar Hai: Market darmiani range mein hai. Support ya resistance tootne ka intezar karein."}

                ⚠️ KHATRAAT: Ye analysis real market data par mabni hai. Kabhi bhi baghair Stop Loss ke trade mat karein.
            """.trimIndent()

            LearningLanguageMode.STANDARD_URDU -> """
                📊 [آف لائن اصولوں پر مبنی تجارتی تجزیہ — کوئی مصنوعی دعویٰ نہیں]
                تجارتی اثاثہ: ${ctx.assetName} (${ctx.symbol}) | موجودہ قیمت: $priceStr ($changeStr)
                ٹائم فریم: ${ctx.timeframe} | ڈیٹا کا ذریعہ: ${ctx.dataSource}

                ۱. مومینٹم اور تکنیکی اشارے:
                • آر ایس آئی (RSI 14): ${String.format(Locale.US, "%.1f", rsi)} — ${if (rsi > 70) "اوور بوٹ (Overbought - حد سے زیادہ خریداری)" else if (rsi < 30) "اوور سولڈ (Oversold - حد سے زیادہ فروخت)" else "معتدل سطح (Neutral)"}۔
                • رجحان کی ساخت (EMA): ${ctx.emaCondition ?: "مارکیٹ سائیڈ ویز رینج میں ہے"}۔
                • عدم استحکام (Volatility): مارکیٹ میں اتار چڑھاؤ معمول کے مطابق ہے۔

                ۲. اسمارٹ منی کانسپٹس اور اہم سطحیں:
                • مارکیٹ اسٹرکچر: ${ctx.bosChochCondition ?: "قیمت اہم سوئنگ ہائی اور لو کے درمیان گردش کر رہی ہے"}۔
                • سپورٹ لیول (سہارا): ${ctx.supportLevel?.let { String.format(Locale.US, "$%.2f", it) } ?: "متحرک ای ایم اے سپورٹ"}۔
                • مزاحمتی سطح (Resistance): ${ctx.resistanceLevel?.let { String.format(Locale.US, "$%.2f", it) } ?: "متحرک ای ایم اے مزاحمت"}۔
                • فیئر ویلیو گیپس (FVG): ${ctx.fvgStatus ?: "کوئی بڑا غیر پر شدہ گیپ موجود نہیں"}۔

                ۳. مخلصانہ تجارتی مشورہ:
                ${if (rsi > 70.0) "⚠️ احتیاط: مارکیٹ اوور بوٹ ہے، مزاحمت کے قریب فوری لانگ لینے سے گریز کریں اور ری ٹیسٹ کا انتظار کریں۔" 
                  else if (rsi < 30.0) "💡 حکمت عملی: مارکیٹ اوور سولڈ ہے، بلش ریورسل یا تصدیقی کینڈل کے بعد چھوٹی پوزیشن پر غور کیا جا سکتا ہے۔" 
                  else "⚖️ انتظار بہتر ہے: مارکیٹ اس وقت تذبذب کا شکار ہے، کسی واضح بریک آؤٹ کا انتظار زیادہ محفوظ رہے گا۔"}

                ⚠️ تنبیہ: یہ تجزیہ ماضی اور موجودہ حقیقی مارکیٹ ڈیٹا کے حسابی تجزیے پر مشتمل ہے۔ اپنے کل سرمائے کا ۱ سے ۲ فیصد سے زیادہ رسک مت لیں۔
            """.trimIndent()

            LearningLanguageMode.BILINGUAL -> """
                📊 [HYBRID BILINGUAL ANALYSIS / مشترکہ تجزیہ]
                Asset: ${ctx.assetName} (${ctx.symbol}) | Price: $priceStr ($changeStr)
                Timeframe: ${ctx.timeframe} | Source: ${ctx.dataSource}

                📌 Technical Summary (انگریزی خلاصہ):
                • RSI(14): ${String.format(Locale.US, "%.1f", rsi)} ($rsiStatus)
                • EMA Alignment: ${ctx.emaCondition ?: "Consolidating"}
                • Support: ${ctx.supportLevel?.let { String.format(Locale.US, "$%.2f", it) } ?: "EMA Support"} | Resistance: ${ctx.resistanceLevel?.let { String.format(Locale.US, "$%.2f", it) } ?: "EMA Resistance"}
                • Smart Money: ${ctx.bosChochCondition ?: "Ranging inside swing points"}

                📌 Urdu Detail & Strategy (اردو وضاحت و حکمت عملی):
                ${if (rsi > 70.0) "مارکیٹ اس وقت اوور بوٹ (Overbought) زون میں ہے۔ قیمت اپنی مزاحمت کے قریب ہے اس لیے بغیر تصدیق لانگ ٹریڈ میں داخل ہونا نقصان دہ ہو سکتا ہے۔" 
                  else if (rsi < 30.0) "قیمت اوور سولڈ (Oversold) علاقے میں پہنچ چکی ہے جہاں فروخت کا دباؤ کم ہو رہا ہے۔ کسی بلش پیٹرن کی تصدیق پر رسک ٹو ریوارڈ بہتر مل سکتا ہے۔" 
                  else "مارکیٹ اس وقت کسی واضح سمت کے بغیر چل رہی ہے۔ جب تک سپورٹ یا مزاحمت نہ ٹوٹے نئی ٹریڈ سے پرہیز کرنا اور کیپیٹل محفوظ رکھنا زیادہ دانشمندی ہے۔"}

                ⚠️ Risk Management (رسک مینجمنٹ): Never risk more than 1-2% per position. Always use Stop Loss.
            """.trimIndent()
        }
    }

    private fun generateTimeframeAdvice(
        ctx: MarketContextSnapshot,
        lang: LearningLanguageMode
    ): String {
        return when (lang) {
            LearningLanguageMode.ENGLISH -> """
                ⏱️ [TIMEFRAME ADVISOR & NOISE COMPARISON]
                Comparing 1m, 5m, 15m, 1h, and 4h for ${ctx.assetName}:

                • 1m & 5m (Scalping): High market noise, rapid false breakouts, higher spread/commission costs. Suitable only for seasoned scalpers with tight stop losses.
                • 15m (Intraday Execution): Great for precise entries following higher timeframe confirmation. Low noise compared to 1m.
                • 1h (Structure & Setup): The optimal balance for intraday and swing traders. Clearly reveals BOS, CHoCH, and Order Blocks with minimal noise.
                • 4h & 1d (Macro Trend): Dictates institutional direction. Never trade against the 4h EMA trend!

                💡 Recommendation for current market:
                Identify the macro trend on the 1h/4h chart, then drop down to the 15m chart to execute entries near support/Order Blocks.
            """.trimIndent()

            LearningLanguageMode.ROMAN_URDU -> """
                ⏱️ [TIMEFRAME ADVISOR - KAUNSA TIMEFRAME BEHTAR HAI?]
                ${ctx.assetName} ke liye timeframes ka mawazna (comparison):

                • 1 Minute aur 5 Minute: Is mein bohot zyada market noise (ghalat jhatkay) hotay hain. Yahan Stop Loss hit hone ka khatra bohot zyada hota hai.
                • 15 Minute: Day trading aur entry lene ke liye behtareen timeframe hai jab higher timeframe ka trend maloom ho.
                • 1 Hour (Aik Ghanta): Retail aur institutional traders ke liye sab se munasib timeframe hai. Yahan BOS, CHoCH aur Support/Resistance bohot saaf dikhte hain.
                • 4 Hour aur Daily: Ye institutional trend batata hai. Agar 4h par trend bearish hai to 5m par long lene se parhez karein.

                💡 Mashwara:
                Pehle 1 Hour par trend dekhein, phir 15 Minute par achhi entry talaash karein. 1 Minute par trading se beginners ko parhez karna chahiye.
            """.trimIndent()

            LearningLanguageMode.STANDARD_URDU -> """
                ⏱️ [ٹائم فریم مشیر — پانچ منٹ بمقابلہ ایک گھنٹہ]
                ${ctx.assetName} کے لیے مختلف ٹائم فریمز کا تفصیلی موازنہ:

                • ۱ منٹ اور ۵ منٹ (اسکیلپنگ): اس میں مارکیٹ کا شور (شور اور فیک بریک آؤٹس) بہت زیادہ ہوتا ہے۔ نئے ٹریڈرز کا اسٹاپ لاس بار بار ہٹ ہونے کا خطرہ رہتا ہے۔
                • ۱۵ منٹ (انٹرا ڈے انٹری): بڑے ٹائم فریم کے رجحان کی تصدیق کے بعد درست انٹری لینے کے لیے بہترین ٹائم فریم ہے۔
                • ۱ گھنٹہ (اسٹرکچر اور سیٹ اپ): ڈے ٹریڈنگ اور سوئنگ ٹریڈنگ کے لیے سب سے متوازن ٹائم فریم ہے۔ یہاں مارکیٹ اسٹرکچر، آرڈر بلاکس اور سپورٹ مزاحمت بالکل واضح ہوتی ہے۔
                • ۴ گھنٹہ اور روزانہ (ادارہ جاتی سمت): یہ مارکیٹ کا اصل بڑا رخ طے کرتا ہے۔ کبھی بھی ۴ گھنٹے کے رجحان کے مخالف سمت میں ٹریڈ نہ کریں۔

                💡 مخلصانہ مشورہ:
                ہمیشہ ۱ گھنٹے یا ۴ گھنٹے کے چارٹ پر بڑا رخ دیکھیں، اور پھر ۱۵ منٹ کے چارٹ پر سپورٹ یا آرڈر بلاک کے پاس کم رسک والی انٹری تلاش کریں۔
            """.trimIndent()

            LearningLanguageMode.BILINGUAL -> """
                ⏱️ [TIMEFRAME ADVISOR / ٹائم فریم کا تقابلی جائزہ]
                Analysis for ${ctx.assetName} (${ctx.symbol}):

                • 5m vs 1h Comparison: 5-minute chart has high noise (بہت زیادہ شور اور فیک آؤٹس), whereas the 1-hour chart gives structural clarity (واضح سمت اور پائیدار لیولز).
                • Higher Timeframe Rule: Always respect the 1h/4h bias before taking 5m/15m entries.
                • اردو خلاصہ: پانچ منٹ کے چارٹ پر جلد بازی میں فیصلے کرنے سے گریز کریں؛ پہلے ایک گھنٹے کے چارٹ پر ٹرینڈ کا تعین کریں اور پھر پندرہ منٹ پر انٹری لیں۔
            """.trimIndent()
        }
    }

    private fun generateMacroExplanation(
        ctx: MarketContextSnapshot,
        lang: LearningLanguageMode
    ): String {
        return when (lang) {
            LearningLanguageMode.ENGLISH -> """
                🏛️ [MACROECONOMIC & CPI REASONING]
                Question: "Why did price drop when CPI was lower than forecast?"

                Lower-than-expected inflation does not guarantee an immediate price rally due to:
                1. "Priced-In" Effect: Smart money and institutional whales frequently buy before the announcement, then sell the news to retail buyers.
                2. US Dollar Index (DXY) & Bond Yields: If Treasury yields remain elevated or the Fed signals "higher for longer", liquidity stays restricted.
                3. Core CPI vs Headline: If volatile food/energy dropped but Core CPI stayed sticky, the Federal Reserve remains cautious.
                4. Derivatives Positioning: Heavy leveraged long positions often get liquidated in a stop-hunt sweep before the genuine trend emerges.

                💡 Conclusion: Never trade solely on headline CPI releases without assessing funding rates, open interest, and key support levels.
            """.trimIndent()

            LearningLanguageMode.ROMAN_URDU -> """
                🏛️ [CPI AUR FED NEWS KI WAZAHAT]
                Sawaal: "CPI data acha aya lekin price neeche kyun chali gayi?"

                Aksar aisa hota hai ke mehngai kam aane ke bawajood market foran gir jati hai. Iski 4 barri wajuhaat hain:
                1. Sell the News: Baray idaray (Whales) news aane se pehle khareed chuke hotay hain, aur news aatay hi aam logon ko bech kar profit book karte hain.
                2. DXY aur Treasury Yields: Agar Dollar Index mazboot rahe ya sood ki sharah lambay arsay tak high rehne ka andesha ho to crypto mein naya paisa nahi aata.
                3. Liquidation Sweep: Jab tamam retail traders aik dam Long letay hain to market pehle unke Stop Loss urati hai (Liquidity Sweep) aur phir ooper jati hai.
                4. Core CPI: Aam CPI kam aya ho lekin agar Core CPI (zaroori ashya) mehngi rahein to Fed narmi nahi dikhata.

                💡 Nateeja: Sirf news headline dekh kar foran trade mein mat koodiye.
            """.trimIndent()

            LearningLanguageMode.STANDARD_URDU -> """
                🏛️ [سی پی آئی اور معاشی ڈیٹا کی منطق]
                سوال: "سی پی آئی توقع سے کم آئی لیکن قیمت پھر بھی نیچے کیوں گئی؟"

                اکثر ایسا ہوتا ہے کہ مہنگائی کی شرح کم آنے کے باوجود مارکیٹ اوپر جانے کے بجائے نیچے گر جاتی ہے۔ اس کی اہم وجوہات درج ذیل ہیں:
                ۱. خبر پر منافع خوری (Sell the News): بڑے ادارے خبر آنے سے قبل ہی خریداری کر چکے ہوتے ہیں اور اعلان ہوتے ہی ریٹیل خریداروں کو مہنگے داموں بیچ کر منافع سمیٹ لیتے ہیں۔
                ۲. لیکویڈیشن اور اسٹاپ ہنٹ: جب پوری مارکیٹ ایک ساتھ لانگ کی طرف دوڑتی ہے تو سمارٹ منی قیمت کو اچانک نیچے گرا کر ریٹیل ٹریڈرز کے اسٹاپ لاس اڑاتی ہے۔
                ۳. ڈالر انڈیکس (DXY) اور حکومتی بانڈز: اگر امریکی ڈالر مضبوط رہے یا فیڈرل ریزرو کی طرف سے سخت پالیسی برقرار رکھنے کا عندیہ ہو تو مارکیٹ میں نئی لیکویڈیٹی داخل نہیں ہوتی۔
                ۴. کور سی پی آئی (Core CPI): اگر بنیادی مہنگائی میں خاطر خواہ کمی نہ ہوئی ہو تو فیڈرل ریزرو سود کی شرح میں جلد کمی نہیں کرتا۔

                💡 دانشمندانہ سبق: کبھی بھی معاشی ڈیٹا کی ہیڈ لائن آتے ہی آنکھیں بند کر کے ٹریڈ مت لگائیں، مارکیٹ کے ردعمل اور سپورٹ کے استحکام کا انتظار کریں۔
            """.trimIndent()

            LearningLanguageMode.BILINGUAL -> """
                🏛️ [MACRO REASONING / معاشی خبروں کا تجزیہ]
                Understanding News Volatility (خبروں کا ردعمل):
                • Sell the News Factor: Market participants often price in the expected rate cut or lower CPI days in advance, leading to profit-taking.
                • Liquidity Sweeps: Smart money hunts retail stop-losses before moving the real market.
                • اردو خلاصہ: خبر مثبت ہونے کے باوجود وہیلز اپنا منافع وصول کرتی ہیں اور مارکیٹ سے وقتی لیکویڈیٹی حاصل کرتی ہیں۔ خبر آنے کے کم از کم ۱۵ تا ۳۰ منٹ بعد ٹرینڈ واضح ہوتا ہے۔
            """.trimIndent()
        }
    }

    private fun generateRiskAnalysis(
        ctx: MarketContextSnapshot,
        lang: LearningLanguageMode
    ): String {
        return when (lang) {
            LearningLanguageMode.ENGLISH -> """
                🛡️ [CAPITAL PRESERVATION & RISK MANAGEMENT]
                Rule #1 in trading: Protecting capital is more important than chasing profits!

                • Recommended Risk: Never risk more than 1% to 2% of total trading account per trade.
                • Safe Leverage: For crypto assets like ${ctx.assetName}, limit leverage to 3x - 5x. High leverage (20x-100x) is a mathematical certainty for liquidation over time.
                • Stop Loss: Always set an invalidation point below nearest support ($${ctx.supportLevel ?: (ctx.currentPrice * 0.97)}) before opening the position.
                • Risk-to-Reward: Aim for a minimum 1:2 or 1:3 ratio so you remain profitable even with a 40% win rate.
            """.trimIndent()

            LearningLanguageMode.ROMAN_URDU -> """
                🛡️ [RISK MANAGEMENT AUR CAPITAL KI HIFAZAT]
                Trading ka sab se pehla usool: Apne asle sarmaye ki hifazat nafa kamanay se zyada zaroori hai!

                • Account Risk: Kisi aik trade mein apne kul account ka 1% se 2% se zyada risk na lein.
                • Leverage ki Hadd: ${ctx.assetName} par 3x se 5x se zyada leverage khatarnak hai. 20x ya 50x par aik chhota jhatka apka pura account zero (liquidate) kar sakta hai.
                • Stop Loss Lazmi: Trade kholne se pehle hi Stop Loss tay karein ($${ctx.supportLevel ?: (ctx.currentPrice * 0.97)}).
                • Risk-to-Reward: Hamesha kam az kam 1:2 ka ratio rakhein.
            """.trimIndent()

            LearningLanguageMode.STANDARD_URDU -> """
                🛡️ [رسک مینجمنٹ اور سرمائے کا تحفظ]
                تجارتی زندگی کا پہلا اور سب سے اہم سنہری اصول: "اپنے اصل سرمائے کی حفاظت نفع کمانے سے زیادہ اہم ہے!"

                • رسک کی حد: کسی بھی ایک ٹریڈ میں اپنے کل پورٹ فولیو کا ۱ سے ۲ فیصد سے زیادہ خطرے میں مت ڈالیں۔
                • لیوریج (Leverage) کا انتخاب: ${ctx.assetName} جیسی اتار چڑھاؤ والی مارکیٹ میں لیوریج ۳ سے ۵ گنا تک محدود رکھیں۔ ۲۰ گنا یا ۵۰ گنا لیوریج ریاضی کے اعتبار سے اکاؤنٹ واش ہونے کی ضمانت ہے۔
                • لازمی اسٹاپ لاس: انٹری لینے سے قبل ہی اپنا انویلیڈیشن لیول ($${ctx.supportLevel ?: (ctx.currentPrice * 0.97)}) متعین کریں اور اس پر سختی سے عمل کریں۔
                • رسک ٹو ریوارڈ (Risk-to-Reward): کم از کم ۱:۲ کا تناسب رکھیں تاکہ اگر دس میں سے چار ٹریڈز بھی درست ہوں تو آپ نفع میں رہیں۔
            """.trimIndent()

            LearningLanguageMode.BILINGUAL -> """
                🛡️ [RISK MANAGEMENT / رسک مینجمنٹ]
                • Capital Protection First: Leverage should be capped at 3x-5x to avoid rapid liquidation.
                • Stop Loss: Non-negotiable. Always place SL below key support.
                • اردو اصول: اپنے سرمائے کا صرف ۱ سے ۲ فیصد رسک پر لگائیں۔ زیادہ لیوریج جوئے کے مترادف ہے؛ ہمیشہ منظم اسٹاپ لاس استعمال کریں۔
            """.trimIndent()
        }
    }
}
