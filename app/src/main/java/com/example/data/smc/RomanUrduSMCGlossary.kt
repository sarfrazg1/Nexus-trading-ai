package com.example.data.smc

/**
 * Educational Glossary providing comprehensive Roman Urdu explanations
 * for all Smart Money Concepts (SMC), Liquidity, and Fibonacci trading terms.
 */
data class SmcGlossaryItem(
    val termKey: String,
    val title: String,
    val category: String,
    val romanUrduSummary: String,
    val romanUrduDetail: String,
    val tradingTipRomanUrdu: String
)

object RomanUrduSMCGlossary {

    private val items = listOf(
        SmcGlossaryItem(
            termKey = "BOS",
            title = "Break of Structure (BOS)",
            category = "Market Structure",
            romanUrduSummary = "Market ka pichlay swing high ya low ko break kar ke trend ko jari rakhna.",
            romanUrduDetail = "Jab market uptrend mein chal rahi ho aur pichlay Higher High (HH) ko tor kar ooper naya high banaye, to isay Bullish BOS kehtay hain. Isi tarah jab downtrend mein price pichlay Lower Low (LL) ko tor kar aur neechay jaye, to ye Bearish BOS kehlata hai. Ye is baat ka saboot hota hai ke baray institutions (Smart Money) abhi bhi usi simt (direction) mein market ko dhabba de rahay hain.",
            tradingTipRomanUrdu = "Tip: Hamesha candle body ke close ka intezar karein. Sirf wick se break hona fakeout ya liquidity sweep ho sakta hai."
        ),
        SmcGlossaryItem(
            termKey = "CHOCH",
            title = "Change of Character (CHoCH)",
            category = "Market Structure",
            romanUrduSummary = "Market trend badalnay ka sab se pehla ahem ishara (Trend Reversal).",
            romanUrduDetail = "CHoCH tab hota hai jab market ka ravaiyya (character) tabdeel hota hai. Maslan, agar market musalsal gir rahi thi (downtrend) aur achanak pichlay Lower High (LH) ko tor kar ooper band ho jaye, to ye Bullish CHoCH hai—yani ab market bullish hone lagi hai. Agar uptrend mein achanak pichla Higher Low (HL) toot jaye to ye Bearish CHoCH hai. Ye reversal trade pakarnay ka golden signal hota hai.",
            tradingTipRomanUrdu = "Tip: CHoCH dekh kar foran entry na lein, balkay price ke wapas Order Block ya FVG par aane (pullback) ka intezar karein."
        ),
        SmcGlossaryItem(
            termKey = "HH_HL",
            title = "Higher High & Higher Low (HH / HL)",
            category = "Market Structure",
            romanUrduSummary = "Uptrend ki pehchan: Har naya high pichlay se ooper, aur har low pichlay low se ooper.",
            romanUrduDetail = "Uptrend tab banta hai jab market Higher Highs (HH) aur Higher Lows (HL) banati hui ooper jati hai. Higher High ka matlab hai khareedar (buyers) bohot taqatwar hain, aur Higher Low ka matlab hai ke sellers market ko zyada girane mein nakaam rahay hain.",
            tradingTipRomanUrdu = "Tip: Uptrend mein sirf Buy ki trades talaash karein, short na karein jab tak CHoCH confirm na ho."
        ),
        SmcGlossaryItem(
            termKey = "LH_LL",
            title = "Lower High & Lower Low (LH / LL)",
            category = "Market Structure",
            romanUrduSummary = "Downtrend ki nishani: Har naya low pichlay se neechay, aur har high pichlay high se neechay.",
            romanUrduDetail = "Downtrend tab banta hai jab market Lower Highs (LH) aur Lower Lows (LL) banati hui neechay girti hai. Lower Low ka matlab farokht-kunandgan (sellers) control mein hain, aur Lower High ka matlab hai buyers market ko ooper khenchne mein nakaam hain.",
            tradingTipRomanUrdu = "Tip: Downtrend mein girte huay chhaqu (falling knife) ko pakarnay ki koshish na karein. Sell on rallies ki strategy behtar hai."
        ),
        SmcGlossaryItem(
            termKey = "BULLISH_OB",
            title = "Bullish Order Block (OB)",
            category = "Order Blocks",
            romanUrduSummary = "Smart Money ki khareedari zone (Baray banks ke Buy orders).",
            romanUrduDetail = "Bullish Order Block wo aakhri down-close (red/bearish) candle hoti hai jahan se market ne bohot taiz ooper ki janib chhalang lagai ho aur market structure tori ho (BOS). Baray institutions is candle par apnay bohot baray buy orders chhor dete hain. Jab price wapas is zone mein aati hai to yahan se tezi se bounce karti hai.",
            tradingTipRomanUrdu = "Tip: Bullish OB ke 50% hissay (Mean Threshold) par entry aur OB ke low ke neechay stop loss lagana smart risk management hai."
        ),
        SmcGlossaryItem(
            termKey = "BEARISH_OB",
            title = "Bearish Order Block (OB)",
            category = "Order Blocks",
            romanUrduSummary = "Smart Money ki bikwali zone (Institutions ke Sell orders).",
            romanUrduDetail = "Bearish Order Block wo aakhri up-close (green/bullish) candle hoti hai jahan se bohot aggressive girawat shuru hui thi aur neechay ka structure break hua tha. Jab price wapas ooper is zone mein aati hai to institutions apnay baqi bache huay sell orders execute kartay hain aur market ko dobara neechay phenk dete hain.",
            tradingTipRomanUrdu = "Tip: Agar candle is OB ke ooper body se close ho jaye to ye OB 'Invalidated' (kharij) ho jata hai."
        ),
        SmcGlossaryItem(
            termKey = "MITIGATION",
            title = "Mitigation & Invalidation",
            category = "Order Blocks",
            romanUrduSummary = "Zone ka dobara touch hona (Mitigation) ya toot kar bekaar ho jana (Invalidation).",
            romanUrduDetail = "Jab price ban-nay ke baad pehli dafa wapas Order Block ya FVG ko aakar chhooti (touch karti) hai, to isay 'Mitigation' kehtay hain. Iska matlab institutions ne apnay pending orders fill kar liye hain. Agar price is zone ko cheer kar doosri taraf nikal jaye to ye 'Invalidated' ho jati hai aur mazeed support/resistance ka kaam nahi karti.",
            tradingTipRomanUrdu = "Tip: Fresh / Unmitigated zones par trade karne ki kamyabi ka imkan mitigated zones se kahin zyada hota hai."
        ),
        SmcGlossaryItem(
            termKey = "FVG",
            title = "Fair Value Gap (FVG)",
            category = "Imbalances",
            romanUrduSummary = "Price ka 3 candles ke darmiyan khali chhoota hua imbalance ya liquidity gap.",
            romanUrduDetail = "Jab market bohot tezi se kisi aik simt mein bhagti hai, to 3 candles ke darmiyan aik aisi jagah reh jati hai jahan sirf aik taraf (sirf buy ya sirf sell) trading hui hoti hai. Candle 1 ki wick aur Candle 3 ki wick ke darmiyan ka fasla Fair Value Gap kehlata hai. Market aik maheenay, haftay ya ghanton mein is gap ko fill karne zaroor aati hai taakay balance qayam ho sakay.",
            tradingTipRomanUrdu = "Tip: Bullish FVG price ke liye support ka kaam karti hai, jabkay Bearish FVG strong resistance banti hai."
        ),
        SmcGlossaryItem(
            termKey = "BSL",
            title = "Buy-Side Liquidity (BSL)",
            category = "Liquidity Analysis",
            romanUrduSummary = "Pichlay swing highs ke ooper retail traders ke buy stop-loss orders.",
            romanUrduDetail = "Retail short traders apnay stop loss pichlay highs ke ooper rakhtay hain. Smart money ko apni arabon rupay ki khareedari ya selling karne ke liye in stop loss ki zaroorat hoti hai. Market aksar swing high ko thorra sa cross karti hai taakay yeh BSL execute ho, aur phir achanak wapas neechay mudh jati hai.",
            tradingTipRomanUrdu = "Tip: BSL sweep hone ke baad short ki position dhoondna smart traders ka mashhoor setup hai."
        ),
        SmcGlossaryItem(
            termKey = "SSL",
            title = "Sell-Side Liquidity (SSL)",
            category = "Liquidity Analysis",
            romanUrduSummary = "Pichlay swing lows ke neechay retail buyers ke sell stop-loss orders.",
            romanUrduDetail = "Jab ziada tar retail traders buy mein bethay hotay hain to unkay stop loss swing lows ke bilkul neechay lagay hotay hain. Market in lows ke neechay aakar unkay stops ko trigger karti hai (liquidity capture), aur liquidity milte hi tez speed se ooper rally kar jati hai.",
            tradingTipRomanUrdu = "Tip: Low tootne par panic sell na karein, pehle check karein kahin ye SSL sweep to nahi ban raha."
        ),
        SmcGlossaryItem(
            termKey = "EQH_EQL",
            title = "Equal Highs & Equal Lows (EQH / EQL)",
            category = "Liquidity Magnets",
            romanUrduSummary = "Do ya teen wicks ka bilkul ek hi price par hona (Double Top / Bottom).",
            romanUrduDetail = "Retail technical analysis mein isay Double Top ya Double Bottom kehtay hain aur samajhtay hain ke ye strong level hai. Lekin Smart Money Concepts ke mutabiq, Equal Highs (EQH) aur Equal Lows (EQL) dunya ka sab se bara liquidity magnet hotay hain. Is level ke theek ooper ya neechay hadd darja stops hotay hain, jinhein market 90% cases mein sweep karne aati hai.",
            tradingTipRomanUrdu = "Tip: Equal Highs ke bilkul ooper apna stop loss mat rakhein, thora fasla dain."
        ),
        SmcGlossaryItem(
            termKey = "LIQUIDITY_SWEEP",
            title = "Liquidity Sweep & Wick Rejection",
            category = "Liquidity Analysis",
            romanUrduSummary = "Level ke bahar lambi wick mar kar foran wapas range mein dakhil hona.",
            romanUrduDetail = "Jab candle kisi ahem swing high ya low ke paar wick to banati hai lekin uski body level ke andar hi band (close) ho jati hai, to isay Liquidity Sweep kehtay hain. Iska matlab hai ke bahar koi real buyers/sellers nahi thay, sirf stops capture kiye gaye thay.",
            tradingTipRomanUrdu = "Tip: Sweep ke foran baad candle confirmation (opposite color pinbar ya engulfing) par entry li jati hai."
        ),
        SmcGlossaryItem(
            termKey = "STOP_HUNT",
            title = "Stop-Hunt (Potential Only)",
            category = "Liquidity Analysis",
            romanUrduSummary = "Retail traders ko trap karne ke liye chalai gayi tez harkat (Not Guaranteed).",
            romanUrduDetail = "Khabardar: Stop-hunt ka lafz market ki tezi ko darshata hai, ye koi 100% guarantee nahi hoti. Aam tor par high-impact news ya session open par price kisi level ko tod kar retail traders ko lalach deti hai, unke stop orders khati hai, aur foran ulat simt mein bhaag jati hai.",
            tradingTipRomanUrdu = "Tip: Is system mein stop-hunt ko 'Potential Only' ke tor par mark kiya gaya hai taakay aap blindly trade na karein."
        ),
        SmcGlossaryItem(
            termKey = "SUPPLY_DEMAND",
            title = "Supply & Demand Zones",
            category = "Support & Resistance",
            romanUrduSummary = "Asli khareedari (Demand) aur farokht (Supply) ke institutional marakiz.",
            romanUrduDetail = "Demand Zone wo qeemat ka area hai jahan se pehle bohot zabardast khareedari shuru hui thi aur market tez ooper gayi thi. Supply Zone wo area hai jahan se heavy selling shuru hui thi. Yeh aam support aur resistance se ziada taqatwar hotay hain kyunkay ye volume aur displacement par mabni hotay hain.",
            tradingTipRomanUrdu = "Tip: Demand zone par Buy aur Supply zone par Sell karne se Risk-to-Reward ratio hamesha behtareen rehta hai."
        ),
        SmcGlossaryItem(
            termKey = "FIB_RETRACEMENT",
            title = "Fibonacci Retracement (0.236 - 0.786)",
            category = "Fibonacci",
            romanUrduSummary = "Puri move ke baad market kitna wapas pull legi (Correction naapnay ka paimana).",
            romanUrduDetail = "Fibonacci levels market mein natural math ratios par kaam kartay hain: 0.236 (shallow pullback), 0.382 (normal), 0.500 (equilibrium/halfway mark), 0.618 (mashhoor Golden Pocket), aur 0.786 (deep discount). Jab market kisi swing high aur low ke darmiyan move karti hai, to smart buyers 0.50 se 0.618 ke darmiyan discounted price par entry letay hain.",
            tradingTipRomanUrdu = "Tip: 0.50 se neechay ka hissa Discount Zone kehlata hai (Buy ke liye behtar), aur 0.50 se ooper Premium Zone (Sell ke liye behtar)."
        ),
        SmcGlossaryItem(
            termKey = "GOLDEN_POCKET",
            title = "Golden Pocket (0.618 - 0.650 Fib)",
            category = "Fibonacci",
            romanUrduSummary = "Dunya ka sab se kamyab aur mashhoor reversal Fibonacci level.",
            romanUrduDetail = "Fibonacci ka 0.618 aur 0.650 level 'Golden Pocket' kehlatay hain. Dunya bhar ke algo trading bots aur institutional traders is level par apnay limit orders set kartay hain. Jab Golden Pocket kisi Order Block ya FVG ke sath mil jaye (confluence), to us trade ki accuracy bohot high ho jati hai.",
            tradingTipRomanUrdu = "Tip: Agar Golden Pocket par Bullish Order Block mojood ho, to ye highest probability trade setup banta hai."
        ),
        SmcGlossaryItem(
            termKey = "FIB_EXTENSION",
            title = "Fibonacci Extension (1.272, 1.618, 2.618)",
            category = "Fibonacci",
            romanUrduSummary = "Take-Profit targets aur naye highs/lows maloom karne ke targets.",
            romanUrduDetail = "Jab market pichlay swing high ko tod kar naye unchartered territory mein dakhil hoti hai, to ye pata lagane ke liye ke price kahan ja kar rukay gi, Fibonacci Extensions (1.272, 1.618 Golden Extension, aur 2.618) istemal kiye jatay hain. Ye traders ke makhsoos Take Profit (TP) levels hotay hain.",
            tradingTipRomanUrdu = "Tip: 1.618 par hamesha apni trade ka kam az kam 50% profit book karein."
        ),
        SmcGlossaryItem(
            termKey = "MTF_CONFLUENCE",
            title = "Multi-Timeframe Analysis (MTF)",
            category = "Strategy",
            romanUrduSummary = "Mukhtalif timeframes (1m se 1w) ke darmiyan ittefaq (agreement) dekhna.",
            romanUrduDetail = "Sirf 1 minute ya 5 minute ke chart par trade karna aksar dhoka deta hai. Smart traders pehle baray timeframe (1D ya 4H) par trend aur Order Block dekhte hain, aur phir chhotay timeframe (15m, 5m, 1m) par accurate entry dhoondte hain. Jab Higher Timeframe aur Lower Timeframe dono aik hi simt mein mutfiq hon (confluence), to trade kamyab hone ke chances double ho jatay hain.",
            tradingTipRomanUrdu = "Tip: Kabhi bhi Higher Timeframe (4H/1D) ke khilaf chhotay timeframe par trade na lagayein."
        )
    )

    fun getAllItems(): List<SmcGlossaryItem> = items

    fun getItem(key: String): SmcGlossaryItem {
        val normalized = key.uppercase().trim()
        return items.firstOrNull {
            it.termKey.equals(normalized, ignoreCase = true) ||
            it.termKey.contains(normalized, ignoreCase = true) ||
            normalized.contains(it.termKey, ignoreCase = true)
        } ?: items.first()
    }
}
