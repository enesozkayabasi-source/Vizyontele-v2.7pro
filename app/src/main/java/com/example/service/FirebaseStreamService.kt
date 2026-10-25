package com.example.service

import android.util.Log
import com.example.model.MediaCollection
import com.example.model.MediaItem
import com.example.model.WatchHistoryItem
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseStreamService {

    private val tag = "FirebaseStreamService"

    private val db: FirebaseFirestore? by lazy {
        try {
            val app = FirebaseApp.getInstance()
            FirebaseFirestore.getInstance(app)
        } catch (_: Throwable) {
            Log.d(tag, "Firebase Firestore hazır değil, yerel ve internet akışları devrede.")
            null
        }
    }

    val isFirebaseAvailable: Boolean
        get() = db != null

    /**
     * Firestore'dan belirtilen koleksiyonu (canli_tv, filmler, diziler)
     * gerçek zamanlı (real-time snapshot listener) olarak dinler.
     */
    fun observeCollection(collection: MediaCollection): Flow<List<MediaItem>> = callbackFlow {
        val firestore = db
        if (firestore == null) {
            // Firebase hazır değilse varsayılan demo verilerini sun
            Log.d(tag, "Firebase hazır değil, doğrulanmış canlı akışlar kullanılıyor: ${collection.collectionName}")
            trySend(getFallbackData(collection))
            awaitClose { }
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = firestore.collection(collection.collectionName)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.d(tag, "${collection.collectionName} dinlenirken bilgi: ${error.message}")
                        // Hata durumunda boş liste yerine varsa lokal veriyi sağla
                        trySend(getFallbackData(collection))
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        val items = snapshot.documents.map { doc ->
                            MediaItem.fromFirestore(
                                id = doc.id,
                                data = doc.data,
                                collection = collection
                            )
                        }
                        trySend(items)
                    } else {
                        // Koleksiyon henüz boşsa demo verilerini göster
                        trySend(getFallbackData(collection))
                    }
                }
        } catch (e: Exception) {
            Log.e(tag, "Snapshot listener eklenirken hata: ${e.message}")
            trySend(getFallbackData(collection))
        }

        awaitClose {
            listener?.remove()
        }
    }

    /**
     * Firestore'a yeni bir yayın ekler.
     * Alanlar: baslik, kategori, resim_url, yayin_url
     */
    fun addMediaItem(
        collection: MediaCollection,
        item: MediaItem,
        onResult: (Boolean, String) -> Unit
    ) {
        val firestore = db
        if (firestore == null) {
            onResult(false, "Firebase bağlantısı kurulamadı. Lütfen yapılandırmayı kontrol edin.")
            return
        }

        firestore.collection(collection.collectionName)
            .add(item.toFirestoreMap())
            .addOnSuccessListener {
                onResult(true, "Yayın başarıyla '${collection.collectionName}' koleksiyonuna eklendi.")
            }
            .addOnFailureListener { e ->
                onResult(false, "Kayıt başarısız: ${e.localizedMessage}")
            }
    }

    /**
     * Firestore'dan bir yayını siler.
     */
    fun deleteMediaItem(
        collection: MediaCollection,
        id: String,
        onResult: (Boolean) -> Unit
    ) {
        val firestore = db ?: run {
            onResult(false)
            return
        }

        firestore.collection(collection.collectionName).document(id)
            .delete()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    /**
     * Firestore'a başlangıç için canli_tv, filmler ve diziler koleksiyonlarını
     * örnek verilerle otomatik doldurur.
     */
    fun seedSampleDataToFirestore(onProgress: (String) -> Unit, onComplete: (Boolean) -> Unit) {
        val firestore = db
        if (firestore == null) {
            onProgress("Firebase Firestore erişilemedi.")
            onComplete(false)
            return
        }

        val allItems = listOf(
            MediaCollection.CANLI_TV to getSampleLiveTv(),
            MediaCollection.HABER to InternetChannelService().getVerifiedNewsChannels(),
            MediaCollection.COCUK to InternetChannelService().getVerifiedKidsChannels()
        )

        var totalItems = allItems.sumOf { it.second.size }
        var uploadedCount = 0

        for ((collection, list) in allItems) {
            for (item in list) {
                firestore.collection(collection.collectionName)
                    .add(item.toFirestoreMap())
                    .addOnCompleteListener { task ->
                        uploadedCount++
                        onProgress("$uploadedCount / $totalItems yayın yüklendi (${item.baslik})")
                        if (uploadedCount >= totalItems) {
                            onComplete(true)
                        }
                    }
            }
        }
    }

    /**
     * Koleksiyon bazında hazır test ve demo akışları
     */
    fun getFallbackData(collection: MediaCollection): List<MediaItem> {
        return when (collection) {
            MediaCollection.CANLI_TV -> getSampleLiveTv()
            MediaCollection.HABER -> InternetChannelService().getVerifiedNewsChannels()
            MediaCollection.COCUK -> InternetChannelService().getVerifiedKidsChannels()
            MediaCollection.SPOR -> InternetChannelService().getTkgsFullChannelList().filter { it.kategori == "Spor" }
            MediaCollection.RADYO -> InternetChannelService().getRadioStationList()
        }
    }

    private fun getSampleLiveTv(): List<MediaItem> {
        return InternetChannelService().getTkgsFullChannelList()
    }

    private fun getSampleMovies(): List<MediaItem> {
        return emptyList()
    }

    private fun getSampleSeries(): List<MediaItem> {
        return emptyList()
    }

    /* =========================================================================
     * İZLEME GEÇMİŞİ (WATCH HISTORY & CONTINUE WATCHING)
     * ========================================================================= */
    companion object {
        const val COLLECTION_WATCH_HISTORY = "izleme_gecmisi"
    }

    private val localWatchHistory = mutableListOf(
        WatchHistoryItem(
            id = "hist_live_trt1",
            mediaId = "live_01",
            baslik = "01 • TRT 1 HD",
            kategori = "Ulusal",
            resim_url = InternetChannelService().getOfficialWebLogo("trt 1"),
            yayin_url = "https://tv-trt1.medya.trt.com.tr/master.m3u8",
            collectionType = MediaCollection.CANLI_TV,
            lastPositionMs = 0L,
            durationMs = 0L,
            timestamp = System.currentTimeMillis() - 1800000L
        ),
        WatchHistoryItem(
            id = "hist_sport_aspor",
            mediaId = "live_07",
            baslik = "07 • A Spor HD",
            kategori = "Spor",
            resim_url = InternetChannelService().getOfficialWebLogo("a spor"),
            yayin_url = "https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/aspor/aspor.m3u8",
            collectionType = MediaCollection.CANLI_TV,
            lastPositionMs = 0L,
            durationMs = 0L,
            timestamp = System.currentTimeMillis() - 7200000L
        )
    )

    /**
     * Firestore "izleme_gecmisi" koleksiyonunu gerçek zamanlı (Flow) dinler.
     */
    fun observeWatchHistory(): Flow<List<WatchHistoryItem>> = callbackFlow {
        val firestore = db
        if (firestore == null) {
            trySend(localWatchHistory.toList())
            awaitClose { }
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = firestore.collection(COLLECTION_WATCH_HISTORY)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "izleme_gecmisi dinlenirken hata: ${error.message}")
                        trySend(localWatchHistory.toList())
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        val items = snapshot.documents.map { doc ->
                            WatchHistoryItem.fromFirestore(doc.id, doc.data)
                        }
                        trySend(items)
                    } else {
                        trySend(localWatchHistory.toList())
                    }
                }
        } catch (e: Exception) {
            Log.w(tag, "Watch history listener hatası: ${e.message}")
            trySend(localWatchHistory.toList())
        }

        awaitClose { listener?.remove() }
    }

    /**
     * Kullanıcı bir yayını izlerken veya durdurduğunda kaldığı yeri Firestore'a kaydeder.
     */
    fun saveWatchProgress(item: MediaItem, positionMs: Long, durationMs: Long) {
        if (positionMs <= 1000L) return // İlk 1 saniye ise kaydetmeye gerek yok

        val historyItem = WatchHistoryItem(
            id = item.id.ifBlank { "media_${item.baslik.hashCode()}" },
            mediaId = item.id,
            baslik = item.baslik,
            kategori = item.kategori,
            resim_url = item.resim_url,
            yayin_url = item.yayin_url,
            collectionType = item.collectionType,
            lastPositionMs = positionMs,
            durationMs = durationMs,
            timestamp = System.currentTimeMillis()
        )

        // Lokal belleği güncelle
        localWatchHistory.removeAll { it.mediaId == item.id || it.id == item.id }
        localWatchHistory.add(0, historyItem)

        // Firestore'a kaydet (Varsa güncelle, yoksa ekle)
        val firestore = db ?: return
        try {
            val docId = item.id.ifBlank { "media_${item.baslik.hashCode()}" }
            firestore.collection(COLLECTION_WATCH_HISTORY)
                .document(docId)
                .set(historyItem.toFirestoreMap(), com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(tag, "İzleme geçmişi Firestore'a kaydedildi: ${item.baslik} at $positionMs ms")
                }
                .addOnFailureListener { e ->
                    Log.d(tag, "Firestore izleme geçmişi kaydedilemedi: ${e.message}")
                }
        } catch (e: Exception) {
            Log.w(tag, "Firestore izleme geçmişi kaydedilemedi: ${e.message}")
        }
    }

    /**
     * İzleme geçmişinden bir öğeyi siler.
     */
    fun removeWatchHistoryItem(id: String, onResult: (Boolean) -> Unit = {}) {
        localWatchHistory.removeAll { it.id == id || it.mediaId == id }
        val firestore = db ?: run {
            onResult(true)
            return
        }
        firestore.collection(COLLECTION_WATCH_HISTORY).document(id)
            .delete()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}
