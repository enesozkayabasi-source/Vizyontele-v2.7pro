package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.outlined.Search
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
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

    var showAddDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
            onSelectChannel = { viewModel.playMedia(it) },
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
            // Mobile portrait bottom navigation bar
            bottomBar = {
                if (!isTvLayout) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        MainNavigationTab.entries.forEach { tab ->
                            val isSelected = selectedTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.selectTab(tab) },
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
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = NeonCyan.copy(alpha = 0.2f),
                                    selectedIconColor = NeonCyan,
                                    selectedTextColor = NeonCyan,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
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
                        onSelectTab = { viewModel.selectTab(it) },
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
                        onSearchChange = { viewModel.onSearchQueryChanged(it) },
                        onOpenSettings = { showSettingsDialog = true },
                        onOpenAddDialog = { showAddDialog = true },
                        onAiFindChannel = { query ->
                            viewModel.findAndPlayCustomChannelWithAI(query)
                        },
                        onRefresh = {
                            viewModel.refreshChannels()
                            scope.launch {
                                snackbarHostState.showSnackbar("Yayınlar yenileniyor...")
                            }
                        }
                    )

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
                            // Featured Hero Item at top if no active search
                            if (searchQuery.isBlank() && filteredItems.isNotEmpty()) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    val first = filteredItems.first()
                                    HeroHighlightBanner(
                                        item = first,
                                        language = currentLanguage,
                                        isLandscape = isTvLayout,
                                        onPlay = { viewModel.playMedia(first) }
                                    )
                                }
                            }

                            // Channel & Media Items
                            items(
                                items = if (searchQuery.isBlank() && filteredItems.size > 1) filteredItems.drop(1) else filteredItems,
                                key = { it.id }
                            ) { item ->
                                MediaCard(
                                    item = item,
                                    onClick = { viewModel.playMedia(item) },
                                    onToggleFavorite = { viewModel.toggleFavorite(item) }
                                )
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
                    shape = RoundedCornerShape(10.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.vizyontele_logo_1790878162899),
                        contentDescription = "Vizyontele Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    ) {
        MainNavigationTab.entries.forEach { tab ->
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
    onSearchChange: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAddDialog: () -> Unit,
    onAiFindChannel: (String) -> Unit,
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
            // Brand & Tab Title (Compact for phones)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isLandscape) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.vizyontele_logo_1790878162899),
                            contentDescription = "Vizyontele Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = AppStrings.get(selectedTab.key, language),
                    fontSize = if (isLandscape) 20.sp else 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (selectedTab == MainNavigationTab.SPOR) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = NeonAmber
                    ) {
                        Text(
                            text = "AÇIK SPOR",
                            color = Color.Black,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                } else if (selectedTab == MainNavigationTab.HABER) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = NeonAmber
                    ) {
                        Text(
                            text = "CANLI HABER",
                            color = Color.Black,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                } else if (selectedTab == MainNavigationTab.COCUK) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = NeonCyan
                    ) {
                        Text(
                            text = "ÇOCUK KULÜBÜ",
                            color = Color.Black,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // AI Fast Channel Finder
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = NeonCyan.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.clickable {
                        onAiFindChannel(if (searchQuery.isNotBlank()) searchQuery else "TRT 1")
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Yapay Zeka",
                            tint = NeonCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AI Bul",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Refresh channels button
                IconButton(
                    onClick = onRefresh,
                    enabled = !isRefreshing,
                    modifier = Modifier.size(34.dp)
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
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

                // Add Custom / M3U Playlist button
                IconButton(onClick = onOpenAddDialog, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                        contentDescription = "Kanal Ekle",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // General Settings button
                IconButton(onClick = onOpenSettings, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Genel Ayarlar",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
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
                        text = "Kanal, haber, çocuk veya spor ara (TRT, NTV, Minika, A Spor...)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Arama",
                        tint = NeonCyan,
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
                                color = NeonCyan.copy(alpha = 0.2f),
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Text(
                                    text = "$resultCount içerik",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan,
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
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
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
    val isSport = item.kategori.equals("Spor", ignoreCase = true) || item.baslik.contains("spor", ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(if (isLandscape) 3.0f else 2.3f)
            .clickable(onClick = onPlay)
            .focusable(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (item.resim_url.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.resim_url)
                        .crossfade(true)
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
                    color = if (isSport) NeonAmber else NeonRed
                ) {
                    Text(
                        text = if (isSport) "CANLI SPOR" else "ÖNE ÇIKAN YAYIN",
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
                    text = "${item.kategori} • Canlı HD Akış",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = NeonCyan,
                    modifier = Modifier.clickable(onClick = onPlay)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
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
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
