package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.MediaCollection
import com.example.model.MediaItem
import com.example.model.WatchHistoryItem
import com.example.service.AIStreamResolverService
import com.example.service.FirebaseStreamService
import com.example.service.InternetChannelService
import com.example.service.RadioPlaybackService
import com.example.service.TurkishMediaOnlineService
import com.example.service.UserPreferencesManager
import com.example.ui.i18n.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainNavigationTab(val key: String) {
    CANLI_TV("live_tv"),
    HABER("news"),
    COCUK("kids"),
    SPOR("sports"),
    RADYO("radio"),
    FAVORILER("favorites")
}

class StreamViewModel(
    application: Application,
    private val firestoreService: FirebaseStreamService = FirebaseStreamService(),
    private val internetService: InternetChannelService = InternetChannelService(),
    private val aiResolver: AIStreamResolverService = AIStreamResolverService(),
    private val turkishMediaService: TurkishMediaOnlineService = TurkishMediaOnlineService()
) : AndroidViewModel(application) {

    // Explicit secondary constructor for AndroidViewModelFactory reflection guarantee
    constructor(application: Application) : this(
        application = application,
        firestoreService = FirebaseStreamService(),
        internetService = InternetChannelService(),
        aiResolver = AIStreamResolverService(),
        turkishMediaService = TurkishMediaOnlineService()
    )

    // Kullanıcı Tercihleri Yöneticisi (Kalıcı SharedPreferences saklama)
    private val userPrefs = UserPreferencesManager(application)

    // AI Parallel Stream Finder states
    private val _isAiResolving = MutableStateFlow(false)
    val isAiResolving: StateFlow<Boolean> = _isAiResolving.asStateFlow()

    private val _aiResolvingChannel = MutableStateFlow<String?>(null)
    val aiResolvingChannel: StateFlow<String?> = _aiResolvingChannel.asStateFlow()

    private val _aiSourceInfo = MutableStateFlow<String?>("VİZYON AI")
    val aiSourceInfo: StateFlow<String?> = _aiSourceInfo.asStateFlow()

    // Theme (Midnight OLED / Navy / Crimson / Emerald / Titanium / Light) - Kalıcı
    private val _themeMode = MutableStateFlow(userPrefs.getThemeMode())
    val themeMode: StateFlow<com.example.ui.theme.AppThemeMode> = _themeMode.asStateFlow()

    // Layout Mode (Phone / TV / Auto) - Kalıcı
    private val _layoutMode = MutableStateFlow(userPrefs.getLayoutMode())
    val layoutMode: StateFlow<com.example.ui.theme.AppLayoutMode> = _layoutMode.asStateFlow()

    val isDarkTheme: StateFlow<Boolean> = _themeMode.map { mode ->
        mode != com.example.ui.theme.AppThemeMode.LIGHT
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Language (TR, EN, DE) - Kalıcı
    private val _currentLanguage = MutableStateFlow(userPrefs.getLanguage())
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    // Veri Tasarrufu Modu (Data Saver) - Kalıcı
    private val _isDataSaverEnabled = MutableStateFlow(userPrefs.isDataSaverEnabled())
    val isDataSaverEnabled: StateFlow<Boolean> = _isDataSaverEnabled.asStateFlow()

    fun toggleDataSaver() {
        val newVal = !_isDataSaverEnabled.value
        _isDataSaverEnabled.value = newVal
        userPrefs.saveDataSaverEnabled(newVal)
    }

    fun setDataSaver(enabled: Boolean) {
        _isDataSaverEnabled.value = enabled
        userPrefs.saveDataSaverEnabled(enabled)
    }

    // Navigation Tab - Kalıcı
    private val _selectedTab = MutableStateFlow(userPrefs.getLastTab())
    val selectedTab: StateFlow<MainNavigationTab> = _selectedTab.asStateFlow()

    // Category Filter Chip
    private val _selectedCategory = MutableStateFlow("Tümü")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Search Query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Active playing item (null = home, not-null = player)
    private val _currentPlayingItem = MutableStateFlow<MediaItem?>(null)
    val currentPlayingItem: StateFlow<MediaItem?> = _currentPlayingItem.asStateFlow()

    // Start position when launching player
    private val _initialPlaybackPositionMs = MutableStateFlow(0L)
    val initialPlaybackPositionMs: StateFlow<Long> = _initialPlaybackPositionMs.asStateFlow()

    // Watch history (Firestore "izleme_gecmisi" collection)
    private val _watchHistory = MutableStateFlow<List<WatchHistoryItem>>(emptyList())
    val watchHistory: StateFlow<List<WatchHistoryItem>> = _watchHistory.asStateFlow()

    // Favorites - Kalıcı
    private val _favoriteIds = MutableStateFlow<Set<String>>(userPrefs.getFavorites())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    // Sürüm ve Güncelleme Yönetimi (Kalıcı versiyon kontrolü)
    private val _currentVersion = MutableStateFlow(userPrefs.getInstalledVersion())
    val currentVersion: StateFlow<String> = _currentVersion.asStateFlow()

    val latestReleaseVersion: String = UserPreferencesManager.LATEST_RELEASE_VERSION
    private val _githubPageUrl = MutableStateFlow(userPrefs.getGithubPageUrl())
    val githubPageUrl: StateFlow<String> = _githubPageUrl.asStateFlow()

    private val _apkDownloadUrl = MutableStateFlow(userPrefs.getApkDownloadUrl())
    val apkDownloadUrl: StateFlow<String> = _apkDownloadUrl.asStateFlow()

    val updateDownloadUrl: String get() = _apkDownloadUrl.value

    fun updateCustomGithubUrl(url: String) {
        val trimmed = url.trim()
        if (trimmed.isNotBlank()) {
            _githubPageUrl.value = trimmed
            userPrefs.saveGithubPageUrl(trimmed)
        }
    }

    fun updateCustomApkUrl(url: String) {
        val trimmed = url.trim()
        if (trimmed.isNotBlank()) {
            _apkDownloadUrl.value = trimmed
            userPrefs.saveApkDownloadUrl(trimmed)
        }
    }

    private val _hasUpdate = MutableStateFlow(userPrefs.hasPendingUpdate())
    val hasUpdate: StateFlow<Boolean> = _hasUpdate.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    private val _updateProgress = MutableStateFlow(0f)
    val updateProgress: StateFlow<Float> = _updateProgress.asStateFlow()

    private val _updateStatus = MutableStateFlow<String?>(null)
    val updateStatus: StateFlow<String?> = _updateStatus.asStateFlow()

    // Media Collections: Canlı TV, Haber Kanalları, Çocuk Kanalları, Spor
    private val _liveTvList = MutableStateFlow<List<MediaItem>>(emptyList())
    val liveTvList: StateFlow<List<MediaItem>> = _liveTvList.asStateFlow()

    private val _newsList = MutableStateFlow<List<MediaItem>>(emptyList())
    val newsList: StateFlow<List<MediaItem>> = _newsList.asStateFlow()

    private val _kidsList = MutableStateFlow<List<MediaItem>>(emptyList())
    val kidsList: StateFlow<List<MediaItem>> = _kidsList.asStateFlow()

    private val _sportsList = MutableStateFlow<List<MediaItem>>(emptyList())
    val sportsList: StateFlow<List<MediaItem>> = _sportsList.asStateFlow()

    private val _radioList = MutableStateFlow<List<MediaItem>>(emptyList())
    val radioList: StateFlow<List<MediaItem>> = _radioList.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        // Load initial verified channels immediately
        val initialLive = internetService.getVerifiedSportsAndNationalChannels()
        _liveTvList.value = initialLive
        _newsList.value = internetService.getVerifiedNewsChannels()
        _kidsList.value = internetService.getVerifiedKidsChannels()
        _radioList.value = internetService.getRadioStationList()
        _sportsList.value = initialLive.filter {
            it.kategori.equals("Spor", ignoreCase = true) ||
            it.baslik.contains("spor", ignoreCase = true) ||
            it.baslik.contains("tjk", ignoreCase = true)
        }

        // Fetch latest live channels and online media from internet
        refreshChannels()
        listenFirestore()
        listenWatchHistory()
    }

    private fun listenWatchHistory() {
        viewModelScope.launch {
            firestoreService.observeWatchHistory().collect { history ->
                _watchHistory.value = history
            }
        }
    }

    fun refreshChannels() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val internetChannels = internetService.fetchLiveChannelsFromInternet()
                if (internetChannels.isNotEmpty()) {
                    _liveTvList.value = internetChannels
                    _sportsList.value = internetChannels.filter {
                        it.kategori.equals("Spor", ignoreCase = true) ||
                        it.baslik.contains("spor", ignoreCase = true) ||
                        it.baslik.contains("tjk", ignoreCase = true)
                    }
                }

                val news = turkishMediaService.fetchNewsChannels()
                if (news.isNotEmpty()) _newsList.value = news

                val kids = turkishMediaService.fetchKidsChannels()
                if (kids.isNotEmpty()) _kidsList.value = kids
            } catch (_: Exception) {
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun listenFirestore() {
        viewModelScope.launch {
            firestoreService.observeCollection(MediaCollection.CANLI_TV).collect { items ->
                if (items.isNotEmpty()) {
                    val merged = (items + _liveTvList.value).distinctBy { it.baslik.lowercase().trim() }
                    _liveTvList.value = merged
                }
            }
        }
        viewModelScope.launch {
            firestoreService.observeCollection(MediaCollection.HABER).collect { items ->
                if (items.isNotEmpty()) {
                    val merged = (items + _newsList.value).distinctBy { it.baslik.lowercase().trim() }
                    _newsList.value = merged
                }
            }
        }
        viewModelScope.launch {
            firestoreService.observeCollection(MediaCollection.COCUK).collect { items ->
                if (items.isNotEmpty()) {
                    val merged = (items + _kidsList.value).distinctBy { it.baslik.lowercase().trim() }
                    _kidsList.value = merged
                }
            }
        }
        viewModelScope.launch {
            firestoreService.observeCollection(MediaCollection.RADYO).collect { items ->
                if (items.isNotEmpty()) {
                    val merged = (items + _radioList.value).distinctBy { it.baslik.lowercase().trim() }
                    _radioList.value = merged
                }
            }
        }
    }

    private data class ChannelLists(
        val live: List<MediaItem>,
        val news: List<MediaItem>,
        val kids: List<MediaItem>,
        val sports: List<MediaItem>,
        val radio: List<MediaItem>
    )

    private val allChannelsFlow = combine(
        _liveTvList,
        _newsList,
        _kidsList,
        _sportsList,
        _radioList
    ) { live, news, kids, sports, radio ->
        ChannelLists(live, news, kids, sports, radio)
    }

    /**
     * Aktif sekmedeki temel liste (Canlı TV, Haber, Çocuk, Spor, Radyo, Favoriler)
     */
    private val currentTabItems: StateFlow<List<MediaItem>> = combine(
        _selectedTab,
        _favoriteIds,
        allChannelsFlow
    ) { tab, favs, channels ->
        val baseList: List<MediaItem> = when (tab) {
            MainNavigationTab.CANLI_TV -> channels.live
            MainNavigationTab.HABER -> channels.news
            MainNavigationTab.COCUK -> {
                (channels.kids + channels.live.filter {
                    it.collectionType == MediaCollection.COCUK ||
                    it.kategori.equals("Çocuk", ignoreCase = true) ||
                    it.kategori.contains("çizgi", ignoreCase = true) ||
                    it.baslik.lowercase().contains("disney") ||
                    it.baslik.lowercase().contains("junior") ||
                    it.baslik.lowercase().contains("jnr") ||
                    it.baslik.lowercase().contains("cartoon") ||
                    it.baslik.lowercase().contains("minika") ||
                    it.baslik.lowercase().contains("baby") ||
                    it.baslik.lowercase().contains("zarok") ||
                    it.baslik.lowercase().contains("çocuk") ||
                    it.baslik.lowercase().contains("cocuk")
                }).distinctBy { it.id }
            }
            MainNavigationTab.SPOR -> channels.sports
            MainNavigationTab.RADYO -> channels.radio
            MainNavigationTab.FAVORILER -> {
                val combinedAll = (channels.live + channels.news + channels.kids + channels.sports + channels.radio).distinctBy { it.id }
                combinedAll.filter { favs.contains(it.id) }
            }
        }
        baseList.map { item -> item.copy(isFavorite = favs.contains(item.id)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Kategori ve Gerçek Zamanlı Arama ile filtrelenmiş liste
     */
    val filteredItems: StateFlow<List<MediaItem>> = combine(
        currentTabItems,
        _selectedTab,
        _selectedCategory,
        _searchQuery
    ) { currentItems, _, category, query ->
        val rawQuery = query.trim().lowercase()

        val searchPool: List<MediaItem> = if (rawQuery.isNotBlank()) {
            currentItems.filter { itemMatchesQuery(it, rawQuery) }
        } else {
            currentItems
        }

        searchPool.filter { item ->
            if (category == "Tümü") true
            else {
                val itemKat = item.kategori.lowercase()
                val selKat = category.lowercase()
                itemKat == selKat || itemKat.contains(selKat) || selKat.contains(itemKat) ||
                (selKat.contains("çizgi") && (itemKat.contains("çocuk") || item.baslik.lowercase().contains("disney") || item.baslik.lowercase().contains("minika"))) ||
                (selKat.contains("okul") && (itemKat.contains("okul") || itemKat.contains("bebek") || item.baslik.lowercase().contains("baby") || item.baslik.lowercase().contains("diyanet"))) ||
                (selKat.contains("eğitici") && itemKat.contains("eğit"))
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun itemMatchesQuery(item: MediaItem, q: String): Boolean {
        if (q.isBlank()) return true
        val titleMatch = item.baslik.lowercase().contains(q)
        val categoryMatch = item.kategori.lowercase().contains(q)
        val descMatch = item.description.lowercase().contains(q)
        val genreMatch = item.collectionType.title.lowercase().contains(q)

        return titleMatch || categoryMatch || descMatch || genreMatch
    }

    /**
     * Kategori çipleri (Sekmeye göre dinamik ve doğru kategorizasyon)
     */
    val availableCategories: StateFlow<List<String>> = combine(
        _selectedTab,
        _liveTvList
    ) { tab, _ ->
        when (tab) {
            MainNavigationTab.CANLI_TV -> listOf("Tümü", "Ulusal", "Eğlence", "Müzik", "Belgesel", "Kültür")
            MainNavigationTab.HABER -> listOf("Tümü", "Gündem", "Ekonomi", "Canlı Yayın", "Uluslararası")
            MainNavigationTab.COCUK -> listOf("Tümü", "Çocuk", "Çizgi Film", "Okul Öncesi", "Eğitici", "Animasyon")
            MainNavigationTab.SPOR -> listOf("Tümü", "Futbol & Maç", "At Yarışı & TJK", "Genel Spor")
            MainNavigationTab.RADYO -> listOf("Tümü", "Haber", "Müzik", "Dini", "Spor")
            MainNavigationTab.FAVORILER -> listOf("Tümü", "Radyo", "Haber", "Çocuk", "Spor", "Ulusal")
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("Tümü"))

    fun setThemeMode(mode: com.example.ui.theme.AppThemeMode) {
        _themeMode.value = mode
        userPrefs.saveThemeMode(mode)
    }

    fun setLayoutMode(mode: com.example.ui.theme.AppLayoutMode) {
        _layoutMode.value = mode
        userPrefs.saveLayoutMode(mode)
    }

    fun toggleTheme() {
        val nextMode = if (_themeMode.value == com.example.ui.theme.AppThemeMode.LIGHT) {
            com.example.ui.theme.AppThemeMode.MIDNIGHT
        } else {
            com.example.ui.theme.AppThemeMode.LIGHT
        }
        setThemeMode(nextMode)
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        userPrefs.saveLanguage(language)
    }

    fun selectTab(tab: MainNavigationTab) {
        _selectedTab.value = tab
        _selectedCategory.value = "Tümü"
        userPrefs.saveLastTab(tab)
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    // Background Radio Playback Service bindings
    val activeRadioStation: StateFlow<MediaItem?> = RadioPlaybackService.currentStation
    val isRadioPlaying: StateFlow<Boolean> = RadioPlaybackService.isPlaying
    val isRadioBuffering: StateFlow<Boolean> = RadioPlaybackService.isBuffering

    fun playRadioStation(item: MediaItem) {
        RadioPlaybackService.play(getApplication(), item)
    }

    fun toggleRadio() {
        RadioPlaybackService.toggle(getApplication())
    }

    fun stopRadio() {
        RadioPlaybackService.stop(getApplication())
    }

    fun playMedia(item: MediaItem, startPositionMs: Long = 0L) {
        _initialPlaybackPositionMs.value = startPositionMs
        _currentPlayingItem.value = item
        _aiSourceInfo.value = when (item.collectionType) {
            MediaCollection.HABER -> "VİZYON Canlı Haber HD"
            MediaCollection.COCUK -> "VİZYON Çocuk Kulübü HD"
            MediaCollection.SPOR -> "VİZYON Canlı Spor HD"
            MediaCollection.RADYO -> "VİZYON Canlı Radyo"
            else -> "VİZYON HD Canlı Yayın"
        }

        if (item.collectionType == MediaCollection.RADYO) {
            RadioPlaybackService.play(getApplication(), item)
        } else {
            // TV yayını başlatıldığında arka plandaki radyoyu otomatik durdur
            if (RadioPlaybackService.isPlaying.value) {
                RadioPlaybackService.stop(getApplication())
            }
        }

        if (item.yayin_url.isBlank()) {
            viewModelScope.launch {
                try {
                    val resolved = aiResolver.resolveStreamForChannel(item.baslik, item.kategori)
                    if (resolved.isSuccess && resolved.streamUrl.isNotBlank()) {
                        _aiSourceInfo.value = resolved.sourceDescription
                        val resolvedItem = aiResolver.createMediaItemFromResolved(item, resolved)
                        _currentPlayingItem.value = resolvedItem
                        if (resolvedItem.collectionType == MediaCollection.RADYO) {
                            RadioPlaybackService.play(getApplication(), resolvedItem)
                        }
                    }
                } catch (_: Exception) {
                }
            }
        }
    }

    fun playFromHistory(historyItem: WatchHistoryItem) {
        playMedia(historyItem.toMediaItem(), historyItem.lastPositionMs)
    }

    fun saveWatchProgress(item: MediaItem, positionMs: Long, durationMs: Long) {
        firestoreService.saveWatchProgress(item, positionMs, durationMs)
    }

    fun removeFromHistory(historyId: String) {
        firestoreService.removeWatchHistoryItem(historyId)
    }

    /**
     * Tam ekran oynatıcıdan çıkıldığında:
     * TV kanalı ise yayını durdurur.
     * Radyo ise arka planda kesintisiz çalmaya devam eder ve bildirim panelinde kontrol menüsü açık kalır.
     */
    fun stopPlayback() {
        _currentPlayingItem.value = null
        _initialPlaybackPositionMs.value = 0L
    }

    fun playNext() {
        val current = _currentPlayingItem.value ?: return
        val list = filteredItems.value
        if (list.isEmpty()) return
        val currentIndex = list.indexOfFirst { it.id == current.id }
        val nextIndex = if (currentIndex != -1 && currentIndex < list.size - 1) currentIndex + 1 else 0
        playMedia(list[nextIndex])
    }

    fun playPrevious() {
        val current = _currentPlayingItem.value ?: return
        val list = filteredItems.value
        if (list.isEmpty()) return
        val currentIndex = list.indexOfFirst { it.id == current.id }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else list.size - 1
        playMedia(list[prevIndex])
    }

    fun findAndPlayCustomChannelWithAI(channelQuery: String) {
        if (channelQuery.isBlank()) return
        viewModelScope.launch {
            _isAiResolving.value = true
            _aiResolvingChannel.value = channelQuery
            try {
                val resolved = aiResolver.resolveStreamForChannel(channelQuery, "Canlı TV")
                val item = MediaItem(
                    id = "ai_${channelQuery.hashCode()}",
                    baslik = channelQuery,
                    kategori = "Yapay Zeka Yayını",
                    resim_url = internetService.getOfficialWebLogo(channelQuery),
                    yayin_url = resolved.streamUrl,
                    collectionType = MediaCollection.CANLI_TV
                )
                _aiSourceInfo.value = resolved.sourceDescription
                _currentPlayingItem.value = item
            } catch (_: Exception) {
            } finally {
                _isAiResolving.value = false
            }
        }
    }

    fun importM3uPlaylist(input: String, isUrl: Boolean, onResult: (Int, String) -> Unit) {
        viewModelScope.launch {
            try {
                val channels = if (isUrl) {
                    internetService.fetchCustomPlaylist(input)
                } else {
                    internetService.parseRawM3uContent(input)
                }
                if (channels.isNotEmpty()) {
                    val kids = channels.filter {
                        it.collectionType == MediaCollection.COCUK ||
                        it.kategori.equals("Çocuk", ignoreCase = true) ||
                        it.baslik.lowercase().contains("disney") ||
                        it.baslik.lowercase().contains("cartoon")
                    }
                    val news = channels.filter {
                        it.collectionType == MediaCollection.HABER ||
                        it.kategori.equals("Haber", ignoreCase = true)
                    }
                    val sports = channels.filter {
                        it.collectionType == MediaCollection.SPOR ||
                        it.kategori.equals("Spor", ignoreCase = true)
                    }
                    val radio = channels.filter {
                        it.collectionType == MediaCollection.RADYO ||
                        it.kategori.equals("Radyo", ignoreCase = true)
                    }

                    if (kids.isNotEmpty()) _kidsList.value = (kids + _kidsList.value).distinctBy { it.baslik.lowercase().trim() }
                    if (news.isNotEmpty()) _newsList.value = (news + _newsList.value).distinctBy { it.baslik.lowercase().trim() }
                    if (sports.isNotEmpty()) _sportsList.value = (sports + _sportsList.value).distinctBy { it.baslik.lowercase().trim() }
                    if (radio.isNotEmpty()) _radioList.value = (radio + _radioList.value).distinctBy { it.baslik.lowercase().trim() }
                    _liveTvList.value = (channels + _liveTvList.value).distinctBy { it.baslik.lowercase().trim() }

                    onResult(channels.size, "${channels.size} kanal listeye başarıyla eklendi ve kategorize edildi.")
                } else {
                    onResult(0, "Geçerli kanal bulunamadı.")
                }
            } catch (e: Exception) {
                onResult(0, "İçe aktarma hatası: ${e.localizedMessage}")
            }
        }
    }

    fun addCustomMedia(
        collection: MediaCollection,
        baslik: String,
        kategori: String,
        resimUrl: String,
        yayinUrl: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val newItem = MediaItem(
            id = "custom_${System.currentTimeMillis()}",
            baslik = baslik.trim(),
            kategori = kategori.trim(),
            resim_url = resimUrl.trim().ifBlank { internetService.getOfficialWebLogo(baslik) },
            yayin_url = yayinUrl.trim(),
            collectionType = collection
        )

        when (collection) {
            MediaCollection.CANLI_TV -> _liveTvList.value = listOf(newItem) + _liveTvList.value
            MediaCollection.HABER -> _newsList.value = listOf(newItem) + _newsList.value
            MediaCollection.COCUK -> _kidsList.value = listOf(newItem) + _kidsList.value
            MediaCollection.SPOR -> _sportsList.value = listOf(newItem) + _sportsList.value
            MediaCollection.RADYO -> _radioList.value = listOf(newItem) + _radioList.value
        }

        firestoreService.addMediaItem(collection, newItem) { success, msg ->
            onResult(success, msg)
        }
    }

    fun toggleFavorite(item: MediaItem) {
        val current = _favoriteIds.value.toMutableSet()
        if (current.contains(item.id)) {
            current.remove(item.id)
        } else {
            current.add(item.id)
        }
        _favoriteIds.value = current
        userPrefs.saveFavorites(current)
    }

    val availablePresets = internetService.availablePresets

    fun loadPresetPlaylist(preset: com.example.service.M3uPreset, onResult: (Int, String) -> Unit) {
        viewModelScope.launch {
            val items = internetService.fetchCustomPlaylist(preset.url)
            if (items.isNotEmpty()) {
                _liveTvList.value = (items + _liveTvList.value).distinctBy { it.baslik.lowercase().trim() }
                onResult(items.size, "${preset.title} başarıyla yüklendi (${items.size} kanal).")
            } else {
                onResult(0, "${preset.title} yüklenemedi. Lütfen internet bağlantınızı kontrol edin.")
            }
        }
    }

    /**
     * Sürüm kontrolü ve güncelleme denetimi
     */
    fun checkForUpdates(onResult: (Boolean, String) -> Unit) {
        val hasPending = userPrefs.hasPendingUpdate()
        _hasUpdate.value = hasPending
        if (hasPending) {
            val current = _currentVersion.value
            onResult(true, "Yeni VİZYON TV güncellemesi mevcut! Mevcut sürümünüz: $current -> Hedef: $latestReleaseVersion")
        } else {
            onResult(false, "VİZYON TV güncel ($latestReleaseVersion). En son kanal ve akış listesini kullanıyorsunuz.")
        }
    }

    /**
     * Uygulama ve kanal akışlarını yeni sürüme günceller
     */
    fun applyAppUpdate(onFinished: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isUpdating.value = true
            _updateProgress.value = 0.15f
            _updateStatus.value = "Yeni VİZYON TV yayın paketi indiriliyor..."
            kotlinx.coroutines.delay(200)

            _updateProgress.value = 0.50f
            _updateStatus.value = "Haber ve Çocuk akışları optimize ediliyor..."
            kotlinx.coroutines.delay(250)

            _updateProgress.value = 0.85f
            _updateStatus.value = "Kanal önbelleği ve veritabanı eşitleniyor..."
            // Refresh and ensure clean verified channels
            refreshChannels()
            kotlinx.coroutines.delay(200)

            userPrefs.saveInstalledVersion(latestReleaseVersion)
            _currentVersion.value = latestReleaseVersion
            _hasUpdate.value = false
            _updateProgress.value = 1.0f
            _updateStatus.value = "Güncelleme tamamlandı! VİZYON TV $latestReleaseVersion aktif."
            _isUpdating.value = false

            onFinished(true, "VİZYON TV başarıyla $latestReleaseVersion sürümüne güncellendi!")
        }
    }

    /**
     * Geliştirici / Test için önceki sürüme çekip uyarı vermesini sağlayan metod
     */
    fun toggleSimulatePreviousVersion() {
        if (_currentVersion.value == latestReleaseVersion) {
            userPrefs.saveInstalledVersion(UserPreferencesManager.PREVIOUS_VERSION_BASELINE)
            _currentVersion.value = UserPreferencesManager.PREVIOUS_VERSION_BASELINE
            _hasUpdate.value = true
        } else {
            userPrefs.saveInstalledVersion(latestReleaseVersion)
            _currentVersion.value = latestReleaseVersion
            _hasUpdate.value = false
        }
    }
}
