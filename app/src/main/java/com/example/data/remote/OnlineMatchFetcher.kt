package com.example.data.remote

import com.example.data.local.MatchEntity
import com.example.model.Tournament
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * Загрузчик расписания матчей по официальному календарю UEFA (ЛЧ и ЛЕ).
 * Не требует API-токенов или авторизации, работает полностью через открытый календарь.
 */
class OnlineMatchFetcher(
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) {

    private val utcDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private val htmlNumericEntityPattern = Pattern.compile("&#(\\d+);")

    suspend fun fetchEuropaLeagueOnline(): List<MatchEntity> = withContext(Dispatchers.IO) {
        fetchFromFixtureCalendar(
            url = "https://fixturedownload.com/results/europa-league-2026",
            tournament = Tournament.EUROPA_LEAGUE
        )
    }

    suspend fun fetchChampionsLeagueOnline(): List<MatchEntity> = withContext(Dispatchers.IO) {
        fetchFromFixtureCalendar(
            url = "https://fixturedownload.com/results/champions-league-2026",
            tournament = Tournament.CHAMPIONS_LEAGUE
        )
    }

    private fun fetchFromFixtureCalendar(url: String, tournament: Tournament): List<MatchEntity> {
        val matches = mutableListOf<MatchEntity>()
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile)")
                .header("Accept", "text/html,application/xhtml+xml")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return emptyList()

            val html = response.body?.string() ?: return emptyList()

            val rowPattern = Pattern.compile("<tr>(.*?)</tr>", Pattern.DOTALL)
            val rowMatcher = rowPattern.matcher(html)

            var isFirstRow = true
            while (rowMatcher.find()) {
                val rowContent = rowMatcher.group(1) ?: continue
                if (isFirstRow) {
                    isFirstRow = false
                    continue
                }

                val tdPattern = Pattern.compile("<td>(.*?)</td>", Pattern.DOTALL)
                val tdMatcher = tdPattern.matcher(rowContent)
                val cells = mutableListOf<String>()

                while (tdMatcher.find()) {
                    val rawCell = tdMatcher.group(1) ?: ""
                    val cleanText = rawCell.replace(Regex("<[^>]+>"), "").trim()
                    cells.add(unescapeHtml(cleanText))
                }

                if (cells.size >= 6) {
                    val roundNumber = cells[0].trim()
                    val dateStr = cells[1].trim()
                    val location = cells[2].trim()
                    val homeTeam = cells[3].trim()
                    val awayTeam = cells[4].trim()

                    if (homeTeam.isNotBlank() && awayTeam.isNotBlank()) {
                        val kickoffMillis = try {
                            utcDateFormat.parse(dateStr)?.time ?: System.currentTimeMillis()
                        } catch (_: Exception) {
                            System.currentTimeMillis()
                        }

                        val stageText = if (roundNumber.toIntOrNull() != null) {
                            "Общий этап • Тур $roundNumber"
                        } else {
                            roundNumber.ifBlank { "Общий этап" }
                        }

                        val prefix = if (tournament == Tournament.EUROPA_LEAGUE) "uel" else "ucl"
                        val matchId = "${prefix}_r${roundNumber}_${homeTeam.hashCode()}_${awayTeam.hashCode()}"

                        matches.add(
                            MatchEntity(
                                id = matchId,
                                tournament = tournament.name,
                                homeTeam = homeTeam,
                                awayTeam = awayTeam,
                                homeCountry = extractCountry(homeTeam),
                                awayCountry = extractCountry(awayTeam),
                                kickoffTimeMillis = kickoffMillis,
                                stadium = if (location.isNotBlank()) location else "Стадион турнира",
                                stage = stageText,
                                broadcastChannel = "Матч ТВ / Okko Спорт",
                                isReminderEnabled = true,
                                lastUpdatedMillis = System.currentTimeMillis()
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            // Safe fallback
        }
        return matches
    }

    private fun unescapeHtml(text: String): String {
        var result = text
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")

        val matcher = htmlNumericEntityPattern.matcher(result)
        val sb = StringBuffer()
        while (matcher.find()) {
            val codePoint = matcher.group(1)?.toIntOrNull()
            if (codePoint != null) {
                matcher.appendReplacement(sb, codePoint.toChar().toString())
            }
        }
        matcher.appendTail(sb)
        return sb.toString()
    }

    fun extractCountry(teamName: String): String {
        val lower = teamName.lowercase()
        return when {
            lower.contains("madrid") || lower.contains("barcelona") || lower.contains("atleti") ||
                    lower.contains("bilbao") || lower.contains("sociedad") || lower.contains("celta") ||
                    lower.contains("villarreal") || lower.contains("betis") -> "Испания"

            lower.contains("manchester") || lower.contains("man utd") || lower.contains("man city") ||
                    lower.contains("arsenal") || lower.contains("liverpool") || lower.contains("spurs") ||
                    lower.contains("tottenham") || lower.contains("chelsea") || lower.contains("aston") ||
                    lower.contains("palace") || lower.contains("bournemouth") || lower.contains("sunderland") -> "Англия"

            lower.contains("bayern") || lower.contains("dortmund") || lower.contains("leipzig") ||
                    lower.contains("leverkusen") || lower.contains("stuttgart") || lower.contains("frankfurt") ||
                    lower.contains("hoffenheim") -> "Германия"

            lower.contains("roma") || lower.contains("inter") || lower.contains("milan") ||
                    lower.contains("juventus") || lower.contains("lazio") || lower.contains("atalanta") ||
                    lower.contains("napoli") || lower.contains("como") -> "Италия"

            lower.contains("paris") || lower.contains("psg") || lower.contains("lille") ||
                    lower.contains("monaco") || lower.contains("nice") || lower.contains("lyon") ||
                    lower.contains("marseille") || lower.contains("rennes") || lower.contains("lens") -> "Франция"

            lower.contains("porto") || lower.contains("benfica") || lower.contains("sporting") ||
                    lower.contains("braga") || lower.contains("torreense") -> "Португалия"

            lower.contains("ajax") || lower.contains("psv") || lower.contains("feyenoord") ||
                    lower.contains("alkmaar") || lower.contains("n.e.c") || lower.contains("twente") -> "Нидерланды"

            lower.contains("celtic") || lower.contains("rangers") -> "Шотландия"
            lower.contains("salzburg") || lower.contains("sturm") || lower.contains("lask") -> "Австрия"
            lower.contains("sparta") || lower.contains("slavia") || lower.contains("plzen") -> "Чехия"
            lower.contains("olympiacos") || lower.contains("ofi") || lower.contains("paok") || lower.contains("aek") -> "Греция"
            lower.contains("fenerbahce") || lower.contains("galatasaray") || lower.contains("besiktas") -> "Турция"
            lower.contains("anderlecht") || lower.contains("union sg") || lower.contains("brugge") -> "Бельгия"
            lower.contains("poznan") || lower.contains("jagiellonia") -> "Польша"
            lower.contains("omonia") -> "Кипр"
            lower.contains("ararat") -> "Армения"
            lower.contains("celje") -> "Словения"
            lower.contains("beer-sheva") || lower.contains("maccabi") -> "Израиль"
            lower.contains("levski") || lower.contains("ludogorets") -> "Болгария"
            lower.contains("dinamo") -> "Хорватия"
            lower.contains("lillestr") || lower.contains("bod") || lower.contains("viking") -> "Норвегия"
            lower.contains("ferencv") -> "Венгрия"
            lower.contains("shakhtar") || lower.contains("kyiv") -> "Украина"
            else -> "Европа"
        }
    }
}
