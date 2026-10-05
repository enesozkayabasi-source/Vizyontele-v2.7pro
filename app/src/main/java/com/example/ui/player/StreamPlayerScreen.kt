package com.example.ui.player

import android.annotation.SuppressLint
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
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
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.model.MediaCollection
import com.example.model.MediaItem
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
    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showControls by remember { mutableStateOf(true) }
    var showChannelDrawer by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var resizeModeIndex by remember { mutableIntStateOf(0) }
    var activeStreamUrl by remember(item.yayin_url) { mutableStateOf(item.yayin_url) }

    val resizeModes = listOf(
        AspectRatioFrameLayout.RESIZE_MODE_FIT to "16:9 (FIT)",
        AspectRatioFrameLayout.RESIZE_MODE_ZOOM to "YAKINLAŞTIR (ZOOM)",
        AspectRatioFrameLayout.RESIZE_MODE_FILL to "TAM EKRAN (FILL)"
    )

    // Create single ExoPlayer instance for screen lifecycle
    val httpDataSourceFactory = remember {
        DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(10000)
            .setReadTimeoutMs(10000)
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

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                2500, // minBufferMs
                15000, // maxBufferMs
                800, // bufferForPlaybackMs
                1500  // bufferForPlaybackAfterRebufferMs
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        ExoPlayer.Builder(context, renderersFactory)
            .setLoadControl(loadControl)
            .build()
            .apply {
                playWhenReady = true
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            }
    }

    // Kanaldan çıkıldığında yayını anında durduran ve kaynakları temizleyen fonksiyon
    val exitPlayer: () -> Unit = {
        try {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
        } catch (_: Throwable) {}
        onBack()
    }

    // Uygulama arka plana geçtiğinde veya ekran kapandığında yayını anında durdur
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE || event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                try {
                    exoPlayer.pause()
                    exoPlayer.stop()
                } catch (_: Throwable) {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
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

    val isYouTube = activeStreamUrl.contains("youtube.com") || activeStreamUrl.contains("youtu.be")
    val youtubeVideoId = remember(activeStreamUrl) {
        if (isYouTube) extractYouTubeVideoId(activeStreamUrl) else null
    }

    // Keep activeStreamUrl in sync with item changes
    LaunchedEffect(item.yayin_url) {
        if (item.yayin_url.isNotBlank() && activeStreamUrl != item.yayin_url) {
            activeStreamUrl = item.yayin_url
        }
    }

    // Load active stream on existing ExoPlayer
    LaunchedEffect(activeStreamUrl, isYouTube) {
        if (activeStreamUrl.isBlank()) return@LaunchedEffect
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
            val exoItem = ExoMediaItem.fromUri(uri)
            val isHls = activeStreamUrl.contains(".m3u8", ignoreCase = true) || item.isHlsStream
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
                isBuffering = false
                val reason = error.localizedMessage ?: "Bağlantı zaman aşımı"
                errorMessage = "${item.baslik} yayını şu anda çevrimdışı veya bağlantı koptu ($reason)"
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
                when (event.key) {
                    Key.DirectionCenter, Key.Enter -> {
                        showControls = !showControls
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
                    Key.DirectionUp -> {
                        onPrevious()
                        true
                    }
                    Key.DirectionDown -> {
                        onNext()
                        true
                    }
                    Key.MediaPlayPause -> {
                        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                        true
                    }
                    Key.Back, Key.Escape -> {
                        if (showChannelDrawer) {
                            showChannelDrawer = false
                            true
                        } else {
                            exitPlayer()
                            true
                        }
                    }
                    else -> false
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

        // Buffering indicator (non-blocking translucent)
        if (isBuffering && errorMessage == null) {
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
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Tekrar Dene",
                                    tint = Color.Black
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Yeniden Bağlan", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.clickable { onNext() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Sonraki Kanal",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sonraki Kanal", color = Color.White, fontWeight = FontWeight.Bold)
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
                    .width(320.dp)
                    .fillMaxHeight(),
                color = MidnightDark.copy(alpha = 0.94f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    if (item.isSeries && item.seasons.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Dizi Bölümleri",
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${item.totalEpisodes} Bölüm",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item.seasons.forEach { season ->
                                item {
                                    Text(
                                        text = season.title,
                                        color = NeonAmber,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                                items(season.episodes, key = { "drawer_${it.seasonNumber}_${it.episodeNumber}" }) { ep ->
                                    val isCurrent = ep.seasonNumber == item.currentSeasonNumber && ep.episodeNumber == item.currentEpisodeNumber
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isCurrent) NeonCyan.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
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
                                            .border(
                                                width = if (isCurrent) 1.5.dp else 0.dp,
                                                color = if (isCurrent) NeonCyan else Color.Transparent,
                                                shape = RoundedCornerShape(10.dp)
                                            )
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
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    tint = NeonCyan,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Kanal Rehberi",
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${channelList.size} Kanal",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(channelList, key = { it.id }) { ch ->
                                val isCurrent = ch.id == item.id
                                val chLogo = remember(ch.resim_url, ch.baslik) {
                                    if (ch.resim_url.isNotBlank() && !ch.resim_url.contains("unsplash")) ch.resim_url
                                    else com.example.service.InternetChannelService().getOfficialWebLogo(ch.baslik)
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isCurrent) NeonCyan.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSelectChannel(ch)
                                            showChannelDrawer = false
                                        }
                                        .border(
                                            width = if (isCurrent) 1.5.dp else 0.dp,
                                            color = if (isCurrent) NeonCyan else Color.Transparent,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF0C101A),
                                            modifier = Modifier.size(40.dp, 30.dp)
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
                                            Text(
                                                text = ch.kategori,
                                                color = TextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }

                                        if (isCurrent) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                tint = NeonCyan,
                                                modifier = Modifier.size(18.dp)
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
                                Color.Black.copy(alpha = 0.8f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .align(Alignment.TopStart),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = exitPlayer,
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        val currentChannelLogo = remember(item.resim_url, item.baslik) {
                            if (item.resim_url.isNotBlank() && !item.resim_url.contains("unsplash")) item.resim_url
                            else com.example.service.InternetChannelService().getOfficialWebLogo(item.baslik)
                        }

                        if (currentChannelLogo.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0C101A),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                modifier = Modifier.size(46.dp, 34.dp)
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
                            Spacer(modifier = Modifier.width(10.dp))
                        }

                        Column {
                            val displayHeaderTitle = item.baslik

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = displayHeaderTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when (item.collectionType) {
                                        MediaCollection.CANLI_TV -> NeonRed
                                        MediaCollection.HABER -> NeonAmber
                                        MediaCollection.COCUK -> NeonCyan
                                        MediaCollection.SPOR -> NeonAmber
                                    }
                                ) {
                                    Text(
                                        text = when (item.collectionType) {
                                            MediaCollection.CANLI_TV -> "CANLI"
                                            MediaCollection.HABER -> "HABER"
                                            MediaCollection.COCUK -> "ÇOCUK"
                                            MediaCollection.SPOR -> "SPOR"
                                        },
                                        color = if (item.collectionType == MediaCollection.COCUK) Color.Black else Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                if (!aiSource.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = NeonCyan.copy(alpha = 0.2f),
                                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f))
                                    ) {
                                        Text(
                                            text = "HD AKIŞ",
                                            color = NeonCyan,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "${item.kategori} • ${if (item.isHlsStream) "HLS Canlı Yayın" else "Canlı HD Yayın"}",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Top Quality badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HighQuality,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Full HD",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // BOTTOM CONTROLS BAR (AŞAĞIDA ÇUBUK OLARAK TÜM BUTONLAR)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .align(Alignment.BottomCenter)
                        .testTag("player_bottom_control_bar"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF0A0F1D).copy(alpha = 0.92f)
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
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
                                    fontSize = 11.sp
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
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                        }

                        // Ana Butonlar Çubuğu (Yatay Çubuk)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Sol: Kanal Listesi Çekmecesi Açma Butonu
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (showChannelDrawer) NeonCyan.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, if (showChannelDrawer) NeonCyan else Color.White.copy(alpha = 0.15f)),
                                modifier = Modifier.clickable { showChannelDrawer = !showChannelDrawer }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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

                            // Orta: Oynatma & Kanal Değiştirme Kontrolleri
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Önceki Kanal
                                IconButton(
                                    onClick = onPrevious,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
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
                                            .size(40.dp)
                                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FastRewind,
                                            contentDescription = "10sn Geri",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                // Ana Oynat / Duraklat Butonu
                                Surface(
                                    shape = CircleShape,
                                    color = NeonCyan,
                                    shadowElevation = 6.dp,
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clickable {
                                            if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (isPlaying) "Duraklat" else "Oynat",
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
                                            .size(40.dp)
                                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FastForward,
                                            contentDescription = "10sn İleri",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                // Sonraki Kanal
                                IconButton(
                                    onClick = onNext,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SkipNext,
                                        contentDescription = "Sonraki Kanal",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // Sağ: En-Boy Oranı & Yenile
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Ekran En-Boy Oranı Değiştir (FIT, ZOOM, FILL)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.08f),
                                    modifier = Modifier.clickable {
                                        resizeModeIndex = (resizeModeIndex + 1) % resizeModes.size
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AspectRatio,
                                            contentDescription = resizeModes[resizeModeIndex].second,
                                            tint = NeonCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
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

                                // Yeniden Bağlan / Akışı Yenile
                                IconButton(
                                    onClick = {
                                        errorMessage = null
                                        isBuffering = true
                                        exoPlayer.stop()
                                        exoPlayer.prepare()
                                        exoPlayer.play()
                                    },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Yeniden Bağlan",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Kumanda Kısayol Bilgisi
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Kumanda: [OK] Menü / Kanallar • [Sol/Sağ] Sar • [Yukarı/Aşağı] Kanal Değiştir",
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

