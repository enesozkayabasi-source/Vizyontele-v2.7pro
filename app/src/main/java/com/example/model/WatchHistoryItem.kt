package com.example.model

/**
 * Firestore "izleme_gecmisi" koleksiyonunda saklanan izleme ilerlemesi nesnesi.
 */
data class WatchHistoryItem(
    val id: String = "",
    val mediaId: String = "",
    val baslik: String = "",
    val kategori: String = "",
    val resim_url: String = "",
    val yayin_url: String = "",
    val collectionType: MediaCollection = MediaCollection.CANLI_TV,
    val lastPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
) {
    val progressPercent: Float
        get() = if (durationMs > 0L) (lastPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    val formattedProgress: String
        get() {
            val currentMins = (lastPositionMs / 1000) / 60
            val currentSecs = (lastPositionMs / 1000) % 60
            val totalMins = (durationMs / 1000) / 60
            val totalSecs = (durationMs / 1000) % 60
            return if (durationMs > 0L) {
                String.format("%02d:%02d / %02d:%02d", currentMins, currentSecs, totalMins, totalSecs)
            } else {
                String.format("%02d:%02d izlendi", currentMins, currentSecs)
            }
        }

    fun toMediaItem(): MediaItem {
        return MediaItem(
            id = mediaId.ifBlank { id },
            baslik = baslik,
            kategori = kategori,
            resim_url = resim_url,
            yayin_url = yayin_url,
            collectionType = collectionType
        )
    }

    fun toFirestoreMap(): Map<String, Any> {
        return mapOf(
            "mediaId" to mediaId,
            "baslik" to baslik,
            "kategori" to kategori,
            "resim_url" to resim_url,
            "yayin_url" to yayin_url,
            "collectionType" to collectionType.collectionName,
            "lastPositionMs" to lastPositionMs,
            "durationMs" to durationMs,
            "timestamp" to timestamp
        )
    }

    companion object {
        fun fromFirestore(docId: String, data: Map<String, Any>?): WatchHistoryItem {
            val colName = (data?.get("collectionType") as? String) ?: "canli_tv"
            return WatchHistoryItem(
                id = docId,
                mediaId = (data?.get("mediaId") as? String) ?: docId,
                baslik = (data?.get("baslik") as? String) ?: "İsimsiz Yayın",
                kategori = (data?.get("kategori") as? String) ?: "Genel",
                resim_url = (data?.get("resim_url") as? String) ?: "",
                yayin_url = (data?.get("yayin_url") as? String) ?: "",
                collectionType = MediaCollection.fromKey(colName),
                lastPositionMs = (data?.get("lastPositionMs") as? Number)?.toLong() ?: 0L,
                durationMs = (data?.get("durationMs") as? Number)?.toLong() ?: 0L,
                timestamp = (data?.get("timestamp") as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
