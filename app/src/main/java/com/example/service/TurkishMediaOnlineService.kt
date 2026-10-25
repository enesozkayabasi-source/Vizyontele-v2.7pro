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
        getVerifiedNewsChannels()
    }

    suspend fun fetchKidsChannels(): List<MediaItem> = withContext(Dispatchers.IO) {
        getVerifiedKidsChannels()
    }

    // Deprecated compatibility methods
    fun getVerifiedTurkishMovies(): List<MediaItem> = emptyList()
    fun getVerifiedTurkishSeries(): List<MediaItem> = emptyList()
    suspend fun fetchTurkishMovies(): List<MediaItem> = emptyList()
    suspend fun fetchTurkishSeries(): List<MediaItem> = emptyList()
}
