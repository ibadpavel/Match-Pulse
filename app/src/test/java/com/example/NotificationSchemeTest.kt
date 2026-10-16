package com.example

import com.example.data.local.MatchEntity
import com.example.model.Tournament
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class NotificationSchemeTest {

    @Test
    fun morningDigest_formatsUniqueTimesWithoutRepetition() {
        val timeFormat = SimpleDateFormat("HH:mm", Locale("ru")).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        // Create 4 matches: 2 at 18:45 UTC and 2 at 21:00 UTC
        val matches = listOf(
            MatchEntity(
                id = "m1",
                tournament = Tournament.CHAMPIONS_LEAGUE.name,
                homeTeam = "Club A",
                awayTeam = "Club B",
                homeCountry = "Испания",
                awayCountry = "Италия",
                kickoffTimeMillis = 1761072300000L, // 18:45 UTC
                stadium = "Stadium A",
                stage = "Тур 3"
            ),
            MatchEntity(
                id = "m2",
                tournament = Tournament.EUROPA_LEAGUE.name,
                homeTeam = "Club C",
                awayTeam = "Club D",
                homeCountry = "Англия",
                awayCountry = "Германия",
                kickoffTimeMillis = 1761072300000L, // 18:45 UTC (duplicate time)
                stadium = "Stadium C",
                stage = "Тур 3"
            ),
            MatchEntity(
                id = "m3",
                tournament = Tournament.CHAMPIONS_LEAGUE.name,
                homeTeam = "Club E",
                awayTeam = "Club F",
                homeCountry = "Франция",
                awayCountry = "Португалия",
                kickoffTimeMillis = 1761080400000L, // 21:00 UTC
                stadium = "Stadium E",
                stage = "Тур 3"
            ),
            MatchEntity(
                id = "m4",
                tournament = Tournament.EUROPA_LEAGUE.name,
                homeTeam = "Club G",
                awayTeam = "Club H",
                homeCountry = "Нидерланды",
                awayCountry = "Бельгия",
                kickoffTimeMillis = 1761080400000L, // 21:00 UTC (duplicate time)
                stadium = "Stadium G",
                stage = "Тур 3"
            )
        )

        val distinctTimes = matches
            .map { timeFormat.format(Date(it.kickoffTimeMillis)) }
            .distinct()
            .sorted()

        assertEquals(2, distinctTimes.size)
        assertEquals("18:45", distinctTimes[0])
        assertEquals("21:00", distinctTimes[1])

        val timesJoined = distinctTimes.joinToString(", ")
        assertEquals("18:45, 21:00", timesJoined)

        val count = matches.size
        val tournCount = matches.map { it.tournament }.distinct()
        val tourLabel = when {
            tournCount.contains(Tournament.CHAMPIONS_LEAGUE.name) && tournCount.contains(Tournament.EUROPA_LEAGUE.name) -> "ЛЧ/ЛЕ"
            tournCount.contains(Tournament.CHAMPIONS_LEAGUE.name) -> "ЛЧ"
            tournCount.contains(Tournament.EUROPA_LEAGUE.name) -> "ЛЕ"
            else -> "ЛЧ/ЛЕ"
        }

        val formattedMessage = "Сегодня $count матчей $tourLabel в $timesJoined"
        assertEquals("Сегодня 4 матчей ЛЧ/ЛЕ в 18:45, 21:00", formattedMessage)
    }
}
