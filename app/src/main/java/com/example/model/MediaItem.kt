package com.example.model

/**
 * Dizi Bölümü Bilgisi (Sezon ve Bölüm Listelemesi)
 */
data class EpisodeItem(
    val episodeNumber: Int = 1,
    val seasonNumber: Int = 1,
    val title: String = "",
    val description: String = "",
    val duration: String = "45 dk",
    val thumbnailUrl: String = "",
    val streamUrl: String = ""
) {
    val displayCode: String
        get() = "S${seasonNumber.toString().padStart(2, '0')}E${episodeNumber.toString().padStart(2, '0')}"

    val displayTitle: String
        get() = if (title.isNotBlank()) "${displayCode} • $title" else "${displayCode} • ${episodeNumber}. Bölüm"
}

/**
 * Dizi Sezonu Bilgisi
 */
data class SeasonItem(
    val seasonNumber: Int = 1,
    val title: String = "${seasonNumber}. Sezon",
    val episodes: List<EpisodeItem> = emptyList()
)

/**
 * Firestore'da ve internet akışlarında saklanan yayın nesnesi.
 * - Koleksiyonlar: canli_tv, filmler, diziler
 * - Alanlar: baslik, kategori, resim_url, yayin_url, seasons, description, year, imdbRating
 */
data class MediaItem(
    val id: String = "",
    val baslik: String = "",
    val kategori: String = "",
    val resim_url: String = "",
    val yayin_url: String = "",
    val backup_urls: List<String> = emptyList(),
    val collectionType: MediaCollection = MediaCollection.CANLI_TV,
    val isFavorite: Boolean = false,
    val description: String = "",
    val year: String = "",
    val imdbRating: String = "",
    val cast: String = "",
    val seasons: List<SeasonItem> = emptyList(),
    val currentSeasonNumber: Int = 1,
    val currentEpisodeNumber: Int = 1
) {
    fun toFirestoreMap(): Map<String, Any> {
        return mapOf(
            "baslik" to baslik,
            "kategori" to kategori,
            "resim_url" to resim_url,
            "yayin_url" to yayin_url,
            "description" to description,
            "year" to year,
            "imdbRating" to imdbRating
        )
    }

    val isHlsStream: Boolean
        get() = yayin_url.contains(".m3u8", ignoreCase = true)

    val isSeries: Boolean
        get() = seasons.isNotEmpty()

    val totalSeasons: Int
        get() = seasons.size

    val totalEpisodes: Int
        get() = seasons.sumOf { it.episodes.size }

    fun getAllStreamUrls(): List<String> {
        val primary = getActiveStreamUrl()
        val all = mutableListOf<String>()
        if (primary.isNotBlank()) all.add(primary)
        all.addAll(backup_urls.filter { it.isNotBlank() && it != primary })
        return all.distinct()
    }

    fun getActiveStreamUrl(): String {
        if (isSeries && seasons.isNotEmpty()) {
            val season = seasons.find { it.seasonNumber == currentSeasonNumber } ?: seasons.firstOrNull()
            val ep = season?.episodes?.find { it.episodeNumber == currentEpisodeNumber } ?: season?.episodes?.firstOrNull()
            if (!ep?.streamUrl.isNullOrBlank()) {
                return ep!!.streamUrl
            }
        }
        return yayin_url
    }

    fun getCurrentEpisode(): EpisodeItem? {
        val season = seasons.find { it.seasonNumber == currentSeasonNumber } ?: seasons.firstOrNull()
        return season?.episodes?.find { it.episodeNumber == currentEpisodeNumber } ?: season?.episodes?.firstOrNull()
    }

    companion object {
        fun fromFirestore(id: String, data: Map<String, Any>?, collection: MediaCollection): MediaItem {
            return MediaItem(
                id = id,
                baslik = (data?.get("baslik") as? String) ?: "İsimsiz Yayın",
                kategori = (data?.get("kategori") as? String) ?: "Genel",
                resim_url = (data?.get("resim_url") as? String) ?: "",
                yayin_url = (data?.get("yayin_url") as? String) ?: "",
                description = (data?.get("description") as? String) ?: "",
                year = (data?.get("year") as? String) ?: "",
                imdbRating = (data?.get("imdbRating") as? String) ?: "",
                collectionType = collection
            )
        }
    }
}

enum class MediaCollection(
    val collectionName: String,
    val title: String,
    val description: String
) {
    CANLI_TV("canli_tv", "Canlı TV", "Canlı genel ve ulusal TV kanalları"),
    HABER("haber", "Haber", "Canlı haber, ekonomi ve gündem kanalları"),
    COCUK("cocuk", "Çocuk", "Çocuk ve çizgi film kanalları"),
    SPOR("spor", "Spor", "Canlı spor yayınları"),
    RADYO("radyo", "Radyo", "Canlı radyo istasyonları");

    companion object {
        fun fromKey(key: String): MediaCollection {
            return entries.firstOrNull { it.collectionName == key } ?: CANLI_TV
        }
    }
}
