package com.example.ui.i18n

enum class AppLanguage(val code: String, val title: String, val flag: String) {
    TR("tr", "Türkçe", "🇹🇷"),
    EN("en", "English", "🇬🇧"),
    DE("de", "Deutsch", "🇩🇪")
}

object AppStrings {
    private val translations = mapOf(
        "app_name" to mapOf("tr" to "VİZYON TV", "en" to "VİZYON TV", "de" to "VİZYON TV"),
        "live_tv" to mapOf("tr" to "Canlı TV", "en" to "Live TV", "de" to "Live-TV"),
        "news" to mapOf("tr" to "Haber", "en" to "News", "de" to "Nachrichten"),
        "kids" to mapOf("tr" to "Çocuk", "en" to "Kids", "de" to "Kinder"),
        "sports" to mapOf("tr" to "Spor", "en" to "Sports", "de" to "Sport"),
        "movies" to mapOf("tr" to "Filmler", "en" to "Movies", "de" to "Filme"),
        "series" to mapOf("tr" to "Diziler", "en" to "Series", "de" to "Serien"),
        "radio" to mapOf("tr" to "Radyo", "en" to "Radio", "de" to "Radio"),
        "favorites" to mapOf("tr" to "Favoriler", "en" to "Favorites", "de" to "Favoriten"),
        "religious" to mapOf("tr" to "Dini", "en" to "Religious", "de" to "Religiös"),
        "music" to mapOf("tr" to "Müzik", "en" to "Music", "de" to "Musik"),
        "all" to mapOf("tr" to "Tümü", "en" to "All", "de" to "Alle"),
        "settings" to mapOf("tr" to "Ayarlar", "en" to "Settings", "de" to "Einstellungen"),
        "search_hint" to mapOf("tr" to "Kanal, haber, çocuk veya spor ara...", "en" to "Search channel, news, kids or sport...", "de" to "Kanal, Nachrichten, Kinder oder Sport suchen..."),
        "watch_now" to mapOf("tr" to "Hemen İzle", "en" to "Watch Now", "de" to "Jetzt Anschauen"),
        "add_channel" to mapOf("tr" to "Kanal Ekle", "en" to "Add Channel", "de" to "Kanal Hinzufügen"),
        "theme" to mapOf("tr" to "Tema Modu", "en" to "Theme Mode", "de" to "Design-Modus"),
        "theme_dark" to mapOf("tr" to "Koyu Sinema", "en" to "Dark Cinema", "de" to "Dunkles Kino"),
        "theme_light" to mapOf("tr" to "Açık Modern", "en" to "Light Modern", "de" to "Helles Modern"),
        "language" to mapOf("tr" to "Uygulama Dili", "en" to "Language", "de" to "Sprache"),
        "refresh_channels" to mapOf("tr" to "Kanalları Yenile", "en" to "Refresh Channels", "de" to "Kanäle Aktualisieren"),
        "no_channels" to mapOf("tr" to "Yayın bulunamadı", "en" to "No streams found", "de" to "Keine Streams gefunden"),
        "live_badge" to mapOf("tr" to "CANLI", "en" to "LIVE", "de" to "LIVE"),
        "remote_hint" to mapOf("tr" to "Kumanda: [OK] Oynat/Kanal Listesi • [Sol/Sağ] Sarma • [Yukarı/Aşağı] Kanal Değiştir", "en" to "Remote: [OK] Play/Channel List • [Left/Right] Seek • [Up/Down] Zap Channel", "de" to "Fernbedienung: [OK] Wiedergabe/Senderliste • [Links/Rechts] Spulen • [Auf/Ab] Sender Wechseln"),
        "sport_tag" to mapOf("tr" to "CANLI MAÇ & SPOR", "en" to "LIVE MATCH & SPORT", "de" to "LIVE-SPIEL & SPORT"),
        "continue_watching" to mapOf("tr" to "İzlemeye Devam Et", "en" to "Continue Watching", "de" to "Weiterschauen"),
        "clear_history" to mapOf("tr" to "Kaldır", "en" to "Remove", "de" to "Entfernen")
    )

    fun get(key: String, language: AppLanguage): String {
        return translations[key]?.get(language.code) ?: translations[key]?.get("tr") ?: key
    }
}
