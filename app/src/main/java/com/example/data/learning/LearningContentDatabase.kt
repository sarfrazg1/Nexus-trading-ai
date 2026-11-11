package com.example.data.learning

import java.util.Locale

/**
 * 100% Offline Educational Content Database for NEXUS TRADING AI.
 * Contains 56 comprehensive, beginner-friendly lessons across 7 categories
 * in English and Roman Urdu with practical examples, pro tips, and risk warnings.
 */
object LearningContentDatabase {

    val allLessons: List<LessonItem> = listOf(
        // =========================================================================
        // CATEGORY A: TRADING BASICS (7 Lessons)
        // =========================================================================
        LessonItem(
            id = "what_is_trading",
            termKey = "TRADING",
            category = LessonCategory.TRADING_BASICS,
            titleEnglish = "What is Trading?",
            titleRomanUrdu = "Trading Kya Hai?",
            summaryEnglish = "Trading is the active buying and selling of financial assets (crypto, stocks, commodities) to profit from price fluctuations.",
            summaryRomanUrdu = "Trading ka matlab munafay ke husool ke liye financial assets (crypto, gold, currencies) ko sasti qeemat par khareedna aur mehangi par bechna hai.",
            detailEnglish = "Unlike long-term investing where assets are held for years regardless of interim volatility, trading seeks to capture short to medium-term price swings. Traders use technical analysis (charts), fundamental analysis (news/economics), and risk management to generate returns across timeframes ranging from minutes (scalping) to weeks (swing trading).",
            detailRomanUrdu = "Investing mein banda saalon tak coin hold karta hai, jabkay Trading mein short-term qeematon ke utaar charhao (price movements) se faida uthaya jata hai. Aik kamyab trader charts ko parhna seekhta hai, jazbaat (greed aur fear) ko control karta hai aur hamesha stop loss lagata hai.",
            practicalExampleEnglish = "Buying 1 BTC at $60,000 and selling it at $64,000 three days later yields a $4,000 gross profit.",
            practicalExampleRomanUrdu = "Aap ne Bitcoin $60,000 par buy kiya aur jab qeemat $64,000 par pohanchi to bech diya, jis se $4,000 ka saaf munafa hua.",
            proTipEnglish = "Treat trading as a probability business, not gambling. Protecting your capital is rule number one.",
            proTipRomanUrdu = "Trading ko jowa na samjhein balkay karobar ki tarah lein jahan har trade mein calculation zaroori hai.",
            riskWarningEnglish = "Trading involves substantial risk of financial loss. Never trade with money you cannot afford to lose completely.",
            riskWarningRomanUrdu = "Trading mein nuqsan ka khatra rehta hai. Sirf wahi paisa lagayein jiske khonay ka aap bardasht kar saktay hon.",
            relatedTerms = listOf("SPOT", "FUTURES", "RISK_REWARD")
        ),

        LessonItem(
            id = "spot_trading",
            termKey = "SPOT",
            category = LessonCategory.TRADING_BASICS,
            titleEnglish = "Spot Trading",
            titleRomanUrdu = "Spot Trading (Haqeeqi Milkiyat)",
            summaryEnglish = "Spot trading involves purchasing actual crypto or assets for immediate delivery and full ownership.",
            summaryRomanUrdu = "Spot trading mein aap asal coin ke malik bantay hain aur usay jab tak chahein baghair kisi liquidation ke hold kar saktay hain.",
            detailEnglish = "When trading spot, you exchange currency directly for the asset without leverage. You cannot be liquidated by price drops—even if the asset drops 90%, you still own the exact coin count until you decide to sell. Spot is the safest foundation for all beginners.",
            detailRomanUrdu = "Spot market mein koi leverage nahi hoti aur na hi account wash (liquidation) hone ka dar hota hai. Agar coin ki qeemat 50% gir bhi jaye, tab bhi apke paas coins ki tadad poori rehti hai aur qeemat wapis aanay tak hold kiya ja sakta hai.",
            practicalExampleEnglish = "Exchanging 1,000 USDT on Binance to receive 0.015 BTC directly into your spot wallet.",
            practicalExampleRomanUrdu = "Binance spot wallet mein $1,000 de kar Solana (SOL) buy karna aur apnay wallet mein mehfooz rakhna.",
            proTipEnglish = "Beginners should spend at least 6 months mastering spot trading before ever touching futures.",
            proTipRomanUrdu = "Naye traders ko pehle 6 maheenay sirf spot trading karni chahiye taake charts aur emotions samajh aa sakein.",
            relatedTerms = listOf("FUTURES", "MARKET_ORDERS")
        ),

        LessonItem(
            id = "futures_trading",
            termKey = "FUTURES",
            category = LessonCategory.TRADING_BASICS,
            titleEnglish = "Futures & Derivative Trading",
            titleRomanUrdu = "Futures Trading (Maa'hada ki Trading)",
            summaryEnglish = "Trading contracts representing an asset's future price using leverage, allowing traders to profit from both rising and falling markets.",
            summaryRomanUrdu = "Futures mein aap coin ke malik nahi bantay balkay qeemat ke ooper ya neechay janay par contract trade karte hain.",
            detailEnglish = "Futures contracts allow traders to speculate with leverage (e.g., 2x to 100x). You can take a LONG position if you expect prices to increase, or a SHORT position if you expect prices to fall. However, leverage introduces a liquidation price where your entire margin can be seized by the exchange if the market moves against you.",
            detailRomanUrdu = "Futures trading mein aap Long (qeemat barhne par profit) aur Short (qeemat girnay par profit) dono kar saktay hain. Exchange aap ko leverage (udhar) deta hai, lekin agar trade ghalat simt chali jaye to Liquidation Price hit honay par apka sara sarmaya zaya ho jata hai.",
            practicalExampleEnglish = "Using $500 margin with 5x leverage opens a $2,500 position. A 4% favorable move yields a 20% gain ($100 profit).",
            practicalExampleRomanUrdu = "$100 margin ke sath 5x leverage le kar $500 ka trade kholna. Agar market 2% oper gayi to apko 10% profit ($10) milega.",
            proTipEnglish = "Never use futures without a predetermined hard Stop Loss. Slippage on high leverage can wipe out your balance in seconds.",
            proTipRomanUrdu = "Futures mein baghair Stop Loss ke trade lagana khud-kushi ke barabar hai. Hamesha trade lagatay waqt hi SL set karein.",
            riskWarningEnglish = "Over 90% of retail futures traders lose money due to excessive leverage. Keep leverage strictly below 5x.",
            riskWarningRomanUrdu = "90% se zyada log high leverage ki wajah se apna account wash kar letay hain. 3x-5x se zyada leverage hargiz na lein.",
            relatedTerms = listOf("LEVERAGE", "LIQUIDATION", "SHORT")
        ),

        LessonItem(
            id = "long_and_short",
            termKey = "LONG_SHORT",
            category = LessonCategory.TRADING_BASICS,
            titleEnglish = "Long vs. Short Positions",
            titleRomanUrdu = "Long aur Short kya hotay hain?",
            summaryEnglish = "A Long position profits when price goes UP; a Short position profits when price goes DOWN.",
            summaryRomanUrdu = "Long ka matlab qeemat barhne se faida uthana, aur Short ka matlab qeemat girnay se munafa kamana hai.",
            detailEnglish = "Going Long means buying low to sell high. Going Short in derivatives involves borrowing the asset, selling it at the current high price, and buying it back cheaper later to return to the lender, pocketing the difference as profit.",
            detailRomanUrdu = "Agar aap ko lagta hai ke Bitcoin $65,000 se $70,000 par jayega, to aap 'LONG' position letay hain. Agar aap ko lagta hai ke $65,000 se gir kar $60,000 par aayega, to aap 'SHORT' position open karte hain.",
            practicalExampleEnglish = "Shorting Ethereum at $3,500 and closing at $3,200 nets $300 profit per ETH.",
            practicalExampleRomanUrdu = "Gold par $2,650 se Short lagaya aur $2,600 par close kar diya, to girawat ke doran munafa lock ho gaya.",
            proTipEnglish = "Don't fight the higher timeframe trend. If the 1D chart is in a strong uptrend, favor Long setups.",
            proTipRomanUrdu = "Bari timeframe ke trend ke sath chalein. Agar 1D chart par market oper ja rahi hai to short karnay se guraiz karein.",
            relatedTerms = listOf("FUTURES", "STOP_LOSS", "TAKE_PROFIT")
        ),

        LessonItem(
            id = "market_vs_limit_orders",
            termKey = "ORDERS",
            category = LessonCategory.TRADING_BASICS,
            titleEnglish = "Market Orders vs. Limit Orders",
            titleRomanUrdu = "Market Order aur Limit Order mein Farq",
            summaryEnglish = "Market orders execute instantly at the current price; Limit orders execute only at your desired price or better.",
            summaryRomanUrdu = "Market order foran moujooda qeemat par execute hota hai, jabkay Limit order aap ki pasandeeda qeemat par intezar karta hai.",
            detailEnglish = "A Market order guarantees immediate execution but incurs higher taker fees and price slippage during volatile moves. A Limit order sits in the order book, guarantees your price (or better), pays lower maker fees, but risks not getting filled if price turns around before reaching it.",
            detailRomanUrdu = "Market order ka faida ye hai ke trade foran lag jati hai, lekin fees zyada hoti hai. Limit order mein aap pehle se bolte hain ke 'jab BTC $62,500 par aaye tab khareedna'. Is mein fees kam hoti hai aur behtareen entry milti hai.",
            practicalExampleEnglish = "Setting a limit buy at $63,000 when BTC is at $63,800 ensures you buy at support rather than chasing.",
            practicalExampleRomanUrdu = "Bitcoin $64,000 par chal raha hai. Aap ne $62,800 par limit buy lagayi, taake jab market pullback le to automatic sasti entry mil jaye.",
            proTipEnglish = "Professional traders enter 80%+ of positions via Limit orders to save on fees and avoid emotional chasing.",
            proTipRomanUrdu = "Hamesha limit orders istemal karein taake exchange ki fees bache aur FOMO mein ghalat entry na ho.",
            relatedTerms = listOf("ORDER_BOOK", "SPOT")
        ),

        LessonItem(
            id = "leverage_and_margin",
            termKey = "LEVERAGE",
            category = LessonCategory.TRADING_BASICS,
            titleEnglish = "Leverage and Margin",
            titleRomanUrdu = "Leverage aur Margin ki Haqeeqat",
            summaryEnglish = "Margin is your personal capital collateral; Leverage is the borrowed multiplier provided by the exchange.",
            summaryRomanUrdu = "Margin apka apna sarmaya hai, aur Leverage exchange se liya gaya udhari multiplier hai.",
            detailEnglish = "Leverage magnifies both gains and losses. With 10x leverage, a 1% price move becomes a 10% return on your margin. However, an adverse 10% price move completely wipes out your entire margin (liquidation).",
            detailRomanUrdu = "Leverage do-dhari talwar hai. Agar aap 10x leverage lagate hain to 1% munafay par 10% profit milta hai, lekin agar market 10% ulat chali jaye to apka sara paisa zero (liquidate) ho jata hai.",
            practicalExampleEnglish = "With $200 margin and 5x leverage, your position size is $1,000. Risk is magnified 5-fold.",
            practicalExampleRomanUrdu = "$100 margin ke sath 3x leverage par $300 ki trade khul sakti hai.",
            proTipEnglish = "Treat leverage as a capital efficiency tool, not an instrument to get rich quick.",
            proTipRomanUrdu = "Kabhi bhi jaldi ameer honay ke chakar mein 20x ya 50x leverage na lein.",
            riskWarningEnglish = "High leverage is the single greatest cause of retail trader account destruction in crypto markets.",
            riskWarningRomanUrdu = "Crypto mein account wash hone ki sab se bari wajah zyada leverage ka istemal hai.",
            relatedTerms = listOf("LIQUIDATION", "FUTURES")
        ),

        LessonItem(
            id = "cross_vs_isolated_margin",
            termKey = "MARGIN_MODE",
            category = LessonCategory.TRADING_BASICS,
            titleEnglish = "Cross Margin vs. Isolated Margin",
            titleRomanUrdu = "Cross Margin aur Isolated Margin",
            summaryEnglish = "Isolated margin limits risk strictly to the trade allocation; Cross margin risks your entire account balance to avoid liquidation.",
            summaryRomanUrdu = "Isolated margin mein sirf trade ka paisa risk par hota hai, jabkay Cross margin mein apka poora wallet risk par lag jata hai.",
            detailEnglish = "In Isolated Margin mode, if you allocate $100 to a trade and it hits liquidation, you only lose that $100. In Cross Margin mode, the liquidation engine can pull funds from your entire futures wallet to keep the position open, potentially draining your whole account during an unexpected flash crash.",
            detailRomanUrdu = "Isolated margin sab se mehfooz hai kyunki agar trade ghalat ho jaye to sirf wahi paisa jata hai jo aap ne trade mein dala tha. Cross margin mein agar market ulat jaye to exchange apke wallet ka baki paisa bhi katna shuru kar deta hai.",
            practicalExampleEnglish = "Using Isolated margin on a $50 trade ensures the remaining $2,000 in your futures account is 100% safe.",
            practicalExampleRomanUrdu = "$1,000 ke account mein se $50 Isolated mode mein trade lagana taake baki $950 hamesha mehfooz rahein.",
            proTipEnglish = "Always default to Isolated Margin mode to prevent unexpected catastrophic account liquidations.",
            proTipRomanUrdu = "Hamesha Isolated Margin mode use karein taake kisi flash crash mein poora account wash na ho.",
            relatedTerms = listOf("LEVERAGE", "RISK_MANAGEMENT")
        ),

        // =========================================================================
        // CATEGORY B: TECHNICAL ANALYSIS (12 Lessons)
        // =========================================================================
        LessonItem(
            id = "candlestick_charts",
            termKey = "CANDLESTICKS",
            category = LessonCategory.TECHNICAL_ANALYSIS,
            titleEnglish = "Candlestick Charts",
            titleRomanUrdu = "Candlestick Charts ko Parhna",
            summaryEnglish = "Japanese candlesticks visually display the Open, High, Low, and Close (OHLC) price action over a specific timeframe.",
            summaryRomanUrdu = "Candlestick chart kisi makhsoos waqt mein Open, High, Low aur Close qeemat ko wazeh dikhata hai.",
            detailEnglish = "A green (bullish) candle forms when the Close is higher than the Open. A red (bearish) candle forms when the Close is lower than the Open. The thick middle section is called the Body (representing transaction volume), while the thin lines at top and bottom are Wicks or Shadows (representing rejected high and low price tests).",
            detailRomanUrdu = "Green candle ka matlab qeemat ooper band hui (kharidaar taqatwar thay). Red candle ka matlab qeemat neechay band hui (bechnay walay taqatwar thay). Candle ki body asal volume dikhati hai aur lambi wicks (sholay) rejection ko zahir karti hain.",
            practicalExampleEnglish = "A long lower wick on a 4H candle at support demonstrates that sellers tried to push down, but aggressive buyers stepped in.",
            practicalExampleRomanUrdu = "Agar support level par lambi neechay wali wick banay to iska matlab hai ke buyers ne achanak entry li aur qeemat ko wapis ooper khench liya.",
            proTipEnglish = "Wicks tell you the story of price rejection, while candle bodies reveal true conviction and closing consensus.",
            proTipRomanUrdu = "Wicks rejection dikhati hain aur candle ki body asal faisla dikhati hai.",
            relatedTerms = listOf("SUPPORT_RESISTANCE", "ORDER_BLOCKS")
        ),

        LessonItem(
            id = "support_and_resistance",
            termKey = "SUPPORT_RESISTANCE",
            category = LessonCategory.TECHNICAL_ANALYSIS,
            titleEnglish = "Support and Resistance Zones",
            titleRomanUrdu = "Support aur Resistance (Floor aur Chhat)",
            summaryEnglish = "Support acts as a price floor where buying interest halts a decline; Resistance acts as a ceiling where selling stops an advance.",
            summaryRomanUrdu = "Support zameen (floor) ki tarah qeemat ko girnay se rokti hai, aur Resistance chhat ki tarah qeemat ko ooper janay se rokti hai.",
            detailEnglish = "Support and resistance are zones of liquidity rather than single exact dollar prices. When price breaks cleanly above a resistance level, that old resistance frequently flips into new support (S/R Flip), offering retest entry opportunities.",
            detailRomanUrdu = "Support par kharidaar intizar kar rahe hotay hain, is liye qeemat wahan se bounce karti hai. Resistance par bechnay walay hotay hain is liye qeemat wahan se wapis aati hai. Jab resistance toot jaye to wo support ban jati hai.",
            practicalExampleEnglish = "Bitcoin repeatedly bouncing off $60,000 establishes $60k as a strong psychological support zone.",
            practicalExampleRomanUrdu = "Gold bar bar $2,600 se bounce le raha hai, to $2,600 aik mazboot support zone ban gaya.",
            proTipEnglish = "Look for the confluence of horizontal S/R zones with high-volume nodes and Fibonacci levels.",
            proTipRomanUrdu = "Support aur Resistance ko aik line ke bajaye aik 'Zone' (range) samjhein.",
            relatedTerms = listOf("CANDLESTICKS", "ORDER_BLOCKS", "FIBONACCI")
        ),

        LessonItem(
            id = "trendlines",
            termKey = "TRENDLINES",
            category = LessonCategory.TECHNICAL_ANALYSIS,
            titleEnglish = "Trendlines & Market Channels",
            titleRomanUrdu = "Trendlines aur Channels",
            summaryEnglish = "Diagonal lines drawn across consecutive swing lows (uptrend) or swing highs (downtrend) to visualize trajectory.",
            summaryRomanUrdu = "Trendlines market ke rukh (direction) ko dikhanay ke liye swing points ko aapas mein jor kar banai jati hain.",
            detailEnglish = "A valid trendline requires at least 3 confirmed touches. An ascending trendline acts as dynamic support during bull runs, while a descending trendline acts as dynamic resistance in bear phases. A clean body close through a trendline signals potential trend exhaustion or reversal.",
            detailRomanUrdu = "Kam az kam 3 touches ke sath banai gayi trendline authentic hoti hai. Uptrend mein trendline neechay banai jati hai aur support deti hai. Jab ye toot jaye to trend badal sakta hai.",
            practicalExampleEnglish = "Drawing a line connecting Higher Lows at $55k, $58k, and $61k provides a trend guide for buying dips.",
            practicalExampleRomanUrdu = "Uptrend mein har naye Higher Low ko jor kar line khenchein. Jab price wahan aaye to confirmation dekh kar buy karein.",
            proTipEnglish = "Never force a trendline across candle bodies; anchor it cleanly across candle wicks.",
            proTipRomanUrdu = "Trendline hamesha wicks ke sath touch karein, candles ke darmiyan se zabardasti na guzarein.",
            relatedTerms = listOf("MARKET_STRUCTURE", "BOS")
        ),

        LessonItem(
            id = "ema_and_sma",
            termKey = "MOVING_AVERAGES",
            category = LessonCategory.TECHNICAL_ANALYSIS,
            titleEnglish = "Moving Averages (EMA & SMA)",
            titleRomanUrdu = "Moving Averages (EMA aur SMA)",
            summaryEnglish = "Technical indicators that smooth out noisy price data to identify prevailing trend direction and dynamic support/resistance.",
            summaryRomanUrdu = "Moving average pichli candles ki average qeemat nikaal kar market ka wazeh trend dikhati hai.",
            detailEnglish = "SMA (Simple Moving Average) calculates the arithmetic mean of closing prices. EMA (Exponential Moving Average) applies greater weight to recent candles, reacting much faster to sudden market reversals. Key benchmarks include the EMA 9 & 20 (short-term momentum), EMA 50 (medium-term trend), and SMA/EMA 200 (institutional bull/bear divider).",
            detailRomanUrdu = "EMA taaza qeematon ko zyada aehmiyat deta hai is liye jaldi ishara karta hai. Jab 20 EMA 50 EMA ke ooper ho to market uptrend mein hoti hai. 200 EMA ke ooper rehna Bull market ki nishani hai.",
            practicalExampleEnglish = "The 'Golden Cross' occurs when the 50 SMA crosses above the 200 SMA, signaling long-term bullish continuation.",
            practicalExampleRomanUrdu = "Jab 50 EMA 200 EMA ko neechay se ooper cross karti hai to isay Golden Cross kehtay hain aur market tezi se barhti hai.",
            proTipEnglish = "In strong trends, the 20 EMA and 50 EMA serve as premier pullback retest zones for entry.",
            proTipRomanUrdu = "Mazboot trend mein jab qeemat 20 ya 50 EMA par wapis aaye to buy karne ka behtareen moqa hota hai.",
            relatedTerms = listOf("TRENDLINES", "MACD")
        ),

        LessonItem(
            id = "rsi_relative_strength_index",
            termKey = "RSI",
            category = LessonCategory.TECHNICAL_ANALYSIS,
            titleEnglish = "RSI (Relative Strength Index)",
            titleRomanUrdu = "RSI Indicator kya hai?",
            summaryEnglish = "A bounded momentum oscillator (0 to 100) measuring the speed and magnitude of recent price changes to identify overbought or oversold conditions.",
            summaryRomanUrdu = "RSI aik oscillator hai jo batata hai ke market mein kharidari had se zyada ho chuki hai (Overbought) ya farokht (Oversold).",
            detailEnglish = "RSI values above 70 indicate an overbought market ripe for a pullback or consolidation. RSI values below 30 indicate an oversold market where sellers may be exhausted. Additionally, RSI divergence (e.g., price makes Higher High while RSI makes Lower High) offers one of the most reliable early trend reversal warnings in technical trading.",
            detailRomanUrdu = "Agar RSI 70 se ooper ho to iska matlab market bohot mehangi ho chuki hai aur correction aa sakti hai. Agar RSI 30 se neechay ho to market sasti hai aur bounce aa sakta hai. Bullish Divergence par buy karna behtareen hota hai.",
            practicalExampleEnglish = "Price makes a lower low at $58,000, but RSI forms a higher low at 34 (Bullish Divergence), signaling an imminent rally.",
            practicalExampleRomanUrdu = "Price ne naya low banaya lekin RSI ne ooper ka rukh kiya (Bullish Divergence), is ke foran baad price ooper chali gayi.",
            proTipEnglish = "In strong bull runs, RSI can stay overbought (>70) for weeks. Never short purely because RSI is high.",
            proTipRomanUrdu = "Mazboot bull run mein RSI kaafi din 70 ke ooper reh sakta hai, sirf RSI dekh kar short mat karein.",
            relatedTerms = listOf("MACD", "STOCHASTIC")
        ),

        LessonItem(
            id = "macd",
            termKey = "MACD",
            category = LessonCategory.TECHNICAL_ANALYSIS,
            titleEnglish = "MACD (Moving Average Convergence Divergence)",
            titleRomanUrdu = "MACD Indicator",
            summaryEnglish = "A trend-following momentum indicator displaying the relationship between two moving averages of price.",
            summaryRomanUrdu = "MACD indicator market ki raftaar (momentum) aur trend ke badalnay ka pehle se ishara deta hai.",
            detailEnglish = "MACD consists of the MACD Line (12 EMA minus 26 EMA), the Signal Line (9 EMA of MACD Line), and the Histogram (visualizing the gap between the two). When the MACD line crosses above the Signal line below zero, a bullish buy signal is generated. When it crosses below above zero, a bearish sell signal is generated.",
            detailRomanUrdu = "Jab MACD ki blue line red signal line ko neechay se ooper cross kare to buy signal banta hai. Histogram jab green bars barha raha ho to iska matlab kharidari ka zor barh raha hai.",
            practicalExampleEnglish = "A bullish MACD crossover occurring on the 1D chart with expanding green histogram confirms buying acceleration.",
            practicalExampleRomanUrdu = "Daily chart par MACD ka Golden Cross hona baray rally ka aaghaz sabit hota hai.",
            proTipEnglish = "Trade MACD crosses that occur in the direction of the dominant higher-timeframe trend.",
            proTipRomanUrdu = "MACD crossover tab sab se achha kaam karta hai jab Higher Timeframe bhi usi rukh mein ho.",
            relatedTerms = listOf("RSI", "MOVING_AVERAGES")
        ),

        LessonItem(
            id = "stochastic_rsi",
            termKey = "STOCHASTIC",
            category = LessonCategory.TECHNICAL_ANALYSIS,
            titleEnglish = "Stochastic RSI & Oscillator",
            titleRomanUrdu = "Stochastic RSI",
            summaryEnglish = "An indicator of an indicator, Stochastic RSI applies the Stochastic formula to RSI values, creating an ultra-sensitive momentum trigger.",
            summaryRomanUrdu = "Stochastic RSI aam RSI se zyada taiz hota hai aur market ke turning points ko foran pakarta hai.",
            detailEnglish = "Stochastic RSI oscillates between 0 and 100. Readings below 20 represent extreme oversold conditions, while readings above 80 represent extreme overbought territory. When the fast %K line crosses above the slow %D line below 20, it signals a high-precision entry timing trigger for pullbacks.",
            detailRomanUrdu = "Stochastic RSI 20 se neechay ho to foran bounce ki umeed hoti hai. Jab blue line orange line ko neechay cross kare to ye turning point ka signal hota hai.",
            practicalExampleEnglish = "In a 1H uptrend, waiting for Stochastic RSI to dip below 15 and cross bullishly gives a tight-risk pullback entry.",
            practicalExampleRomanUrdu = "Uptrend mein jab Stochastic RSI 10-15 par aa jaye to buy order lagana sab se tight stop loss deta hai.",
            proTipEnglish = "Use Stoch RSI primarily for execution timing, not for determining overall trend direction.",
            proTipRomanUrdu = "Stochastic RSI ko entry ka time tay karne ke liye use karein, trend janne ke liye nahi.",
            relatedTerms = listOf("RSI", "BOLLINGER_BANDS")
        ),

        LessonItem(
            id = "bollinger_bands",
            termKey = "BOLLINGER_BANDS",
            category = LessonCategory.TECHNICAL_ANALYSIS,
            titleEnglish = "Bollinger Bands & The Volatility Squeeze",
            titleRomanUrdu = "Bollinger Bands aur Volatility Squeeze",
            summaryEnglish = "A volatility envelope consisting of a middle 20 SMA and two standard deviation bands that expand and contract with market volatility.",
            summaryRomanUrdu = "Bollinger bands batate hain ke market mein tezi aanay wali hai ya market sust (silent) ho chuki hai.",
            detailEnglish = "When volatility drops, the upper and lower bands pinch closely together (known as a 'Bollinger Squeeze'). Squeezes are invariably followed by massive explosive breakouts. Over 90% of price action remains contained within the bands; touching the outer band signifies a statistical extreme.",
            detailRomanUrdu = "Jab dono bands aapas mein bilkul qareeb aa jayein to isay 'Squeeze' kehtay hain. Iska matlab hai ke achanak aik bohot bara dhamaka (breakout) hone wala hai.",
            practicalExampleEnglish = "Bitcoin consolidating for 10 days with bands squeezing tightly before erupting $4,000 in a single 4-hour candle.",
            practicalExampleRomanUrdu = "Band patlay hone ke baad jab candle band ke ooper close ho to market mein zabardast tezi aati hai.",
            proTipEnglish = "Do not blindly short the upper band during a squeeze; a breakout riding the outer band can continue for extended periods.",
            proTipRomanUrdu = "Squeeze ke doran band ke ooper janay par foran short mat karein kyunki breakout bohot lamba ja sakta hai.",
            relatedTerms = listOf("ATR", "VOLATILITY")
        ),

        LessonItem(
            id = "vwap",
            termKey = "VWAP",
            category = LessonCategory.TECHNICAL_ANALYSIS,
            titleEnglish = "VWAP (Volume-Weighted Average Price)",
            titleRomanUrdu = "VWAP (Volume Weighted Average Price)",
            summaryEnglish = "The true benchmark price of an asset based on both volume and price, used heavily by institutional algorithms.",
            summaryRomanUrdu = "VWAP volume aur qeemat ko mila kar asal institutional benchmark qeemat nikaalta hai.",
            detailEnglish = "VWAP is calculated from the start of the trading day or session. Institutional buyers consider price below VWAP as a 'discount' and price above VWAP as a 'premium'. In intraday trading, VWAP acts as formidable support during bullish sessions and stiff resistance during bearish sessions.",
            detailRomanUrdu = "Baray banks aur institutions VWAP ko dekhtay hain. Agar qeemat VWAP ke neechay ho to wo isay sasti (discount) samajh kar buy karte hain. Intraday trading mein VWAP zabardast support ka kaam karta hai.",
            practicalExampleEnglish = "During the New York session, price dips to test the daily VWAP at $63,200 and immediately prints a bullish rejection wick.",
            practicalExampleRomanUrdu = "Session ke doran jab qeemat VWAP par wapis aati hai aur wahan se reject ho kar ooper uthti hai to behtareen long trade banta hai.",
            proTipEnglish = "If price is trading consistently below daily VWAP, avoid taking aggressive long scalp positions.",
            proTipRomanUrdu = "Agar qeemat VWAP ke neechay chal rahi ho to long trade lene se parhez karein.",
            relatedTerms = listOf("VOLUME_ANALYSIS", "ORDER_BLOCKS")
        ),

        LessonItem(
            id = "atr_average_true_range",
            termKey = "ATR",
            category = LessonCategory.TECHNICAL_ANALYSIS,
            titleEnglish = "ATR (Average True Range)",
            titleRomanUrdu = "ATR (Market ki Average Harkat)",
            summaryEnglish = "A volatility indicator that measures the average price movement of an asset over a set period (typically 14 candles).",
            summaryRomanUrdu = "ATR batata hai ke rozana ya har ghantay market average kitnay dollars ooper ya neechay harkat karti hai.",
            detailEnglish = "ATR does not indicate price direction, only market volatility. If BTC has a 1H ATR of $800, placing a $150 stop loss is almost guaranteed to get hit by normal market breathing. Professional risk management multiplies ATR (e.g., 1.5x or 2.0x ATR) to set realistic, non-random stop loss distances.",
            detailRomanUrdu = "ATR kisi rukh ka ishara nahi deta balkay volatility batata hai. Agar Bitcoin ka 1 ghantay ka ATR $800 hai to aap $100 ka stop loss lagayein ge to wo zaroor hit ho jayega. Stop loss hamesha ATR ke mutabiq set karein.",
            practicalExampleEnglish = "Entering BTC at $64,000 with a 1.5x ATR buffer ($600 x 1.5 = $900) yields a logical structural stop loss at $63,100.",
            practicalExampleRomanUrdu = "ATR $500 hai to stop loss kam az kam $750 door rakhein taake aam wicks apko trade se bahar na nikaal sakein.",
            proTipEnglish = "NEXUS AI utilizes ATR directly to calculate adaptive Stop Loss levels and safe maximum leverage.",
            proTipRomanUrdu = "NEXUS AI stop loss calculate karne ke liye ATR ka istemal karta hai.",
            relatedTerms = listOf("STOP_LOSS", "LEVERAGE")
        ),

        LessonItem(
            id = "adx_trend_strength",
            termKey = "ADX",
            category = LessonCategory.TECHNICAL_ANALYSIS,
            titleEnglish = "ADX (Average Directional Index)",
            titleRomanUrdu = "ADX (Trend ki Taaqat ka Meter)",
            summaryEnglish = "An indicator that quantifies the strength of a trend regardless of whether it is an uptrend or downtrend.",
            summaryRomanUrdu = "ADX batata hai ke chalnay wala trend kitna mazboot hai ya market chop (sideways) mein hai.",
            detailEnglish = "ADX values range from 0 to 100. An ADX reading below 20 indicates a weak, absent, or choppy market where breakout strategies fail. An ADX reading above 25 confirms a strong trending market, while readings above 40 indicate an extreme directional trend.",
            detailRomanUrdu = "Agar ADX 20 se neechay ho to market be-jaan aur sideways hoti hai; aisi sorat mein breakout trades fail hotay hain. Agar ADX 25 se ooper ho to trend mazboot hota hai.",
            practicalExampleEnglish = "Waiting for ADX to cross above 25 before taking breakout trades prevents getting chopped up in range-bound markets.",
            practicalExampleRomanUrdu = "Breakout trade sirf tab karein jab ADX 25 se ooper ho taake fake breakout se bacha ja sakay.",
            proTipEnglish = "Remember: ADX measures trend strength, not direction. A crashing market can have an ADX of 50.",
            proTipRomanUrdu = "ADX sirf taaqat batata hai, simt nahi. Market gir bhi rahi ho to ADX ooper ja sakta hai.",
            relatedTerms = listOf("TRENDLINES", "MOVING_AVERAGES")
        ),

        LessonItem(
            id = "volume_analysis",
            termKey = "VOLUME",
            category = LessonCategory.TECHNICAL_ANALYSIS,
            titleEnglish = "Volume & Volume Delta Analysis",
            titleRomanUrdu = "Volume aur Volume Delta Analysis",
            summaryEnglish = "Analyzing trading volume confirms whether price movements are supported by genuine institutional participation.",
            summaryRomanUrdu = "Volume batata hai ke market ki harkat ke peechay baray kharidaaron ka sarmaya laga hai ya ye retail trap hai.",
            detailEnglish = "Price moving up on high volume signifies real institutional accumulation. Price moving up on declining volume warns of a 'volume divergence' and impending exhaustion. Volume Delta analyzes aggressor buyer volume versus aggressor seller volume on every tick.",
            detailRomanUrdu = "Agar market barh rahi ho aur volume bhi barh raha ho to trend jari rahay ga. Agar market naye high par jaye lekin volume kam ho to samjh jayein ke fakeout hone wala hai.",
            practicalExampleEnglish = "A resistance breakout on 2.5x Relative Volume (RVOL) confirms institutional buying and high continuation probability.",
            practicalExampleRomanUrdu = "Resistance tootnay par agar aam dinon se dugna volume aaye to breakout kamyab hota hai.",
            proTipEnglish = "Never trust a breakout that occurs on thin, below-average volume.",
            proTipRomanUrdu = "Kam volume par honay walay breakout par kabhi aitbaar na karein.",
            relatedTerms = listOf("VWAP", "LIQUIDITY")
        ),

        // =========================================================================
        // CATEGORY C: SMART MONEY CONCEPTS (SMC) (9 Lessons)
        // =========================================================================
        LessonItem(
            id = "break_of_structure",
            termKey = "BOS",
            category = LessonCategory.SMART_MONEY,
            titleEnglish = "Break of Structure (BOS)",
            titleRomanUrdu = "Break of Structure (BOS)",
            summaryEnglish = "A market structure continuation signal confirming that the current trend is expanding as price breaches prior swing extremes.",
            summaryRomanUrdu = "Market ka pichlay swing high ya low ko tor kar trend ko jari rakhna.",
            detailEnglish = "In an uptrend, a Bullish BOS occurs when a candle body closes cleanly above the previous verified Higher High (HH). In a downtrend, a Bearish BOS occurs when a candle body closes below the previous verified Lower Low (LL). This proves institutional order flow is actively driving the trend forward.",
            detailRomanUrdu = "Jab uptrend mein price pichlay Higher High ko tor kar naya high banaye to Bullish BOS kehlata hai. Iska matlab hai ke baray institutions abhi bhi usi simt mein market ko ooper khench rahe hain.",
            practicalExampleEnglish = "BTC breaking and closing above $65,000 (prior high) confirms a Bullish BOS targeting $68k.",
            practicalExampleRomanUrdu = "Bitcoin ne pichla swing high $65,000 tora aur candle ooper band hui, to ye Bullish BOS ban gaya.",
            proTipEnglish = "Always wait for a full candle body close. Wicks piercing past a swing are often liquidity sweeps rather than genuine BOS.",
            proTipRomanUrdu = "Hamesha candle body ke close ka intezar karein. Sirf wick se break hona fakeout ya sweep ho sakta hai.",
            relatedTerms = listOf("CHOCH", "ORDER_BLOCKS", "SWINGS")
        ),

        LessonItem(
            id = "change_of_character",
            termKey = "CHOCH",
            category = LessonCategory.SMART_MONEY,
            titleEnglish = "Change of Character (CHoCH)",
            titleRomanUrdu = "Change of Character (CHoCH)",
            summaryEnglish = "The very first structural signal indicating a potential trend reversal from bullish to bearish or vice versa.",
            summaryRomanUrdu = "Trend ke ulatnay (reversal) ka sab se pehla structural ishara.",
            detailEnglish = "A Bullish CHoCH occurs when price in a downtrend breaks and closes above the most recent Lower High (LH). A Bearish CHoCH occurs when price in an uptrend breaks and closes below the most recent Higher Low (HL). Unlike BOS which shows continuation, CHoCH warns of trend exhaustion and order flow reversal.",
            detailRomanUrdu = "Jab girtee hui market achanak pichlay Lower High ko ooper break kar de to Bullish CHoCH kehlata hai. Iska matlab hai ke ab market ka rukh badal chuka hai aur bears kamzor par gaye hain.",
            practicalExampleEnglish = "After dropping for days, ETH closes a 4H candle above the last swing high of $2,800, printing a Bullish CHoCH.",
            practicalExampleRomanUrdu = "Downtrend ke baad jab price ne pehla major swing high toda to trend reversal ka signal mil gaya.",
            proTipEnglish = "A CHoCH on the 1H or 4H timeframe provides an early signal to stop longing or shorting against the emerging reversal.",
            proTipRomanUrdu = "CHoCH dekhte hi mukhalif trades band kar dein aur naye trend ke pullback ka intezar karein.",
            relatedTerms = listOf("BOS", "ORDER_BLOCKS")
        ),

        LessonItem(
            id = "order_blocks",
            termKey = "ORDER_BLOCK",
            category = LessonCategory.SMART_MONEY,
            titleEnglish = "Order Blocks (Bullish & Bearish)",
            titleRomanUrdu = "Order Blocks (OB)",
            summaryEnglish = "Specific footprint candles where institutional market makers placed massive orders prior to an aggressive price displacement.",
            summaryRomanUrdu = "Wo makhsoos candles jahan baray banks ne massive buy ya sell orders lagaye hotay hain.",
            detailEnglish = "A Bullish Order Block is the last down-close candle before an explosive upward impulse that creates a BOS or FVG. A Bearish Order Block is the last up-close candle before a sharp downward plunge. When price retests these zones, unfilled institutional limit orders frequently trigger sharp, high-probability bounces.",
            detailRomanUrdu = "Bullish Order Block wo aakhri red candle hoti hai jiske baad achanak baray green candles banay hon. Jab price wapis is zone mein aati hai to wahan par institutions ke bache hue limit orders trigger hotay hain aur price foran bounce karti hai.",
            practicalExampleEnglish = "Placing a limit long order at the 50% equilibrium level of an unmitigated 4H Bullish Order Block.",
            practicalExampleRomanUrdu = "Unmitigated Bullish Order Block ke 50% (Mean Threshold) par entry order lagana behtareen risk-reward deta hai.",
            proTipEnglish = "An order block that has already been tested (mitigated) loses strength. Favor fresh, unmitigated order blocks.",
            proTipRomanUrdu = "Fresh unmitigated order block sab se mazboot hota hai. Jo pehle se retest ho chuka ho wo kamzor ho jata hai.",
            relatedTerms = listOf("BOS", "FAIR_VALUE_GAP", "SUPPLY_DEMAND")
        ),

        LessonItem(
            id = "fair_value_gaps",
            termKey = "FVG",
            category = LessonCategory.SMART_MONEY,
            titleEnglish = "Fair Value Gaps (FVG) / Imbalances",
            titleRomanUrdu = "Fair Value Gaps (FVG)",
            summaryEnglish = "3-candle price inefficiencies where aggressive buying or selling left an unfilled liquidity vacuum on the chart.",
            summaryRomanUrdu = "Chart par tezi se aane wali harkat ki wajah se bacha hua khala (imbalance).",
            detailEnglish = "A Bullish FVG exists when Candle 1's High does not overlap Candle 3's Low, leaving a gap across Candle 2's body. The market algorithm acts like a magnet to return to these imbalance zones and rebalance liquidity before continuing the main trend direction.",
            detailRomanUrdu = "Jab Candle 1 ki high aur Candle 3 ki low aapas mein na milein to darmiyan mein FVG ban jata hai. Market aik magnet ki tarah is khala ko bharne wapis aati hai aur phir aglay rukh rawana hoti hai.",
            practicalExampleEnglish = "Entering a long position as price pulls back into a 1H Bullish FVG between $63,200 and $63,600.",
            practicalExampleRomanUrdu = "Jab price wapis aakar 1H FVG ke darmiyan tap kare to wahan se entry li jati hai.",
            proTipEnglish = "Combine an unmitigated Order Block sitting directly inside or behind an unfilled Fair Value Gap for maximum confluence.",
            proTipRomanUrdu = "Jab Order Block aur FVG aik hi jagah mil jayein to wo sab se high-probability trade banta hai.",
            relatedTerms = listOf("ORDER_BLOCKS", "LIQUIDITY")
        ),

        LessonItem(
            id = "liquidity_concepts",
            termKey = "LIQUIDITY",
            category = LessonCategory.SMART_MONEY,
            titleEnglish = "Liquidity & Order Flow",
            titleRomanUrdu = "Liquidity aur Stop-Loss Pools",
            summaryEnglish = "Liquidity is the fuel of financial markets; it represents dense clusters of resting stop-loss orders and breakout limit orders.",
            summaryRomanUrdu = "Liquidity market ka tail (fuel) hai jahan aam logon ke Stop Loss jama hotay hain.",
            detailEnglish = "Retail traders naturally place their stop losses above obvious swing highs and below swing lows. Institutional algorithms need massive counterparty volume to fill large positions without slippage. Therefore, smart money deliberately hunts and sweeps these liquidity pools before initiating real directional moves.",
            detailRomanUrdu = "Bari whales ko lakhoon dollars ki trade open karne ke liye doosron ke stop losses chahiye hotay hain. Is liye price pehle aam logon ke stop loss hit karti hai aur phir asal rukh par chalti hai.",
            practicalExampleEnglish = "Equal Lows at $60,000 act as a magnet for a sell-side liquidity sweep before a 10% pump.",
            practicalExampleRomanUrdu = "Double bottom ke neechay sab ke stop loss hotay hain. Whales pehle unhein hunt karti hain phir pump karti hain.",
            proTipEnglish = "Ask yourself: 'Where is retail putting their stop losses?' That is precisely where the smart money is aiming.",
            proTipRomanUrdu = "Sochein ke aam logon ne stop loss kahan lagaya hoga? Market wahi ja kar unhein clean karegi.",
            relatedTerms = listOf("LIQUIDITY_SWEEPS", "BUY_SELL_LIQUIDITY")
        ),

        LessonItem(
            id = "liquidity_sweeps",
            termKey = "LIQUIDITY_SWEEPS",
            category = LessonCategory.SMART_MONEY,
            titleEnglish = "Liquidity Sweeps & Stop Hunts",
            titleRomanUrdu = "Liquidity Sweeps aur Stop Hunts",
            summaryEnglish = "When price momentarily punctures past a key high or low to trigger retail stops, then rapidly closes back inside the range.",
            summaryRomanUrdu = "Jab price achanak wick maar kar stop loss hit kare aur foran wapis andar close ho jaye.",
            detailEnglish = "A liquidity sweep is characterized by a long wick that spikes past a prominent swing high or low but fails to sustain a full candle body close. Once the resting orders are consumed (swept), price immediately reverses in the opposite direction.",
            detailRomanUrdu = "Is mein candle ki wick pichlay high ya low ke paar jati hai lekin body wapis andar close hoti hai. Isay 'Stop Hunt' kehtay hain. Iske foran baad market tezi se reversal karti hai.",
            practicalExampleEnglish = "BTC wicks down to $59,800 below the $60,000 double bottom and closes back at $60,400 within the same 15m candle.",
            practicalExampleRomanUrdu = "Bitcoin ne $60,000 ke neechay $59,800 par wick maari aur 15 minute ke andar wapis $60,400 par aa gaya.",
            proTipEnglish = "Enter immediately after a sweep candle closes back inside the range with stop loss placed at the sweep wick tip.",
            proTipRomanUrdu = "Sweep hone ke baad jab candle range ke andar close ho to wick ke neechay stop loss rakh kar enter hon.",
            relatedTerms = listOf("LIQUIDITY", "BUY_SELL_LIQUIDITY")
        ),

        LessonItem(
            id = "buy_side_sell_side_liquidity",
            termKey = "BUY_SELL_LIQUIDITY",
            category = LessonCategory.SMART_MONEY,
            titleEnglish = "Buy-Side Liquidity (BSL) & Sell-Side Liquidity (SSL)",
            titleRomanUrdu = "Buy-Side (BSL) aur Sell-Side (SSL) Liquidity",
            summaryEnglish = "BSL resides above swing highs (buy stops of short sellers); SSL resides below swing lows (sell stops of long buyers).",
            summaryRomanUrdu = "BSL ooper ke swing highs par hota hai aur SSL neechay ke swing lows par hota hai.",
            detailEnglish = "When an asset is in an accumulation phase, smart money engineers liquidity by sweeping SSL first (taking out longs) to buy cheap, and then targeting BSL (taking out shorts) as their exit take-profit destination.",
            detailRomanUrdu = "Market hamesha aik liquidity pool se doosray liquidity pool ki taraf travel karti hai: SSL se BSL, ya BSL se SSL.",
            practicalExampleEnglish = "Targeting the $68,000 swing high (BSL) as Take Profit 2 after buying the $62,000 SSL sweep.",
            practicalExampleRomanUrdu = "Neechay SSL sweep par khareed kar ooper BSL pools ko apna take-profit target banana.",
            proTipEnglish = "Always use external liquidity pools (BSL/SSL) as your high-probability take profit targets.",
            proTipRomanUrdu = "BSL aur SSL levels ko apna Take Profit target banayein.",
            relatedTerms = listOf("LIQUIDITY", "TAKE_PROFIT")
        ),

        LessonItem(
            id = "supply_and_demand",
            termKey = "SUPPLY_DEMAND",
            category = LessonCategory.SMART_MONEY,
            titleEnglish = "Supply & Demand Zones",
            titleRomanUrdu = "Supply aur Demand Zones",
            summaryEnglish = "Broad areas where massive institutional buying (Demand) or aggressive selling (Supply) originated.",
            summaryRomanUrdu = "Chart ke wo hissay jahan se qeemat ne tezi se ooper (Demand) ya tezi se neechay (Supply) ka safar shuru kiya tha.",
            detailEnglish = "Demand zones form when aggressive buying overwhelms sellers, leaving unfilled orders behind. Supply zones form when sellers overpower buyers. When price re-enters a fresh demand zone, the remaining buy orders create strong upward pressure.",
            detailRomanUrdu = "Demand zone wo jagah hai jahan sasti qeemat par baray kharidaar intizar karte hain. Supply zone wo jagah hai jahan mehangi qeemat par bechnay walay baithe hotay hain.",
            practicalExampleEnglish = "Drawing a demand zone around the base consolidation before a $5,000 upward rally.",
            practicalExampleRomanUrdu = "Bari rally shuru hone se pehle wali consolidation ko demand zone banayein.",
            proTipEnglish = "The faster price leaves a zone, the stronger and more imbalanced the supply or demand at that level.",
            proTipRomanUrdu = "Qeemat jitni tezi se zone se nikli hogi, wo zone utna hi mazboot hoga.",
            relatedTerms = listOf("SUPPORT_RESISTANCE", "ORDER_BLOCKS")
        ),

        LessonItem(
            id = "market_structure_swings",
            termKey = "MARKET_STRUCTURE",
            category = LessonCategory.SMART_MONEY,
            titleEnglish = "Market Structure (HH, HL, LH, LL)",
            titleRomanUrdu = "Market Structure (HH, HL, LH, LL)",
            summaryEnglish = "The framework of price action formed by waves of Higher Highs & Higher Lows (Uptrend) or Lower Highs & Lower Lows (Downtrend).",
            summaryRomanUrdu = "Market ka bunyadi structure jo Higher Highs aur Lower Lows ki shakl mein banta hai.",
            detailEnglish = "A healthy Bullish Trend consists of Higher Highs (HH) followed by Higher Lows (HL). A healthy Bearish Trend consists of Lower Highs (LH) followed by Lower Lows (LL). As long as Higher Lows hold in an uptrend, the structural trend remains bullish.",
            detailRomanUrdu = "Jab tak market naye Higher Highs aur Higher Lows bana rahi hai, trend uptrend rehta hai. Jab aik Higher Low toot jaye to structure break ho jata hai.",
            practicalExampleEnglish = "Buying pullbacks that hold above the previous Higher Low ensures you trade with the trend structure.",
            practicalExampleRomanUrdu = "Higher Low ke qareeb buy karein aur stop loss us low ke neechay rakhein.",
            proTipEnglish = "Identify market structure on the 4H/1D first before taking execution entries on the 15m/5m.",
            proTipRomanUrdu = "Pehle 4H chart par structure dekhein, phir 15M par entry talaash karein.",
            relatedTerms = listOf("BOS", "CHOCH", "SWINGS")
        ),

        // =========================================================================
        // CATEGORY D: FIBONACCI TRADING (4 Lessons)
        // =========================================================================
        LessonItem(
            id = "fibonacci_retracement",
            termKey = "FIB_RETRACEMENT",
            category = LessonCategory.FIBONACCI,
            titleEnglish = "Fibonacci Retracement Levels",
            titleRomanUrdu = "Fibonacci Retracement Levels",
            summaryEnglish = "Mathematical ratios derived from the Fibonacci sequence (0.236, 0.382, 0.500, 0.618, 0.786) identifying pullback reversal depths.",
            summaryRomanUrdu = "Fibonacci ratios jo batati hain ke market kahan tak wapis gir kar dobara ooper ja sakti hai.",
            detailEnglish = "In an uptrend, draw the Fibonacci Retracement tool from the verified swing low to the swing high. Key levels include the 0.382 (shallow pullback), 0.500 (equilibrium benchmark), and 0.618 (golden ratio). Pullbacks into the 0.500–0.786 discount zone offer the most favorable risk-reward entries.",
            detailRomanUrdu = "Fibonacci tool ko swing low se swing high tak khencha jata hai. 0.50 aur 0.618 levels par qeemat aksar ruk kar dobara trend pakarti hai.",
            practicalExampleEnglish = "BTC rallying from $50k to $60k; a 50% retracement sits at $55,000, and the 0.618 sits at $53,820.",
            practicalExampleRomanUrdu = "$50,000 se $60,000 tak ki rally ke baad 0.618 level $53,820 par banta hai jahan buy order lagaya ja sakta hai.",
            proTipEnglish = "Never buy at the 0.236 level in crypto; wait for deep discount pullbacks to 0.618 for asymmetric risk/reward.",
            proTipRomanUrdu = "Crypto mein hamesha deep pullbacks (0.618 level) ka intezar karein.",
            relatedTerms = listOf("GOLDEN_POCKET", "FIB_EXTENSION")
        ),

        LessonItem(
            id = "fibonacci_golden_pocket",
            termKey = "GOLDEN_POCKET",
            category = LessonCategory.FIBONACCI,
            titleEnglish = "The Fibonacci Golden Pocket (0.618 - 0.65)",
            titleRomanUrdu = "Fibonacci Golden Pocket Zone",
            summaryEnglish = "The highest-probability reversal zone on the entire Fibonacci grid, situated between the 0.618 and 0.65 (or 0.786) levels.",
            summaryRomanUrdu = "Fibonacci ka sab se ahem aur kamyab zone (0.618 se 0.65) jahan se aksar baray reversals hotay hain.",
            detailEnglish = "The Golden Pocket represents the mathematical sweet spot where institutional algorithms program automated accumulation. When the Golden Pocket coincides with a fresh Order Block or Support Zone, it produces one of the most reliable trading setups in existence.",
            detailRomanUrdu = "Golden Pocket wo zone hai jahan institutions ke algorithms active hotay hain. Jab 0.618 level ke sath Support ya Order Block bhi mil jaye to kamyabi ke imkanaat 80%+ ho jatay hain.",
            practicalExampleEnglish = "Entering a long position inside the Golden Pocket with Stop Loss placed safely below the 0.786 or swing low.",
            practicalExampleRomanUrdu = "Golden Pocket mein dakhil hon aur stop loss 0.786 ya swing low ke neechay rakhein.",
            proTipEnglish = "Look for confluence: Golden Pocket + Bullish Order Block + 200 EMA = High-grade setup.",
            proTipRomanUrdu = "Jab Golden Pocket aur Order Block aik hi jagah hon to wo 'A+ Setup' hota hai.",
            relatedTerms = listOf("FIB_RETRACEMENT", "ORDER_BLOCKS")
        ),

        LessonItem(
            id = "fibonacci_extension",
            termKey = "FIB_EXTENSION",
            category = LessonCategory.FIBONACCI,
            titleEnglish = "Fibonacci Extensions (Price Targets)",
            titleRomanUrdu = "Fibonacci Extensions (Munafay ke Targets)",
            summaryEnglish = "Extension ratios (1.272, 1.618, 2.618) projected beyond previous swing highs to establish take-profit targets in price discovery.",
            summaryRomanUrdu = "Jab market naye All-Time High par ho to Take Profit tay karne ke liye Fib Extensions istemal hoti hain.",
            detailEnglish = "When an asset breaks into new All-Time Highs (price discovery), there is no historical resistance to gauge targets. Fibonacci extensions project mathematical targets: 1.272 is the first conservative target, 1.618 (The Golden Extension) is the primary target, and 2.618 is the extended runner target.",
            detailRomanUrdu = "Jab koi coin naya record high banata hai to chart par koi purani resistance nahi hoti. Aisi sorat mein 1.618 extension target banaya jata hai jahan par profit book kiya ja sakay.",
            practicalExampleEnglish = "Projecting the 1.618 extension on Bitcoin's weekly breakout gives a macro target of $84,500.",
            practicalExampleRomanUrdu = "Weekly breakout par 1.618 level target rakh kar bara munafa lock kiya ja sakta hai.",
            proTipEnglish = "Always scale out 50% of your position at the 1.272 and 1.618 levels to lock in guaranteed profit.",
            proTipRomanUrdu = "1.272 aur 1.618 levels par kam az kam aadha profit book kar lena chahiye.",
            relatedTerms = listOf("FIB_RETRACEMENT", "TAKE_PROFIT")
        ),

        LessonItem(
            id = "practical_fibonacci_examples",
            termKey = "FIB_PRACTICE",
            category = LessonCategory.FIBONACCI,
            titleEnglish = "Practical Fibonacci Trading Framework",
            titleRomanUrdu = "Fibonacci ko Chart par Laganay ka Amali Tariqa",
            summaryEnglish = "Step-by-step methodology to correctly anchor Fibonacci tools on confirmed fractal swings.",
            summaryRomanUrdu = "Step-by-step tariqa ke chart par Fibonacci tool kahan se kahan tak lagana chahiye.",
            detailEnglish = "Rule 1: Always anchor to verified fractal swing pivots (do not pick arbitrary middle candles).\nRule 2: Anchor left-to-right from swing low to swing high in uptrends; swing high to swing low in downtrends.\nRule 3: Only take trades where the Fib level aligns with an Order Block, FVG, or major horizontal S/R zone.",
            detailRomanUrdu = "Rule 1: Hamesha verified swing points se shuru karein.\nRule 2: Uptrend mein neechay se ooper khenchein, downtrend mein ooper se neechay.\nRule 3: Sirf tab trade lein jab Fib level kisi Order Block ya Support ke sath match kar raha ho.",
            practicalExampleEnglish = "Scanning the 4H chart for a confirmed swing low with at least 2 lower wicks on either side before pulling the tool.",
            practicalExampleRomanUrdu = "4H chart par clear swing low dekh kar Fibonacci tool khenchein aur 0.618 par alert set karein.",
            proTipEnglish = "Avoid drawing Fibonacci across choppy, overlapping ranges; it works best on clean, impulsive expansion legs.",
            proTipRomanUrdu = "Sideways market mein Fibonacci kaam nahi karti; sirf saaf trend wali waves par lagayein.",
            relatedTerms = listOf("FIB_RETRACEMENT", "GOLDEN_POCKET")
        ),

        // =========================================================================
        // CATEGORY E: RISK MANAGEMENT (7 Lessons)
        // =========================================================================
        LessonItem(
            id = "stop_loss_mastery",
            termKey = "STOP_LOSS",
            category = LessonCategory.RISK_MANAGEMENT,
            titleEnglish = "Stop Loss Mastery (Capital Protection)",
            titleRomanUrdu = "Stop Loss ki Aehmiyat aur Tariqa",
            summaryEnglish = "An automated exit order that liquidates your position at a predetermined price to prevent catastrophic account loss.",
            summaryRomanUrdu = "Aik auto order jo apke trade ko aik tay shuda nuqsan par khud-ba-khud band kar deta hai.",
            detailEnglish = "A Stop Loss is the non-negotiable insurance policy of professional trading. It should never be set at an arbitrary dollar figure; rather, it belongs placed invalidating the structural thesis (e.g., just beneath the invalidation Order Block or swing pivot with an ATR buffer). Moving your stop loss further away during an active trade is the hallmark of emotional ruin.",
            detailRomanUrdu = "Stop Loss trading ki sab se ahem life-insurance hai. Agar trade apke khilaf jaye to Stop Loss apke 98%+ sarmaye ko aglay trades ke liye bacha leta hai. Chalti trade mein Stop Loss ko peechay hatana sab se bari ghalti hai.",
            practicalExampleEnglish = "Entering at $64,000 with a strict Stop Loss at $62,900. If price hits $62,900, the thesis is broken and you exit safely with a small controlled loss.",
            practicalExampleRomanUrdu = "$64,000 par buy karke $62,900 par Stop Loss lagana. Agar market gir bhi jaye to sirf $1,100 ka controlled loss hoga.",
            proTipEnglish = "Once price achieves Take Profit 1 (TP1), immediately move your Stop Loss to your Entry Price (Break-Even trade).",
            proTipRomanUrdu = "TP1 hit hotay hi Stop Loss ko Entry price par le aayein taake trade 100% Risk-Free ho jaye.",
            riskWarningEnglish = "Trading without a Stop Loss is the direct cause of over 95% of retail account wipes in crypto.",
            riskWarningRomanUrdu = "Baghair Stop Loss ke trade lagane se account wash hone ka khatra 100% barh jata hai.",
            relatedTerms = listOf("TAKE_PROFIT", "RISK_REWARD", "POSITION_SIZING")
        ),

        LessonItem(
            id = "take_profit_strategy",
            termKey = "TAKE_PROFIT",
            category = LessonCategory.RISK_MANAGEMENT,
            titleEnglish = "Take Profit Multi-Target Strategy",
            titleRomanUrdu = "Take Profit (Munafa Wasool karne ki Strategy)",
            summaryEnglish = "Pre-planned price targets where position portions are liquidated to guarantee locked-in realized profits.",
            summaryRomanUrdu = "Pehle se tay shuda muqamat jahan pohnchne par trade se munafa lock kiya jata hai.",
            detailEnglish = "Traders often give back profits due to greed. NEXUS AI recommends a 3-tier scaling exit:\n• TP1 (1.5x R:R / First Resistance): Close 40-50% position and move Stop Loss to Break-Even.\n• TP2 (2.5x-3x R:R / Major Target): Close 30-40% position.\n• TP3 (Fib 1.618 / Macro Extension): Let remaining 20% runner capture extended trend momentum.",
            detailRomanUrdu = "Traders lalach ki wajah se munafa ganwa bethte hain. 3 hisson mein profit book karein:\n• TP1: 40-50% profit book karein aur Stop Loss entry par le aayein.\n• TP2: Mazeed 30% profit lock karein.\n• TP3: Baki 20% hissay ko bare target ke liye chhor dein.",
            practicalExampleEnglish = "On a 1 BTC long position: closing 0.5 BTC at TP1 ($66k), 0.3 BTC at TP2 ($68k), and leaving 0.2 BTC to ride toward $72k.",
            practicalExampleRomanUrdu = "TP1 par aadhi trade close karein, TP2 par mazeed hissa bechein, aur baki ko run karne dein.",
            proTipEnglish = "Never regret taking profit. Unrealized profit is not yours until the position is closed.",
            proTipRomanUrdu = "Munafa waqt par lock karein. Jab tak trade close na ho, wo sirf screen par dikhnay wala number hota hai.",
            relatedTerms = listOf("STOP_LOSS", "RISK_REWARD")
        ),

        LessonItem(
            id = "risk_to_reward_ratio",
            termKey = "RISK_REWARD",
            category = LessonCategory.RISK_MANAGEMENT,
            titleEnglish = "Risk-to-Reward Ratio (R:R)",
            titleRomanUrdu = "Risk-to-Reward Ratio (R:R)",
            summaryEnglish = "The ratio of potential loss versus potential gain on a trade; minimum 1:2 standard required for long-term edge.",
            summaryRomanUrdu = "Har $1 ke nuqsan ke muqablay mein kitnay dollars ka munafa expected hai.",
            detailEnglish = "With a 1:3 Risk-to-Reward ratio, you risk $100 to make $300. With this math, you only need to win 30% of your trades to remain comfortably profitable over time:\n• 10 Trades: 3 Wins ($900) minus 7 Losses ($700) = Net +$200 Profit!\nNEXUS AI rejects any setup with an R:R below 1:1.5.",
            detailRomanUrdu = "Agar aap $10 risk par laga kar $25 ka target rakhte hain to R:R 1:2.5 hai. Agar aap 10 mein se sirf 4 trades bhi jeetain, tab bhi aap net profit mein rehte hain:\n4 jeet ($100) - 6 haar ($60) = +$40 Net Munafa!",
            practicalExampleEnglish = "Entering BTC at $64,000 with SL at $63,000 ($1,000 risk) and TP2 at $66,500 ($2,500 reward) = 1:2.5 R:R.",
            practicalExampleRomanUrdu = "$1,000 ke risk par $2,500 ka target rakhna aik 1:2.5 R:R setup hai.",
            proTipEnglish = "Focus on asymmetric risk-reward rather than obsessing over an unrealistic 90% win rate.",
            proTipRomanUrdu = "Win-rate ke peechay bhaagnay ke bajaye Risk-Reward ratio ko behtar banayein.",
            relatedTerms = listOf("POSITION_SIZING", "STOP_LOSS")
        ),

        LessonItem(
            id = "position_sizing_rule",
            termKey = "POSITION_SIZING",
            category = LessonCategory.RISK_MANAGEMENT,
            titleEnglish = "Position Sizing & The 1-2% Rule",
            titleRomanUrdu = "Position Sizing aur 1-2% ka Usool",
            summaryEnglish = "Never risk more than 1% to 2% of your total account balance on any single trading opportunity.",
            summaryRomanUrdu = "Kisi aik trade mein apnay total account ka sirf 1% se 2% hi risk par lagana.",
            detailEnglish = "If your account is $10,000, your maximum dollar loss on any trade should be $100 (1%) to $200 (2%). Position Size Formula:\nPosition Size = (Account Balance * Risk %) / (Entry Price - Stop Loss Price).\nThis ensures that even a catastrophic streak of 10 consecutive losses leaves over 88% of your trading capital fully intact.",
            detailRomanUrdu = "Agar apka account $1,000 ka hai to kisi aik trade mein $10 ya $20 se zyada ka risk na lein. Agar musalsal 5 trades bhi ghalat ho jayein, tab bhi apka 90% sarmaya mehfooz rehta hai.",
            practicalExampleEnglish = "Account $5,000. Risk 1% = $50. If SL distance is 2.5%, maximum position size is $2,000 ($50 / 0.025).",
            practicalExampleRomanUrdu = "$1,000 ke account par 1% risk ($10) rakhein. Stop loss jitna door hoga, lot size utna chota hoga.",
            proTipEnglish = "Calculate your position size from your Stop Loss, not from how much profit you hope to make.",
            proTipRomanUrdu = "Lot size hamesha Stop Loss ki doori dekh kar calculate karein.",
            relatedTerms = listOf("RISK_REWARD", "MAX_DRAWDOWN")
        ),

        LessonItem(
            id = "liquidation_price_protection",
            termKey = "LIQUIDATION",
            category = LessonCategory.RISK_MANAGEMENT,
            titleEnglish = "Liquidation Price & Margin Call Prevention",
            titleRomanUrdu = "Liquidation Price aur Account Bachana",
            summaryEnglish = "The exact price at which the exchange forcibly closes your leveraged position, confiscating your allocated margin balance.",
            summaryRomanUrdu = "Wo qeemat jahan pohnch kar exchange apka trade zabardasti band karke sara margin zabt kar leti hai.",
            detailEnglish = "Liquidation occurs when your maintenance margin falls below the exchange threshold. The higher your leverage, the closer your liquidation price is to your entry (e.g., 50x leverage liquidates on just a 2% adverse move). A disciplined trader ensures their Stop Loss triggers long before the liquidation price is ever approached.",
            detailRomanUrdu = "Zyada leverage lene se Liquidation price bilkul entry ke qareeb aa jati hai. Kamyab trader hamesha apna Stop Loss liquidation price se bohot pehle rakhta hai taake account kabhi zero na ho.",
            practicalExampleEnglish = "Opening a 20x Long at $60,000 sets liquidation at ~$57,300 (~4.5% drop). An unexpected wick liquidates the position instantly.",
            practicalExampleRomanUrdu = "20x leverage par sirf 4.5% market girnay se poora balance khatam ho jata hai.",
            proTipEnglish = "Keep leverage at 3x-5x max so your liquidation price sits 20%-30%+ away from current price action.",
            proTipRomanUrdu = "3x leverage rakhein taake liquidation price 25-30% door rahe aur account safe rahe.",
            riskWarningEnglish = "Never add margin to a losing trade to push away liquidation. That is the quickest path to total account destruction.",
            riskWarningRomanUrdu = "Neechay girti trade mein mazeed paisa daal kar liquidation door karne ki koshish hargiz na karein.",
            relatedTerms = listOf("LEVERAGE", "STOP_LOSS")
        ),

        LessonItem(
            id = "maximum_drawdown",
            termKey = "MAX_DRAWDOWN",
            category = LessonCategory.RISK_MANAGEMENT,
            titleEnglish = "Maximum Drawdown & Recovery Math",
            titleRomanUrdu = "Maximum Drawdown aur Recovery Math",
            summaryEnglish = "The peak-to-trough decline in your trading account equity; understanding drawdown math reveals why preserving capital is paramount.",
            summaryRomanUrdu = "Account ke highest peak se honay wala total nuqsan aur usay wapis barabar karne ki mushkilat.",
            detailEnglish = "Drawdown recovery is asymmetric:\n• 10% loss requires +11.1% gain to recover.\n• 20% loss requires +25% gain to recover.\n• 50% loss requires +100% gain to recover!\n• 90% loss requires +900% gain just to get back to even!\nProtecting your capital during bad market conditions ensures you stay in the game.",
            detailRomanUrdu = "Nuqsan ko wapis poora karna aasan nahi hota:\n• 10% loss ko recover karne ke liye 11% munafa chahiye.\n• 50% loss ke baad account wapis laane ke liye 100% (double) munafa chahiye!\nIs liye nuqsan ko hamesha chota rakhein.",
            practicalExampleEnglish = "Halting live trading for 48 hours whenever total account drawdown reaches 5% in a single week.",
            practicalExampleRomanUrdu = "Agar hafte mein 5% loss ho jaye to 2 din ke liye trading band karke dimagh ko fresh karein.",
            proTipEnglish = "Set a daily max loss limit (e.g., 3%). If reached, close your trading app and step away for the day.",
            proTipRomanUrdu = "Rozana ka max loss limit tay karein (maslan 3%). Agar hit ho jaye to us din mazeed trade na karein.",
            relatedTerms = listOf("POSITION_SIZING", "CAPITAL_PROTECTION")
        ),

        LessonItem(
            id = "capital_protection_philosophy",
            termKey = "CAPITAL_PROTECTION",
            category = LessonCategory.RISK_MANAGEMENT,
            titleEnglish = "Capital Protection Philosophy",
            titleRomanUrdu = "Sarmaya Bachanay ki Hikmat-e-Amli",
            summaryEnglish = "The cardinal rule of financial markets: defense comes before offense. Protect your ammunition.",
            summaryRomanUrdu = "Trading ka pehla usool: Munafa kamanay se pehle apnay asal sarmaye ko bachana.",
            detailEnglish = "Amateur traders obsess over how much money they can make on a trade; professional traders obsess over how much money they might lose. Opportunities in crypto and commodities are limitless, but your trading capital is finite. When in doubt, cash is a valid and powerful market position.",
            detailRomanUrdu = "Naye log sochte hain ke kitna munafa hoga, jabkay professionals sochte hain ke kitna loss ho sakta hai. Market mein moqay roz milte hain lekin agar paisa khatam ho gaya to aap game se bahar ho jayein ge. Cash mein bethna bhi aik position hoti hai.",
            practicalExampleEnglish = "Sitting in 100% USDT for 3 weeks during an erratic chop range while preserving 100% of your capital for a clear breakout.",
            practicalExampleRomanUrdu = "Jab market clear na ho to sabar se cash mein bethein aur clear trend ka intezar karein.",
            proTipEnglish = "Surviving the bad market conditions allows you to thrive during the easy market trends.",
            proTipRomanUrdu = "Buri market mein capital bacha lein ge to achhi market mein zabardast profit banayein ge.",
            relatedTerms = listOf("MAX_DRAWDOWN", "STOP_LOSS")
        ),

        // =========================================================================
        // CATEGORY F: FUNDAMENTAL & MACRO ANALYSIS (9 Lessons)
        // =========================================================================
        LessonItem(
            id = "bitcoin_crypto_market_cycles",
            termKey = "MARKET_CYCLES",
            category = LessonCategory.FUNDAMENTAL_MACRO,
            titleEnglish = "Bitcoin 4-Year Halving Cycles",
            titleRomanUrdu = "Bitcoin 4-Saala Halving Cycles",
            summaryEnglish = "The recurring 4-year macro cycle driven by Bitcoin halvings, liquidity shifts, and psychological boom-bust phases.",
            summaryRomanUrdu = "Bitcoin ka 4 saala cycle jo Halving aur market liquidity ke sath chalta hai.",
            detailEnglish = "Every 210,000 blocks (roughly 4 years), the block reward paid to Bitcoin miners is cut in half, reducing new supply issuance by 50%. Historically, this supply shock combined with global M2 fiat liquidity expansion triggers a 12-18 month parabolic bull market, followed by a bear market accumulation phase.",
            detailRomanUrdu = "Har 4 saal baad Bitcoin miners ka reward aadha ho jata hai jis se market mein naye aane walay BTC ki tadaad 50% kam ho jati hai. Supply kam hone aur demand barhne se aam tor par 1 se 1.5 saal tak zabardast Bull Run chalta hai.",
            practicalExampleEnglish = "The 2012, 2016, 2020, and 2024 halvings all initiated historical macro supply-tightening cycles.",
            practicalExampleRomanUrdu = "2016, 2020 aur 2024 ki halvings ke baad Bitcoin ne naye All-Time High banaye.",
            proTipEnglish = "Accumulate during deep bear market despair when sentiment is at maximum fear; distribute near cycle tops.",
            proTipRomanUrdu = "Jab har taraf khauf aur mayusi ho tab buy karein, aur jab har koi ameer ban raha ho tab profit book karein.",
            relatedTerms = listOf("MACRO_ECONOMICS", "ETF_FLOWS")
        ),

        LessonItem(
            id = "federal_reserve_macro",
            termKey = "FED",
            category = LessonCategory.FUNDAMENTAL_MACRO,
            titleEnglish = "The Federal Reserve & Global Liquidity",
            titleRomanUrdu = "Federal Reserve aur Global Liquidity",
            summaryEnglish = "The US central bank controls global dollar liquidity, interest rates, and the cost of borrowing worldwide.",
            summaryRomanUrdu = "America ka central bank jo sood ki sharah (interest rates) aur dollar ki supply ko control karta hai.",
            detailEnglish = "When the Fed engages in Quantitative Easing (printing money / expanding balance sheet), capital floods risk assets like Bitcoin, tech stocks, and crypto. When the Fed engages in Quantitative Tightening (hiking rates / shrinking balance sheet), liquidity is drained, driving capital into cash and Treasury yields.",
            detailRomanUrdu = "Jab Fed sasta paisa market mein daalta hai (QE) to Bitcoin aur crypto tezi se ooper jatay hain. Jab Fed sood barhata hai (QT) to log risk assets se paisa nikaal kar safe investments mein daaltay hain.",
            practicalExampleEnglish = "The massive 2020–2021 global stimulus created a massive speculative bull run across crypto and commodities.",
            practicalExampleRomanUrdu = "2020 mein jab America ne trillion dollars print kiye to Bitcoin $4,000 se $69,000 par chala gaya.",
            proTipEnglish = "Never fight the Fed. Follow the expansion or contraction of global M2 fiat money supply.",
            proTipRomanUrdu = "Fed ki policies ke khilaf trade na karein. Global money supply ko follow karein.",
            relatedTerms = listOf("INTEREST_RATES", "DXY")
        ),

        LessonItem(
            id = "interest_rates_impact",
            termKey = "INTEREST_RATES",
            category = LessonCategory.FUNDAMENTAL_MACRO,
            titleEnglish = "Interest Rates (Federal Funds Rate)",
            titleRomanUrdu = "Interest Rates (Sood ki Sharah)",
            summaryEnglish = "The benchmark lending rate set by the Fed; higher rates strengthen the US dollar and suppress speculative assets.",
            summaryRomanUrdu = "Sood ki sharah barhne se dollar mazboot hota hai aur crypto/gold par dabao aata hai.",
            detailEnglish = "High interest rates increase the yield of risk-free US Treasury bills (e.g., 5%+), reducing the incentive for institutional capital to take risk on volatile assets like crypto. Rate cuts (pivot) cheapen debt and ignite liquidity inflows into risk assets.",
            detailRomanUrdu = "Jab interest rates zyada hon to baray idaray risk lene ke bajaye government bonds mein paisa rakhte hain. Jab sood kam kiya jata hai to crypto aur gold mein tezi aati hai.",
            practicalExampleEnglish = "Fed rate cut cycles historically precede significant multi-month rallies in Bitcoin and Gold.",
            practicalExampleRomanUrdu = "Jab Fed sood kam karna shuru karta hai to Gold aur Bitcoin dono mein zabardast tezi aati hai.",
            proTipEnglish = "Watch FOMC interest rate decision meetings; high volatility spikes occur within seconds of rate announcements.",
            proTipRomanUrdu = "FOMC meeting ke waqt market mein achanak bari wicks aati hain, is waqt new trade na lagayein.",
            relatedTerms = listOf("FED", "DXY", "CPI")
        ),

        LessonItem(
            id = "cpi_and_ppi",
            termKey = "CPI_PPI",
            category = LessonCategory.FUNDAMENTAL_MACRO,
            titleEnglish = "CPI and PPI (Inflation Metrics)",
            titleRomanUrdu = "CPI aur PPI (Mehangai ke Adaad-o-Shumaar)",
            summaryEnglish = "Consumer Price Index (CPI) and Producer Price Index (PPI) measure retail and wholesale inflation rates.",
            summaryRomanUrdu = "Mehangai (inflation) ko naapnay ke ahem tareen data reports jo har maheenay aati hain.",
            detailEnglish = "If CPI comes in higher than expectations ('hot inflation'), the Fed is pressured to keep interest rates higher for longer, causing stocks and crypto to dump. If CPI comes in cooler than expected, markets rally on hopes of upcoming rate cuts.",
            detailRomanUrdu = "Agar CPI report mein mehangai zyada aaye to market girti hai kyunki Fed sood kam nahi karega. Agar CPI kam aaye to market pump karti hai kyunki sasta paisa aane ki umeed barh jati hai.",
            practicalExampleEnglish = "CPI releasing at 2.8% vs. 3.1% expected immediately prompts a $2,000 green candle in Bitcoin.",
            practicalExampleRomanUrdu = "CPI report umeed se behtar aane par Bitcoin foran $2,000 pump kar jata hai.",
            proTipEnglish = "Never hold high-leverage positions into the 8:30 AM EST CPI release. Spread widening and slippage can wipe you out.",
            proTipRomanUrdu = "CPI data aane se 15 minute pehle high-leverage trades band kar dein kyunki spread bohot barh jata hai.",
            relatedTerms = listOf("FED", "INTEREST_RATES", "PCE_NFP")
        ),

        LessonItem(
            id = "pce_and_nfp",
            termKey = "PCE_NFP",
            category = LessonCategory.FUNDAMENTAL_MACRO,
            titleEnglish = "PCE & NFP (Jobs & Consumption)",
            titleRomanUrdu = "PCE aur NFP (Rozgar aur Kharch ka Data)",
            summaryEnglish = "Personal Consumption Expenditures (PCE) is the Fed's preferred inflation gauge; Non-Farm Payrolls (NFP) measures US job creation.",
            summaryRomanUrdu = "PCE Fed ka pasandeeda mehangai meter hai aur NFP America mein naye rozgar ka data dikhata hai.",
            detailEnglish = "NFP releases on the first Friday of every month. Strong jobs data gives the Fed room to stay hawkish (bearish for crypto), while weak jobs data signals an economic slowdown and potential emergency rate cuts.",
            detailRomanUrdu = "Har maheenay ke pehle juma ko NFP data aata hai. Agar rozgar bohot zyada ho to Fed sood barha sakta hai. Agar berozgari barhe to Fed sasta paisa daalne par majboor hota hai.",
            practicalExampleEnglish = "High unemployment numbers in NFP triggering rate-cut expectations and a Gold rally to new highs.",
            practicalExampleRomanUrdu = "Berozgari barhne ki khabar par Gold tezi se ooper chala jata hai.",
            proTipEnglish = "NFP Friday mornings exhibit erratic two-way wicks designed to hunt stops on both sides.",
            proTipRomanUrdu = "NFP ke waqt market dono taraf wicks maar kar stop loss hunt karti hai.",
            relatedTerms = listOf("CPI_PPI", "FED")
        ),

        LessonItem(
            id = "dxy_us_dollar_index",
            termKey = "DXY",
            category = LessonCategory.FUNDAMENTAL_MACRO,
            titleEnglish = "DXY (US Dollar Index)",
            titleRomanUrdu = "DXY (US Dollar Index)",
            summaryEnglish = "Measures the strength of the US Dollar against a basket of major world currencies (Euro, Yen, Pound, etc.).",
            summaryRomanUrdu = "DXY dikhata hai ke dunya ki doosri currencies ke muqablay mein American Dollar kitna taqatwar hai.",
            detailEnglish = "The DXY shares a strong historical inverse correlation with Bitcoin and Gold. When the DXY is pumping (Dollar strength), crypto and commodities usually dump. When the DXY breaks down into a macro downtrend, risk assets experience historic bull runs.",
            detailRomanUrdu = "DXY aur Bitcoin ka aapas mein ulta (inverse) rishta hai. Jab Dollar index ooper jata hai to Bitcoin aur Gold girtay hain. Jab Dollar kamzor hota hai to Bitcoin aur Gold naye records banatay hain.",
            practicalExampleEnglish = "DXY rejecting from 106.0 and dropping to 101.0 coincides with a 40% bull run in Bitcoin.",
            practicalExampleRomanUrdu = "DXY gir kar 101 par aaya to Bitcoin $50k se $70k par chala gaya.",
            proTipEnglish = "Keep a DXY chart open on your watchlist. A breakdown in DXY is a green light for crypto longs.",
            proTipRomanUrdu = "Hamesha DXY chart par nazar rakhein. DXY ki kamzori crypto ke liye khush-khabri hoti hai.",
            relatedTerms = listOf("INTEREST_RATES", "FED")
        ),

        LessonItem(
            id = "treasury_yields",
            termKey = "YIELDS",
            category = LessonCategory.FUNDAMENTAL_MACRO,
            titleEnglish = "US Treasury Yields (10Y & 2Y)",
            titleRomanUrdu = "Treasury Yields (Sarkari Bonds ka Munafa)",
            summaryEnglish = "The interest rate paid on US government debt obligations, reflecting macro growth and inflation expectations.",
            summaryRomanUrdu = "American government ke bonds par milnay wala return jo macro market ki direction tay karta hai.",
            detailEnglish = "The 10-Year Treasury Yield reflects long-term economic growth expectations, while the 2-Year Yield closely tracks Fed interest rate trajectory. An inverted yield curve (2Y yield higher than 10Y yield) has historically been an accurate recession warning.",
            detailRomanUrdu = "Jab 10-Year yield barhti hai to capital bonds mein chala jata hai aur stocks/crypto thanday par jatay hain. Yield curve inversion aam tor par aane walay bohran (recession) ka pehla ishara hota hai.",
            practicalExampleEnglish = "10-Year yield plunging from 5.0% to 3.8% unlocks massive institutional liquidity for crypto.",
            practicalExampleRomanUrdu = "Bond yields girnay par bari institutions crypto mein paisa shift karti hain.",
            proTipEnglish = "Falling Treasury yields provide strong tailwinds for both Gold and Bitcoin.",
            proTipRomanUrdu = "Yields ka girna Gold aur Bitcoin dono ke liye faida-mand hota hai.",
            relatedTerms = listOf("DXY", "FED")
        ),

        LessonItem(
            id = "spot_etf_flows",
            termKey = "ETF_FLOWS",
            category = LessonCategory.FUNDAMENTAL_MACRO,
            titleEnglish = "Spot ETF Flows (Institutional Inflows)",
            titleRomanUrdu = "Spot ETF Flows (Idaron ka Sarmaya)",
            summaryEnglish = "Tracking net capital inflows and outflows into regulated Spot Bitcoin and Ethereum ETFs (BlackRock, Fidelity, etc.).",
            summaryRomanUrdu = "BlackRock aur doosray baray fund managers ke Spot ETFs mein aane aur nikalnay walay sarmaye ki tracking.",
            detailEnglish = "Spot ETFs require issuers to purchase and hold actual physical Bitcoin in custody to back newly issued shares. Consecutive days of positive net inflows (e.g., +$500M/day) absorb spot market supply, creating sustained structural upward pressure.",
            detailRomanUrdu = "Spot ETF ka matlab hai ke jab BlackRock ka client $100M daalega to BlackRock ko asal Bitcoin khareed kar custody mein rakhna parega. Lagataar positive inflows market ko zabardast support deti hain.",
            practicalExampleEnglish = "Five consecutive days of +$800M ETF inflows propelling BTC past prior resistance levels.",
            practicalExampleRomanUrdu = "Hafte bhar mein $2 Billion ke naye ETF inflows ne Bitcoin ko naye high par pohncha diya.",
            proTipEnglish = "Monitor net ETF flow data published every evening; large negative outflows warn of institutional risk-off days.",
            proTipRomanUrdu = "Rozana shaam ko ETF inflow/outflow data check karein taake institutions ka mood pata chal sake.",
            relatedTerms = listOf("MARKET_CYCLES", "WHALE_ACTIVITY")
        ),

        LessonItem(
            id = "crypto_news_and_events",
            termKey = "CRYPTO_NEWS",
            category = LessonCategory.FUNDAMENTAL_MACRO,
            titleEnglish = "Trading Crypto News & Regulatory Events",
            titleRomanUrdu = "Crypto News aur Regulatory Events",
            summaryEnglish = "Navigating market reactions to SEC decisions, regulatory crackdowns, exchange hacks, and token unlocks.",
            summaryRomanUrdu = "Khabron, SEC ke faislon aur baray token unlocks par trading ke asraat.",
            detailEnglish = "Markets trade on expectations ('Buy the rumor, sell the news'). Highly anticipated events often see aggressive rallies leading into the event, followed by sharp profit-taking dumps immediately upon the actual announcement.",
            detailRomanUrdu = "Khabron ke aane se pehle market barhti hai aur khabar aate hi baray log apna munafa bech kar nikal jatay hain ('Buy the rumor, sell the news'). Bara token unlock aane par selling pressure barh jata hai.",
            practicalExampleEnglish = "The approval of the Spot Bitcoin ETF in January 2024 saw an initial brief pump followed by a classic 15% 'sell-the-news' retracement.",
            practicalExampleRomanUrdu = "ETF approve hone ki khabar aate hi market ne pehle 15% ka dip liya phir naya bull run shuru kiya.",
            proTipEnglish = "Never buy a coin when a major positive news story is already trending on social media; retail is the liquidity exit.",
            proTipRomanUrdu = "Jab social media par sab kisi coin ki tareef kar rahe hon to samajh jayein ke bechnay ka waqt aa chuka hai.",
            relatedTerms = listOf("ETF_FLOWS", "MARKET_CYCLES")
        ),

        // =========================================================================
        // CATEGORY G: ADVANCED TRADING (8 Lessons)
        // =========================================================================
        LessonItem(
            id = "open_interest_analysis",
            termKey = "OPEN_INTEREST",
            category = LessonCategory.ADVANCED_TRADING,
            titleEnglish = "Open Interest (OI) Analysis",
            titleRomanUrdu = "Open Interest (OI) kya hota hai?",
            summaryEnglish = "The total number of open, unsettled derivative contracts (longs and shorts) active in the futures market.",
            summaryRomanUrdu = "Futures market mein moujood total active aur unclosed trades ki tadaad.",
            detailEnglish = "Rising price + Rising OI = Aggressive new money entering, confirming strong trend continuation.\nRising price + Falling OI = Shorts covering / profit-taking, indicating a weakening rally.\nDropping price + Rising OI = Aggressive new shorts opening.\nDropping price + Falling OI = Long liquidation cascade.",
            detailRomanUrdu = "Agar price barh rahi ho aur Open Interest bhi barh raha ho to trend bohot mazboot hota hai. Agar price barhay lekin OI giray to iska matlab sirf short sellers majboori mein close kar rahe hain aur rally jald khatam ho sakti hai.",
            practicalExampleEnglish = "Bitcoin rallies $2,000 while Open Interest spikes by $1.2B, confirming heavy leverage building up for a volatile squeeze.",
            practicalExampleRomanUrdu = "Price barhne ke sath jab OI mein $1 Billion ka izafa ho to market mein bara move aane wala hota hai.",
            proTipEnglish = "When Open Interest reaches all-time highs while price is stuck in a tight range, prepare for a violent liquidation sweep.",
            proTipRomanUrdu = "Jab OI bohot zyada barh jaye aur price ruk jaye to bara liquidation dhamaka hone wala hota hai.",
            relatedTerms = listOf("FUNDING_RATE", "LIQUIDATION_HEATMAP")
        ),

        LessonItem(
            id = "funding_rates",
            termKey = "FUNDING_RATE",
            category = LessonCategory.ADVANCED_TRADING,
            titleEnglish = "Funding Rates (Perpetual Swaps)",
            titleRomanUrdu = "Funding Rates (Futures Fees)",
            summaryEnglish = "Periodic payments between long and short traders to keep perpetual contract prices pegged to the spot index price.",
            summaryRomanUrdu = "Long aur Short walon ke darmiyan har 8 ghantay baad exchange hone wali fees jo price ko balance rakhti hai.",
            detailEnglish = "When Funding is positive (> 0.01%), Longs pay Shorts. This indicates excessive bullish optimism; if too high (> 0.05%), long squeeze risk is extreme.\nWhen Funding is negative (< 0), Shorts pay Longs. This indicates crowded bearish sentiment; short squeeze upside risk is extreme.",
            detailRomanUrdu = "Agar funding rate bohot zyada positive ho to iska matlab har koi Long laga kar betha hai aur market unhein liquidate karne neechay gir sakti hai. Agar funding rate negative ho to Short squeeze ka khatra hota hai.",
            practicalExampleEnglish = "Negative funding rates (-0.03%) on Solana while price consolidates at support triggers a massive short squeeze rally.",
            practicalExampleRomanUrdu = "Funding negative hone par achanak market mein Short Squeeze aata hai aur price tezi se pump kar jati hai.",
            proTipEnglish = "Counter-trade extreme funding rates: look for Long entries when funding is deeply negative, and Short entries when funding is dangerously overheated.",
            proTipRomanUrdu = "Jab funding rate had se zyada negative ho to Short mat karein balkay bounce ka intizar karein.",
            relatedTerms = listOf("OPEN_INTEREST", "LONG_SHORT_RATIO")
        ),

        LessonItem(
            id = "long_short_ratio",
            termKey = "LONG_SHORT_RATIO",
            category = LessonCategory.ADVANCED_TRADING,
            titleEnglish = "Long/Short Ratio & Retail Sentiment",
            titleRomanUrdu = "Long/Short Ratio aur Retail Sentiment",
            summaryEnglish = "The ratio of accounts holding net-long positions versus net-short positions across leading exchanges.",
            summaryRomanUrdu = "Market mein kitnay percent log Long hain aur kitnay percent Short hain uska tanasub.",
            detailEnglish = "The Long/Short Ratio is a premier contrarian indicator. When 75%+ of retail traders are piled into Long positions, the market almost always dumps to liquidate the majority. The market makers and whales always seek the path of maximum financial pain.",
            detailRomanUrdu = "Trading mein aksariyat (majority) hamesha loss karti hai. Agar 75% retail traders Long position mein hon to market neechay gir kar unhein wash karti hai.",
            practicalExampleEnglish = "Top traders are net short (0.8 ratio) while retail accounts are 78% long, signaling an imminent downside flush.",
            practicalExampleRomanUrdu = "Jab retail traders bohot zyada Long hon to whales market ko dump karke unke Stop Losses khati hain.",
            proTipEnglish = "Use the Long/Short ratio as a contrarian filter. Avoid taking long positions when retail longs are at euphoric highs (> 3.0 ratio).",
            proTipRomanUrdu = "Jab sab log Long mein baithe hon to unke sath mat bethein; market ulta rukh le sakti hai.",
            relatedTerms = listOf("FUNDING_RATE", "LIQUIDATION_HEATMAP")
        ),

        LessonItem(
            id = "liquidation_heatmaps",
            termKey = "LIQUIDATION_HEATMAP",
            category = LessonCategory.ADVANCED_TRADING,
            titleEnglish = "Liquidation Heatmaps & Liquidity Levels",
            titleRomanUrdu = "Liquidation Heatmaps",
            summaryEnglish = "Visual heatmaps depicting cumulative liquidation leverage clusters where cascade triggers are concentrated.",
            summaryRomanUrdu = "Wo visual naksha jo dikhata hai ke kis price level par sab se zyada accounts wash honge.",
            detailEnglish = "Liquidation heatmaps predict where market volatility will accelerate. Bright yellow/white clusters represent dense zones of liquidation orders. Price moves toward these high-density liquidity zones like a guided missile because hitting them provides massive market order execution volume for whales.",
            detailRomanUrdu = "Heatmap par peela (yellow) rang dikhata hai ke wahan lakhoon dollars ke liquidation orders hain. Market aik magnet ki tarah pehle us level ko touch karne jati hai taake unhein wash kar sake.",
            practicalExampleEnglish = "A massive $400M liquidation cluster sitting at $68,200 pulls price directly upward to clear the pocket.",
            practicalExampleRomanUrdu = "$68,200 par $400M ka liquidation cluster tha, market ne tezi se ooper ja kar sab ko liquidate kiya aur phir wapis gir gayi.",
            proTipEnglish = "Never place your Stop Loss inside or just before a major liquidation cluster; place it beyond the cluster exhaustion level.",
            proTipRomanUrdu = "Apna Stop Loss kabhi bhi bare liquidation cluster ke andar na rakhein.",
            relatedTerms = listOf("OPEN_INTEREST", "LIQUIDITY_SWEEPS")
        ),

        LessonItem(
            id = "order_book_depth",
            termKey = "ORDER_BOOK",
            category = LessonCategory.ADVANCED_TRADING,
            titleEnglish = "Order Book Depth & Level 2 Data",
            titleRomanUrdu = "Order Book aur Market Depth",
            summaryEnglish = "The real-time list of outstanding limit buy bids and limit sell asks showing immediate supply/demand walls.",
            summaryRomanUrdu = "Exchange par lagay hue live khareed aur farokht ke limit orders ki mukammal fehrist.",
            detailEnglish = "The order book displays genuine liquidity at every price tick. Large limit orders ('buy walls' and 'sell walls') show where institutions are willing to transact. However, traders must beware of 'spoofing'—fake walls placed by algorithms and cancelled right before price arrives.",
            detailRomanUrdu = "Order book mein dikhta hai ke kis qeemat par kitnay Bitcoin khareedne ya bechne ke orders lagay hain. Lekin dhyan rahe ke baaz dafa whales jhooti 'Buy Walls' banati hain aur qeemat aate hi order cancel kar deti hain (Spoofing).",
            practicalExampleEnglish = "Spotting a genuine 500 BTC bid wall that holds multiple aggressive market sell sweeps, creating a solid support floor.",
            practicalExampleRomanUrdu = "$63,000 par 500 BTC ka bara buy order dekhna jo price ko neechay girnay se rok raha hai.",
            proTipEnglish = "Only trust order book walls that actually absorb filled market orders on the tape rather than disappearing upon approach.",
            proTipRomanUrdu = "Sirf un orders par bharosa karein jo fill hon, un par nahi jo price aate hi gayab ho jayein.",
            relatedTerms = listOf("CVD", "WHALE_ACTIVITY")
        ),

        LessonItem(
            id = "cumulative_volume_delta",
            termKey = "CVD",
            category = LessonCategory.ADVANCED_TRADING,
            titleEnglish = "Cumulative Volume Delta (CVD)",
            titleRomanUrdu = "CVD (Cumulative Volume Delta)",
            summaryEnglish = "Cumulative net difference between market buy volume and market sell volume, revealing aggressive market order pressure.",
            summaryRomanUrdu = "Market orders ke zariye aane wali asal kharidari aur farokht ke farq ka cumulative record.",
            detailEnglish = "CVD Divergence is one of the most powerful institutional reversal indicators:\n• Bullish CVD Divergence: Price makes a Lower Low, but CVD makes a Higher Low (aggressive sellers are being absorbed by passive limit orders).\n• Bearish CVD Divergence: Price makes a Higher High, but CVD makes a Lower High (buyers are exhausting).",
            detailRomanUrdu = "Agar price naya low banaye lekin CVD ooper jaye to iska matlab hai ke bechnay walon ka zor khatam ho raha hai aur koi chupke se sab kuch khareed raha hai (Absorption). Is ke baad bara pump aata hai.",
            practicalExampleEnglish = "Price dumps $1,000 to retest support while CVD forms a massive upward slope, confirming heavy limit absorption and preceding a $3,000 rally.",
            practicalExampleRomanUrdu = "Support par jab CVD divergence banay to ye smart money ke chupke se khareedne ka saboot hota hai.",
            proTipEnglish = "When price breaks resistance on declining CVD, suspect a false breakout driven purely by thin liquidity rather than real demand.",
            proTipRomanUrdu = "Agar CVD gir raha ho aur price ooper ja rahi ho to breakout par aitbaar na karein.",
            relatedTerms = listOf("VOLUME", "ORDER_BOOK")
        ),

        LessonItem(
            id = "whale_activity_tracking",
            termKey = "WHALE_ACTIVITY",
            category = LessonCategory.ADVANCED_TRADING,
            titleEnglish = "Whale Activity & On-Chain Tracking",
            titleRomanUrdu = "Whale Activity aur On-Chain Tracking",
            summaryEnglish = "Monitoring high-net-worth wallet transfers, exchange deposits/withdrawals, and miner inventory shifts.",
            summaryRomanUrdu = "Baray sarmayadaroon (Whales) ke wallets aur exchange par aane janay walay baray transactions ki nigrani.",
            detailEnglish = "Large exchange inflows of stablecoins (USDT/USDC) signal dry powder preparing to buy. Large inflows of BTC into spot exchanges indicate potential selling/dumping pressure. Conversely, massive outflows of Bitcoin from exchanges into cold storage indicate long-term institutional accumulation.",
            detailRomanUrdu = "Jab whales baray paimanay par Bitcoin exchange se nikaal kar apnay cold wallet mein bhejti hain to iska matlab hai ke wo lambay arsay ke liye hold kar rahi hain (Bullish). Agar exchange par BTC bhejein to selling ka khatra hota hai.",
            practicalExampleEnglish = "Over 40,000 BTC withdrawn from Coinbase into institutional custody in a single 24-hour window.",
            practicalExampleRomanUrdu = "Coinbase se hazaron BTC ka cold storage mein transfer hona baray bull run ka aaghaz hota hai.",
            proTipEnglish = "Track on-chain exchange reserves. Declining exchange reserves create an illiquid supply shock that accelerates rallies.",
            proTipRomanUrdu = "Exchange reserves jab kam ho rahe hon to market mein tezi aati hai.",
            relatedTerms = listOf("ETF_FLOWS", "MANIPULATION_RISKS")
        ),

        LessonItem(
            id = "market_manipulation_risks",
            termKey = "MANIPULATION_RISKS",
            category = LessonCategory.ADVANCED_TRADING,
            titleEnglish = "Market Manipulation & Spoofing Risks",
            titleRomanUrdu = "Market Manipulation aur Dhokay baaziyon se Bachna",
            summaryEnglish = "Recognizing institutional tactics like stop-hunting, wash trading, spoofing, and bart patterns designed to trap retail traders.",
            summaryRomanUrdu = "Whales ke dhokay maslan fake pump-and-dump, stop hunting aur spoofing se apna account bachana.",
            detailEnglish = "In unregulated crypto markets, high-frequency algorithms and market makers execute tactics to induce retail FOMO before dumping, or trigger panic selling at support to accumulate cheap inventory. A classic example is the 'Bart Pattern'—a sudden vertical pump, hours of flat consolidation, followed by an immediate identical vertical dump.",
            detailRomanUrdu = "Crypto mein baray khiladi aam logon ko phasane ke liye achanak pump karte hain taake log FOMO mein buy karein, aur phir foran dump kar dete hain. In se bachne ka wahid rasta discipline aur Stop Loss hai.",
            practicalExampleEnglish = "Recognizing a rapid weekend pump on thin volume as a potential bull trap and refusing to buy the top.",
            practicalExampleRomanUrdu = "Hafte ke aakhri dinon (weekend) mein kam volume par hone walay achanak pump par buy na karein kyunki ye trap ho sakta hai.",
            proTipEnglish = "If a move looks too obvious and clean on social media, you are likely the target of a liquidity trap. Stay patient.",
            proTipRomanUrdu = "Agar koi trade chart par bohot aasan aur obvious lag rahi ho to samjhein wahan trap banaya gaya hai.",
            riskWarningEnglish = "Never chase sudden green candles. 90% of retail traders trapped at the top become exit liquidity for whales.",
            riskWarningRomanUrdu = "Bari green candle ke peechay bhaag kar buy na karein; aap whales ka munafa ban jayein ge.",
            relatedTerms = listOf("LIQUIDITY_SWEEPS", "CAPITAL_PROTECTION")
        )
    )

    /**
     * Finds a lesson by exact ID or termKey.
     */
    fun findLesson(query: String): LessonItem? {
        val q = query.trim().uppercase(Locale.US)
        return allLessons.firstOrNull { it.id.equals(query, ignoreCase = true) }
            ?: allLessons.firstOrNull { it.termKey.equals(q, ignoreCase = true) }
            ?: allLessons.firstOrNull { it.termKey.contains(q, ignoreCase = true) }
    }

    /**
     * Filters lessons by category.
     */
    fun getLessonsByCategory(category: LessonCategory): List<LessonItem> {
        return allLessons.filter { it.category == category }
    }

    /**
     * Search lessons across titles, summaries, details, examples, and tags.
     */
    fun search(query: String): List<LessonItem> {
        val q = query.trim().lowercase(Locale.US)
        if (q.isBlank()) return allLessons

        return allLessons.filter { lesson ->
            lesson.titleEnglish.lowercase(Locale.US).contains(q) ||
                    lesson.titleRomanUrdu.lowercase(Locale.US).contains(q) ||
                    lesson.summaryEnglish.lowercase(Locale.US).contains(q) ||
                    lesson.summaryRomanUrdu.lowercase(Locale.US).contains(q) ||
                    lesson.detailEnglish.lowercase(Locale.US).contains(q) ||
                    lesson.detailRomanUrdu.lowercase(Locale.US).contains(q) ||
                    lesson.termKey.lowercase(Locale.US).contains(q) ||
                    lesson.category.title.lowercase(Locale.US).contains(q) ||
                    lesson.category.romanUrduTitle.lowercase(Locale.US).contains(q)
        }
    }
}
