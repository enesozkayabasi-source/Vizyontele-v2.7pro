package com.example.service

import android.util.Log
import com.example.model.MediaCollection
import com.example.model.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * İnternetten ve doğrulanmış yayın kaynaklarından Türkçe Haber ve Çocuk kanallarını çeken servis.
 * Film ve Dizi alanlarının yerine eklenen iki bağımsız kategoriyi yönetir.
 */
class TurkishMediaOnlineService(
    private val internetService: InternetChannelService = InternetChannelService()
) {

    private val tag = "TurkishMediaService"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    fun getVerifiedNewsChannels(): List<MediaItem> {
        return internetService.getVerifiedNewsChannels()
    }

    fun getVerifiedKidsChannels(): List<MediaItem> {
        return internetService.getVerifiedKidsChannels()
    }

    suspend fun fetchNewsChannels(): List<MediaItem> = withContext(Dispatchers.IO) {
        val verified = getVerifiedNewsChannels()
        try {
            val request = Request.Builder()
                .url("https://iptv-org.github.io/iptv/categories/news.m3u")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val parsed = internetService.parseRawM3uContent(body)
                            .filter { it.baslik.lowercase().contains("tr") || it.kategori.equals("Haber", ignoreCase = true) }
                            .map { item ->
                                val logo = internetService.getOfficialWebLogo(item.baslik)
                                item.copy(
                                    collectionType = MediaCollection.HABER,
                                    resim_url = if (logo.isNotBlank() && !logo.contains("unsplash")) logo else item.resim_url
                                )
                            }
                        if (parsed.isNotEmpty()) {
                            return@withContext (verified + parsed).distinctBy { it.baslik.lowercase().trim() }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Haber kanalları internet güncelleme uyarısı: ${e.message}")
        }
        verified
    }

    suspend fun fetchKidsChannels(): List<MediaItem> = withContext(Dispatchers.IO) {
        val verified = getVerifiedKidsChannels()
        try {
            val request = Request.Builder()
                .url("https://iptv-org.github.io/iptv/categories/kids.m3u")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val parsed = internetService.parseRawM3uContent(body)
                            .filter { it.baslik.lowercase().contains("tr") || it.kategori.equals("Çocuk", ignoreCase = true) }
                            .map { item ->
                                val logo = internetService.getOfficialWebLogo(item.baslik)
                                item.copy(
                                    collectionType = MediaCollection.COCUK,
                                    resim_url = if (logo.isNotBlank() && !logo.contains("unsplash")) logo else item.resim_url
                                )
                            }
                        if (parsed.isNotEmpty()) {
                            return@withContext (verified + parsed).distinctBy { it.baslik.lowercase().trim() }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Çocuk kanalları internet güncelleme uyarısı: ${e.message}")
        }
        verified
    }

    // Deprecated compatibility methods
    fun getVerifiedTurkishMovies(): List<MediaItem> = emptyList()
    fun getVerifiedTurkishSeries(): List<MediaItem> = emptyList()
    suspend fun fetchTurkishMovies(): List<MediaItem> = emptyList()
    suspend fun fetchTurkishSeries(): List<MediaItem> = emptyList()
}
