package com.example

import com.example.data.remote.IcsScheduleParser
import com.example.model.Tournament
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IcsScheduleParserTest {

    @Test
    fun parseIcsContent_extractsMatchesCorrectly() {
        val icsSample = """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-//UEFA//Match Calendar//EN
            BEGIN:VEVENT
            UID:ucl-m3-real-bvb-101
            DTSTART:20261020T190000Z
            SUMMARY:Real Madrid vs Borussia Dortmund
            LOCATION:Santiago Bernabeu, Madrid
            DESCRIPTION:UEFA Champions League - League Phase - Matchday 3
            END:VEVENT
            BEGIN:VEVENT
            UID:uel-m3-fener-manutd-102
            DTSTART:20261022T190000Z
            SUMMARY:Fenerbahce vs Manchester United
            LOCATION:Sukru Saracoglu, Istanbul
            DESCRIPTION:UEFA Europa League - League Phase - Matchday 3
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()

        val parser = IcsScheduleParser()
        val matches = parser.parseIcsContent(icsSample)

        assertNotNull(matches)
        assertEquals(2, matches.size)

        val uclMatch = matches[0]
        assertEquals("Real Madrid", uclMatch.homeTeam)
        assertEquals("Borussia Dortmund", uclMatch.awayTeam)
        assertEquals(Tournament.CHAMPIONS_LEAGUE.name, uclMatch.tournament)
        assertEquals("Santiago Bernabeu, Madrid", uclMatch.stadium)
        assertTrue(uclMatch.kickoffTimeMillis > 0)

        val uelMatch = matches[1]
        assertEquals("Fenerbahce", uelMatch.homeTeam)
        assertEquals("Manchester United", uelMatch.awayTeam)
        assertEquals(Tournament.EUROPA_LEAGUE.name, uelMatch.tournament)
    }
}
