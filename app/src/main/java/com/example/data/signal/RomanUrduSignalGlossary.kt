package com.example.data.signal

import com.example.data.smc.SmcGlossaryItem

/**
 * Educational Roman Urdu glossary for Trading Signals and Risk Management concepts.
 * Designed for beginners and intermediate traders to understand algorithmic trading terms easily.
 */
object RomanUrduSignalGlossary {

    val items: Map<String, SmcGlossaryItem> = mapOf(
        "LONG" to SmcGlossaryItem(
            termKey = "LONG",
            title = "LONG Position (Buy Signal)",
            category = "Trade Direction",
            romanUrduSummary = "Market Kharidna (Prices oper janay ka trade)",
            romanUrduDetail = "Trading mein 'Long' ka matlab asset ko lower price par khareed kar higher price par bechna hai. " +
                    "NEXUS AI ka LONG signal sirf tab banta hai jab EMAs (9/20/50), RSI, MACD aur Smart Money Structure (BOS/Order Block) sab ek sath uptrend ki tasdeeq (confluence) karein. " +
                    "Is mein entry zone, stop loss aur 3 take-profit targets pehle se tay hotay hain taake jazbaat (emotions) ke baghair discipline se trade ki ja sakay.",
            tradingTipRomanUrdu = "Pro Tip: Kabhi bhi green candle ke peak par FOMO mein Long na lein. Hamesha Bullish Order Block ya 0.618 Fibonacci Golden Pocket retracement ka intezar karein."
        ),

        "SHORT" to SmcGlossaryItem(
            termKey = "SHORT",
            title = "SHORT Position (Sell Signal)",
            category = "Trade Direction",
            romanUrduSummary = "Market Bechna (Prices neechay girnay ka trade)",
            romanUrduDetail = "Futures ya Margin trading mein 'Short' ka matlab girtee hui market se munafa kamana hai. " +
                    "Jab price Bearish Order Block se reject ho, RSI overbought se neechay giray, aur Lower High (LH) / Lower Low (LL) structure ban raha ho, to algorithm SHORT signal generate karta hai. " +
                    "Short trade mein Stop Loss entry price se oper rakha jata hai, aur Take Profit targets neechay ke demand zones par hotay hain.",
            tradingTipRomanUrdu = "Pro Tip: Short trade mein Stop Loss sakhti se lagana zaroori hai kyunki market mein sudden short-squeeze (rapid pump) account ko nuqsan pohncha sakta hai."
        ),

        "NO_TRADE" to SmcGlossaryItem(
            termKey = "NO_TRADE",
            title = "NO TRADE Condition (Patience Advisory)",
            category = "Risk Management",
            romanUrduSummary = "Sabar ki halat (Trade na karne ka mashwara)",
            romanUrduDetail = "Professional trading ka sab se bara secret ye hai ke har waqt trade mein hona zaroori nahi hota. " +
                    "Jab market choppy (sideways range) mein phansi ho, RSI neutral ho (45-55), ya Higher Timeframe aur Lower Timeframe mukhalif ishara de rahe hon, to fakeouts bohot zyada hotay hain. " +
                    "NEXUS AI apke capital (sarmaye) ko mehfooz rakhne ke liye NO TRADE flag karta hai taake be-maqsad losses se bacha ja sakay.",
            tradingTipRomanUrdu = "Pro Tip: Cash bhi ek position hoti hai ('Cash is a valid position'). Ghalat trade mein loss karne se behtar hai achi setup ka sabar se intezar kiya jaye."
        ),

        "ENTRY" to SmcGlossaryItem(
            termKey = "ENTRY",
            title = "Entry Price / Entry Zone",
            category = "Execution Levels",
            romanUrduSummary = "Dakhlay ki Qeemat (Trade shuru karne ka level)",
            romanUrduDetail = "Market order se kisi bhi qeemat par dakhil hona amateur trader ki nishani hai. " +
                    "NEXUS AI aik 'Entry Zone' deta hai jo ke Order Block, Fair Value Gap ya Fibonacci Discount level par mabni hota hai. " +
                    "Agar price is zone se oper nikal jaye to peechay bhaag kar khareedna nahi chahiye balkay pull-back ka intezar karna chahiye.",
            tradingTipRomanUrdu = "Pro Tip: Entry Zone ke darmiyan limit orders distribute karna average entry price ko behtareen banata hai."
        ),

        "STOP_LOSS" to SmcGlossaryItem(
            termKey = "STOP_LOSS",
            title = "Stop Loss (SL)",
            category = "Capital Protection",
            romanUrduSummary = "Nuqsan Rokne ki Had (Capital Protection)",
            romanUrduDetail = "Stop Loss trading ki sab se ahem life-insurance hai. " +
                    "NEXUS AI stop loss ko random numbers par nahi balkay Market Structure (Swing Low/High) aur ATR (Average True Range) ki madad se calculate karta hai. " +
                    "Agar market apke analysis ke khilaf chali jaye to Stop Loss hit hone par trade band ho jati hai aur apka 98%+ sarmaya aglay trades ke liye mehfooz rehta hai.",
            tradingTipRomanUrdu = "Pro Tip: Kabhi bhi chalti trade mein Stop Loss ko peechay na hatayein (Never move your SL further away in hope). Rule ko follow karein."
        ),

        "TAKE_PROFIT" to SmcGlossaryItem(
            termKey = "TAKE_PROFIT",
            title = "Take Profit (TP1, TP2, TP3)",
            category = "Profit Targets",
            romanUrduSummary = "Munafa Wasool karne ke Muqamat",
            romanUrduDetail = "Ziada tar traders is liye loss karte hain kyunki wo munafa waqt par nahi lete. " +
                    "NEXUS AI teen targets fraham karta hai:\n" +
                    "• TP1 (Conservative): Pehla resistance ya liquidity pool. Yahan 40-50% position close karke Stop Loss ko Break-Even (entry price) par le aana chahiye.\n" +
                    "• TP2 (Main Target): Strong supply zone ya 2.5x Risk-Reward level jahan mazeed 30% profit book kiya jata hai.\n" +
                    "• TP3 (Runner): Fibonacci Extension (1.618) ya major high jahan baki bacha hissa maximize hota hai.",
            tradingTipRomanUrdu = "Pro Tip: TP1 hit hotay hi Stop Loss ko Entry price par shift kar dein (Risk-Free Trade strategy)."
        ),

        "RISK_REWARD" to SmcGlossaryItem(
            termKey = "RISK_REWARD",
            title = "Risk-to-Reward Ratio (R:R)",
            category = "Math & Probability",
            romanUrduSummary = "Khatray aur Munafe ka Tanasub",
            romanUrduDetail = "Har $1 ke nuqsan ke risk ke muqablay mein kitnay dollars ka munafa expected hai. Minimum 1:2 standard mana jata hai. " +
                    "Achi R:R ratio ka faida ye hai ke agar aap 10 mein se sirf 4 trades bhi jeetain, tab bhi aap overall profit mein rehte hain:\n" +
                    "4 jeet x $25 = +$100\n" +
                    "6 haar x $10 = -$60\n" +
                    "Net Profit = +$40!\n" +
                    "NEXUS AI 1:1.5 se kam R:R wali trade ko reject karke NO TRADE declare karta hai.",
            tradingTipRomanUrdu = "Pro Tip: Kamyab trader win-rate se zyada Risk-Reward ratio aur position sizing par tawajjo dete hain."
        ),

        "CONFIDENCE_SCORE" to SmcGlossaryItem(
            termKey = "CONFIDENCE_SCORE",
            title = "Signal Confidence Score",
            category = "AI Confluence",
            romanUrduSummary = "Itefaq-e-Raye ka Score (Confluence Rating)",
            romanUrduDetail = "Confidence score kisi qism ka 'profit guarantee' nahi hai, balkay ye mathematical data confirmation rating hai. " +
                    "Score 7 ahem hisson se mil kar banta hai:\n" +
                    "• Trend (20 pts)\n" +
                    "• Momentum (15 pts)\n" +
                    "• Volume (15 pts)\n" +
                    "• Market Structure (20 pts)\n" +
                    "• Liquidity (10 pts)\n" +
                    "• Support/Resistance (10 pts)\n" +
                    "• Multi-Timeframe Agreement (10 pts)\n" +
                    "Jab score 75 se oper ho to setup ko 'High Confluence' samjha jata hai.",
            tradingTipRomanUrdu = "Pro Tip: 60 se kam score ka matlab hai ke market mein split opinion hai; aisi sorat mein lot size chota rakhein ya trade avoid karein."
        ),

        "LEVERAGE" to SmcGlossaryItem(
            termKey = "LEVERAGE",
            title = "Suggested Maximum Leverage",
            category = "Position Sizing",
            romanUrduSummary = "Udhari Taaqat / Udhar Sarmaya (Strict Limit)",
            romanUrduDetail = "Leverage aik double-edged talwar hai. Agar aap 10x leverage lete hain to market sirf 10% ghalat jaane par apka 100% sarmaya zero (liquidate) ho jata hai. " +
                    "NEXUS AI asset ki ATR volatility aur Stop Loss ki doori ko naap kar 'Suggested Max Leverage' calculate karta hai. " +
                    "High volatility mein system 2x-3x se zyada ki ijazat nahi deta taake apka account hamesha safe rahe.",
            tradingTipRomanUrdu = "Pro Tip: Crypto mein 20x, 50x ya 100x leverage gambling (jowa) hai. Professional traders 2x se 5x se zyada leverage bohot kam istemal karte hain."
        ),

        "INVALIDATION" to SmcGlossaryItem(
            termKey = "INVALIDATION",
            title = "Trade Invalidation Condition",
            category = "Discipline & Rules",
            romanUrduSummary = "Mansookhi ki Shart (Trade Setup Khatam hone ka point)",
            romanUrduDetail = "Har trade aik logical hypothesis (farziya) par banti hai, maslan: 'Price Bullish Order Block ko respect karegi'. " +
                    "Agar 1-hour candle is Order Block ke neechay close ho jaye, to trade setup technically invalidate (batil) ho chuka hota hai. " +
                    "Informaton card mein clear likha hota hai ke konsa candle close ya CHoCH hone par trade se foran nikal jana chahiye, bajaye Stop Loss poora hit hone ka intezar karne ke.",
            tradingTipRomanUrdu = "Pro Tip: Jab invalidation level toot jaye to market se ladaye mat karein; ghalati tasleem karke foran exit karein."
        )
    )

    fun getTerm(id: String): SmcGlossaryItem? = items[id]
}
