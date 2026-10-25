package com.example.service

import android.content.Context
import android.content.SharedPreferences
import com.example.ui.i18n.AppLanguage
import com.example.ui.theme.AppLayoutMode
import com.example.ui.theme.AppThemeMode
import com.example.ui.viewmodel.MainNavigationTab

/**
 * Kullanıcı tercihlerini (Seçilen tema, dil, yerleşim modu, favoriler)
 * SharedPreferences üzerinde kalıcı olarak saklayan ve yükleyen yönetici sınıfı.
 * Kullanıcı uygulamadan çıkıp girince tercihler aynı şekilde korunur.
 */
class UserPreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("vizyontele_user_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME_MODE = "pref_theme_mode"
        private const val KEY_LAYOUT_MODE = "pref_layout_mode"
        private const val KEY_LANGUAGE = "pref_language"
        private const val KEY_FAVORITES = "pref_favorites"
        private const val KEY_LAST_TAB = "pref_last_tab"
        private const val KEY_APP_VERSION = "pref_app_version"
        const val LATEST_RELEASE_VERSION = "v2.9.0"
        const val PREVIOUS_VERSION_BASELINE = "v2.8.5"
        const val KEY_CUSTOM_GITHUB_URL = "pref_custom_github_url"
        const val KEY_CUSTOM_APK_URL = "pref_custom_apk_url"
        const val DEFAULT_GITHUB_PAGE_URL = "https://github.com/vizyontv/vizyontv-app/releases"
        const val DEFAULT_APK_DOWNLOAD_URL = "https://github.com/vizyontv/vizyontv-app/releases/latest/download/vizyontv.apk"
        const val OFFICIAL_UPDATE_URL = DEFAULT_APK_DOWNLOAD_URL
    }

    fun getGithubPageUrl(): String {
        return prefs.getString(KEY_CUSTOM_GITHUB_URL, DEFAULT_GITHUB_PAGE_URL) ?: DEFAULT_GITHUB_PAGE_URL
    }

    fun saveGithubPageUrl(url: String) {
        prefs.edit().putString(KEY_CUSTOM_GITHUB_URL, url.trim()).apply()
    }

    fun getApkDownloadUrl(): String {
        return prefs.getString(KEY_CUSTOM_APK_URL, DEFAULT_APK_DOWNLOAD_URL) ?: DEFAULT_APK_DOWNLOAD_URL
    }

    fun saveApkDownloadUrl(url: String) {
        prefs.edit().putString(KEY_CUSTOM_APK_URL, url.trim()).apply()
    }

    fun getInstalledVersion(): String {
        return prefs.getString(KEY_APP_VERSION, PREVIOUS_VERSION_BASELINE) ?: PREVIOUS_VERSION_BASELINE
    }

    fun saveInstalledVersion(version: String) {
        prefs.edit().putString(KEY_APP_VERSION, version).apply()
    }

    fun hasPendingUpdate(): Boolean {
        return getInstalledVersion() != LATEST_RELEASE_VERSION
    }

    fun saveThemeMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    fun getThemeMode(): AppThemeMode {
        val name = prefs.getString(KEY_THEME_MODE, AppThemeMode.LUXURY_GOLD.name) ?: AppThemeMode.LUXURY_GOLD.name
        return try {
            AppThemeMode.valueOf(name)
        } catch (_: Exception) {
            AppThemeMode.LUXURY_GOLD
        }
    }

    fun saveLayoutMode(mode: AppLayoutMode) {
        prefs.edit().putString(KEY_LAYOUT_MODE, mode.name).apply()
    }

    fun getLayoutMode(): AppLayoutMode {
        val name = prefs.getString(KEY_LAYOUT_MODE, AppLayoutMode.AUTO.name) ?: AppLayoutMode.AUTO.name
        return try {
            AppLayoutMode.valueOf(name)
        } catch (_: Exception) {
            AppLayoutMode.AUTO
        }
    }

    fun saveLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
    }

    fun getLanguage(): AppLanguage {
        val code = prefs.getString(KEY_LANGUAGE, "tr") ?: "tr"
        return AppLanguage.entries.firstOrNull { it.code == code } ?: AppLanguage.TR
    }

    fun saveFavorites(favorites: Set<String>) {
        prefs.edit().putStringSet(KEY_FAVORITES, favorites).apply()
    }

    fun getFavorites(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun saveLastTab(tab: MainNavigationTab) {
        prefs.edit().putString(KEY_LAST_TAB, tab.name).apply()
    }

    fun getLastTab(): MainNavigationTab {
        val name = prefs.getString(KEY_LAST_TAB, MainNavigationTab.CANLI_TV.name) ?: MainNavigationTab.CANLI_TV.name
        return try {
            MainNavigationTab.valueOf(name)
        } catch (_: Exception) {
            MainNavigationTab.CANLI_TV
        }
    }

    fun isDataSaverEnabled(): Boolean {
        return prefs.getBoolean("pref_data_saver", false)
    }

    fun saveDataSaverEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("pref_data_saver", enabled).apply()
    }
}
