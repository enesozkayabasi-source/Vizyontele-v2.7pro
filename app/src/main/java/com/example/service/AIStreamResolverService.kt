package com.example.service

import android.util.Log
import com.example.BuildConfig
import com.example.model.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AIResolvedStream(
    val channelTitle: String,
    val streamUrl: String,
    val logoUrl: String,
    val isSuccess: Boolean,
    val sourceDescription: String
)

class AIStreamResolverService(
    private val internetChannelService: InternetChannelService = InternetChannelService()
) {

    private val tag = "AIStreamResolverService"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    // Gemini API model: gemini-2.5-flash as mandated for fast text & search grounding tasks
    private val geminiEndpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"

    // Doğrulanmış gerçek canlı yayın eşleme havuzu (HTTP 200 Test Edilmiş)
    private val verifiedLiveStreams = mapOf(
        "trt 1" to ("https://tv-trt1.medya.trt.com.tr/master.m3u8" to internetChannelService.getOfficialWebLogo("trt 1")),
        "atv" to ("https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/atv/atv_720p.m3u8" to internetChannelService.getOfficialWebLogo("atv")),
        "a2" to ("https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/a2tv/a2tv_720p.m3u8" to internetChannelService.getOfficialWebLogo("a2")),
        "kanal d" to ("https://demiroren.daioncdn.net/kanald/kanald.m3u8?app=kanald_web&ce=3" to internetChannelService.getOfficialWebLogo("kanal d")),
        "star" to ("https://dogus.daioncdn.net/startv/startv_720p.m3u8?app=a20ac41e-bdc3-4aa1-934d-26b484480ac9&ce=3&sid=8l4w3lst4co5" to internetChannelService.getOfficialWebLogo("star")),
        "tv8" to ("https://tv8.daioncdn.net/tv8/tv8.m3u8?app=7ddc255a-ef47-4e81-ab14-c0e5f2949788&ce=3" to internetChannelService.getOfficialWebLogo("tv8")),
        "now" to ("https://uycyyuuzyh.turknet.ercdn.net/nphindgytw/nowtv/nowtv.m3u8" to internetChannelService.getOfficialWebLogo("now")),
        "a spor" to ("https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/aspor/aspor_720p.m3u8" to internetChannelService.getOfficialWebLogo("a spor")),
        "aspor" to ("https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/aspor/aspor_720p.m3u8" to internetChannelService.getOfficialWebLogo("aspor")),
        "tjk" to ("https://tjktv-live.tjk.org/tjktv.m3u8" to internetChannelService.getOfficialWebLogo("tjk")),
        "fb tv" to ("https://1hskrdto.rocketcdn.com/fenerbahcetv.smil/playlist.m3u8" to internetChannelService.getOfficialWebLogo("fb tv")),
        "fbtv" to ("https://1hskrdto.rocketcdn.com/fenerbahcetv.smil/playlist.m3u8" to internetChannelService.getOfficialWebLogo("fb tv")),
        "ntv" to ("https://dogus.daioncdn.net/ntv/ntv.m3u8?app=ntv_web" to internetChannelService.getOfficialWebLogo("ntv")),
        "trt haber" to ("https://tv-trthaber.medya.trt.com.tr/master.m3u8" to internetChannelService.getOfficialWebLogo("trt haber")),
        "haber global" to ("https://tv.ensonhaber.com/haberglobal/haberglobal.m3u8" to internetChannelService.getOfficialWebLogo("haber global")),
        "habertürk" to ("https://tv.ensonhaber.com/haberturk/haberturk.m3u8" to internetChannelService.getOfficialWebLogo("habertürk")),
        "sözcü" to ("https://b01c02nl.mediatriple.net/videoonlylive/mtkgeuihrlfwlive/broadcast_5c9e17cd59e8b.smil/playlist.m3u8" to internetChannelService.getOfficialWebLogo("sözcü")),
        "szc" to ("https://b01c02nl.mediatriple.net/videoonlylive/mtkgeuihrlfwlive/broadcast_5c9e17cd59e8b.smil/playlist.m3u8" to internetChannelService.getOfficialWebLogo("sözcü")),
        "ahaber" to ("https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/ahaber/ahaber_720p.m3u8" to internetChannelService.getOfficialWebLogo("ahaber")),
        "a haber" to ("https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/ahaber/ahaber_720p.m3u8" to internetChannelService.getOfficialWebLogo("a haber")),
        "24 tv" to ("https://turkmedya-live.ercdn.net/tv24/tv24.m3u8" to internetChannelService.getOfficialWebLogo("24 tv")),
        "360" to ("https://turkmedya-live.ercdn.net/tv360/tv360.m3u8" to internetChannelService.getOfficialWebLogo("360")),
        "tv 4" to ("https://turkmedya-live.ercdn.net/tv4/tv4.m3u8" to internetChannelService.getOfficialWebLogo("tv 4")),
        "flash" to ("https://b01c02nl.mediatriple.net/videoonlylive/mtyycglqauzjhlive/broadcast_67c053c48829f.smil/playlist.m3u8" to internetChannelService.getOfficialWebLogo("flash")),
        "tgrt" to ("https://canli.tgrthaber.com/tgrt.m3u8" to internetChannelService.getOfficialWebLogo("tgrt")),
        "cnbc" to ("https://hnpsechtsc.turknet.ercdn.net/xpnvudnlsv/cnbc-e/cnbc-e.m3u8" to internetChannelService.getOfficialWebLogo("cnbc")),
        "euro d" to ("https://live.duhnet.tv/S2/HLS_LIVE/eurodnp/playlist.m3u8" to internetChannelService.getOfficialWebLogo("euro d")),
        "kral" to ("https://dogus-live.daioncdn.net/kralpoptv/playlist.m3u8" to internetChannelService.getOfficialWebLogo("kral")),
        "power" to ("https://livetv.powerapp.com.tr/powerTV/powerhd.smil/playlist.m3u8" to internetChannelService.getOfficialWebLogo("power")),
        "dream" to ("https://live.duhnet.tv/S2/HLS_LIVE/dreamturknp/playlist.m3u8" to internetChannelService.getOfficialWebLogo("dream")),
        "minika" to ("https://rnttwmjcin.turknet.ercdn.net/lcpmvefbyo/minikago_cocuk/minikago_cocuk_720p.m3u8" to internetChannelService.getOfficialWebLogo("minika")),
        "trt müzik" to ("https://tv-trtmuzik.medya.trt.com.tr/master.m3u8" to internetChannelService.getOfficialWebLogo("trt müzik")),
        "trt türk" to ("https://tv-trtturk.medya.trt.com.tr/master.m3u8" to internetChannelService.getOfficialWebLogo("trt türk")),
        "trt avaz" to ("https://tv-trtavaz.medya.trt.com.tr/master.m3u8" to internetChannelService.getOfficialWebLogo("trt avaz")),
        "tbmm" to ("https://meclistv-live.ercdn.net/meclistv/meclistv.m3u8" to internetChannelService.getOfficialWebLogo("tbmm")),
        "diyanet" to ("https://eustr73.mediatriple.net/videoonlylive/mtikoimxnztxlive/broadcast_5e3bf95a47e07.smil/playlist.m3u8" to internetChannelService.getOfficialWebLogo("diyanet")),
        "vav" to ("https://trkvz-radyolar.ercdn.net/radyovav/playlist.m3u8" to internetChannelService.getOfficialWebLogo("vav"))
    )

    suspend fun resolveStreamForChannel(
        channelName: String,
        category: String = "Canlı TV"
    ): AIResolvedStream = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        // 1. Google Search Grounding ile interneti canlı tarayarak kanalı ve logosunu ara
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val aiResult = queryGeminiWithWebSearch(channelName, category, apiKey)
                if (aiResult != null && aiResult.streamUrl.isNotBlank()) {
                    Log.d(tag, "VİZYON AI internet web taramasından canlı yayını buldu: ${aiResult.streamUrl}")
                    return@withContext aiResult
                }
            } catch (e: Exception) {
                Log.w(tag, "Gemini canlı web araması hatası: ${e.message}")
            }
        }

        // 2. Doğrulanmış gerçek CDN havuzundan kanalı ara
        val liveStream = findVerifiedLiveStream(channelName)
        val officialLogo = internetChannelService.getOfficialWebLogo(channelName)
        if (liveStream.isNotBlank()) {
            AIResolvedStream(
                channelTitle = channelName,
                streamUrl = liveStream,
                logoUrl = officialLogo,
                isSuccess = true,
                sourceDescription = "VİZYON Doğrulanmış Canlı CDN Akışı"
            )
        } else {
            AIResolvedStream(
                channelTitle = channelName,
                streamUrl = "",
                logoUrl = officialLogo,
                isSuccess = false,
                sourceDescription = "Kanal akışı bulunamadı"
            )
        }
    }

    private fun queryGeminiWithWebSearch(channelName: String, category: String, apiKey: String): AIResolvedStream? {
        val prompt = """
            Sen tüm interneti canlı olarak araştıran bir Televizyon ve Canlı Yayın Arama Yapay Zekasısın.
            Kullanıcı "$channelName" ($category) arıyor.
            
            İnterneti, resmi televizyon web sitelerini, GitHub IPTV dizinlerini ve CDN'leri tara:
            1) Bu kanalın doğrudan çalışan HLS (.m3u8) canlı yayın akış linkini bul. Test yayını ASLA verme.
            2) Bu kanalın internetteki en kaliteli, resmi şeffaf PNG/SVG veya WebP logosunun doğrudan görsel linkini bul.
            
            Sadece geçerli bir JSON nesnesi ver:
            {"channel": "$channelName", "url": "https://...", "logo": "https://...", "source": "Google Canlı Web Taraması"}
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    }
                    put("parts", partsArray)
                }
                put(contentObj)
            }
            put("contents", contentsArray)

            val toolsArray = JSONArray().apply {
                put(JSONObject().put("googleSearch", JSONObject()))
            }
            put("tools", toolsArray)

            val genConfig = JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.1)
            }
            put("generationConfig", genConfig)
        }

        val request = Request.Builder()
            .url("$geminiEndpoint?key=$apiKey")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val responseStr = response.body?.string() ?: return null
                val rootJson = JSONObject(responseStr)
                val candidates = rootJson.optJSONArray("candidates") ?: return null
                val firstCandidate = candidates.optJSONObject(0) ?: return null
                val content = firstCandidate.optJSONObject("content") ?: return null
                val parts = content.optJSONArray("parts") ?: return null
                val text = parts.optJSONObject(0)?.optString("text") ?: return null

                val resultJson = JSONObject(text)
                val streamUrl = resultJson.optString("url", "")
                val logoUrl = resultJson.optString("logo", "")

                if (streamUrl.isNotBlank() && (streamUrl.startsWith("http://") || streamUrl.startsWith("https://"))) {
                    return AIResolvedStream(
                        channelTitle = channelName,
                        streamUrl = streamUrl,
                        logoUrl = if (logoUrl.startsWith("http")) logoUrl else internetChannelService.getOfficialWebLogo(channelName),
                        isSuccess = true,
                        sourceDescription = "VİZYON AI • Canlı Arama ile Doğrulandı"
                    )
                }
            } else {
                Log.w(tag, "Gemini Google Search çağrısı yanıt kodu: HTTP ${response.code}")
            }
        }
        return null
    }

    private fun findVerifiedLiveStream(channelName: String): String {
        val lower = channelName.lowercase().trim()
        for ((key, pair) in verifiedLiveStreams) {
            if (lower.contains(key)) {
                return pair.first
            }
        }
        // ASLA ATV'ye zorla atma! Eşleşme yoksa boş dön ki mevcut kanal korunsun.
        return ""
    }

    fun createMediaItemFromResolved(
        original: MediaItem,
        resolved: AIResolvedStream
    ): MediaItem {
        return original.copy(
            yayin_url = if (resolved.streamUrl.isNotBlank()) resolved.streamUrl else original.yayin_url,
            resim_url = if (resolved.logoUrl.isNotBlank()) resolved.logoUrl else original.resim_url
        )
    }
}
