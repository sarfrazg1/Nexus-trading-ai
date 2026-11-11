package com.example.data.settings

import com.example.data.learning.LearningLanguageMode

/**
 * Universal 4-Language Localization Dictionary for NEXUS TRADING AI.
 * Supports:
 * 1. English
 * 2. Roman Urdu
 * 3. Standard Urdu (اردو script with Nastaleeq typography)
 * 4. Bilingual (English + Urdu)
 *
 * Preserves standard trading acronyms and financial terms (BOS, RSI, BTC, Long/Short).
 */
object NexusLocalization {

    fun tr(
        lang: LearningLanguageMode,
        english: String,
        romanUrdu: String,
        urdu: String = romanUrdu
    ): String = when (lang) {
        LearningLanguageMode.ENGLISH -> english
        LearningLanguageMode.ROMAN_URDU -> romanUrdu
        LearningLanguageMode.STANDARD_URDU -> urdu
        LearningLanguageMode.BILINGUAL -> "$english / $urdu"
    }

    // Navigation Tabs
    fun tabDashboard(lang: LearningLanguageMode) = tr(lang, "Dashboard", "Dashboard", "ڈیش بورڈ")
    fun tabMarkets(lang: LearningLanguageMode) = tr(lang, "Markets", "Market", "مارکیٹس")
    fun tabSignals(lang: LearningLanguageMode) = tr(lang, "Signals", "Signals", "سگنلز")
    fun tabNexusAi(lang: LearningLanguageMode) = tr(lang, "NEXUS AI", "NEXUS AI", "نیکسس اے آئی")
    fun tabAcademy(lang: LearningLanguageMode) = tr(lang, "Academy", "Academy (Taleem)", "اکیڈمی")
    fun tabScanner(lang: LearningLanguageMode) = tr(lang, "Scanner", "Scanner", "اسکینر")
    fun tabNews(lang: LearningLanguageMode) = tr(lang, "News", "Khabrein", "خبریں")
    fun tabSettings(lang: LearningLanguageMode) = tr(lang, "Settings", "Settings", "سیٹنگز")

    // Common Action Buttons
    fun btnRefresh(lang: LearningLanguageMode) = tr(lang, "Refresh", "Taaza Karein", "تازہ کریں")
    fun btnApply(lang: LearningLanguageMode) = tr(lang, "Apply", "Laagu Karein", "لاگو کریں")
    fun btnCancel(lang: LearningLanguageMode) = tr(lang, "Cancel", "Mansookh", "منسوخ")
    fun btnConfirm(lang: LearningLanguageMode) = tr(lang, "Confirm", "Tasdeeq Karein", "تصدیق کریں")
    fun btnClearCache(lang: LearningLanguageMode) = tr(lang, "Clear Cache", "Cache Saaf Karein", "کیشے صاف کریں")
    fun btnGotIt(lang: LearningLanguageMode) = tr(lang, "Got It", "Samajh Gaya", "سمجھ گیا")
    fun btnClose(lang: LearningLanguageMode) = tr(lang, "Close", "Band Karein", "بند کریں")
    fun btnSend(lang: LearningLanguageMode) = tr(lang, "Send", "Bhejein", "ارسال کریں")
    fun btnCopy(lang: LearningLanguageMode) = tr(lang, "Copy", "Copy Karein", "کاپی کریں")
    fun btnNewChat(lang: LearningLanguageMode) = tr(lang, "New Chat", "Nayi Guftagu", "نئی گفتگو")
    fun btnClearChat(lang: LearningLanguageMode) = tr(lang, "Clear Chat", "Chat Saaf Karein", "چیٹ صاف کریں")

    // Settings Screen Headers & Sections
    fun settingsTitle(lang: LearningLanguageMode) = tr(lang, "TERMINAL CONFIGURATION", "TERMINAL KI TARTIB", "ٹرمینل ترتیبات")
    fun settingsSubtitle(lang: LearningLanguageMode) = tr(lang, "System, Themes & Performance", "System, Themes aur Performance", "سسٹم، تھیمز اور کارکردگی")
    fun sectionAppearance(lang: LearningLanguageMode) = tr(lang, "APPEARANCE & THEME", "THEME AUR RANG", "تھیم اور ظاہری وضع")
    fun sectionLanguage(lang: LearningLanguageMode) = tr(lang, "LANGUAGE & TYPOGRAPHY", "ZABAN AUR FONTS", "زبان اور نستعلیق فونٹ")
    fun sectionPerformance(lang: LearningLanguageMode) = tr(lang, "PERFORMANCE & DATA SAVING", "PERFORMANCE AUR DATA BACHAO", "کارکردگی اور ڈیٹا کی بچت")
    fun sectionDefaults(lang: LearningLanguageMode) = tr(lang, "DEFAULT TERMINAL PREFERENCES", "BUNYADI TARTIBAT", "بنیادی ترجیحات")
    fun sectionNotifications(lang: LearningLanguageMode) = tr(lang, "NOTIFICATIONS & SOUND", "ITLA'AT AUR AWAAZ", "اطلاعات اور آواز")
    fun sectionStorage(lang: LearningLanguageMode) = tr(lang, "CACHE & STORAGE", "CACHE AUR MEMORY", "کیشے اور اسٹوریج")
    fun sectionAboutLegal(lang: LearningLanguageMode) = tr(lang, "ABOUT & LEGAL DISCLAIMERS", "MALOOMAT AUR KHATRAAT", "معلومات اور خطرات کا اعلان")

    // Setting Labels
    fun themeLabel(lang: LearningLanguageMode) = tr(lang, "Display Theme", "Display Theme", "ڈسپلے تھیم")
    fun themeDesc(lang: LearningLanguageMode) = tr(lang, "Select Dark, Light or System mode", "Dark, Light ya System mode muntakhib karein", "ڈارک، لائٹ یا سسٹم وضع منتخب کریں")
    fun languageLabel(lang: LearningLanguageMode) = tr(lang, "Interface Language", "App ki Zaban", "انٹرفیس کی زبان")
    fun languageDesc(lang: LearningLanguageMode) = tr(lang, "Choose English, Roman Urdu, Urdu Script, or Bilingual", "English, Roman Urdu, Urdu ya Bilingual chunein", "انگریزی، رومن اردو، نستعلیق اردو یا بائلنگول منتخب کریں")
    fun urduFontSizeLabel(lang: LearningLanguageMode) = tr(lang, "Urdu Nastaleeq Font Size", "Nastaleeq Font Size", "نستعلیق فونٹ کا سائز")
    fun urduFontSizeDesc(lang: LearningLanguageMode) = tr(lang, "Adjust size for Urdu messages & lessons", "Urdu text ka size chhota ya bada karein", "اردو پیغامات اور اسباق کے لیے فونٹ کا سائز ایڈجسٹ کریں")

    fun dataSavingLabel(lang: LearningLanguageMode) = tr(lang, "Data Saving Mode", "Data Saving Mode (Internet Bachao)", "ڈیٹا سیونگ موڈ")
    fun dataSavingDesc(lang: LearningLanguageMode) = tr(
        lang,
        "Reduces background data, pauses continuous streams, and loads charts on demand.",
        "Background data kam karta hai, charts zaroorat par load karta hai aur battery bachata hai.",
        "پس منظر کا ڈیٹا کم کرتا ہے، چارٹس مانگ پر لوڈ کرتا ہے اور بیٹری بچاتا ہے۔"
    )

    fun lowPerfLabel(lang: LearningLanguageMode) = tr(lang, "Low Performance Device Mode", "Low Performance Device Mode", "کم ریم والے فونز کی سہولت")
    fun lowPerfDesc(lang: LearningLanguageMode) = tr(
        lang,
        "Optimized for 2GB-4GB RAM devices. Disables heavy animations, simplifies charts, and limits cached candles.",
        "2GB-4GB RAM phones ke liye behtareen. Heavy animations band karta hai aur chart memory kam karta hai.",
        "2GB تا 4GB ریم والے آلات کے لیے موزوں۔ غیر ضروری اینیمیشنز بند کر کے میموری محفوظ رکھتا ہے۔"
    )

    fun defaultTfLabel(lang: LearningLanguageMode) = tr(lang, "Default Chart Timeframe", "Bunyadi Chart Timeframe", "بنیادی ٹائم فریم")
    fun defaultWatchlistLabel(lang: LearningLanguageMode) = tr(lang, "Default Watchlist Category", "Bunyadi Watchlist", "بنیادی واچ لسٹ زمرہ")
    fun refreshIntervalLabel(lang: LearningLanguageMode) = tr(lang, "Market Data Polling Rate", "Market Data Refresh ki Raftaar", "ڈیٹا ریفریش کی رفتار")
    fun priceAlertsLabel(lang: LearningLanguageMode) = tr(lang, "Price Alert Notifications", "Price Alert ki Itla'at", "قیمت کے انتباہات")
    fun soundFxLabel(lang: LearningLanguageMode) = tr(lang, "Sound Effects & Audio Cues", "Awaaz aur Audio Alerts", "آڈیو الرٹس اور آواز")

    // Cache clearing
    fun clearCacheDesc(lang: LearningLanguageMode) = tr(
        lang,
        "Frees memory by clearing cached candle records and sparkline buffers. Your settings and bookmarks will remain safe.",
        "Cached candle records aur temporary memory saaf karta hai. Apke settings aur bookmarks mehfooz raheinge.",
        "عارضی کیشے صاف کر کے میموری خالی کرتا ہے۔ آپ کی محفوظ شدہ ترتیبات برقرار رہیں گی۔"
    )
    fun clearCacheConfirmTitle(lang: LearningLanguageMode) = tr(lang, "Clear Application Cache?", "Cache Saaf Karna Chahte Hain?", "کیا آپ کیشے صاف کرنا چاہتے ہیں؟")
    fun clearCacheSuccessMessage(lang: LearningLanguageMode) = tr(lang, "Temporary cache cleared successfully", "Temporary cache kamyabi se saaf ho gaya", "عارضی کیشے کامیابی سے صاف ہو گیا")

    // Legal & Disclaimers
    fun aboutTitle(lang: LearningLanguageMode) = tr(lang, "About NEXUS TRADING AI", "NEXUS TRADING AI ke Baray Mein", "نیکسس ٹریڈنگ اے آئی کے بارے میں")
    fun privacyPolicyTitle(lang: LearningLanguageMode) = tr(lang, "Privacy Policy", "Privacy Policy (Nijiyat)", "پرائیویسی پالیسی")
    fun riskDisclaimerTitle(lang: LearningLanguageMode) = tr(lang, "Risk Disclaimer", "Risk Disclaimer (Khatraat ka Bayan)", "خطرات کی وضاحت")

    val riskDisclaimerEnglish = """
        IMPORTANT RISK WARNING & DISCLAIMER:
        Trading cryptocurrency, derivatives, futures, commodities, and foreign exchange involves a high level of risk and may not be suitable for all investors. High leverage can work against you as well as for you.
        
        Before deciding to trade, carefully consider your investment objectives, level of experience, and risk appetite. You could sustain a loss of some or all of your initial investment; do not trade with capital you cannot afford to lose.
        
        NEXUS TRADING AI is an educational, analytical, and charting terminal. It DOES NOT provide financial advice, execution of trades, or guaranteed profits. All calculations, SMC zones, signals, and Fibonacci levels represent algorithmic probability models derived from real market data, not financial endorsements.
    """.trimIndent()

    val riskDisclaimerRomanUrdu = """
        AHEM KHATRAAT KI WAZAHAT (RISK DISCLAIMER):
        Crypto, Futures, Forex aur Commodities mein trading intehai khatarnak (high risk) karobar hai aur har kisi ke liye munasib nahi hai. Ziada leverage jahan faida de sakti hai, wahan aik lamhay mein apka pura sarmaya wash (zero) bhi kar sakti hai.
        
        Trading shuru karne se pehle apni salahiyat aur risk lene ki taqat ka jaiza lein. Sirf wahi paisa lagayein jiske khonay ka aap bardasht kar saktay hon.
        
        NEXUS TRADING AI sirf aik taleemi aur technical analysis tool hai. Ye kisi qisam ka financial mashwara ya nafa ka yaqeen nahi deta. Tamam signals aur calculations market data ke hisab se hain aur ghalat bhi ho saktay hain. Hamesha apna stop loss lagayein.
    """.trimIndent()

    val riskDisclaimerUrdu = """
        اہم خطرات کی وضاحت (Risk Disclaimer):
        کرپٹو کرنسی، ڈیریویٹوز، فیوچرز، کموڈٹیز اور فاریکس ٹریڈنگ انتہائی پرخطر شعبہ ہے اور ہر فرد کے لیے موزوں نہیں ہے۔ زیادہ لیوریج جتنا فائدہ دے سکتی ہے، اتنا ہی فوری نقصان کا باعث بھی بن سکتی ہے۔
        
        ٹریڈنگ سے قبل اپنے مالی اہداف اور رسک برداشت کرنے کی صلاحیت کا باریک بینی سے جائزہ لیں۔ کبھی بھی وہ رقم مت لگائیں جس کے ضائع ہونے کا آپ متحمل نہیں ہو سکتے۔
        
        نیکسس ٹریڈنگ اے آئی صرف ایک تعلیمی اور تجزیاتی ٹرمینل ہے۔ یہ کوئی حتمی مالی مشورہ، تجارت کی خودکار انجام دہی یا منافع کی گارنٹی فراہم نہیں کرتا۔ تمام حسابی ماڈلز حقیقی ڈیٹا پر مبنی احتمالات ہیں۔ ہمیشہ مناسب رسک مینجمنٹ اور اسٹاپ لاس کا استعمال کریں۔
    """.trimIndent()

    val privacyPolicyEnglish = """
        PRIVACY & DATA PROTECTION:
        • 100% On-Device Local Storage: Your bookmarks, lesson progress, theme, and API keys are stored solely on your local device.
        • No Personal Tracking: NEXUS TRADING AI does not collect, sell, or transmit personal identity records or browsing habits.
        • Secure Public Feeds: Market data is fetched directly through verified public APIs (Binance, CoinGecko). No user credentials are sent to third parties.
    """.trimIndent()

    val privacyPolicyRomanUrdu = """
        NIJIYAT AUR DATA KI HIFAZAT (PRIVACY POLICY):
        • 100% Local Device Storage: Apke bookmarks, lesson progress, settings aur keys sirf apke phone mein mehfooz rehte hain.
        • Koi Zaati Data Collect Nahi Hota: NEXUS TRADING AI apka personal data ya trading history kisi ko nahi bechta.
        • Mehfooz Public Feeds: Market rates verified public sources se aatay hain. Apka phone poori tarah mehfooz hai.
    """.trimIndent()

    val privacyPolicyUrdu = """
        پرائیویسی اور ڈیٹا کی حفاظت:
        • 100 فیصد لوکل ڈیوائس اسٹوریج: آپ کی محفوظ کردہ ترتیبات، بک مارکس اور چیٹ ہسٹری صرف آپ کے فون میں محفوظ رہتی ہے۔
        • ذاتی ڈیٹا کی عدم فروخت: نیکسس ٹریڈنگ اے آئی آپ کی ذاتی معلومات یا تجارتی تاریخ کو کبھی فروخت نہیں کرتا۔
        • محفوظ عوامی فیڈز: مارکیٹ ریٹس بین الاقوامی مصدقہ پبلک ذرائع سے براہ راست حاصل کیے جاتے ہیں۔
    """.trimIndent()
}
