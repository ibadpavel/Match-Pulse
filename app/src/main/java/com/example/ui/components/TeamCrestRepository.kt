package com.example.ui.components

import android.content.Context
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Резолвер эмблем:
 * 1) static [TeamCrestCatalog] (приоритетнее prefs)
 * 2) versioned SharedPreferences (только dynamic hits)
 * 3) scored fuzzy search по open football-logos catalog (порог качества)
 */
object TeamCrestRepository {

    private const val CDN_JSDELIVR_LOGOS =
        "https://cdn.jsdelivr.net/gh/luukhopman/football-logos@master"
    private const val CACHE_VERSION = 3
    private const val PREFS_NAME = "team_badge_cache_v$CACHE_VERSION"
    private const val MIN_DYNAMIC_SCORE = 0.72

    private val inMemoryCache = ConcurrentHashMap<String, String>()
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val catalogMutex = Mutex()

    @Volatile
    private var cachedOpenCatalogPaths: List<String>? = null

    /**
     * Синхронный fast-path: catalog → memory → prefs (dynamic only).
     * Статический каталог всегда побеждает устаревший prefs.
     */
    fun getFastBadgeUrl(context: Context?, rawTeamName: String): String? {
        val cleanName = TeamNameNormalizer.clean(rawTeamName)
        if (cleanName.isBlank()) return null

        TeamCrestCatalog.resolve(cleanName)?.crestUrl?.let { url ->
            inMemoryCache[cleanName] = url
            return url
        }

        inMemoryCache[cleanName]?.let { return it }

        context?.let { ctx ->
            val cachedUrl = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(cleanName, null)
            if (!cachedUrl.isNullOrBlank()) {
                inMemoryCache[cleanName] = cachedUrl
                return cachedUrl
            }
        }

        return null
    }

    /**
     * Асинхронный поиск по open catalog с scoring.
     * Слабые совпадения не кэшируются (fallback на initials).
     */
    suspend fun resolveDynamicBadge(context: Context, rawTeamName: String): String? =
        withContext(Dispatchers.IO) {
            val cleanName = TeamNameNormalizer.clean(rawTeamName)
            if (cleanName.isBlank()) return@withContext null

            getFastBadgeUrl(context, rawTeamName)?.let { return@withContext it }

            try {
                val paths = loadCatalogPaths() ?: return@withContext null
                val best = findBestPath(cleanName, paths) ?: return@withContext null
                if (best.score < MIN_DYNAMIC_SCORE) return@withContext null

                val encodedPath = best.path.split("/").joinToString("/") { part ->
                    URLEncoder.encode(part, "UTF-8").replace("+", "%20")
                }
                val url = "$CDN_JSDELIVR_LOGOS/$encodedPath"
                persistDynamic(context, cleanName, url)
                url
            } catch (_: Exception) {
                null
            }
        }

    fun getTeamInitials(teamName: String): String {
        val clean = TeamNameNormalizer.unescapeHtml(teamName)
            .replace("FC", "", ignoreCase = true)
            .replace("CF", "", ignoreCase = true)
            .replace("ФК", "", ignoreCase = true)
            .replace("FK", "", ignoreCase = true)
            .replace("GNK", "", ignoreCase = true)
            .replace("SK", "", ignoreCase = true)
            .trim()

        val words = clean.split(" ").filter { it.isNotBlank() }
        return when {
            words.size >= 2 -> "${words[0].take(1)}${words[1].take(1)}".uppercase(Locale.ROOT)
            clean.length >= 3 -> clean.take(3).uppercase(Locale.ROOT)
            clean.isNotEmpty() -> clean.uppercase(Locale.ROOT)
            else -> "UEFA"
        }
    }

    fun getTeamColors(teamName: String): Pair<Color, Color> {
        TeamCrestCatalog.resolveRaw(teamName)?.let {
            return it.primaryColor to it.secondaryColor
        }
        val clean = TeamNameNormalizer.clean(teamName)
        val hash = clean.hashCode().toLong()
        val c1 = Color(0xFF000000L or (hash and 0x7F7F7FL) or 0x1A2B4CL)
        val c2 = Color(0xFF000000L or ((hash shr 8) and 0x7F7F7FL) or 0x00D2C4L)
        return c1 to c2
    }

    /** Для unit-тестов: сброс in-memory кэша. */
    fun clearMemoryCache() {
        inMemoryCache.clear()
    }

    internal data class ScoredPath(val path: String, val score: Double)

    internal fun scorePath(cleanName: String, path: String): Double {
        val fileName = path.substringAfterLast("/")
            .removeSuffix(".png")
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9]+"), " ")
            .trim()

        val query = cleanName.replace(Regex("[^a-z0-9]+"), " ").trim()
        if (query.isBlank() || fileName.isBlank()) return 0.0

        if (fileName == query) return 1.0

        val queryTokens = query.split(" ").filter { it.length >= 2 }
        val fileTokens = fileName.split(" ").filter { it.length >= 2 }
        if (queryTokens.isEmpty() || fileTokens.isEmpty()) return 0.0

        val matched = queryTokens.count { qt -> fileTokens.any { ft -> ft == qt || ft.startsWith(qt) } }
        val coverage = matched.toDouble() / queryTokens.size
        val precision = matched.toDouble() / fileTokens.size
        val extraPenalty = ((fileTokens.size - matched).coerceAtLeast(0)) * 0.08
        return ((coverage * 0.7) + (precision * 0.3) - extraPenalty).coerceIn(0.0, 1.0)
    }

    private fun findBestPath(cleanName: String, paths: List<String>): ScoredPath? {
        var best: ScoredPath? = null
        for (path in paths) {
            if (!path.endsWith(".png")) continue
            // Prefer current logos/ over history/
            val score = scorePath(cleanName, path).let { base ->
                when {
                    path.startsWith("logos/") -> base
                    path.startsWith("history/") -> base * 0.92
                    else -> base * 0.85
                }
            }
            if (best == null || score > best.score) {
                best = ScoredPath(path, score)
            }
        }
        return best
    }

    private suspend fun loadCatalogPaths(): List<String>? = catalogMutex.withLock {
        cachedOpenCatalogPaths?.let { return it }

        val request = Request.Builder()
            .url("https://api.github.com/repos/luukhopman/football-logos/git/trees/master?recursive=1")
            .header("User-Agent", "UefaMatchPulse/1.0 (Android App)")
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) return null

        val bodyString = response.body?.string() ?: return null
        val json = JSONObject(bodyString)
        val tree = json.optJSONArray("tree") ?: return null
        val list = mutableListOf<String>()
        for (i in 0 until tree.length()) {
            val p = tree.getJSONObject(i).optString("path", "")
            if (p.endsWith(".png")) list.add(p)
        }
        cachedOpenCatalogPaths = list
        list
    }

    private fun persistDynamic(context: Context, cleanName: String, url: String) {
        inMemoryCache[cleanName] = url
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(cleanName, url)
            .apply()
    }
}

/**
 * Обратная совместимость с прежним API (тесты / старые вызовы).
 */
object TeamBadgeProvider {
    fun getFastBadgeUrl(context: Context?, rawTeamName: String): String? =
        TeamCrestRepository.getFastBadgeUrl(context, rawTeamName)

    suspend fun resolveDynamicBadge(context: Context, rawTeamName: String): String? =
        TeamCrestRepository.resolveDynamicBadge(context, rawTeamName)

    fun getTeamInitials(teamName: String): String =
        TeamCrestRepository.getTeamInitials(teamName)

    fun getTeamColors(teamName: String): Pair<Color, Color> =
        TeamCrestRepository.getTeamColors(teamName)
}
