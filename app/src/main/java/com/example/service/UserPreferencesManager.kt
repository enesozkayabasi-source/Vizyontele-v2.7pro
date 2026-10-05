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
    }

    fun saveThemeMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    fun getThemeMode(): AppThemeMode {
        val name = prefs.getString(KEY_THEME_MODE, AppThemeMode.MIDNIGHT.name) ?: AppThemeMode.MIDNIGHT.name
        return try {
            AppThemeMode.valueOf(name)
        } catch (_: Exception) {
            AppThemeMode.MIDNIGHT
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
}
