package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.MediaCollection
import com.example.model.MediaItem
import com.example.ui.components.AddMediaDialog
import com.example.ui.components.AiStreamResolvingDialog
import com.example.ui.components.MediaCard
import com.example.ui.components.SettingsDialog
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.player.StreamPlayerScreen
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonRed
import com.example.ui.viewmodel.MainNavigationTab
import com.example.ui.viewmodel.StreamViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: StreamViewModel,
    modifier: Modifier = Modifier
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredItems by viewModel.filteredItems.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()
    val currentPlayingItem by viewModel.currentPlayingItem.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val layoutMode by viewModel.layoutMode.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val initialPlaybackPositionMs by viewModel.initialPlaybackPositionMs.collectAsState()
    val isAiResolving by viewModel.isAiResolving.collectAsState()
    val aiResolvingChannel by viewModel.aiResolvingChannel.collectAsState()
    val aiSourceInfo by viewModel.aiSourceInfo.collectAsState()
    val isDataSaverEnabled by viewModel.isDataSaverEnabled.collectAsState()

    // Background Radio States
    val activeRadioStation by viewModel.activeRadioStation.collectAsState()
    val isRadioPlaying by viewModel.isRadioPlaying.collectAsState()
    val isRadioBuffering by viewModel.isRadioBuffering.collectAsState()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "Kilit ekranı ve bildirim panelinde radyo kontrolü için bildirim izni gereklidir.",
                    actionLabel = "İzin Ver",
                    duration = androidx.compose.material3.SnackbarDuration.Long
                )
                if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                    try {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                }
            }
        }
    }

    val requestNotificationPermissionIfNeeded: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == MainNavigationTab.RADYO) {
            requestNotificationPermissionIfNeeded()
        }
    }

    val currentVersion by viewModel.currentVersion.collectAsState()
    val hasUpdate by viewModel.hasUpdate.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    val updateProgress by viewModel.updateProgress.collectAsState()
    val updateStatus by viewModel.updateStatus.collectAsState()
    val githubPageUrl by viewModel.githubPageUrl.collectAsState()
    val apkDownloadUrl by viewModel.apkDownloadUrl.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val gridState = rememberLazyGridState()

    var lastClickedTab by remember { mutableStateOf<MainNavigationTab?>(null) }
    var lastClickTime by remember { androidx.compose.runtime.mutableLongStateOf(0L) }

    val onTabItemClick: (MainNavigationTab) -> Unit = { tab ->
        val now = System.currentTimeMillis()
        val isDoubleOrReClick = (selectedTab == tab) || (lastClickedTab == tab && (now - lastClickTime) < 800L)
        lastClickedTab = tab
        lastClickTime = now

        if (tab == MainNavigationTab.RADYO) {
            requestNotificationPermissionIfNeeded()
        }

        if (isDoubleOrReClick) {
            scope.launch {
                gridState.animateScrollToItem(0)
            }
        } else {
            viewModel.selectTab(tab)
            scope.launch {
                gridState.scrollToItem(0)
            }
        }
    }

    // Full screen player mode
    if (currentPlayingItem != null) {
        StreamPlayerScreen(
            item = currentPlayingItem!!,
            channelList = filteredItems,
            initialPositionMs = initialPlaybackPositionMs,
            aiSource = aiSourceInfo,
            onSaveProgress = { pos, dur ->
                viewModel.saveWatchProgress(currentPlayingItem!!, pos, dur)
            },
            onSelectChannel = {
                if (it.collectionType == MediaCollection.RADYO) {
                    requestNotificationPermissionIfNeeded()
                }
                viewModel.playMedia(it)
            },
            onBack = { viewModel.stopPlayback() },
            onNext = { viewModel.playNext() },
            onPrevious = { viewModel.playPrevious() }
        )
        return
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val isLandscape = maxWidth > maxHeight
        val isTvLayout = when (layoutMode) {
            com.example.ui.theme.AppLayoutMode.TV -> true
            com.example.ui.theme.AppLayoutMode.PHONE -> false
            com.example.ui.theme.AppLayoutMode.AUTO -> isLandscape || screenWidth > 650.dp
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background,
            // Mobile portrait bottom navigation bar & Background Radio Mini Player
            bottomBar = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Arka Planda Çalan Radyo Kontrol Menüsü (Mini Player)
                    AnimatedVisibility(
                        visible = activeRadioStation != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        activeRadioStation?.let { radioItem ->
                            RadioMiniPlayerBar(
                                station = radioItem,
                                isPlaying = isRadioPlaying,
                                isBuffering = isRadioBuffering,
                                onTogglePlayPause = { viewModel.toggleRadio() },
                                onStop = { viewModel.stopRadio() },
                                onOpenFullPlayer = {
                                    viewModel.playMedia(radioItem)
                                }
                            )
                        }
                    }

                    if (!isTvLayout) {
                        val isLightMode = themeMode == com.example.ui.theme.AppThemeMode.LIGHT
                        val navBarColor = if (isLightMode) Color(0xFFFFFFFF) else Color(0xFF000000)
                        val unselectedColor = if (isLightMode) Color(0xFF64748B) else Color(0xFF94A3B8)

                        val visibleTabs = listOf(
                            MainNavigationTab.CANLI_TV,
                            MainNavigationTab.HABER,
                            MainNavigationTab.COCUK,
                            MainNavigationTab.SPOR,
                            MainNavigationTab.RADYO
                        )

                        NavigationBar(
                            containerColor = navBarColor,
                            tonalElevation = 8.dp
                        ) {
                            visibleTabs.forEach { tab ->
                                val isSelected = selectedTab == tab
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { onTabItemClick(tab) },
                                    icon = {
                                        Icon(
                                            imageVector = getTabIcon(tab),
                                            contentDescription = tab.name,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = AppStrings.get(tab.key, currentLanguage),
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = unselectedColor,
                                        unselectedTextColor = unselectedColor
                                    )
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // TV Navigation Rail (Left Sidebar in TV Mode)
                if (isTvLayout) {
                    TvNavigationSidebar(
                        selectedTab = selectedTab,
                        onSelectTab = { onTabItemClick(it) },
                        currentLanguage = currentLanguage,
                        onOpenSettings = { showSettingsDialog = true },
                        onOpenAddDialog = { showAddDialog = true }
                    )
                }

                // Main Content Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    // Top App Header & Real-Time Search Bar
                    TopBarHeader(
                        selectedTab = selectedTab,
                        language = currentLanguage,
                        searchQuery = searchQuery,
                        resultCount = filteredItems.size,
                        isRefreshing = isRefreshing,
                        isLandscape = isTvLayout,
                        hasUpdate = hasUpdate,
                        onOpenUpdate = {
                            viewModel.applyAppUpdate { success, msg ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(msg)
                                }
                            }
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(apkDownloadUrl)).apply {
                                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        onSearchChange = { viewModel.onSearchQueryChanged(it) },
                        onOpenSettings = { showSettingsDialog = true },
                        onOpenAddDialog = { showAddDialog = true },
                        onSelectTab = { viewModel.selectTab(it) },
                        isDataSaverEnabled = isDataSaverEnabled,
                        onToggleDataSaver = {
                            viewModel.toggleDataSaver()
                            scope.launch {
                                val msg = if (!isDataSaverEnabled) "Veri Tasarrufu AÇIK (Mobil internet %85 korunuyor)" else "Veri Tasarrufu KAPALI (Orijinal Yüksek Kalite)"
                                snackbarHostState.showSnackbar(msg)
                            }
                        },
                        onRefresh = {
                            viewModel.refreshChannels()
                            scope.launch {
                                snackbarHostState.showSnackbar("Yayınlar yenileniyor...")
                            }
                        }
                    )

                    // Güncelleme Bildirim Banner'ı (Doğrudan APK İndir & GitHub Sayfamız)
                    AnimatedVisibility(
                        visible = hasUpdate,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = if (isTvLayout) 18.dp else 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = NeonAmber.copy(alpha = 0.15f)),
                            border = BorderStroke(1.2.dp, NeonAmber)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(NeonAmber.copy(alpha = 0.25f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudDownload,
                                            contentDescription = null,
                                            tint = NeonAmber,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "VİZYON TV ${viewModel.latestReleaseVersion} Güncellemesi Hazır!",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Karnaval radyoları ve kesintisiz akışlar optimize edildi.",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Doğrudan APK İndirme Butonu
                                    Button(
                                        onClick = {
                                            viewModel.applyAppUpdate { success, msg ->
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(msg)
                                                }
                                            }
                                            try {
                                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(apkDownloadUrl)).apply {
                                                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                                context.startActivity(intent)
                                            } catch (_: Exception) {}
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonAmber),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("banner_direct_apk_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "APK İndir",
                                            color = Color.Black,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 10.sp
                                        )
                                    }

                                    // GitHub Sayfası Butonu
                                    Button(
                                        onClick = {
                                            try {
                                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(githubPageUrl)).apply {
                                                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                                context.startActivity(intent)
                                            } catch (_: Exception) {}
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan.copy(alpha = 0.22f)),
                                        border = BorderStroke(1.dp, NeonCyan),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("banner_github_button")
                                    ) {
                                        Text(
                                            text = "GitHub",
                                            color = NeonCyan,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Categories horizontal filter chips
                    if (availableCategories.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = if (isTvLayout) 18.dp else 12.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            availableCategories.forEach { category ->
                                val isSelected = selectedCategory == category
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.selectCategory(category) },
                                    label = {
                                        Text(
                                            text = category,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                    )
                                )
                            }
                        }
                    }

                    // Content Grid
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                        },
                        label = "tab_content_transition",
                        modifier = Modifier.fillMaxSize()
                    ) { targetTab ->
                        if (filteredItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.LiveTv,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(54.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = AppStrings.get("no_channels", currentLanguage),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = AppStrings.get("search_hint", currentLanguage),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else {
                            LazyVerticalGrid(
                                state = gridState,
                                // Phone portrait: 2 fixed columns; TV Mode: 3-4 columns
                                columns = if (isTvLayout) {
                                    if (screenWidth > 950.dp) GridCells.Fixed(4) else GridCells.Fixed(3)
                                } else {
                                    if (screenWidth > 500.dp) GridCells.Fixed(3) else GridCells.Fixed(2)
                                },
                                contentPadding = PaddingValues(horizontal = if (isTvLayout) 18.dp else 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("media_grid")
                            ) {
                                // Radio info banner for background playback and notification panel controls
                                if (selectedTab == MainNavigationTab.RADYO && searchQuery.isBlank()) {
                                    item(span = { GridItemSpan(maxLineSpan) }) {
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Radio,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "Arka Planda Radyo & Bildirim Menüsü",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "Radyoyu dinlerken başka uygulamaya geçebilir, ekranı kapatabilirsiniz. Bildirim panelinden oynat/duraklat/değiştir kontrolleri kesintisiz çalışır.",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Featured Hero Item at top if no active search
                                if (searchQuery.isBlank() && filteredItems.isNotEmpty()) {
                                    item(span = { GridItemSpan(maxLineSpan) }) {
                                        val first = filteredItems.first()
                                        HeroHighlightBanner(
                                            item = first,
                                            language = currentLanguage,
                                            isLandscape = isTvLayout,
                                            onPlay = {
                                                if (first.collectionType == MediaCollection.RADYO || selectedTab == MainNavigationTab.RADYO) {
                                                    requestNotificationPermissionIfNeeded()
                                                }
                                                viewModel.playMedia(first)
                                            }
                                        )
                                    }
                                }

                                // Channel & Media Items
                                items(
                                    items = if (searchQuery.isBlank() && filteredItems.size > 1) filteredItems.drop(1) else filteredItems,
                                    key = { it.id },
                                    contentType = { "channel_card" }
                                ) { item ->
                                    MediaCard(
                                        item = item,
                                        onClick = {
                                            if (item.collectionType == MediaCollection.RADYO || selectedTab == MainNavigationTab.RADYO) {
                                                requestNotificationPermissionIfNeeded()
                                            }
                                            viewModel.playMedia(item)
                                        },
                                        onToggleFavorite = { viewModel.toggleFavorite(item) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // General Settings Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            currentThemeMode = themeMode,
            onSelectThemeMode = { viewModel.setThemeMode(it) },
            currentLayoutMode = layoutMode,
            onSelectLayoutMode = { viewModel.setLayoutMode(it) },
            currentLanguage = currentLanguage,
            onSelectLanguage = { viewModel.setLanguage(it) },
            presets = viewModel.availablePresets,
            isDataSaverEnabled = isDataSaverEnabled,
            onToggleDataSaver = { viewModel.setDataSaver(it) },
            onLoadPreset = { preset ->
                viewModel.loadPresetPlaylist(preset) { count, msg ->
                    scope.launch {
                        snackbarHostState.showSnackbar(msg)
                    }
                }
                showSettingsDialog = false
            },
            onImportM3u = { input, isUrl ->
                viewModel.importM3uPlaylist(input, isUrl) { count, msg ->
                    scope.launch {
                        snackbarHostState.showSnackbar(msg)
                    }
                }
                showSettingsDialog = false
            },
            currentVersion = currentVersion,
            latestVersion = viewModel.latestReleaseVersion,
            hasUpdate = hasUpdate,
            isUpdating = isUpdating,
            updateProgress = updateProgress,
            updateStatus = updateStatus,
            githubPageUrl = githubPageUrl,
            apkDownloadUrl = apkDownloadUrl,
            updateUrl = apkDownloadUrl,
            onSaveCustomUrls = { gh, apk ->
                viewModel.updateCustomGithubUrl(gh)
                viewModel.updateCustomApkUrl(apk)
                scope.launch {
                    snackbarHostState.showSnackbar("GitHub ve APK indirme bağlantıları güncellendi!")
                }
            },
            onCheckForUpdates = {
                viewModel.checkForUpdates { isNew, msg ->
                    scope.launch {
                        snackbarHostState.showSnackbar(msg)
                    }
                }
            },
            onApplyUpdate = {
                viewModel.applyAppUpdate { success, msg ->
                    scope.launch {
                        snackbarHostState.showSnackbar(msg)
                    }
                }
            },
            onToggleSimulatePreviousVersion = {
                viewModel.toggleSimulatePreviousVersion()
                scope.launch {
                    val alertText = if (viewModel.hasUpdate.value) 
                        "Önceki sürüme (v2.8.5) alındı! Güncelleme uyarısı aktif." 
                    else 
                        "En güncel sürüme (v2.9.0) eşitlendi."
                    snackbarHostState.showSnackbar(alertText)
                }
            },
            onOpenUpdateLink = { url ->
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)).apply {
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {
                    scope.launch {
                        snackbarHostState.showSnackbar("İndirme Bağlantısı: $url")
                    }
                }
            },
            onDismiss = { showSettingsDialog = false }
        )
    }

    // Add Media Dialog
    if (showAddDialog) {
        AddMediaDialog(
            onDismiss = { showAddDialog = false },
            onSave = { col, baslik, kat, resim, yayin ->
                viewModel.addCustomMedia(col, baslik, kat, resim, yayin) { success, msg ->
                    scope.launch {
                        snackbarHostState.showSnackbar(msg)
                    }
                }
            }
        )
    }

    // AI Stream Resolving HUD Dialog
    if (isAiResolving && !aiResolvingChannel.isNullOrBlank()) {
        AiStreamResolvingDialog(
            channelName = aiResolvingChannel!!,
            onDismiss = {}
        )
    }
}

private fun getTabIcon(tab: MainNavigationTab): androidx.compose.ui.graphics.vector.ImageVector {
    return when (tab) {
        MainNavigationTab.CANLI_TV -> Icons.Default.LiveTv
        MainNavigationTab.HABER -> Icons.Default.Newspaper
        MainNavigationTab.COCUK -> Icons.Default.ChildCare
        MainNavigationTab.SPOR -> Icons.Default.SportsSoccer
        MainNavigationTab.RADYO -> Icons.Default.Radio
        MainNavigationTab.FAVORILER -> Icons.Default.Favorite
    }
}

@Composable
private fun TvNavigationSidebar(
    selectedTab: MainNavigationTab,
    onSelectTab: (MainNavigationTab) -> Unit,
    currentLanguage: AppLanguage,
    onOpenSettings: () -> Unit,
    onOpenAddDialog: () -> Unit
) {
    val tvTabs = listOf(
        MainNavigationTab.CANLI_TV,
        MainNavigationTab.HABER,
        MainNavigationTab.COCUK,
        MainNavigationTab.SPOR,
        MainNavigationTab.RADYO
    )

    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.width(68.dp),
        header = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F1523),
                    border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFF00D2FF)))),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_corner_logo_premium_1791395314114),
                        contentDescription = "VİZYON TV Premium Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    ) {
        tvTabs.forEach { tab ->
            val isSelected = selectedTab == tab
            NavigationRailItem(
                selected = isSelected,
                onClick = { onSelectTab(tab) },
                icon = {
                    Icon(
                        imageVector = getTabIcon(tab),
                        contentDescription = tab.name,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = AppStrings.get(tab.key, currentLanguage),
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Settings Button
        IconButton(onClick = onOpenSettings) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Genel Ayarlar",
                tint = NeonCyan,
                modifier = Modifier.size(22.dp)
            )
        }

        // Add Channel Button
        IconButton(onClick = onOpenAddDialog) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Kanal Ekle",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun TopBarHeader(
    selectedTab: MainNavigationTab,
    language: AppLanguage,
    searchQuery: String,
    resultCount: Int,
    isRefreshing: Boolean,
    isLandscape: Boolean,
    hasUpdate: Boolean = false,
    onOpenUpdate: () -> Unit = {},
    onSearchChange: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAddDialog: () -> Unit,
    onSelectTab: (MainNavigationTab) -> Unit,
    isDataSaverEnabled: Boolean = false,
    onToggleDataSaver: () -> Unit = {},
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isLandscape) 18.dp else 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand & Tab Title (Adaptive layout - weight=1f, fill=true so action buttons are measured first with full sizes)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f, fill = true)
                    .padding(end = 6.dp)
            ) {
                if (!isLandscape) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0F1523),
                        border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFF00D2FF)))),
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                    ) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_corner_logo_premium_1791395314114),
                            contentDescription = "VİZYON TV Premium Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Column(modifier = Modifier.weight(1f, fill = true)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "VİZYON TV",
                            fontSize = if (isLandscape) 18.sp else 15.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = androidx.compose.ui.text.TextStyle(
                                brush = Brush.linearGradient(
                                    listOf(Color(0xFFFFE57F), Color(0xFFFFD700), Color(0xFFFFA000))
                                )
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF241C0A),
                            border = BorderStroke(0.8.dp, Color(0xFFFFD700).copy(alpha = 0.7f))
                        ) {
                            Text(
                                text = "PRO",
                                color = Color(0xFFFFD700),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = AppStrings.get(selectedTab.key, language),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Action Buttons (Samsung S25 FE ve tüm ekran oranlarında sabit boyutlu, asla sıkışmayan veya nokta haline gelmeyen buton grubu)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.wrapContentWidth()
            ) {
                // Favoriler Button
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedTab == MainNavigationTab.FAVORILER)
                        NeonRed.copy(alpha = 0.22f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(
                        1.dp,
                        if (selectedTab == MainNavigationTab.FAVORILER)
                            NeonRed
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            if (selectedTab == MainNavigationTab.FAVORILER) {
                                onSelectTab(MainNavigationTab.CANLI_TV)
                            } else {
                                onSelectTab(MainNavigationTab.FAVORILER)
                            }
                        }
                        .testTag("favorites_nav_button")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = if (selectedTab == MainNavigationTab.FAVORILER) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favoriler",
                            tint = if (selectedTab == MainNavigationTab.FAVORILER) NeonRed else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Yenile Button
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(enabled = !isRefreshing) { onRefresh() }
                        .testTag("refresh_channels_button")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Yenile",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Geniş ekranlarda Kanal Ekle Butonu
                if (isLandscape) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onOpenAddDialog() }
                            .testTag("add_custom_channel_button")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                                contentDescription = "Kanal Ekle",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Veri Tasarrufu Hızlı Butonu (%85 Daha Az İnternet)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDataSaverEnabled) NeonCyan.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(
                        1.2.dp,
                        if (isDataSaverEnabled) NeonCyan else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onToggleDataSaver() }
                        .testTag("data_saver_quick_button")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = if (isDataSaverEnabled) "Veri Tasarrufu Açık" else "Veri Tasarrufu Kapalı",
                            tint = if (isDataSaverEnabled) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // GÜNCELLE Butonu (Telefona atıldığında veya güncelleme hazır olduğunda belirgin buton)
                if (hasUpdate) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = NeonAmber.copy(alpha = 0.22f),
                        border = BorderStroke(1.5.dp, NeonAmber),
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .height(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onOpenUpdate() }
                            .testTag("topbar_update_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = "Güncelle",
                                tint = NeonAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "GÜNCELLE",
                                color = NeonAmber,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Genel Ayarlar Butonu (Samsung S25 FE ve tüm ekranlarda belirgin, 44dp dokunma hedefli, asla nokta olmaz)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NeonCyan.copy(alpha = 0.18f),
                    border = BorderStroke(1.4.dp, NeonCyan),
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onOpenSettings() }
                        .testTag("settings_button")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Genel Ayarlar",
                            tint = NeonCyan,
                            modifier = Modifier.size(23.dp)
                        )
                    }
                }
            }
        }

        // Real-Time Search Bar (Always visible on Home Screen)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = {
                    Text(
                        text = "Kanal, radyo, haber, çocuk veya spor ara (TRT, Kral, NTV, Minika...)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Arama",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        if (searchQuery.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Text(
                                    text = "$resultCount içerik",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            IconButton(
                                onClick = { onSearchChange("") },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Aramayı Temizle",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_text_field"),
                singleLine = true
            )
        }
    }
}

@Composable
private fun HeroHighlightBanner(
    item: MediaItem,
    language: AppLanguage,
    isLandscape: Boolean,
    onPlay: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bannerScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "hero_banner_scale"
    )

    val isHaber = item.collectionType == MediaCollection.HABER ||
        item.kategori.contains("haber", ignoreCase = true) ||
        item.baslik.contains("haber", ignoreCase = true) ||
        item.baslik.contains("ntv", ignoreCase = true)

    val isCocuk = item.collectionType == MediaCollection.COCUK ||
        item.kategori.contains("çocuk", ignoreCase = true) ||
        item.kategori.contains("cocuk", ignoreCase = true) ||
        item.baslik.contains("çocuk", ignoreCase = true) ||
        item.baslik.contains("minika", ignoreCase = true)

    val isSport = !isHaber && !isCocuk && (item.kategori.equals("Spor", ignoreCase = true) || item.baslik.contains("spor", ignoreCase = true))

    val heroCategoryText = when {
        isHaber -> "Haber & Gündem"
        isCocuk -> "Çocuk & Çizgi Dizi"
        isSport -> "Spor & Canlı Maç"
        else -> item.kategori.replace("Film", "Yayın", ignoreCase = true)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(if (isLandscape) 3.0f else 2.3f)
            .scale(bannerScale)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = Color(0xFFFFD700)),
                onClick = onPlay
            )
            .focusable()
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(Color(0xFFFFD700).copy(alpha = 0.5f), Color(0xFF00D2FF).copy(alpha = 0.2f), Color.Transparent)),
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (item.resim_url.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.resim_url)
                        .crossfade(false)
                        .size(800, 450)
                        .memoryCacheKey(item.resim_url)
                        .diskCacheKey(item.resim_url)
                        .build(),
                    contentDescription = item.baslik,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Dark gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.94f),
                                Color.Black.copy(alpha = 0.55f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Hero Details
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(if (isLandscape) 18.dp else 12.dp)
                    .fillMaxWidth(0.75f)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when {
                        isHaber -> NeonAmber
                        isCocuk -> NeonCyan
                        isSport -> NeonAmber
                        else -> NeonRed
                    }
                ) {
                    Text(
                        text = when {
                            isHaber -> "CANLI HABER"
                            isCocuk -> "ÇOCUK YAYINI"
                            isSport -> "CANLI SPOR"
                            else -> "ÖNE ÇIKAN YAYIN"
                        },
                        color = Color.Black,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.baslik,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )

                Text(
                    text = "$heroCategoryText • Canlı HD Akış",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 4.dp,
                    modifier = Modifier.clickable(onClick = onPlay)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = AppStrings.get("watch_now", language),
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Arka planda kesintisiz çalan radyo için HomeScreen üzerinde yüzen Mini Player Bar.
 * Bildirim paneline paralel olarak ekran içi hızlı Oynat / Duraklat ve Durdur kontrolleri sağlar.
 */
@Composable
private fun RadioMiniPlayerBar(
    station: MediaItem,
    isPlaying: Boolean,
    isBuffering: Boolean,
    onTogglePlayPause: () -> Unit,
    onStop: () -> Unit,
    onOpenFullPlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spotifyGreen = Color(0xFF1DB954)
    val spotifyDarkCard = Color(0xFF282828)
    val spotifyTextMuted = Color(0xFFB3B3B3)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onOpenFullPlayer() },
        shape = RoundedCornerShape(8.dp),
        color = spotifyDarkCard,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // İstasyon Logosu (Spotify 6dp rounded square)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF181818),
                        modifier = Modifier.size(42.dp)
                    ) {
                        AsyncImage(
                            model = station.resim_url,
                            contentDescription = station.baslik,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(6.dp))
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = station.baslik,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(if (isPlaying) spotifyGreen else spotifyTextMuted, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isBuffering) "Bağlanıyor..." else if (isPlaying) "VİZYON TV • Canlı Radyo" else "Duraklatıldı",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isPlaying) Color.White.copy(alpha = 0.85f) else spotifyTextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Spotify Kontrolleri
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier.size(38.dp)
                    ) {
                        if (isBuffering) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = spotifyGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Duraklat" else "Oynat",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onStop,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = spotifyTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Spotify tarzı altta ince yeşil canlı ilerleme çizgisi
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .background(Color(0xFF383838))
            ) {
                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.5.dp)
                            .background(spotifyGreen)
                    )
                }
            }
        }
    }
}
