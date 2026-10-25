package com.example.ui.player

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.StayCurrentLandscape
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ripple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.service.RadioPlaybackService
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.model.MediaCollection
import com.example.model.MediaItem
import com.example.ui.theme.LuxuryDarkGlass
import com.example.ui.theme.LuxuryGoldAccent
import com.example.ui.theme.LuxuryGoldPrimary
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@SuppressLint("UnsafeOptInUsageError")
@OptIn(UnstableApi::class)
@Composable
fun StreamPlayerScreen(
    item: MediaItem,
    channelList: List<MediaItem>,
    initialPositionMs: Long = 0L,
    aiSource: String? = null,
    onSaveProgress: ((positionMs: Long, durationMs: Long) -> Unit)? = null,
    onSelectChannel: (MediaItem) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showControls by remember { mutableStateOf(true) }
    var showChannelDrawer by remember { mutableStateOf(false) }
    var drawerSearchQuery by remember { mutableStateOf("") }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var resizeModeIndex by remember { mutableIntStateOf(0) }

    // Çoklu Sunucu Yönetimi: Kanala ait tüm sunucular (birincil + yedekler)
    val serverList = remember(item) {
        val list = item.getAllStreamUrls().filter { it.isNotBlank() }
        if (list.isEmpty()) listOf(item.yayin_url).filter { it.isNotBlank() } else list
    }
    var currentServerIndex by remember(item.id) { mutableIntStateOf(0) }
    var activeStreamUrl by remember(item.id, item.yayin_url) {
        mutableStateOf(serverList.getOrNull(0) ?: item.yayin_url)
    }
    var serverNotice by remember { mutableStateOf<String?>(null) }

    val userPrefs = remember { com.example.service.UserPreferencesManager(context) }
    var isDataSaver by remember { mutableStateOf(userPrefs.isDataSaverEnabled()) }

    // Otomatik sunucu bildirimini 3.5 saniye sonra kapat
    LaunchedEffect(serverNotice) {
        if (serverNotice != null) {
            delay(3500)
            serverNotice = null
        }
    }

    val resizeModes = listOf(
        AspectRatioFrameLayout.RESIZE_MODE_FIT to "16:9 (FIT)",
        AspectRatioFrameLayout.RESIZE_MODE_ZOOM to "YAKINLAŞTIR (ZOOM)",
        AspectRatioFrameLayout.RESIZE_MODE_FILL to "TAM EKRAN (FILL)"
    )

    // Create single ExoPlayer instance for screen lifecycle
    val httpDataSourceFactory = remember {
        DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(5000)
            .setReadTimeoutMs(6000)
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36")
            .setDefaultRequestProperties(
                mapOf(
                    "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36",
                    "Referer" to "https://www.google.com/",
                    "Accept" to "*/*"
                )
            )
    }

    val exoPlayer = remember {
        val renderersFactory = DefaultRenderersFactory(context)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
            .setEnableDecoderFallback(true)

        // Hızlı açılış ve akıcı canlı yayın için optimize edilmiş LoadControl:
        // 250ms ilk arabellek ile anında açılış (özellikle ATV, A Haber gibi 10sn segmentli kanallarda bekleme yapmaz)
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                1500, // minBufferMs
                20000, // maxBufferMs
                250, // bufferForPlaybackMs (Anında başlatma - ultra hızlı açılış)
                750  // bufferForPlaybackAfterRebufferMs (Hızlı toparlanma)
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .setBackBuffer(5000, false)
            .build()

        val trackSelector = DefaultTrackSelector(context).apply {
            setParameters(
                buildUponParameters()
                    .setViewportSizeToPhysicalDisplaySize(context, true)
                    .setForceHighestSupportedBitrate(false)
            )
        }

        ExoPlayer.Builder(context, renderersFactory)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .build()
            .apply {
                playWhenReady = true
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            }
    }

    // Veri Tasarrufu Modu: Açıkken SD/480p kaliteye ve 750 kbps maksimum bit hızına sınırlandırır (Mobil interneti %85 korur)
    LaunchedEffect(exoPlayer, isDataSaver) {
        try {
            val builder = exoPlayer.trackSelectionParameters.buildUpon()
            if (isDataSaver) {
                builder.setMaxVideoSizeSd()
                    .setMaxVideoBitrate(750_000)
            } else {
                builder.clearVideoSizeConstraints()
                    .setMaxVideoBitrate(Int.MAX_VALUE)
            }
            exoPlayer.trackSelectionParameters = builder.build()
        } catch (_: Throwable) {}
    }

    val isYouTube = activeStreamUrl.contains("youtube.com") || activeStreamUrl.contains("youtu.be")
    val youtubeVideoId = remember(activeStreamUrl) {
        if (isYouTube) extractYouTubeVideoId(activeStreamUrl) else null
    }
    val isRadio = remember(item) {
        item.collectionType == MediaCollection.RADYO ||
        item.kategori.contains("radyo", ignoreCase = true) ||
        item.baslik.contains("radyo", ignoreCase = true) ||
        item.baslik.contains("fm", ignoreCase = true)
    }

    val isRadioServicePlaying by RadioPlaybackService.isPlaying.collectAsState()
    val isRadioServiceBuffering by RadioPlaybackService.isBuffering.collectAsState()
    val effectiveIsPlaying = if (isRadio) isRadioServicePlaying else isPlaying
    val effectiveIsBuffering = if (isRadio) isRadioServiceBuffering else isBuffering

    // Kanaldan çıkıldığında yayını durduran fonksiyon
    val exitPlayer: () -> Unit = {
        try {
            // Restore orientation to sensor when exiting player
            val act = context as? Activity
            act?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            if (!isRadio) {
                exoPlayer.stop()
                exoPlayer.clearMediaItems()
            }
        } catch (_: Throwable) {}
        onBack()
    }

    // Uygulama arka plana geçtiğinde veya ekran kapandığında:
    // TV yayını ise durdurulur; Radyo ise arka planda kesintisiz çalmaya devam eder ve bildirim kontrolleri devrede kalır
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, isRadio) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (!isRadio && (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE || event == androidx.lifecycle.Lifecycle.Event.ON_STOP)) {
                try {
                    exoPlayer.pause()
                    exoPlayer.stop()
                } catch (_: Throwable) {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            try {
                (context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            } catch (_: Throwable) {}
        }
    }

    // Back handler: closes drawer if open, otherwise exits player and stops stream
    BackHandler {
        if (showChannelDrawer) {
            showChannelDrawer = false
        } else {
            exitPlayer()
        }
    }

    // Keep activeStreamUrl in sync with item changes (reset to primary server on channel switch)
    LaunchedEffect(item.id) {
        currentServerIndex = 0
        val firstUrl = serverList.getOrNull(0) ?: item.yayin_url
        if (firstUrl.isNotBlank() && activeStreamUrl != firstUrl) {
            activeStreamUrl = firstUrl
        }
    }

    // Load active stream on existing ExoPlayer
    LaunchedEffect(activeStreamUrl, isYouTube, isRadio) {
        if (activeStreamUrl.isBlank()) return@LaunchedEffect
        if (isRadio) {
            exoPlayer.stop()
            RadioPlaybackService.play(context, item.copy(yayin_url = activeStreamUrl))
            isBuffering = false
            errorMessage = null
            return@LaunchedEffect
        }
        if (isYouTube) {
            exoPlayer.stop()
            isBuffering = false
            errorMessage = null
            return@LaunchedEffect
        }
        isBuffering = true
        errorMessage = null
        try {
            val uri = Uri.parse(activeStreamUrl)
            val isHls = activeStreamUrl.contains(".m3u8", ignoreCase = true) || item.isHlsStream
            val exoItem = ExoMediaItem.Builder()
                .setUri(uri)
                .build()

            val mediaSource: MediaSource = if (isHls) {
                HlsMediaSource.Factory(httpDataSourceFactory)
                    .setAllowChunklessPreparation(true)
                    .createMediaSource(exoItem)
            } else {
                ProgressiveMediaSource.Factory(httpDataSourceFactory)
                    .createMediaSource(exoItem)
            }

            exoPlayer.stop()
            exoPlayer.setMediaSource(mediaSource)
            if (initialPositionMs > 0L) {
                exoPlayer.seekTo(initialPositionMs)
            }
            exoPlayer.prepare()
            exoPlayer.play()
        } catch (e: Exception) {
            isBuffering = false
            errorMessage = "Yayın başlatılamadı: ${e.localizedMessage}"
        }
    }

    // Attach listener and handle cleanup
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onRenderedFirstFrame() {
                isBuffering = false
                errorMessage = null
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    isBuffering = false
                    errorMessage = null
                    duration = if (exoPlayer.duration != C.TIME_UNSET) exoPlayer.duration else 0L
                } else if (playbackState == Player.STATE_BUFFERING) {
                    // Yükleme durumu
                } else if (playbackState == Player.STATE_ENDED) {
                    isBuffering = false
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                if (playing) {
                    isBuffering = false
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                // 1. Akıllı otomatik yedek sunucuya atlama mekanizması:
                if (currentServerIndex + 1 < serverList.size) {
                    val nextIdx = currentServerIndex + 1
                    val nextUrl = serverList[nextIdx]
                    serverNotice = "Sunucu ${currentServerIndex + 1} yanıt vermedi. Sunucu ${nextIdx + 1}'e aktarılıyor... (${nextIdx + 1}/${serverList.size})"
                    currentServerIndex = nextIdx
                    activeStreamUrl = nextUrl
                    isBuffering = true
                    errorMessage = null
                    return
                }

                // 2. Özel bilinen kanallar için akıllı acil durum yedekleri:
                if (activeStreamUrl.contains("/atv/", ignoreCase = true) && !activeStreamUrl.contains("atv_360p")) {
                    serverNotice = "Alternatif hızlı ATV sunucusuna aktarılıyor..."
                    activeStreamUrl = "http://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/atv/atv_720p.m3u8"
                    isBuffering = true
                    errorMessage = null
                    return
                }
                if (activeStreamUrl.contains("trtcocuk", ignoreCase = true) && !activeStreamUrl.contains("master.m3u8")) {
                    serverNotice = "Alternatif TRT Çocuk sunucusuna aktarılıyor..."
                    activeStreamUrl = "https://tv-trtcocuk.medya.trt.com.tr/master.m3u8"
                    isBuffering = true
                    errorMessage = null
                    return
                }

                isBuffering = false
                val reason = error.localizedMessage ?: "Bağlantı zaman aşımı"
                val serverInfo = if (serverList.size > 1) " (${serverList.size} sunucu denendi)" else ""
                errorMessage = "${item.baslik} yayını şu anda çevrimdışı$serverInfo ($reason)"
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            try {
                exoPlayer.removeListener(listener)
                val dur = if (exoPlayer.duration != C.TIME_UNSET) exoPlayer.duration else 0L
                if (exoPlayer.currentPosition > 2000L) {
                    onSaveProgress?.invoke(exoPlayer.currentPosition, dur)
                }
                exoPlayer.stop()
                exoPlayer.clearMediaItems()
                exoPlayer.release()
            } catch (_: Throwable) {
            }
        }
    }

    // Update playback progress & periodically save to Firestore
    LaunchedEffect(exoPlayer, isPlaying) {
        while (isPlaying) {
            try {
                if (exoPlayer.playbackState != Player.STATE_IDLE) {
                    currentPosition = exoPlayer.currentPosition
                    if (currentPosition > 2000L) {
                        val dur = if (exoPlayer.duration != C.TIME_UNSET) exoPlayer.duration else 0L
                        onSaveProgress?.invoke(currentPosition, dur)
                    }
                }
            } catch (_: Throwable) {
                break
            }
            delay(5000)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightDark)
            .focusable()
            .testTag("stream_player_box")
            .onKeyEvent { event ->
                // Sadece tuşa basılma (KeyDown) anında işlem yap; KeyUp ile 2 defa tetiklenip 2 kanal atlamasını önle
                if (event.type != KeyEventType.KeyDown) {
                    return@onKeyEvent false
                }

                // 1) Kanal / Bölüm Listesi Açıkken Kumanda Tuşları
                if (showChannelDrawer) {
                    val displayedChannels = if (drawerSearchQuery.isBlank()) channelList
                    else channelList.filter {
                        it.baslik.contains(drawerSearchQuery, ignoreCase = true) ||
                        it.kategori.contains(drawerSearchQuery, ignoreCase = true)
                    }

                    when (event.key) {
                        Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
                            // OK tuşuna basıldığında: listedeki aktif kanalı seç ve çekmeceyi kapat
                            val currentIdx = displayedChannels.indexOfFirst { it.id == item.id }
                            if (currentIdx >= 0) {
                                onSelectChannel(displayedChannels[currentIdx])
                            } else if (displayedChannels.isNotEmpty()) {
                                onSelectChannel(displayedChannels[0])
                            }
                            showChannelDrawer = false
                            true
                        }
                        Key.DirectionUp -> {
                            // Listede yukarı çık
                            val currentIdx = displayedChannels.indexOfFirst { it.id == item.id }
                            if (displayedChannels.isNotEmpty()) {
                                val prevIdx = if (currentIdx > 0) currentIdx - 1 else displayedChannels.lastIndex
                                onSelectChannel(displayedChannels[prevIdx])
                            }
                            true
                        }
                        Key.DirectionDown -> {
                            // Listede aşağı in
                            val currentIdx = displayedChannels.indexOfFirst { it.id == item.id }
                            if (displayedChannels.isNotEmpty()) {
                                val nextIdx = if (currentIdx >= 0 && currentIdx < displayedChannels.lastIndex) currentIdx + 1 else 0
                                onSelectChannel(displayedChannels[nextIdx])
                            }
                            true
                        }
                        Key.Back, Key.Escape -> {
                            showChannelDrawer = false
                            true
                        }
                        Key.DirectionLeft, Key.DirectionRight -> {
                            // Sol veya sağ basıldığında listeyi kapatıp oynatıcıya dön
                            showChannelDrawer = false
                            true
                        }
                        else -> false
                    }
                } else {
                    // 2) Normal Oynatma Modundayken Kumanda Tuşları
                    when (event.key) {
                        // [OK] veya [Enter]: Kanallar butonuna doğrudan erişim sağlar ve Kanal Listesini açar (eğer menü kapalıysa menüyü ve kanalları senkronize eder)
                        Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
                            showChannelDrawer = true
                            showControls = false
                            true
                        }
                        // [Menü], [Bilgi], [Rehber], [Kanal Listesi]: Doğrudan kanal listesini açar
                        Key.Menu, Key.Guide, Key.Info, Key.TvContentsMenu -> {
                            showChannelDrawer = !showChannelDrawer
                            if (showChannelDrawer) showControls = false
                            true
                        }
                        Key.DirectionLeft -> {
                            if (exoPlayer.isCurrentMediaItemSeekable) {
                                exoPlayer.seekTo((exoPlayer.currentPosition - 10000).coerceAtLeast(0))
                            }
                            showControls = true
                            true
                        }
                        Key.DirectionRight -> {
                            if (exoPlayer.isCurrentMediaItemSeekable) {
                                exoPlayer.seekTo((exoPlayer.currentPosition + 10000).coerceAtMost(exoPlayer.duration))
                            }
                            showControls = true
                            true
                        }
                        Key.DirectionUp, Key.ChannelUp, Key.PageUp -> {
                            onPrevious()
                            true
                        }
                        Key.DirectionDown, Key.ChannelDown, Key.PageDown -> {
                            onNext()
                            true
                        }
                        Key.MediaPlayPause -> {
                            if (isRadio) {
                                RadioPlaybackService.toggle(context)
                            } else {
                                if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                            }
                            true
                        }
                        Key.MediaPlay -> {
                            if (isRadio) {
                                RadioPlaybackService.resume(context)
                            } else {
                                exoPlayer.play()
                            }
                            true
                        }
                        Key.MediaPause -> {
                            if (isRadio) {
                                RadioPlaybackService.pause(context)
                            } else {
                                exoPlayer.pause()
                            }
                            true
                        }
                        Key.MediaFastForward -> {
                            if (exoPlayer.isCurrentMediaItemSeekable) {
                                exoPlayer.seekTo((exoPlayer.currentPosition + 10000).coerceAtMost(exoPlayer.duration))
                            }
                            true
                        }
                        Key.MediaRewind -> {
                            if (exoPlayer.isCurrentMediaItemSeekable) {
                                exoPlayer.seekTo((exoPlayer.currentPosition - 10000).coerceAtLeast(0))
                            }
                            true
                        }
                        Key.Back, Key.Escape -> {
                            if (showControls) {
                                showControls = false
                                true
                            } else {
                                exitPlayer()
                                true
                            }
                        }
                        else -> false
                    }
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (showChannelDrawer) {
                    showChannelDrawer = false
                } else {
                    showControls = !showControls
                }
            }
    ) {
        // Video View: YouTube Embed or ExoPlayer
        if (isYouTube && !youtubeVideoId.isNullOrBlank()) {
            YouTubePlayerView(
                videoId = youtubeVideoId,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = exoPlayer
                        useController = false
                        setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                        keepScreenOn = true
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        resizeMode = resizeModes[resizeModeIndex].first
                    }
                },
                update = { playerView ->
                    if (playerView.player != exoPlayer) {
                        playerView.player = exoPlayer
                    }
                    playerView.resizeMode = resizeModes[resizeModeIndex].first
                },
                onRelease = { playerView ->
                    playerView.player = null
                }
            )
        }

        // Dedicated Radio Stream Visualizer Overlay
        if (isRadio) {
            RadioVisualizerView(
                item = item,
                isPlaying = effectiveIsPlaying,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Buffering indicator (non-blocking translucent)
        if (effectiveIsBuffering && errorMessage == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = NeonCyan,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Canlı Yayın Yükleniyor...",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Live Server Switch Notice Toast Banner
        AnimatedVisibility(
            visible = serverNotice != null,
            enter = fadeIn() + androidx.compose.animation.slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + androidx.compose.animation.slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 68.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.90f),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.7f)),
                shadowElevation = 10.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = NeonCyan,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = serverNotice ?: "",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Error Banner with Fallback / Retry
        if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.88f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "⚠️ Akış Bağlantı Hatası",
                        style = MaterialTheme.typography.titleLarge,
                        color = NeonRed,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Diğer Sunucuya Geç Butonu (Eğer alternatif sunucu varsa)
                        if (serverList.size > 1) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = LuxuryGoldPrimary,
                                modifier = Modifier.clickable {
                                    val nextIdx = (currentServerIndex + 1) % serverList.size
                                    currentServerIndex = nextIdx
                                    activeStreamUrl = serverList[nextIdx]
                                    serverNotice = "Sunucu ${nextIdx + 1}'e bağlanılıyor... (${nextIdx + 1}/${serverList.size})"
                                    errorMessage = null
                                    isBuffering = true
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Diğer Sunucuya Geç",
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Diğer Sunucu (${(currentServerIndex + 1) % serverList.size + 1}/${serverList.size})",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NeonCyan,
                            modifier = Modifier.clickable {
                                errorMessage = null
                                isBuffering = true
                                exoPlayer.prepare()
                                exoPlayer.play()
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Tekrar Dene",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Yeniden Bağlan", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.clickable { onNext() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Sonraki Kanal",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sonraki Kanal", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // TV Channel Zapping Drawer (Left Sidebar)
        AnimatedVisibility(
            visible = showChannelDrawer,
            enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = 360.dp)
                    .fillMaxHeight(),
                color = Color.Black.copy(alpha = 0.88f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    if (item.isSeries && item.seasons.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = NeonCyan.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.padding(4.dp).size(16.dp)
                                    )
                                }
                                Text(
                                    text = "Dizi Bölümleri",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(
                                onClick = { showChannelDrawer = false },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Kapat",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item.seasons.forEach { season ->
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color.White.copy(alpha = 0.05f),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = season.title,
                                            color = LuxuryGoldPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                items(season.episodes, key = { "drawer_${it.seasonNumber}_${it.episodeNumber}" }) { ep ->
                                    val isCurrent = ep.seasonNumber == item.currentSeasonNumber && ep.episodeNumber == item.currentEpisodeNumber
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isCurrent) NeonCyan.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.04f),
                                        border = BorderStroke(
                                            width = if (isCurrent) 1.5.dp else 1.dp,
                                            color = if (isCurrent) NeonCyan else Color.White.copy(alpha = 0.08f)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onSelectChannel(
                                                    item.copy(
                                                        currentSeasonNumber = ep.seasonNumber,
                                                        currentEpisodeNumber = ep.episodeNumber,
                                                        yayin_url = ep.streamUrl.ifBlank { item.yayin_url }
                                                    )
                                                )
                                                showChannelDrawer = false
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = ep.displayTitle,
                                                    color = if (isCurrent) NeonCyan else TextPrimary,
                                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = 13.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${ep.duration} • ${ep.description}",
                                                    color = TextSecondary,
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            if (isCurrent) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = NeonCyan,
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Default.PlayArrow,
                                                            contentDescription = null,
                                                            tint = Color.Black,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = NeonCyan.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LiveTv,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.padding(5.dp).size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Kanal Rehberi",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${channelList.size} Canlı Yayın",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            IconButton(
                                onClick = { showChannelDrawer = false },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color.White.copy(alpha = 0.08f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Kapat",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Fast In-Drawer Channel Search
                        OutlinedTextField(
                            value = drawerSearchQuery,
                            onValueChange = { drawerSearchQuery = it },
                            placeholder = {
                                Text(
                                    "Kanal ara...",
                                    fontSize = 12.sp,
                                    color = TextSecondary.copy(alpha = 0.7f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = if (drawerSearchQuery.isNotBlank()) NeonCyan else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (drawerSearchQuery.isNotBlank()) {
                                    IconButton(
                                        onClick = { drawerSearchQuery = "" },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Temizle",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.06f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
                                focusedBorderColor = NeonCyan.copy(alpha = 0.6f),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val displayedChannels = remember(channelList, drawerSearchQuery) {
                            if (drawerSearchQuery.isBlank()) channelList
                            else channelList.filter {
                                it.baslik.contains(drawerSearchQuery, ignoreCase = true) ||
                                it.kategori.contains(drawerSearchQuery, ignoreCase = true)
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(displayedChannels, key = { it.id }) { ch ->
                                val isCurrent = ch.id == item.id
                                val chLogo = remember(ch.resim_url, ch.baslik) {
                                    if (ch.resim_url.isNotBlank() && !ch.resim_url.contains("unsplash")) ch.resim_url
                                    else com.example.service.InternetChannelService().getOfficialWebLogo(ch.baslik)
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isCurrent) NeonCyan.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f),
                                    border = BorderStroke(
                                        width = if (isCurrent) 1.5.dp else 1.dp,
                                        color = if (isCurrent) NeonCyan else Color.White.copy(alpha = 0.08f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSelectChannel(ch)
                                            showChannelDrawer = false
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF070B14),
                                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                                            modifier = Modifier.size(44.dp, 32.dp)
                                        ) {
                                            if (chLogo.isNotBlank()) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data(chLogo)
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = ch.baslik,
                                                    contentScale = ContentScale.Fit,
                                                    modifier = Modifier.fillMaxSize().padding(3.dp)
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.LiveTv,
                                                    contentDescription = null,
                                                    tint = NeonCyan.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = ch.baslik,
                                                color = if (isCurrent) NeonCyan else TextPrimary,
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = RoundedCornerShape(3.dp),
                                                    color = when (ch.collectionType) {
                                                        MediaCollection.CANLI_TV -> NeonRed.copy(alpha = 0.25f)
                                                        MediaCollection.HABER -> NeonAmber.copy(alpha = 0.25f)
                                                        MediaCollection.COCUK -> NeonCyan.copy(alpha = 0.25f)
                                                        MediaCollection.SPOR -> NeonAmber.copy(alpha = 0.25f)
                                                        MediaCollection.RADYO -> NeonAmber.copy(alpha = 0.25f)
                                                    },
                                                    modifier = Modifier.padding(end = 4.dp)
                                                ) {
                                                    Text(
                                                        text = when (ch.collectionType) {
                                                            MediaCollection.CANLI_TV -> "CANLI"
                                                            MediaCollection.HABER -> "HABER"
                                                            MediaCollection.COCUK -> "ÇOCUK"
                                                            MediaCollection.SPOR -> "SPOR"
                                                            MediaCollection.RADYO -> "RADYO"
                                                        },
                                                        color = when (ch.collectionType) {
                                                            MediaCollection.CANLI_TV -> NeonRed
                                                            MediaCollection.HABER -> NeonAmber
                                                            MediaCollection.COCUK -> NeonCyan
                                                            MediaCollection.SPOR -> NeonAmber
                                                            MediaCollection.RADYO -> NeonAmber
                                                        },
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                                Text(
                                                    text = ch.kategori,
                                                    color = TextSecondary,
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        if (isCurrent) {
                                            Surface(
                                                shape = CircleShape,
                                                color = NeonCyan,
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.PlayArrow,
                                                        contentDescription = null,
                                                        tint = Color.Black,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Controls Overlay
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.82f),
                                Color.Black.copy(alpha = 0.15f),
                                Color.Black.copy(alpha = 0.88f)
                            )
                        )
                    )
            ) {
                // Top Header Bar with Safe insets
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .align(Alignment.TopStart),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.65f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                            modifier = Modifier.size(42.dp)
                        ) {
                            IconButton(onClick = exitPlayer) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Geri",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        val currentChannelLogo = remember(item.resim_url, item.baslik) {
                            if (item.resim_url.isNotBlank() && !item.resim_url.contains("unsplash")) item.resim_url
                            else com.example.service.InternetChannelService().getOfficialWebLogo(item.baslik)
                        }

                        if (currentChannelLogo.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF070B14),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                                shadowElevation = 4.dp,
                                modifier = Modifier.size(48.dp, 36.dp)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(currentChannelLogo)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = item.baslik,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            val displayHeaderTitle = if (item.isSeries && item.currentSeasonNumber > 0 && item.currentEpisodeNumber > 0) {
                                "${item.baslik} - S${item.currentSeasonNumber}B${item.currentEpisodeNumber}"
                            } else {
                                item.baslik
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = displayHeaderTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )

                                Surface(
                                    shape = RoundedCornerShape(5.dp),
                                    color = when (item.collectionType) {
                                        MediaCollection.CANLI_TV -> NeonRed
                                        MediaCollection.HABER -> NeonAmber
                                        MediaCollection.COCUK -> NeonCyan
                                        MediaCollection.SPOR -> NeonAmber
                                        MediaCollection.RADYO -> NeonAmber
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(
                                                if (item.collectionType == MediaCollection.COCUK) Color.Black else Color.White,
                                                CircleShape
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = when (item.collectionType) {
                                                MediaCollection.CANLI_TV -> "CANLI"
                                                MediaCollection.HABER -> "HABER"
                                                MediaCollection.COCUK -> "ÇOCUK"
                                                MediaCollection.SPOR -> "SPOR"
                                                MediaCollection.RADYO -> "RADYO"
                                            },
                                            color = if (item.collectionType == MediaCollection.COCUK) Color.Black else Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }

                            val cleanPlayerCategory = remember(item.kategori, item.collectionType) {
                                when (item.collectionType) {
                                    MediaCollection.HABER -> item.kategori.replace("Film", "Haber", ignoreCase = true)
                                    MediaCollection.COCUK -> item.kategori.replace("Çizgi Film", "Çizgi Dizi", ignoreCase = true).replace("Film", "Çocuk", ignoreCase = true)
                                    else -> item.kategori
                                }
                            }

                            Text(
                                text = "$cleanPlayerCategory • ${if (item.isHlsStream) "HLS Canlı Yayın" else "Canlı HD Yayın"}",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Ekran Büyütme / Genişletme (En-Boy Oranı: 16:9, ZOOM, TAM EKRAN)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Black.copy(alpha = 0.65f),
                            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                            modifier = Modifier.clickable {
                                resizeModeIndex = (resizeModeIndex + 1) % resizeModes.size
                                serverNotice = "Ekran Modu: ${resizeModes[resizeModeIndex].second}"
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AspectRatio,
                                    contentDescription = resizeModes[resizeModeIndex].second,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = when (resizeModeIndex) {
                                        0 -> "16:9"
                                        1 -> "ZOOM"
                                        else -> "TAM"
                                    },
                                    color = NeonCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Dikey / Yatay Ekran Döndürme Butonu
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Black.copy(alpha = 0.65f),
                            border = BorderStroke(1.dp, if (isLandscape) LuxuryGoldPrimary.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.22f)),
                            modifier = Modifier.clickable {
                                val act = context as? Activity
                                act?.requestedOrientation = if (isLandscape) {
                                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                } else {
                                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                }
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = if (isLandscape) Icons.Default.StayCurrentPortrait else Icons.Default.StayCurrentLandscape,
                                    contentDescription = if (isLandscape) "Dikey Ekrana Geç" else "Yatay Ekrana Geç",
                                    tint = if (isLandscape) LuxuryGoldPrimary else Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = if (isLandscape) "Dikey" else "Yatay",
                                    color = if (isLandscape) LuxuryGoldPrimary else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // BOTTOM CONTROLS BAR (Floating Glass Pill Card - Adaptive for Portrait & Landscape)
                Card(
                    modifier = Modifier
                        .then(
                            if (isLandscape) Modifier.widthIn(max = 680.dp) else Modifier.fillMaxWidth()
                        )
                        .navigationBarsPadding()
                        .padding(horizontal = if (isLandscape) 24.dp else 12.dp, vertical = if (isLandscape) 8.dp else 10.dp)
                        .align(Alignment.BottomCenter)
                        .testTag("player_bottom_control_bar"),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.88f)
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = if (isLandscape) 18.dp else 12.dp, vertical = if (isLandscape) 8.dp else 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Film/Dizi veya Geriye Sarılabilir Yayınlar İçin İlerleme Çubuğu
                        if (exoPlayer.isCurrentMediaItemSeekable && duration > 0L) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatTime(currentPosition),
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Slider(
                                    value = currentPosition.toFloat(),
                                    onValueChange = { exoPlayer.seekTo(it.toLong()) },
                                    valueRange = 0f..duration.toFloat(),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp),
                                    colors = SliderDefaults.colors(
                                        thumbColor = NeonCyan,
                                        activeTrackColor = NeonCyan,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                    )
                                )
                                Text(
                                    text = formatTime(duration),
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                        }

                        // Dikey Modda Üst Buton Sırası: Hızlı Menüler (Kanallar + Aspect + Yenile)
                        if (!isLandscape) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Sol: Kanal Listesi Çekmecesi Açma Butonu (Açıldığında alt menüyü saklayarak listeyi tam gösterir)
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (showChannelDrawer) NeonCyan.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.08f),
                                    border = BorderStroke(1.dp, if (showChannelDrawer) NeonCyan else Color.White.copy(alpha = 0.16f)),
                                    modifier = Modifier.clickable {
                                        showChannelDrawer = !showChannelDrawer
                                        if (showChannelDrawer) {
                                            showControls = false
                                        }
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                                            contentDescription = if (item.isSeries) "Bölüm Listesi" else "Kanal Listesi",
                                            tint = if (showChannelDrawer) NeonCyan else Color.White,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (item.isSeries) "Bölümler" else "Kanallar",
                                            color = if (showChannelDrawer) NeonCyan else Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Sunucu Değiştirme Butonu (Çoklu Sunucu Desteği)
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (serverList.size > 1) NeonCyan.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.08f),
                                        border = BorderStroke(1.dp, if (serverList.size > 1) NeonCyan.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.16f)),
                                        modifier = Modifier.clickable(enabled = serverList.size > 1) {
                                            val nextIdx = (currentServerIndex + 1) % serverList.size
                                            currentServerIndex = nextIdx
                                            activeStreamUrl = serverList[nextIdx]
                                            serverNotice = "Sunucu ${nextIdx + 1}'e geçildi (${nextIdx + 1}/${serverList.size})"
                                            isBuffering = true
                                            errorMessage = null
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Tune,
                                                contentDescription = "Sunucu Değiştir",
                                                tint = if (serverList.size > 1) NeonCyan else TextSecondary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (serverList.size > 1) "Sunucu ${currentServerIndex + 1}/${serverList.size}" else "Sunucu 1",
                                                color = if (serverList.size > 1) NeonCyan else TextSecondary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // Veri Tasarrufu Modu Butonu (SD / HD Hızlı Geçiş)
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isDataSaver) NeonCyan.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.08f),
                                        border = BorderStroke(1.dp, if (isDataSaver) NeonCyan else Color.White.copy(alpha = 0.16f)),
                                        modifier = Modifier.clickable {
                                            isDataSaver = !isDataSaver
                                            userPrefs.saveDataSaverEnabled(isDataSaver)
                                            serverNotice = if (isDataSaver) "Veri Tasarrufu AÇIK (SD Kalite • %85 Kota Tasarrufu)" else "Veri Tasarrufu KAPALI (Orijinal Yüksek Kalite)"
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.HighQuality,
                                                contentDescription = "Veri Tasarrufu",
                                                tint = if (isDataSaver) NeonCyan else TextSecondary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isDataSaver) "SD (Tasarruf)" else "HD (Kalite)",
                                                color = if (isDataSaver) NeonCyan else TextSecondary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Ana Oynatma Kontrol Sırası
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isLandscape) Arrangement.SpaceBetween else Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Yatay Modda Sol Taraf: Kanal Listesi Çekmecesi Açma Butonu (Açıldığında alt menüyü saklar)
                            if (isLandscape) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (showChannelDrawer) NeonCyan.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.08f),
                                    border = BorderStroke(1.dp, if (showChannelDrawer) NeonCyan else Color.White.copy(alpha = 0.16f)),
                                    modifier = Modifier.clickable {
                                        showChannelDrawer = !showChannelDrawer
                                        if (showChannelDrawer) {
                                            showControls = false
                                        }
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                                            contentDescription = if (item.isSeries) "Bölüm Listesi" else "Kanal Listesi",
                                            tint = if (showChannelDrawer) NeonCyan else Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (item.isSeries) "Bölümler" else "Kanallar",
                                            color = if (showChannelDrawer) NeonCyan else Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Oynatma & Kanal Değiştirme Kontrolleri (Merkez)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(if (isLandscape) 14.dp else 12.dp)
                            ) {
                                // Önceki Kanal Butonu
                                IconButton(
                                    onClick = onPrevious,
                                    modifier = Modifier
                                        .size(if (isLandscape) 46.dp else 42.dp)
                                        .background(Color.White.copy(alpha = 0.09f), CircleShape)
                                        .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SkipPrevious,
                                        contentDescription = "Önceki Kanal",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                // 10sn Geri Sar (Filmler için)
                                if (exoPlayer.isCurrentMediaItemSeekable) {
                                    IconButton(
                                        onClick = {
                                            exoPlayer.seekTo((exoPlayer.currentPosition - 10000).coerceAtLeast(0))
                                        },
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(Color.White.copy(alpha = 0.09f), CircleShape)
                                            .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FastRewind,
                                            contentDescription = "10sn Geri",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                // Ana Oynat / Duraklat Butonu (Parlak Camgöbeği & Şık Gölge)
                                Surface(
                                    shape = CircleShape,
                                    color = NeonCyan,
                                    shadowElevation = 10.dp,
                                    modifier = Modifier
                                        .size(if (isLandscape) 54.dp else 50.dp)
                                        .clickable {
                                            if (isRadio) {
                                                RadioPlaybackService.toggle(context)
                                            } else {
                                                if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                                            }
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (effectiveIsPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (effectiveIsPlaying) "Duraklat" else "Oynat",
                                            tint = Color.Black,
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }

                                // 10sn İleri Sar (Filmler için)
                                if (exoPlayer.isCurrentMediaItemSeekable) {
                                    IconButton(
                                        onClick = {
                                            exoPlayer.seekTo((exoPlayer.currentPosition + 10000).coerceAtMost(exoPlayer.duration))
                                        },
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(Color.White.copy(alpha = 0.09f), CircleShape)
                                            .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FastForward,
                                            contentDescription = "10sn İleri",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                // Sonraki Kanal Butonu
                                IconButton(
                                    onClick = onNext,
                                    modifier = Modifier
                                        .size(if (isLandscape) 46.dp else 42.dp)
                                        .background(Color.White.copy(alpha = 0.09f), CircleShape)
                                        .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SkipNext,
                                        contentDescription = "Sonraki Kanal",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // Yatay Modda Sağ Taraf: Sunucu Seçimi, En-Boy Oranı & Yenile
                            if (isLandscape) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Sunucu Seçme (Çoklu Sunucu Desteği)
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (serverList.size > 1) NeonCyan.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.08f),
                                        border = BorderStroke(1.dp, if (serverList.size > 1) NeonCyan.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.16f)),
                                        modifier = Modifier.clickable(enabled = serverList.size > 1) {
                                            val nextIdx = (currentServerIndex + 1) % serverList.size
                                            currentServerIndex = nextIdx
                                            activeStreamUrl = serverList[nextIdx]
                                            serverNotice = "Sunucu ${nextIdx + 1}'e geçildi (${nextIdx + 1}/${serverList.size})"
                                            isBuffering = true
                                            errorMessage = null
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Tune,
                                                contentDescription = "Sunucu Değiştir",
                                                tint = if (serverList.size > 1) NeonCyan else TextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = if (serverList.size > 1) "Sunucu ${currentServerIndex + 1}/${serverList.size}" else "Sunucu 1",
                                                color = if (serverList.size > 1) NeonCyan else TextSecondary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // Veri Tasarrufu Modu Butonu (SD / HD Hızlı Geçiş)
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isDataSaver) NeonCyan.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.08f),
                                        border = BorderStroke(1.dp, if (isDataSaver) NeonCyan else Color.White.copy(alpha = 0.16f)),
                                        modifier = Modifier.clickable {
                                            isDataSaver = !isDataSaver
                                            userPrefs.saveDataSaverEnabled(isDataSaver)
                                            serverNotice = if (isDataSaver) "Veri Tasarrufu AÇIK (SD Kalite • %85 Kota Tasarrufu)" else "Veri Tasarrufu KAPALI (Orijinal Yüksek Kalite)"
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.HighQuality,
                                                contentDescription = "Veri Tasarrufu",
                                                tint = if (isDataSaver) NeonCyan else TextSecondary,
                                                modifier = Modifier.size(17.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isDataSaver) "SD (Tasarruf)" else "HD (Kalite)",
                                                color = if (isDataSaver) NeonCyan else TextSecondary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Kumanda & Dokunmatik Kısayol Bilgisi
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isLandscape) "Kumanda / Dokunma: [OK] Menü / Kanallar • [Sol/Sağ] 10sn Sar • [Yukarı/Aşağı] Tek Tek Kanal Değiştir"
                                   else "Dokunma: Menüyü açıp kapatmak için ekrana tıklayın • Ekran yönünü üstteki butondan yönetin",
                            color = TextSecondary.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubePlayerView(
    videoId: String,
    modifier: Modifier = Modifier
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE, androidx.lifecycle.Lifecycle.Event.ON_STOP -> {
                    try {
                        webViewRef?.onPause()
                        webViewRef?.pauseTimers()
                    } catch (_: Throwable) {}
                }
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> {
                    try {
                        webViewRef?.onResume()
                        webViewRef?.resumeTimers()
                    } catch (_: Throwable) {}
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            try {
                webViewRef?.apply {
                    stopLoading()
                    onPause()
                    pauseTimers()
                    loadUrl("about:blank")
                    destroy()
                }
            } catch (_: Throwable) {}
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.domStorageEnabled = true
                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()
                val embedHtml = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
                    <style>body,html{margin:0;padding:0;background:#000;overflow:hidden;width:100%;height:100%;}</style>
                    </head>
                    <body>
                    <iframe width="100%" height="100%" src="https://www.youtube.com/embed/$videoId?autoplay=1&playsinline=1&controls=1&rel=0" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>
                    </body>
                    </html>
                """.trimIndent()
                loadDataWithBaseURL("https://www.youtube.com", embedHtml, "text/html", "utf-8", null)
                webViewRef = this
            }
        },
        onRelease = { webView ->
            try {
                webView.stopLoading()
                webView.onPause()
                webView.pauseTimers()
                webView.loadUrl("about:blank")
                webView.destroy()
            } catch (_: Throwable) {}
        }
    )
}

fun extractYouTubeVideoId(url: String): String? {
    val regexes = listOf(
        Regex("""(?:youtube\.com/(?:watch\?v=|live/|v/|embed/))([a-zA-Z0-9_-]{11})"""),
        Regex("""(?:youtu\.be/)([a-zA-Z0-9_-]{11})""")
    )
    for (r in regexes) {
        val match = r.find(url)
        if (match != null) return match.groupValues[1]
    }
    return null
}

@Composable
private fun RadioVisualizerView(
    item: MediaItem,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radio_pulse")
    val animatedPulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Dynamic bar heights for ambient audio equalizer
    val barCount = 28
    val waveHeights = List(barCount) { index ->
        val duration = 350 + (index * 73) % 550
        val animHeight by infiniteTransition.animateFloat(
            initialValue = 8f,
            targetValue = if (isPlaying) 18f + ((index * 37) % 45).toFloat() else 8f,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$index"
        )
        animHeight
    }

    Box(
        modifier = modifier
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF141F38),
                        Color(0xFF070B14),
                        Color(0xFF030508)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Radio Vinyl / Luxury Audio Emblem
            Surface(
                shape = CircleShape,
                color = Color(0xFF0A0F1D),
                border = BorderStroke(3.dp, Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFF00D2FF)))),
                shadowElevation = 18.dp,
                modifier = Modifier
                    .size(150.dp)
                    .scale(if (isPlaying) animatedPulse else 1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val logoUrl = item.resim_url.ifBlank {
                        com.example.service.InternetChannelService.getOfficialWebLogo(item.baslik)
                    }
                    if (logoUrl.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(logoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = item.baslik,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(100.dp)
                                .padding(8.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Radio,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(54.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Station Name
            Text(
                text = item.baslik,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Category & Live Broadcast Status Badges
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NeonAmber.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, NeonAmber)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isPlaying) Color(0xFF00E676) else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isPlaying) "CANLI RADYO" else "DURAKLATILDI",
                            color = NeonAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.08f)
                ) {
                    Text(
                        text = "Kategori: ${item.kategori}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Animated Equalizer Waveform Bars
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(60.dp)
                    .padding(horizontal = 16.dp)
            ) {
                waveHeights.forEach { heightVal ->
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(heightVal.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFFFFD700),
                                        Color(0xFF00D2FF)
                                    )
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Arka Planda Çalma & Bildirim Menüsü Bilgilendirme Rozeti
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Radio,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Arka Planda Çalma & Bildirim Kontrolü Aktif",
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}


