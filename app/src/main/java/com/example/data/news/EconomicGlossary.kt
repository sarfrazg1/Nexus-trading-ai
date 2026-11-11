package com.example.data.news

import com.example.data.learning.LearningLanguageMode

data class EconomicTerm(
    val key: String,
    val title: String,
    val abbreviation: String,
    val category: String,
    val explanationEn: String,
    val explanationUrdu: String,
    val marketImpactEn: String,
    val marketImpactUrdu: String
) {
    fun getExplanation(mode: LearningLanguageMode): String {
        return when (mode) {
            LearningLanguageMode.ENGLISH -> explanationEn
            LearningLanguageMode.ROMAN_URDU -> explanationUrdu
            LearningLanguageMode.STANDARD_URDU -> explanationUrdu
            LearningLanguageMode.BILINGUAL -> "$explanationEn\n\n📌 Roman Urdu:\n$explanationUrdu"
        }
    }

    fun getImpact(mode: LearningLanguageMode): String {
        return when (mode) {
            LearningLanguageMode.ENGLISH -> marketImpactEn
            LearningLanguageMode.ROMAN_URDU -> marketImpactUrdu
            LearningLanguageMode.STANDARD_URDU -> marketImpactUrdu
            LearningLanguageMode.BILINGUAL -> "$marketImpactEn\n\n📌 Market Asar:\n$marketImpactUrdu"
        }
    }
}

object EconomicGlossaryDatabase {

    val terms: List<EconomicTerm> = listOf(
        EconomicTerm(
            key = "cpi",
            title = "Consumer Price Index",
            abbreviation = "CPI",
            category = "Inflation",
            explanationEn = "CPI measures the average monthly change in prices paid by consumers for goods and services. It is the primary metric used to track retail inflation.",
            explanationUrdu = "CPI mehngai (inflation) naapne ka aik aham tareen paimana hai. Ye batata hai ke aam awam ki zaroori cheezon ki qeematon mein kitna izafa hua hai.",
            marketImpactEn = "Higher CPI -> Inflation risk -> Fed may raise rates -> Bullish DXY, Bearish Crypto & Stocks. Lower CPI -> Easing inflation -> Bullish Crypto.",
            marketImpactUrdu = "Agar CPI umeed se zyada aaye to mehngai barhti hai aur Fed interest rates barha sakta hai, jis se Crypto aur Stocks girte hain."
        ),
        EconomicTerm(
            key = "ppi",
            title = "Producer Price Index",
            abbreviation = "PPI",
            category = "Inflation",
            explanationEn = "PPI tracks the average change over time in the selling prices received by domestic producers for their output. It serves as an early indicator of consumer inflation.",
            explanationUrdu = "PPI producers (karkhanon aur factories) ki selling prices mein tabdeeli ko naapta hai. Ye CPI se pehle aane wali wholesale mehngai ka ishara deta hai.",
            marketImpactEn = "Higher PPI signals rising production costs passed to consumers -> Inflationary -> Bearish for risk assets.",
            marketImpactUrdu = "Agar PPI barhta hai to iska matlab factories ka kharcha barh raha hai jo aage chal kar CPI aur mehngai barhayega."
        ),
        EconomicTerm(
            key = "fed_rate",
            title = "Federal Funds Rate",
            abbreviation = "Fed Rate",
            category = "Monetary Policy",
            explanationEn = "The interest rate at which commercial banks borrow and lend their excess reserves to each other overnight, directly controlled by the US Federal Reserve.",
            explanationUrdu = "America ke Central Bank (Federal Reserve) ki sood ki sharah (interest rate). Is se poori dunya ki liquidity aur currency asar andaz hoti hai.",
            marketImpactEn = "Rate hikes suck liquidity out of markets -> Bearish Crypto & Equities. Rate cuts inject liquidity -> Bullish Bull Run fuel.",
            marketImpactUrdu = "Jab Fed sood barhata hai to market se paisa nikal kar banks mein jata hai (Bearish). Jab sood kam karta hai to Crypto mein tezi aati hai."
        ),
        EconomicTerm(
            key = "nfp",
            title = "Non-Farm Payrolls",
            abbreviation = "NFP",
            category = "Employment",
            explanationEn = "A monthly statistic representing the total number of paid workers in the US, excluding farm employees, government personnel, and non-profit employees.",
            explanationUrdu = "America mein har maheene banne wali nayi mulazmaton (jobs) ki tadad ka official data. Is se US economy ki taqat ka andaza lagaya jata hai.",
            marketImpactEn = "Strong NFP -> Strong economy -> Fed can keep rates high longer -> High volatility, strong Dollar. Weak NFP -> Potential recession -> Rate cuts.",
            marketImpactUrdu = "Agar NFP zyada aaye to US economy mazboot hoti hai aur Fed interest rate high rakh sakta hai, jis se market mein shadeed jhatkay lagte hain."
        ),
        EconomicTerm(
            key = "pce",
            title = "Personal Consumption Expenditures",
            abbreviation = "PCE",
            category = "Inflation",
            explanationEn = "The Federal Reserve's preferred inflation gauge that captures a wider range of consumer spending habits and substitution patterns than standard CPI.",
            explanationUrdu = "Federal Reserve ka pasandeeda mehngai ka paimana. Ye CPI se zyada gehrai se dekhta hai ke log paisa kahan aur kaisay kharch kar rahe hain.",
            marketImpactEn = "Core PCE above forecast -> Fed remains hawkish -> Bearish crypto. Cooling PCE -> Paves way for rate cuts -> Bullish risk assets.",
            marketImpactUrdu = "Agar PCE kam ho raha ho to Fed ko tasalli milti hai aur wo interest rate kam kar sakta hai jo Crypto ke liye bohot acha hota hai."
        ),
        EconomicTerm(
            key = "dxy",
            title = "US Dollar Index",
            abbreviation = "DXY",
            category = "Currency Macro",
            explanationEn = "An index measuring the value of the US dollar relative to a basket of six major world currencies (Euro, Yen, Pound, CAD, Krona, Franc).",
            explanationUrdu = "American Dollar ki dunya ki 6 bari currencies ke muqablay mein taqat naapne ka index.",
            marketImpactEn = "DXY has a strong inverse correlation with Bitcoin and Gold: Rising DXY = Bearish Bitcoin/Gold. Falling DXY = Bullish Bitcoin/Gold.",
            marketImpactUrdu = "Dollar Index ka Bitcoin aur Sonay (Gold) ke sath ulta (inverse) rishta hai: DXY oper jata hai to Bitcoin aur Gold aksar girte hain."
        ),
        EconomicTerm(
            key = "gdp",
            title = "Gross Domestic Product",
            abbreviation = "GDP",
            category = "Economic Growth",
            explanationEn = "The total monetary or market value of all finished goods and services produced within a country's borders in a specific time period.",
            explanationUrdu = "Kisi mulk ki majmooi maeeshat (economy) ka kul volume. Is se mulki taraqqi ya mandi (recession) ka pata chalta hai.",
            marketImpactEn = "Expanding GDP indicates robust economic health, while two consecutive quarters of negative GDP define a technical recession.",
            marketImpactUrdu = "Agar GDP barh raha ho to maeeshat taraqqi par hai. Agar musalsal 2 quarters giray to isay mandi (recession) kehte hain."
        ),
        EconomicTerm(
            key = "treasury_yields",
            title = "US Treasury Yields",
            abbreviation = "10Y / 2Y Yields",
            category = "Bonds & Rates",
            explanationEn = "The annual return paid by the US government on its sovereign debt securities. 10-year yield is the global benchmark for risk-free lending.",
            explanationUrdu = "American hakoomat ke sarkari bonds par milne wala munafa (sood). 10-saala yield poori dunya ke loans ka benchmark hoti hai.",
            marketImpactEn = "Rising yields attract capital into safe US bonds away from risky tech stocks and crypto, causing valuations to contract.",
            marketImpactUrdu = "Jab Treasury yields barhti hain to baray investors risk wali cheezon (Crypto/Stocks) se paisa nikal kar sarkari bonds mein laga dete hain."
        ),
        EconomicTerm(
            key = "etf_flows",
            title = "Spot ETF Inflows / Outflows",
            abbreviation = "ETF Flows",
            category = "Institutional Crypto",
            explanationEn = "Net capital entering or exiting regulated spot Bitcoin and Ethereum exchange-traded funds managed by Wall Street firms like BlackRock and Fidelity.",
            explanationUrdu = "Wall Street ki bari companies (BlackRock, Fidelity) ke Spot Bitcoin aur Ethereum ETFs mein aane wala ya nikalne wala sarmaya.",
            marketImpactEn = "Sustained net inflows remove physical spot coins from exchange reserves -> Massive supply squeeze -> Strong Bullish momentum.",
            marketImpactUrdu = "Jab ETFs mein baray paimanay par paisa aata hai (Inflow) to Bitcoin exchanges se khareed liya jata hai, jis se zabardast tezi aati hai."
        ),
        EconomicTerm(
            key = "pmi",
            title = "Purchasing Managers' Index",
            abbreviation = "PMI",
            category = "Macro Survey",
            explanationEn = "A prevailing direction of economic trends in manufacturing and service sectors based on monthly surveys of senior supply chain managers.",
            explanationUrdu = "Manufacturing aur service sectors ki business activities ka index. 50 se oper expansion aur 50 se neechay shrinkage hoti hai.",
            marketImpactEn = "PMI above 50 indicates expansion. PMI below 50 signals contracting economic activity and potential slowdown.",
            marketImpactUrdu = "Agar PMI 50 se zyada ho to factories chal rahi hain aur maeeshat taraqqi kar rahi hai. 50 se kam ho to mandi ka khatra hota hai."
        )
    )

    fun findTermByKey(key: String): EconomicTerm? {
        return terms.firstOrNull { it.key.equals(key, ignoreCase = true) || it.abbreviation.equals(key, ignoreCase = true) }
    }

    fun detectTermsInText(text: String): List<EconomicTerm> {
        val lower = text.lowercase()
        return terms.filter { term ->
            lower.contains(term.abbreviation.lowercase()) ||
            lower.contains(term.title.lowercase()) ||
            (term.key == "fed_rate" && (lower.contains("fed") || lower.contains("interest rate") || lower.contains("powell") || lower.contains("fomc"))) ||
            (term.key == "cpi" && (lower.contains("cpi") || lower.contains("inflation"))) ||
            (term.key == "etf_flows" && (lower.contains("etf") || lower.contains("blackrock") || lower.contains("fidelity") || lower.contains("inflow")))
        }
    }
}
