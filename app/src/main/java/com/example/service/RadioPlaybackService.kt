package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.media.AudioAttributes as AndroidAudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.example.R
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import android.util.Log
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.MainActivity
import com.example.model.MediaItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Türkiye canlı radyo istasyonları için kesintisiz arka planda oynatma ve
 * bildirim panelinde (Play / Pause / Stop) kontrolleri sağlayan Foreground Service.
 */
class RadioPlaybackService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var exoPlayer: ExoPlayer? = null
    private var mediaSessionCompat: MediaSessionCompat? = null
    private var audioManager: AudioManager? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var wifiLock: WifiManager.WifiLock? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var hasAudioFocus = false
    private var notificationBitmap: Bitmap? = null

    private var activeStreamUrl: String = ""
    private var activeBackupUrls: List<String> = emptyList()
    private var currentBackupIndex: Int = 0
    private var retryCount: Int = 0
    private var reconnectJob: Job? = null

    companion object {
        const val CHANNEL_ID = "vizyon_radio_playback_v3"
        const val NOTIFICATION_ID = 2026

        const val ACTION_PLAY = "com.example.action.RADIO_PLAY"
        const val ACTION_PAUSE = "com.example.action.RADIO_PAUSE"
        const val ACTION_RESUME = "com.example.action.RADIO_RESUME"
        const val ACTION_TOGGLE = "com.example.action.RADIO_TOGGLE"
        const val ACTION_NEXT = "com.example.action.RADIO_NEXT"
        const val ACTION_PREV = "com.example.action.RADIO_PREV"
        const val ACTION_STOP = "com.example.action.RADIO_STOP"

        const val EXTRA_STREAM_URL = "extra_stream_url"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_LOGO_URL = "extra_logo_url"
        const val EXTRA_STATION_ID = "extra_station_id"
        const val EXTRA_BACKUP_URLS = "extra_backup_urls"

        private val _currentStation = MutableStateFlow<MediaItem?>(null)
        val currentStation: StateFlow<MediaItem?> = _currentStation.asStateFlow()

        private val _isPlaying = MutableStateFlow(false)
        val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

        private val _isBuffering = MutableStateFlow(false)
        val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

        private val _errorMessage = MutableStateFlow<String?>(null)
        val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

        fun play(context: Context, item: MediaItem) {
            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                action = ACTION_PLAY
                putExtra(EXTRA_STREAM_URL, item.yayin_url)
                putExtra(EXTRA_TITLE, item.baslik)
                putExtra(EXTRA_LOGO_URL, item.resim_url)
                putExtra(EXTRA_STATION_ID, item.id)
                putStringArrayListExtra(EXTRA_BACKUP_URLS, ArrayList(item.backup_urls))
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            _currentStation.value = item
        }

        fun pause(context: Context) {
            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun resume(context: Context) {
            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                action = ACTION_RESUME
            }
            context.startService(intent)
        }

        fun toggle(context: Context) {
            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                action = ACTION_TOGGLE
            }
            context.startService(intent)
        }

        fun next(context: Context) {
            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                action = ACTION_NEXT
            }
            context.startService(intent)
        }

        fun previous(context: Context) {
            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                action = ACTION_PREV
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
            _currentStation.value = null
            _isPlaying.value = false
            _isBuffering.value = false
        }
    }

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        createNotificationChannel()
        initializeMediaSession()
        initializePlayer()
    }

    private fun initializeMediaSession() {
        try {
            mediaSessionCompat = MediaSessionCompat(this, "VizyonRadioSession").apply {
                setCallback(object : MediaSessionCompat.Callback() {
                    override fun onPlay() { resume(this@RadioPlaybackService) }
                    override fun onPause() { pause(this@RadioPlaybackService) }
                    override fun onSkipToNext() { next(this@RadioPlaybackService) }
                    override fun onSkipToPrevious() { previous(this@RadioPlaybackService) }
                    override fun onStop() { stop(this@RadioPlaybackService) }
                })
                val openAppIntent = Intent(this@RadioPlaybackService, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pi = PendingIntent.getActivity(
                    this@RadioPlaybackService,
                    0,
                    openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setSessionActivity(pi)
                setFlags(
                    MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                    MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
                )
                isActive = true
            }
        } catch (e: Exception) {
            Log.w("RadioPlaybackService", "MediaSession init error: ${e.message}")
        }
    }

    private fun updateMediaSession(title: String, playing: Boolean) {
        try {
            val state = if (playing) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
            val stateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                    PlaybackStateCompat.ACTION_PAUSE or
                    PlaybackStateCompat.ACTION_PLAY_PAUSE or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                    PlaybackStateCompat.ACTION_STOP
                )
                .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
            mediaSessionCompat?.setPlaybackState(stateBuilder.build())

            val metaBuilder = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "VİZYON TV • Canlı Radyo")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "Türkiye Canlı Radyoları")
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, "Türkiye Canlı Yayın • Kesintisiz")

            notificationBitmap?.let { bmp ->
                metaBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bmp)
                metaBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, bmp)
                metaBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, bmp)
            }
            mediaSessionCompat?.setMetadata(metaBuilder.build())
        } catch (e: Exception) {
            Log.w("RadioPlaybackService", "MediaSession update error: ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "VİZYON TV Radyo Yayını"
            val desc = "Kilit ekranı ve arka planda kesintisiz radyo yayını ve kontrolleri"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = desc
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setBypassDnd(true)
                enableVibration(false)
                enableLights(false)
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun initializePlayer() {
        val renderersFactory = DefaultRenderersFactory(this)
            .setEnableDecoderFallback(true)

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                4000,  // minBufferMs (4 saniye - canlı radyo akışında kesintisiz tampon)
                30000, // maxBufferMs (30 saniye)
                500,   // bufferForPlaybackMs (0.5 saniyede anında başlatma)
                1500   // bufferForPlaybackAfterRebufferMs (1.5 saniye toparlanma)
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .setBackBuffer(0, false)
            .build()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        exoPlayer = ExoPlayer.Builder(this, renderersFactory)
            .setLoadControl(loadControl)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build().apply {
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_BUFFERING -> {
                                _isBuffering.value = true
                                updateNotification()
                            }
                            Player.STATE_READY -> {
                                _isBuffering.value = false
                                _isPlaying.value = playWhenReady
                                retryCount = 0
                                _errorMessage.value = null
                                updateNotification()
                            }
                            Player.STATE_ENDED -> {
                                _isBuffering.value = false
                                _isPlaying.value = false
                                updateNotification()
                            }
                            Player.STATE_IDLE -> {
                                _isBuffering.value = false
                            }
                        }
                    }

                    override fun onIsPlayingChanged(playing: Boolean) {
                        _isPlaying.value = playing
                        updateNotification()
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        Log.w("RadioPlaybackService", "Radyo yayın hatası: ${error.message}")
                        reconnectJob?.cancel()
                        if (retryCount < 3) {
                            retryCount++
                            val delayMs = when (retryCount) {
                                1 -> 800L
                                2 -> 1500L
                                else -> 2500L
                            }
                            _isBuffering.value = true
                            reconnectJob = serviceScope.launch {
                                delay(delayMs)
                                startStreamPlayback(activeStreamUrl)
                            }
                        } else if (currentBackupIndex < activeBackupUrls.size) {
                            val nextUrl = activeBackupUrls[currentBackupIndex]
                            currentBackupIndex++
                            retryCount = 0
                            _isBuffering.value = true
                            startStreamPlayback(nextUrl)
                        } else {
                            _isBuffering.value = false
                            _isPlaying.value = false
                            _errorMessage.value = "Yayın akışı geçici olarak bağlanamadı"
                            updateNotification()
                        }
                    }
                })
            }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val streamUrl = intent.getStringExtra(EXTRA_STREAM_URL) ?: ""
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "Canlı Radyo"
                val logoUrl = intent.getStringExtra(EXTRA_LOGO_URL) ?: ""
                val backups = intent.getStringArrayListExtra(EXTRA_BACKUP_URLS) ?: arrayListOf()

                activeStreamUrl = streamUrl
                activeBackupUrls = backups
                currentBackupIndex = 0
                retryCount = 0
                reconnectJob?.cancel()

                startForegroundNotification(title)
                loadStationLogo(logoUrl)
                startStreamPlayback(streamUrl)
            }
            ACTION_PAUSE -> {
                exoPlayer?.playWhenReady = false
                _isPlaying.value = false
                updateNotification()
            }
            ACTION_RESUME -> {
                requestAudioFocus()
                exoPlayer?.playWhenReady = true
                _isPlaying.value = true
                updateNotification()
            }
            ACTION_TOGGLE -> {
                if (exoPlayer?.isPlaying == true) {
                    exoPlayer?.playWhenReady = false
                    _isPlaying.value = false
                } else {
                    requestAudioFocus()
                    exoPlayer?.playWhenReady = true
                    _isPlaying.value = true
                }
                updateNotification()
            }
            ACTION_NEXT -> {
                val allStations = InternetChannelService().getRadioStationList()
                val current = _currentStation.value
                val curIdx = allStations.indexOfFirst { it.id == current?.id || it.baslik.equals(current?.baslik, ignoreCase = true) }
                val nextStation = if (curIdx != -1 && curIdx < allStations.size - 1) allStations[curIdx + 1] else allStations.firstOrNull()
                if (nextStation != null) {
                    play(this, nextStation)
                }
            }
            ACTION_PREV -> {
                val allStations = InternetChannelService().getRadioStationList()
                val current = _currentStation.value
                val curIdx = allStations.indexOfFirst { it.id == current?.id || it.baslik.equals(current?.baslik, ignoreCase = true) }
                val prevStation = if (curIdx > 0) allStations[curIdx - 1] else allStations.lastOrNull()
                if (prevStation != null) {
                    play(this, prevStation)
                }
            }
            ACTION_STOP -> {
                stopPlaybackInternal()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startForegroundNotification(title: String) {
        val notification = buildNotification(title, _isPlaying.value)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun acquireWakeLocks() {
        try {
            if (wakeLock == null) {
                val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
                wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "VizyonTV:RadioWakeLock")?.apply {
                    setReferenceCounted(false)
                }
            }
            if (wakeLock?.isHeld == false) {
                wakeLock?.acquire(12 * 60 * 60 * 1000L) // 12 saat kesintisiz kilit ekranı oynatımı
            }

            if (wifiLock == null) {
                val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    WifiManager.WIFI_MODE_FULL_LOW_LATENCY
                } else {
                    WifiManager.WIFI_MODE_FULL_HIGH_PERF
                }
                wifiLock = wifiManager?.createWifiLock(mode, "VizyonTV:RadioWifiLock")?.apply {
                    setReferenceCounted(false)
                }
            }
            if (wifiLock?.isHeld == false) {
                wifiLock?.acquire()
            }
        } catch (e: Exception) {
            Log.w("RadioPlaybackService", "WakeLock acquire error: ${e.message}")
        }
    }

    private fun releaseWakeLocks() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
            if (wifiLock?.isHeld == true) {
                wifiLock?.release()
            }
        } catch (_: Exception) {}
    }

    private fun startStreamPlayback(url: String) {
        if (url.isBlank()) return

        requestAudioFocus()
        acquireWakeLocks()

        val extractorsFactory = androidx.media3.extractor.DefaultExtractorsFactory()
            .setConstantBitrateSeekingEnabled(true)
            .setAdtsExtractorFlags(androidx.media3.extractor.ts.AdtsExtractor.FLAG_ENABLE_CONSTANT_BITRATE_SEEKING)

        // Karnaval ve StreamTheWorld gibi CDN sunucularının masaüstü tarayıcı tespit edip 10. saniyede (32KB)
        // bağlantıyı sonlandırmasını önlemek için optimize edilmiş yerel mobil medya başlıkları
        val httpSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(20000)
            .setReadTimeoutMs(35000)
            .setKeepPostFor302Redirects(true)
            .setDefaultRequestProperties(
                mapOf(
                    "Accept" to "*/*",
                    "Connection" to "keep-alive",
                    "Icy-MetaData" to "0"
                )
            )
            .setUserAgent("Karnaval/6.0 (Android; Mobile; rv:125.0) ExoPlayerLib/2.19.1")

        val mediaItem = ExoMediaItem.fromUri(url)
        val isHls = url.contains(".m3u8", ignoreCase = true)
        val mediaSource = if (isHls) {
            HlsMediaSource.Factory(httpSourceFactory)
                .setAllowChunklessPreparation(true)
                .createMediaSource(mediaItem)
        } else {
            val defaultMediaSourceFactory = androidx.media3.exoplayer.source.DefaultMediaSourceFactory(this, extractorsFactory)
                .setDataSourceFactory(httpSourceFactory)
            defaultMediaSourceFactory.createMediaSource(mediaItem)
        }

        exoPlayer?.apply {
            stop()
            clearMediaItems()
            setMediaSource(mediaSource)
            prepare()
            playWhenReady = true
        }
    }

    private fun loadStationLogo(logoUrl: String) {
        if (logoUrl.isBlank()) return
        serviceScope.launch(Dispatchers.IO) {
            try {
                val loader = ImageLoader(this@RadioPlaybackService)
                val request = ImageRequest.Builder(this@RadioPlaybackService)
                    .data(logoUrl)
                    .allowHardware(false)
                    .build()
                val result = (loader.execute(request) as? SuccessResult)?.drawable
                if (result is BitmapDrawable) {
                    notificationBitmap = result.bitmap
                    updateNotification()
                }
            } catch (_: Exception) {}
        }
    }

    private fun updateNotification() {
        val station = _currentStation.value
        val title = station?.baslik ?: "Canlı Radyo"
        val notification = buildNotification(title, _isPlaying.value)
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(title: String, playing: Boolean): Notification {
        updateMediaSession(title, playing)

        // App açma intent'i (tekrar tıklayınca MainActivity'yi ön plana getirir)
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Previous Station PendingIntent
        val prevIntent = Intent(this, RadioPlaybackService::class.java).apply {
            action = ACTION_PREV
        }
        val prevPendingIntent = PendingIntent.getService(
            this,
            3,
            prevIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Play / Pause Toggle PendingIntent
        val toggleIntent = Intent(this, RadioPlaybackService::class.java).apply {
            action = ACTION_TOGGLE
        }
        val togglePendingIntent = PendingIntent.getService(
            this,
            1,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Next Station PendingIntent
        val nextIntent = Intent(this, RadioPlaybackService::class.java).apply {
            action = ACTION_NEXT
        }
        val nextPendingIntent = PendingIntent.getService(
            this,
            4,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Stop PendingIntent
        val stopIntent = Intent(this, RadioPlaybackService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIconRes = if (playing) R.drawable.ic_spotify_pause else R.drawable.ic_spotify_play
        val playPauseText = if (playing) "Duraklat" else "Oynat"

        val mediaStyle = MediaStyle()
            .setShowActionsInCompactView(0, 1, 2)
            .setShowCancelButton(true)
            .setCancelButtonIntent(stopPendingIntent)

        mediaSessionCompat?.sessionToken?.let { token ->
            mediaStyle.setMediaSession(token)
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_radio_notification)
            .setContentTitle(title)
            .setContentText("VİZYON TV • Canlı Radyo Yayını")
            .setSubText("Canlı Yayın")
            .setContentIntent(contentPendingIntent)
            .setDeleteIntent(stopPendingIntent)
            .setOngoing(playing)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setColor(0xFF1DB954.toInt()) // Spotify green
            .setColorized(true)
            .setStyle(mediaStyle)
            .addAction(R.drawable.ic_spotify_prev, "Önceki", prevPendingIntent)
            .addAction(playPauseIconRes, playPauseText, togglePendingIntent)
            .addAction(R.drawable.ic_spotify_next, "Sonraki", nextPendingIntent)
            .addAction(R.drawable.ic_spotify_close, "Kapat", stopPendingIntent)

        if (notificationBitmap != null) {
            builder.setLargeIcon(notificationBitmap)
        } else {
            val defaultLogo = BitmapFactory.decodeResource(resources, R.drawable.vizyontv_logo_1791312957832)
            if (defaultLogo != null) {
                builder.setLargeIcon(defaultLogo)
            }
        }

        return builder.build()
    }

    private fun requestAudioFocus() {
        // AudioFocus is automatically and cleanly managed by ExoPlayer via setAudioAttributes(..., handleAudioFocus = true)
    }

    private fun abandonAudioFocus() {
        // Managed by ExoPlayer
    }

    private fun stopPlaybackInternal() {
        abandonAudioFocus()
        releaseWakeLocks()
        try {
            exoPlayer?.stop()
            exoPlayer?.clearMediaItems()
        } catch (_: Exception) {}
        _isPlaying.value = false
        _isBuffering.value = false
        _currentStation.value = null
        try {
            val stateBuilder = PlaybackStateCompat.Builder()
                .setActions(0)
                .setState(PlaybackStateCompat.STATE_STOPPED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0f)
            mediaSessionCompat?.setPlaybackState(stateBuilder.build())
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        serviceScope.cancel()
        stopPlaybackInternal()
        try {
            mediaSessionCompat?.isActive = false
            mediaSessionCompat?.release()
            mediaSessionCompat = null
        } catch (_: Exception) {}
        exoPlayer?.release()
        exoPlayer = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
