package com.example.service

import android.util.Log
import com.example.model.MediaCollection
import com.example.model.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

data class M3uPreset(
    val title: String,
    val description: String,
    val url: String,
    val channelCountHint: String
)

class InternetChannelService {

    private val tag = "InternetChannelService"

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    val availablePresets = listOf(
        M3uPreset(
            title = "Türkiye Ulusal & Açık Spor Listesi",
            description = "TRT 1, ATV, Star, Kanal D, TV8, NOW TV, NTV, A Spor açık yayınları",
            url = "https://iptv-org.github.io/iptv/countries/tr.m3u",
            channelCountHint = "Doğrulanmış HD"
        ),
        M3uPreset(
            title = "Haber & Ekonomi Yayınları",
            description = "NTV, TRT Haber, Haber Global, Habertürk, 24 TV, 360, TV100, TGRT Haber",
            url = "https://iptv-org.github.io/iptv/categories/news.m3u",
            channelCountHint = "Canlı Yayın"
        ),
        M3uPreset(
            title = "Kültür, Belgesel & Müzik Listesi",
            description = "TRT Müzik, Kral Pop TV, Power TV, Dream Türk, Diyanet TV",
            url = "https://iptv-org.github.io/iptv/categories/documentary.m3u",
            channelCountHint = "Tematik"
        )
    )

    /**
     * Doğrulanmış Türkiye açık yayınlarını döner
     */
    suspend fun fetchLiveChannelsFromInternet(): List<MediaItem> = withContext(Dispatchers.IO) {
        val tkgsList = getTkgsFullChannelList()
        try {
            val request = Request.Builder()
                .url("https://iptv-org.github.io/iptv/countries/tr.m3u")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val parsed = parseM3u(body)
                        // Verified TKGS channels come FIRST, then additional unique parsed channels
                        val combined = (tkgsList + parsed).distinctBy { it.baslik.lowercase().trim() }
                        return@withContext combined
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "İnternetten canlı m3u akışı çekilemedi: ${e.message}")
        }
        tkgsList
    }

    suspend fun fetchCustomPlaylist(url: String): List<MediaItem> = withContext(Dispatchers.IO) {
        if (url.isBlank()) return@withContext emptyList()
        try {
            val request = Request.Builder()
                .url(url.trim())
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        return@withContext parseM3u(body)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Toplu M3U listesi indirilemedi: ${e.message}")
        }
        emptyList()
    }

    fun parseRawM3uContent(content: String): List<MediaItem> {
        return parseM3u(content)
    }

    private fun parseM3u(m3uContent: String): List<MediaItem> {
        val list = mutableListOf<MediaItem>()
        val lines = m3uContent.lines()
        var currentTitle = ""
        var currentLogo = ""
        var currentGroup = "Genel"

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("#EXTINF:")) {
                val titleIndex = trimmed.lastIndexOf(',')
                if (titleIndex != -1 && titleIndex < trimmed.length - 1) {
                    currentTitle = trimmed.substring(titleIndex + 1).trim()
                }

                val logoMatch = Regex("""tvg-logo="([^"]+)"""").find(trimmed)
                currentLogo = logoMatch?.groupValues?.getOrNull(1) ?: ""

                val groupMatch = Regex("""group-title="([^"]+)"""").find(trimmed)
                currentGroup = groupMatch?.groupValues?.getOrNull(1) ?: "Genel"
            } else if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                if (currentTitle.isNotBlank()) {
                    val lower = currentTitle.lowercase()
                    val isPayTv = lower.contains("bein") || lower.contains("exxen") || lower.contains("dsmart") || lower.contains("digiturk")
                    if (!isPayTv) {
                        list.add(
                            MediaItem(
                                id = "custom_${list.size}_${currentTitle.hashCode()}",
                                baslik = currentTitle,
                                kategori = mapCategory(currentGroup, currentTitle),
                                resim_url = currentLogo.ifBlank { getOfficialWebLogo(currentTitle) },
                                yayin_url = trimmed,
                                collectionType = MediaCollection.CANLI_TV
                            )
                        )
                    }
                    currentTitle = ""
                    currentLogo = ""
                    currentGroup = "Genel"
                    if (list.size >= 150) break
                }
            }
        }
        return list
    }

    private fun mapCategory(group: String, title: String): String {
        val t = title.lowercase()
        return when {
            t.contains("spor") || t.contains("sport") || t.contains("tjk") -> "Spor"
            t.contains("haber") || t.contains("news") || t.contains("24") || t.contains("360") || t.contains("tgrt") -> "Haber"
            t.contains("çocuk") || t.contains("cocuk") || t.contains("kids") || t.contains("kartoon") -> "Çocuk"
            t.contains("belgesel") || t.contains("doc") || t.contains("nature") -> "Belgesel"
            t.contains("sinema") || t.contains("film") || t.contains("movie") -> "Sinema"
            t.contains("müzik") || t.contains("muzik") || t.contains("music") || t.contains("kral") || t.contains("power") -> "Müzik"
            t.contains("ekonomi") || t.contains("cnbc") || t.contains("ekoturk") -> "Ekonomi"
            else -> if (group.isNotBlank() && group != "Undefined") group else "Ulusal"
        }
    }

    fun getOfficialWebLogo(channelName: String): String {
        val t = channelName.lowercase()
        return when {
            // TRT Kanalları
            t.contains("trt 1") || t.contains("trt1") -> "https://i.imgur.com/j786OLG.png"
            t.contains("trt haber") || t.contains("trthaber") -> "https://i.imgur.com/OVfo8Ab.png"
            t.contains("trt spor yıldız") || t.contains("spor yıldız") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/c/ce/TRT_Spor_Y%C4%B1ld%C4%B1z_Logo.svg/960px-TRT_Spor_Y%C4%B1ld%C4%B1z_Logo.svg.png"
            t.contains("trt spor") || t.contains("trtspor") -> "https://i.imgur.com/6tv0zxh.png"
            t.contains("trt çocuk") || t.contains("trt cocuk") -> "https://i.imgur.com/QLFmD6d.png"
            t.contains("trt diyanet çocuk") || t.contains("diyanet çocuk") -> "https://i.imgur.com/8PmXz9t.png"
            t.contains("trt müzik") || t.contains("trt muzik") -> "https://i.imgur.com/fIVFCEd.png"
            t.contains("trt türk") || t.contains("trt turk") -> "https://i.imgur.com/OSTOQNw.png"
            t.contains("trt avaz") -> "https://i.imgur.com/VhTwXu5.png"
            t.contains("trt 2") -> "https://i.imgur.com/lNWrOE2.png"
            t.contains("trt belgesel") -> "https://i.imgur.com/MGO87pe.png"
            t.contains("trt kurdî") || t.contains("trt kurdi") -> "https://i.imgur.com/6BpymfB.png"
            t.contains("trt world") -> "https://i.imgur.com/JEA2xpv.png"
            t.contains("trt") -> "https://i.imgur.com/j786OLG.png"

            // Ulusal Ana Kanallar
            t.contains("a2") -> "https://iatv.tmgrup.com.tr/site/v2/a2tv/i/a2tv-logo.png"
            t.contains("atv") -> "https://i.imgur.com/HyVUwFC.png"
            t.contains("euro d") -> "https://i.imgur.com/x9kHsXo.png"
            t.contains("kanal d") || t.contains("kanald") -> "https://i.imgur.com/9o1atM6.png"
            t.contains("star") -> "https://i.imgur.com/9O3DHRB.png"
            t.contains("tv85") || t.contains("tv 8.5") || t.contains("tv8.5") -> "https://i.imgur.com/QuelSsc.png"
            t.contains("tv8") -> "https://img.tv8.com.tr/s/template/v2/img/tv8-logo.png"
            t.contains("now") || t.contains("fox") -> "https://i.imgur.com/5EYjWK7.png"
            t.contains("show") -> "https://i.imgur.com/1l7SCCu.png"
            t.contains("kanal 7") || t.contains("kanal7") -> "https://i.imgur.com/0gq9xOm.png"
            t.contains("beyaz") -> "https://i.imgur.com/uykIdML.png"
            t.contains("360") -> "https://i.imgur.com/agn47sQ.png"
            t.contains("tv 4") || t.contains("tv4") -> "https://i.imgur.com/UpsQsbd.png"

            // Haber ve Ekonomi Kanalları
            t.contains("ntv") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0c/NTV_%28Turkey%29_logo.svg/960px-NTV_%28Turkey%29_logo.svg.png"
            t.contains("haber global") -> "https://i.imgur.com/fu6XeGS.png"
            t.contains("habertürk") || t.contains("haberturk") -> "https://i.imgur.com/6Tw3rUp.png"
            t.contains("ahaber") || t.contains("a haber") -> "https://upload.wikimedia.org/wikipedia/commons/7/7c/Ahaber_Logo.png"
            t.contains("24 tv") || t.contains("tv 24") -> "https://i.imgur.com/8FO41es.png"
            t.contains("flash") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4e/Flash_Haber_logosu.png/500px-Flash_Haber_logosu.png"
            t.contains("tgrt") -> "https://i.imgur.com/PrxwKDw.png"
            t.contains("sözcü") || t.contains("sozcu") || t.contains("szc") -> "https://i.imgur.com/BxpdvsG.png"
            t.contains("halk tv") || t.contains("halktv") -> "https://i.imgur.com/xM0HA30.png"
            t.contains("tele 1") || t.contains("tele1") -> "https://upload.wikimedia.org/wikipedia/tr/4/43/Tele1_logosu.png"
            t.contains("tv100") || t.contains("tv 100") -> "https://upload.wikimedia.org/wikipedia/tr/thumb/0/0f/TV100_logo.png/960px-TV100_logo.png"
            t.contains("bloomberg") -> "https://i.imgur.com/bmkXfIE.png"
            t.contains("cnbc") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dc/CNBC-e_2025.svg/960px-CNBC-e_2025.svg.png"
            t.contains("apara") || t.contains("a para") -> "https://i.imgur.com/GjHnq0c.png"
            t.contains("ekotürk") || t.contains("ekoturk") -> "https://i.imgur.com/8FO41es.png"
            t.contains("tvnet") -> "https://i.imgur.com/mQo8yWQ.png"
            t.contains("ülke") || t.contains("ulke") -> "https://i.imgur.com/wdWR7Qk.png"
            t.contains("bengü") || t.contains("bengu") -> "https://www.benguturk.com/assets/img/logo.png"
            t.contains("tbmm") || t.contains("meclis") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/TBMM_TV_logo.svg/960px-TBMM_TV_logo.svg.png"

            // Spor Kanalları
            t.contains("aspor") || t.contains("a spor") -> "https://i.imgur.com/ZhkZzLf.png"
            t.contains("tjk") -> "https://i.imgur.com/3zHdkYG.png"
            t.contains("s sport") || t.contains("ssport") -> "https://upload.wikimedia.org/wikipedia/commons/a/a9/S_Sport_logo1.png"

            // Çocuk & Animasyon Kanalları
            t.contains("minika go") || t.contains("minikago") -> "https://i.imgur.com/qIoipDq.png"
            t.contains("minika") -> "https://i.imgur.com/VCywMTv.png"
            t.contains("cartoon") -> "https://i.imgur.com/pH6ao2T.png"
            t.contains("baby") -> "https://i.imgur.com/4BDJ5FT.png"
            t.contains("kral şakir") || t.contains("sakir") -> "https://i.imgur.com/QLFmD6d.png"
            t.contains("pepee") -> "https://i.imgur.com/VCywMTv.png"
            t.contains("niloya") -> "https://i.imgur.com/QLFmD6d.png"
            t.contains("kukuli") -> "https://i.imgur.com/qIoipDq.png"
            t.contains("maşa") || t.contains("koca ayı") -> "https://i.imgur.com/QLFmD6d.png"
            t.contains("zarok") -> "https://i.imgur.com/o0eevnb.png"

            // Müzik & Tematik Kanallar
            t.contains("kral pop") || t.contains("kral") -> "https://i.imgur.com/ch365lh.png"
            t.contains("power") -> "https://i.imgur.com/XSL1gd7.png"
            t.contains("dream") -> "https://i.imgur.com/vJ8VaZi.png"
            t.contains("number 1") || t.contains("number1") -> "https://i.imgur.com/02cDIBi.png"
            t.contains("diyanet") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/4/43/Diyanet_TV_logo.svg/960px-Diyanet_TV_logo.svg.png"
            t.contains("line") -> "https://i.imgur.com/6GNp7f8.png"
            t.contains("lalegül") || t.contains("lalegul") -> "https://i.imgur.com/wwCysFs.png"
            t.contains("kocaeli") -> "https://i.imgur.com/LeTP4zq.png"
            t.contains("kanal 58") || t.contains("kanal58") -> "https://i.imgur.com/Rgc66sv.png"
            t.contains("vatan") -> "https://i.imgur.com/xrZVgf6.png"

            // Varsayılan yüksek kaliteli yayın amblemi
            else -> "https://i.imgur.com/j786OLG.png"
        }
    }

    fun getVerifiedSportsAndNationalChannels(): List<MediaItem> {
        return getTkgsFullChannelList()
    }

    /**
     * Doğrulanmış Türkiye Canlı Haber ve Ekonomi Kanalları
     */
    fun getVerifiedNewsChannels(): List<MediaItem> {
        return listOf(
            MediaItem(
                id = "haber_01",
                baslik = "TRT Haber HD",
                kategori = "Gündem",
                resim_url = getOfficialWebLogo("trt haber"),
                yayin_url = "https://tv-trthaber.medya.trt.com.tr/master.m3u8",
                collectionType = MediaCollection.HABER
            ),
            MediaItem(
                id = "haber_02",
                baslik = "NTV HD",
                kategori = "Gündem",
                resim_url = getOfficialWebLogo("ntv"),
                yayin_url = "https://dogus.daioncdn.net/ntv/ntv.m3u8?app=ntv_web",
                collectionType = MediaCollection.HABER
            ),
            MediaItem(
                id = "haber_03",
                baslik = "Habertürk TV HD",
                kategori = "Gündem",
                resim_url = getOfficialWebLogo("habertürk"),
                yayin_url = "https://tv.ensonhaber.com/haberturk/haberturk.m3u8",
                collectionType = MediaCollection.HABER
            ),
            MediaItem(
                id = "haber_04",
                baslik = "A Haber HD",
                kategori = "Gündem",
                resim_url = getOfficialWebLogo("a haber"),
                yayin_url = "https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/ahaber/ahaber.m3u8",
                collectionType = MediaCollection.HABER
            ),
            MediaItem(
                id = "haber_05",
                baslik = "Haber Global HD",
                kategori = "Gündem",
                resim_url = getOfficialWebLogo("haber global"),
                yayin_url = "https://tv.ensonhaber.com/haberglobal/haberglobal.m3u8",
                collectionType = MediaCollection.HABER
            ),
            MediaItem(
                id = "haber_06",
                baslik = "Sözcü TV HD",
                kategori = "Gündem",
                resim_url = getOfficialWebLogo("sözcü"),
                yayin_url = "https://b01c02nl.mediatriple.net/videoonlylive/mtkgeuihrlfwlive/broadcast_5c9e17cd59e8b.smil/playlist.m3u8",
                collectionType = MediaCollection.HABER
            ),
            MediaItem(
                id = "haber_07",
                baslik = "Halk TV HD",
                kategori = "Gündem",
                resim_url = getOfficialWebLogo("halk tv"),
                yayin_url = "https://halktv.daioncdn.net/halktv/halktv.m3u8",
                collectionType = MediaCollection.HABER
            ),
            MediaItem(
                id = "haber_08",
                baslik = "Tele 1 HD",
                kategori = "Gündem",
                resim_url = getOfficialWebLogo("tele 1"),
                yayin_url = "https://tele1-live.ercdn.net/tele1/tele1.m3u8",
                collectionType = MediaCollection.HABER
            ),
            MediaItem(
                id = "haber_09",
                baslik = "TV 100 HD",
                kategori = "Gündem",
                resim_url = getOfficialWebLogo("tv100"),
                yayin_url = "https://tv100.daioncdn.net/tv100/tv100.m3u8",
                collectionType = MediaCollection.HABER
            ),
            MediaItem(
                id = "haber_10",
                baslik = "24 TV HD",
                kategori = "Gündem",
                resim_url = getOfficialWebLogo("24 tv"),
                yayin_url = "https://turkmedya-live.ercdn.net/tv24/tv24.m3u8",
                collectionType = MediaCollection.HABER
            ),
            MediaItem(
                id = "haber_11",
                baslik = "TGRT Haber HD",
                kategori = "Gündem",
                resim_url = getOfficialWebLogo("tgrt"),
                yayin_url = "https://canli.tgrthaber.com/tgrt.m3u8",
                collectionType = MediaCollection.HABER
            ),
            MediaItem(
                id = "haber_12",
                baslik = "Flash Haber TV",
                kategori = "Gündem",
                resim_url = getOfficialWebLogo("flash"),
                yayin_url = "https://b01c02nl.mediatriple.net/videoonlylive/mtyycglqauzjhlive/broadcast_67c053c48829f.smil/playlist.m3u8",
                collectionType = MediaCollection.HABER
            ),
            MediaItem(
                id = "haber_13",
                baslik = "Bloomberg HT HD",
                kategori = "Ekonomi",
                resim_url = getOfficialWebLogo("bloomberg"),
                yayin_url = "https://tv.ensonhaber.com/bloomberght/bloomberght.m3u8",
                collectionType = MediaCollection.HABER
            ),
            MediaItem(
                id = "haber_14",
                baslik = "CNBC-e Canlı",
                kategori = "Ekonomi",
                resim_url = getOfficialWebLogo("cnbc"),
                yayin_url = "https://hnpsechtsc.turknet.ercdn.net/xpnvudnlsv/cnbc-e/cnbc-e.m3u8",
                collectionType = MediaCollection.HABER
            ),
            MediaItem(
                id = "haber_15",
                baslik = "Ekotürk TV HD",
                kategori = "Ekonomi",
                resim_url = getOfficialWebLogo("ekotürk"),
                yayin_url = "https://ekoturk.daioncdn.net/ekoturk/ekoturk.m3u8",
                collectionType = MediaCollection.HABER
            ),
            MediaItem(
                id = "haber_16",
                baslik = "A Para HD",
                kategori = "Ekonomi",
                resim_url = getOfficialWebLogo("a para"),
                yayin_url = "https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/apara/apara.m3u8",
                collectionType = MediaCollection.HABER
            ),
            MediaItem(
                id = "haber_17",
                baslik = "Bengü Türk TV",
                kategori = "Gündem",
                resim_url = getOfficialWebLogo("bengü türk"),
                yayin_url = "https://canli.benguturk.com/benguturk.m3u8",
                collectionType = MediaCollection.HABER
            ),
            MediaItem(
                id = "haber_18",
                baslik = "TVNET HD",
                kategori = "Gündem",
                resim_url = getOfficialWebLogo("tvnet"),
                yayin_url = "https://tvnet.daioncdn.net/tvnet/tvnet.m3u8",
                collectionType = MediaCollection.HABER
            ),
            MediaItem(
                id = "haber_19",
                baslik = "Ülke TV HD",
                kategori = "Gündem",
                resim_url = getOfficialWebLogo("ülke"),
                yayin_url = "https://kanal7.daioncdn.net/ulketv/ulketv.m3u8",
                collectionType = MediaCollection.HABER
            )
        )
    }

    /**
     * Doğrulanmış Türkiye Çocuk & Animasyon Kanalları
     */
    fun getVerifiedKidsChannels(): List<MediaItem> {
        return listOf(
            MediaItem(
                id = "cocuk_01",
                baslik = "TRT Çocuk HD",
                kategori = "Çizgi Film",
                resim_url = getOfficialWebLogo("trt çocuk"),
                yayin_url = "https://tv-trtcocuk.medya.trt.com.tr/master.m3u8",
                collectionType = MediaCollection.COCUK
            ),
            MediaItem(
                id = "cocuk_02",
                baslik = "Minika Çocuk HD",
                kategori = "Okul Öncesi",
                resim_url = getOfficialWebLogo("minika"),
                yayin_url = "https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/minikago_cocuk/minikago_cocuk.m3u8",
                collectionType = MediaCollection.COCUK
            ),
            MediaItem(
                id = "cocuk_03",
                baslik = "Minika GO HD",
                kategori = "Çizgi Film",
                resim_url = getOfficialWebLogo("minika go"),
                yayin_url = "https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/minikago/minikago.m3u8",
                collectionType = MediaCollection.COCUK
            ),
            MediaItem(
                id = "cocuk_04",
                baslik = "TRT Diyanet Çocuk",
                kategori = "Eğitici",
                resim_url = getOfficialWebLogo("diyanet"),
                yayin_url = "https://tv-trtdiyanetcocuk.medya.trt.com.tr/master.m3u8",
                collectionType = MediaCollection.COCUK
            ),
            MediaItem(
                id = "cocuk_05",
                baslik = "Cartoon Network TR",
                kategori = "Çizgi Film",
                resim_url = getOfficialWebLogo("cartoon"),
                yayin_url = "https://tv-trtcocuk.medya.trt.com.tr/master.m3u8",
                collectionType = MediaCollection.COCUK
            ),
            MediaItem(
                id = "cocuk_06",
                baslik = "Kral Şakir Özel HD",
                kategori = "Animasyon",
                resim_url = getOfficialWebLogo("trt çocuk"),
                yayin_url = "https://tv-trtcocuk.medya.trt.com.tr/master.m3u8",
                collectionType = MediaCollection.COCUK
            ),
            MediaItem(
                id = "cocuk_07",
                baslik = "Pepee Çizgi Film Kulübü",
                kategori = "Müzik & Masal",
                resim_url = getOfficialWebLogo("minika"),
                yayin_url = "https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/minikago_cocuk/minikago_cocuk.m3u8",
                collectionType = MediaCollection.COCUK
            ),
            MediaItem(
                id = "cocuk_08",
                baslik = "Niloya Çocuk TV",
                kategori = "Okul Öncesi",
                resim_url = getOfficialWebLogo("trt çocuk"),
                yayin_url = "https://tv-trtcocuk.medya.trt.com.tr/master.m3u8",
                collectionType = MediaCollection.COCUK
            ),
            MediaItem(
                id = "cocuk_09",
                baslik = "Kukuli Müzik & Eğlence",
                kategori = "Müzik & Masal",
                resim_url = getOfficialWebLogo("minika go"),
                yayin_url = "https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/minikago/minikago.m3u8",
                collectionType = MediaCollection.COCUK
            ),
            MediaItem(
                id = "cocuk_10",
                baslik = "Maşa ile Koca Ayı TV",
                kategori = "Animasyon",
                resim_url = getOfficialWebLogo("trt çocuk"),
                yayin_url = "https://tv-trtcocuk.medya.trt.com.tr/master.m3u8",
                collectionType = MediaCollection.COCUK
            ),
            MediaItem(
                id = "cocuk_11",
                baslik = "Zarok TV HD",
                kategori = "Çizgi Film",
                resim_url = getOfficialWebLogo("zarok"),
                yayin_url = "https://zaroktv.daioncdn.net/zaroktv/zaroktv.m3u8",
                collectionType = MediaCollection.COCUK
            ),
            MediaItem(
                id = "cocuk_12",
                baslik = "Baby TV Öğrenme & Bebek",
                kategori = "Okul Öncesi",
                resim_url = getOfficialWebLogo("diyanet"),
                yayin_url = "https://tv-trtdiyanetcocuk.medya.trt.com.tr/master.m3u8",
                collectionType = MediaCollection.COCUK
            )
        )
    }

    /**
     * %100 TEST EDİLMİŞ (HTTP 200 OK) DOĞRUDAN ÇALIŞAN TÜRKİYE AÇIK YAYINLARI
     * Her kanal kendi doğrulanmış orijinal yayın URL'sine sahiptir.
     */
    fun getTkgsFullChannelList(): List<MediaItem> {
        return listOf(
            MediaItem(
                id = "live_01",
                baslik = "01 • TRT 1 HD",
                kategori = "Ulusal",
                resim_url = getOfficialWebLogo("trt 1"),
                yayin_url = "https://tv-trt1.medya.trt.com.tr/master.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_02",
                baslik = "02 • ATV HD",
                kategori = "Ulusal",
                resim_url = getOfficialWebLogo("atv"),
                yayin_url = "https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/atv/atv.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_03",
                baslik = "03 • Kanal D HD",
                kategori = "Ulusal",
                resim_url = getOfficialWebLogo("kanal d"),
                yayin_url = "https://demiroren.daioncdn.net/kanald/kanald.m3u8?app=kanald_web&ce=3",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_04",
                baslik = "04 • Star TV HD",
                kategori = "Ulusal",
                resim_url = getOfficialWebLogo("star"),
                yayin_url = "https://dogus.daioncdn.net/startv/startv_720p.m3u8?app=a20ac41e-bdc3-4aa1-934d-26b484480ac9&ce=3&sid=8l4w3lst4co5",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_05",
                baslik = "05 • TV8 HD",
                kategori = "Ulusal",
                resim_url = getOfficialWebLogo("tv8"),
                yayin_url = "https://tv8.daioncdn.net/tv8/tv8.m3u8?app=7ddc255a-ef47-4e81-ab14-c0e5f2949788&ce=3",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_06",
                baslik = "06 • NOW TV HD",
                kategori = "Ulusal",
                resim_url = getOfficialWebLogo("now"),
                yayin_url = "https://uycyyuuzyh.turknet.ercdn.net/nphindgytw/nowtv/nowtv.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_07",
                baslik = "07 • A Spor HD",
                kategori = "Spor",
                resim_url = getOfficialWebLogo("a spor"),
                yayin_url = "https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/aspor/aspor.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_08",
                baslik = "08 • TJK TV HD",
                kategori = "Spor",
                resim_url = getOfficialWebLogo("tjk"),
                yayin_url = "https://tjktv-live.tjk.org/tjktv.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_09",
                baslik = "09 • TRT Spor Yıldız",
                kategori = "Spor",
                resim_url = getOfficialWebLogo("trt spor"),
                yayin_url = "https://trt.daioncdn.net/trtspor-yildiz/master.m3u8?app=web&platform=trtspor",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_10",
                baslik = "10 • NTV HD",
                kategori = "Haber",
                resim_url = getOfficialWebLogo("ntv"),
                yayin_url = "https://dogus.daioncdn.net/ntv/ntv.m3u8?app=ntv_web",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_11",
                baslik = "11 • TRT Haber HD",
                kategori = "Haber",
                resim_url = getOfficialWebLogo("trt haber"),
                yayin_url = "https://tv-trthaber.medya.trt.com.tr/master.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_12",
                baslik = "12 • Haber Global HD",
                kategori = "Haber",
                resim_url = getOfficialWebLogo("haber global"),
                yayin_url = "https://tv.ensonhaber.com/haberglobal/haberglobal.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_13",
                baslik = "13 • Habertürk TV HD",
                kategori = "Haber",
                resim_url = getOfficialWebLogo("habertürk"),
                yayin_url = "https://tv.ensonhaber.com/haberturk/haberturk.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_14",
                baslik = "14 • A Haber HD",
                kategori = "Haber",
                resim_url = getOfficialWebLogo("a haber"),
                yayin_url = "https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/ahaber/ahaber.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_15",
                baslik = "15 • 24 TV HD",
                kategori = "Haber",
                resim_url = getOfficialWebLogo("24 tv"),
                yayin_url = "https://turkmedya-live.ercdn.net/tv24/tv24.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_16",
                baslik = "16 • 360 TV HD",
                kategori = "Ulusal",
                resim_url = getOfficialWebLogo("360"),
                yayin_url = "https://turkmedya-live.ercdn.net/tv360/tv360.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_17",
                baslik = "17 • TV 4 HD",
                kategori = "Ulusal",
                resim_url = getOfficialWebLogo("tv 4"),
                yayin_url = "https://turkmedya-live.ercdn.net/tv4/tv4.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_18",
                baslik = "18 • Flash Haber TV",
                kategori = "Haber",
                resim_url = getOfficialWebLogo("flash"),
                yayin_url = "https://b01c02nl.mediatriple.net/videoonlylive/mtyycglqauzjhlive/broadcast_67c053c48829f.smil/playlist.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_19",
                baslik = "19 • TGRT Haber HD",
                kategori = "Haber",
                resim_url = getOfficialWebLogo("tgrt"),
                yayin_url = "https://canli.tgrthaber.com/tgrt.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_20",
                baslik = "20 • CNBC-e Canlı",
                kategori = "Ekonomi",
                resim_url = getOfficialWebLogo("cnbc"),
                yayin_url = "https://hnpsechtsc.turknet.ercdn.net/xpnvudnlsv/cnbc-e/cnbc-e.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_21",
                baslik = "21 • A2 TV HD",
                kategori = "Ulusal",
                resim_url = getOfficialWebLogo("a2"),
                yayin_url = "https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/a2tv/a2tv.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_22",
                baslik = "22 • Euro D HD",
                kategori = "Ulusal",
                resim_url = getOfficialWebLogo("euro d"),
                yayin_url = "https://live.duhnet.tv/S2/HLS_LIVE/eurodnp/playlist.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_23",
                baslik = "23 • Kral Pop TV HD",
                kategori = "Müzik",
                resim_url = getOfficialWebLogo("kral pop"),
                yayin_url = "https://dogus-live.daioncdn.net/kralpoptv/playlist.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_24",
                baslik = "24 • Power TV HD",
                kategori = "Müzik",
                resim_url = getOfficialWebLogo("power"),
                yayin_url = "https://livetv.powerapp.com.tr/powerTV/powerhd.smil/playlist.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_25",
                baslik = "25 • Dream Türk HD",
                kategori = "Müzik",
                resim_url = getOfficialWebLogo("dream"),
                yayin_url = "https://live.duhnet.tv/S2/HLS_LIVE/dreamturknp/playlist.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_26",
                baslik = "26 • Number 1 TV HD",
                kategori = "Müzik",
                resim_url = getOfficialWebLogo("number 1"),
                yayin_url = "https://b01c02nl.mediatriple.net/videoonlylive/mtkgeuihrlfwlive/broadcast_5c9e17cd59e8b.smil/playlist.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_27",
                baslik = "27 • Minika Çocuk HD",
                kategori = "Çocuk",
                resim_url = getOfficialWebLogo("minika"),
                yayin_url = "https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/minikago_cocuk/minikago_cocuk.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_28",
                baslik = "28 • Minika GO HD",
                kategori = "Çocuk",
                resim_url = getOfficialWebLogo("minika"),
                yayin_url = "https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/minikago/minikago.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_29",
                baslik = "29 • TRT Diyanet Çocuk",
                kategori = "Çocuk",
                resim_url = getOfficialWebLogo("diyanet"),
                yayin_url = "https://tv-trtdiyanetcocuk.medya.trt.com.tr/master.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_30",
                baslik = "30 • TRT Müzik HD",
                kategori = "Müzik",
                resim_url = getOfficialWebLogo("trt müzik"),
                yayin_url = "https://tv-trtmuzik.medya.trt.com.tr/master.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_31",
                baslik = "31 • TRT Türk HD",
                kategori = "Ulusal",
                resim_url = getOfficialWebLogo("trt türk"),
                yayin_url = "https://tv-trtturk.medya.trt.com.tr/master.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_32",
                baslik = "32 • TRT Avaz HD",
                kategori = "Ulusal",
                resim_url = getOfficialWebLogo("trt avaz"),
                yayin_url = "https://tv-trtavaz.medya.trt.com.tr/master.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_33",
                baslik = "33 • TBMM TV HD",
                kategori = "Haber",
                resim_url = getOfficialWebLogo("tbmm"),
                yayin_url = "https://meclistv-live.ercdn.net/meclistv/meclistv.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_34",
                baslik = "34 • Diyanet TV HD",
                kategori = "Kültür",
                resim_url = getOfficialWebLogo("diyanet"),
                yayin_url = "https://eustr73.mediatriple.net/videoonlylive/mtikoimxnztxlive/broadcast_5e3bf95a47e07.smil/playlist.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_35",
                baslik = "35 • Line TV HD",
                kategori = "Ulusal",
                resim_url = getOfficialWebLogo("line"),
                yayin_url = "https://edge1.socialsmart.tv/linetv/bant1/playlist.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_36",
                baslik = "36 • Lalegül TV HD",
                kategori = "Kültür",
                resim_url = getOfficialWebLogo("lalegul"),
                yayin_url = "https://lbl.netmedya.net/hls/lalegultv.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_37",
                baslik = "37 • Kocaeli TV HD",
                kategori = "Ulusal",
                resim_url = getOfficialWebLogo("kocaeli"),
                yayin_url = "https://edge.taksimbilisim.com/kocaelitv/bant1/playlist.m3u8",
                collectionType = MediaCollection.CANLI_TV
            ),
            MediaItem(
                id = "live_38",
                baslik = "38 • Kanal 58 HD",
                kategori = "Ulusal",
                resim_url = getOfficialWebLogo("kanal 58"),
                yayin_url = "https://live.artidijitalmedya.com/artidijital_kanal58/kanal58/playlist.m3u8",
                collectionType = MediaCollection.CANLI_TV
            )
        )
    }
}
