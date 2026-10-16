package com.example.data.remote

import com.example.data.local.MatchEntity
import com.example.model.Tournament
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class IcsScheduleParser {

    private val utcDateFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val altDateFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.US)

    fun parseIcsContent(icsContent: String, defaultTournament: Tournament = Tournament.CHAMPIONS_LEAGUE): List<MatchEntity> {
        val matches = mutableListOf<MatchEntity>()
        val lines = icsContent.lines()

        var inEvent = false
        var uid = ""
        var dtStartStr = ""
        var summary = ""
        var location = ""
        var description = ""

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.equals("BEGIN:VEVENT", ignoreCase = true)) {
                inEvent = true
                uid = ""
                dtStartStr = ""
                summary = ""
                location = ""
                description = ""
            } else if (line.equals("END:VEVENT", ignoreCase = true)) {
                if (inEvent && summary.isNotBlank()) {
                    val match = buildMatchFromIcs(uid, dtStartStr, summary, location, description, defaultTournament)
                    if (match != null) {
                        matches.add(match)
                    }
                }
                inEvent = false
            } else if (inEvent) {
                when {
                    line.startsWith("UID:", ignoreCase = true) -> {
                        uid = line.substringAfter(":")
                    }
                    line.startsWith("DTSTART", ignoreCase = true) -> {
                        dtStartStr = line.substringAfter(":")
                    }
                    line.startsWith("SUMMARY:", ignoreCase = true) -> {
                        summary = line.substringAfter(":")
                    }
                    line.startsWith("LOCATION:", ignoreCase = true) -> {
                        location = line.substringAfter(":")
                    }
                    line.startsWith("DESCRIPTION:", ignoreCase = true) -> {
                        description = line.substringAfter(":")
                    }
                }
            }
        }

        return matches
    }

    private fun buildMatchFromIcs(
        uid: String,
        dtStartStr: String,
        summary: String,
        location: String,
        description: String,
        defaultTournament: Tournament
    ): MatchEntity? {
        val kickoffMillis = parseDate(dtStartStr) ?: return null

        val delimiters = listOf(" vs ", " v ", " - ", " – ")
        var homeTeam = summary
        var awayTeam = "Гости"

        for (delim in delimiters) {
            if (summary.contains(delim, ignoreCase = true)) {
                val parts = summary.split(Regex(delim, RegexOption.IGNORE_CASE), 2)
                if (parts.size == 2) {
                    homeTeam = parts[0].trim()
                    awayTeam = parts[1].trim()
                    break
                }
            }
        }

        val isEuropa = summary.contains("Europa", ignoreCase = true) ||
                description.contains("Europa", ignoreCase = true) ||
                description.contains("UEL", ignoreCase = true) ||
                defaultTournament == Tournament.EUROPA_LEAGUE

        val tournament = if (isEuropa) Tournament.EUROPA_LEAGUE else Tournament.CHAMPIONS_LEAGUE

        val stage = if (description.isNotBlank()) {
            description.lines().firstOrNull { it.isNotBlank() } ?: "Общий этап"
        } else {
            "Общий этап"
        }

        val matchId = if (uid.isNotBlank()) {
            "ics_${uid.replace(Regex("[^a-zA-Z0-9_-]"), "_")}"
        } else {
            "ics_${homeTeam.hashCode()}_${awayTeam.hashCode()}_${kickoffMillis}"
        }

        return MatchEntity(
            id = matchId,
            tournament = tournament.name,
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            homeCountry = extractCountry(homeTeam),
            awayCountry = extractCountry(awayTeam),
            kickoffTimeMillis = kickoffMillis,
            stadium = if (location.isNotBlank()) location else "Стадион турнира",
            stage = stage,
            broadcastChannel = "Матч ТВ / Okko Спорт",
            isReminderEnabled = true,
            lastUpdatedMillis = System.currentTimeMillis()
        )
    }

    private fun parseDate(dateStr: String): Long? {
        if (dateStr.isBlank()) return null
        return try {
            if (dateStr.endsWith("Z", ignoreCase = true)) {
                utcDateFormat.parse(dateStr)?.time
            } else {
                altDateFormat.parse(dateStr)?.time
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun extractCountry(teamName: String): String {
        val lower = teamName.lowercase()
        return when {
            lower.contains("madrid") || lower.contains("barcelona") || lower.contains("atletico") || lower.contains("bilbao") || lower.contains("sociedad") -> "Испания"
            lower.contains("manchester") || lower.contains("arsenal") || lower.contains("liverpool") || lower.contains("tottenham") || lower.contains("chelsea") || lower.contains("aston") -> "Англия"
            lower.contains("bayern") || lower.contains("dortmund") || lower.contains("leipzig") || lower.contains("leverkusen") || lower.contains("stuttgart") || lower.contains("frankfurt") -> "Германия"
            lower.contains("roma") || lower.contains("inter") || lower.contains("milan") || lower.contains("juventus") || lower.contains("lazio") || lower.contains("atalanta") -> "Италия"
            lower.contains("paris") || lower.contains("psg") || lower.contains("lille") || lower.contains("monaco") || lower.contains("nice") || lower.contains("lyon") -> "Франция"
            lower.contains("porto") || lower.contains("benfica") || lower.contains("sporting") || lower.contains("braga") -> "Португалия"
            lower.contains("ajax") || lower.contains("psv") || lower.contains("feyenoord") || lower.contains("alkmaar") -> "Нидерланды"
            lower.contains("fenerbahce") || lower.contains("galatasaray") || lower.contains("besiktas") -> "Турция"
            lower.contains("shakhtar") || lower.contains("dynamo kyiv") -> "Украина"
            lower.contains("sparta") || lower.contains("slavia") -> "Чехия"
            else -> "Европа"
        }
    }
}
