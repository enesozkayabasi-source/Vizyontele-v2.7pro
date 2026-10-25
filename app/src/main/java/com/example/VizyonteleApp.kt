package com.example

import android.app.Application
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class VizyonteleApp : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()

        // Global crash guard for background threads
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("VizyonteleApp", "Yakalanamayan hata engellendi (${thread.name}): ${throwable.localizedMessage}", throwable)
            // If it's a critical main thread crash, delegate to default handler
            if (thread == android.os.Looper.getMainLooper().thread) {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }

        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId(packageName)
                    .setProjectId("vizyontele-stream")
                    .setApiKey("AIzaSyDummyKeyForVizyonteleStreamBox0")
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.d("VizyonteleApp", "FirebaseApp güvenli modda başlatıldı.")
            }
        } catch (e: Throwable) {
            Log.d("VizyonteleApp", "Firebase başlatma bilgisi: ${e.message}")
        }
    }

    override fun newImageLoader(): ImageLoader {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36 VizyonTV/1.0 (contact: enesozkayabasi@gmail.com)")
                    .build()
                chain.proceed(request)
            }
            .build()

        return ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .components {
                add(SvgDecoder.Factory())
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.30)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.08)
                    .build()
            }
            .crossfade(false)
            .respectCacheHeaders(false)
            .build()
    }
}
