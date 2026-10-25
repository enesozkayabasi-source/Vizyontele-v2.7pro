package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class AppThemeMode(
    val title: String,
    val description: String,
    val primaryColorHex: Long
) {
    LUXURY_GOLD("Premium Altın", "Lüks Gece Siyahı & Parlak Altın Vurgular", 0xFFFFD700),
    MIDNIGHT("OLED Siyah", "Saf siyah zemin & Neon Mavi (Pil tasarrufu)", 0xFF00E5FF),
    NAVY("Gece Mavisi", "Derin lacivert & Safir mavisi tonları", 0xFF38BDF8),
    CRIMSON("Sinema Kırmızı", "Netflix & Sinema tarzı koyu kömür & Canlı Kırmızı", 0xFFFF3366),
    EMERALD("Zümrüt Yeşil", "Koyu zümrüt & Canlı Yeşil vurgular", 0xFF00E676),
    TITANIUM("Koyu Titanyum", "Füme metalik gri & Amber Altın ışıltısı", 0xFFFFB300),
    LIGHT("Açık Modern", "Gündüz kullanımına özel ferah açık tema", 0xFF0284C7)
}

enum class AppLayoutMode(val title: String, val description: String) {
    AUTO("Otomatik", "Cihaz ve ekran yönüne göre akıllı geçiş"),
    PHONE("Telefon Modu", "Mobil uyumlu dikey arayüz, alt menü, dokunmatik kolay kullanım"),
    TV("Android TV Modu", "Geniş ekran TV arayüzü, sol menü rayı, büyük kumanda odaklı kartlar")
}

// 0. Luxury Gold & Obsidian Theme
val LuxuryGoldColorScheme = darkColorScheme(
    primary = Color(0xFFFFD700), // Pure luxury gold
    onPrimary = Color(0xFF0D0F14),
    primaryContainer = Color(0xFF241C0A),
    onPrimaryContainer = Color(0xFFFFE57F),
    secondary = Color(0xFF00D2FF), // Sapphire neon accent
    onSecondary = Color(0xFF050B14),
    tertiary = Color(0xFFFFA000), // Amber gold
    onTertiary = Color(0xFF000000),
    background = Color(0xFF070A12), // Deep obsidian midnight
    onBackground = Color(0xFFF9FAFB),
    surface = Color(0xFF0F1523),
    onSurface = Color(0xFFF9FAFB),
    surfaceVariant = Color(0xFF161F33),
    onSurfaceVariant = Color(0xFFA1ADC7),
    outline = Color(0xFFFFD700)
)

// 1. OLED Pure Black
val MidnightDarkColorScheme = darkColorScheme(
    primary = Color(0xFF00E5FF),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF0D1E2D),
    onPrimaryContainer = Color(0xFF00E5FF),
    secondary = Color(0xFFFF3366),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFFFFB300),
    onTertiary = Color(0xFF000000),
    background = Color(0xFF000000), // Pure OLED black
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF0A0F1A),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF131D2D),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF00E5FF)
)

// 2. Navy Blue Theme
val NavyColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF070D18),
    primaryContainer = Color(0xFF132845),
    onPrimaryContainer = Color(0xFFBAE6FD),
    secondary = Color(0xFF818CF8),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFF34D399),
    onTertiary = Color(0xFF070D18),
    background = Color(0xFF070D18),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF0F1B2E),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF182A45),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF38BDF8)
)

// 3. Cinema Crimson Theme
val CrimsonColorScheme = darkColorScheme(
    primary = Color(0xFFFF3366),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF3E121D),
    onPrimaryContainer = Color(0xFFFFD1DC),
    secondary = Color(0xFFFF8A00),
    onSecondary = Color(0xFF0E070A),
    tertiary = Color(0xFFFF4081),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFF0E070A),
    onBackground = Color(0xFFFFF0F5),
    surface = Color(0xFF1A0D14),
    onSurface = Color(0xFFFFF0F5),
    surfaceVariant = Color(0xFF28141F),
    onSurfaceVariant = Color(0xFFE2B4C3),
    outline = Color(0xFFFF3366)
)

// 4. Emerald Green Theme
val EmeraldColorScheme = darkColorScheme(
    primary = Color(0xFF00E676),
    onPrimary = Color(0xFF06110D),
    primaryContainer = Color(0xFF0D3322),
    onPrimaryContainer = Color(0xFFB9F6CA),
    secondary = Color(0xFF00B0FF),
    onSecondary = Color(0xFF06110D),
    tertiary = Color(0xFFFFD600),
    onTertiary = Color(0xFF06110D),
    background = Color(0xFF06110D),
    onBackground = Color(0xFFF0FDF4),
    surface = Color(0xFF0E2219),
    onSurface = Color(0xFFF0FDF4),
    surfaceVariant = Color(0xFF17382A),
    onSurfaceVariant = Color(0xFFA7D5BD),
    outline = Color(0xFF00E676)
)

// 5. Titanium Dark Gray ColorScheme
val TitaniumColorScheme = darkColorScheme(
    primary = Color(0xFFFFB300), // Amber
    onPrimary = Color(0xFF12141A),
    primaryContainer = Color(0xFF282C37),
    onPrimaryContainer = Color(0xFFFFD54F),
    secondary = Color(0xFF00E5FF),
    onSecondary = Color(0xFF12141A),
    tertiary = Color(0xFFFF5252),
    onTertiary = Color(0xFF12141A),
    background = Color(0xFF14171F),
    onBackground = Color(0xFFECEFF4),
    surface = Color(0xFF1C202B),
    onSurface = Color(0xFFECEFF4),
    surfaceVariant = Color(0xFF242B3A),
    onSurfaceVariant = Color(0xFFA0AABF),
    outline = Color(0xFFFFB300)
)

// 6. Light Modern ColorScheme
val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightSurface,
    primaryContainer = LightCardHover,
    onPrimaryContainer = LightPrimary,
    secondary = NeonRed,
    onSecondary = LightSurface,
    tertiary = NeonAmber,
    onTertiary = LightSurface,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightCard,
    onSurfaceVariant = LightTextSecondary,
    outline = LightOutline
)

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.LUXURY_GOLD,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val colorScheme: ColorScheme = when (themeMode) {
        AppThemeMode.LUXURY_GOLD -> LuxuryGoldColorScheme
        AppThemeMode.MIDNIGHT -> MidnightDarkColorScheme
        AppThemeMode.NAVY -> NavyColorScheme
        AppThemeMode.CRIMSON -> CrimsonColorScheme
        AppThemeMode.EMERALD -> EmeraldColorScheme
        AppThemeMode.TITANIUM -> TitaniumColorScheme
        AppThemeMode.LIGHT -> LightColorScheme
    }

    val isDark = themeMode != AppThemeMode.LIGHT

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !isDark
                insetsController.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
