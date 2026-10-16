package com.example

import com.example.data.remote.ScheduleDataSource
import com.example.model.Tournament
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchScheduleTest {

    @Test
    fun scheduleDataSource_returnsMatchesForBothTournaments() {
        val dataSource = ScheduleDataSource()
        val fixtures = dataSource.fetchLatestFixtures()

        assertNotNull(fixtures)
        assertTrue(fixtures.isNotEmpty())

        val hasUcl = fixtures.any { it.tournament == Tournament.CHAMPIONS_LEAGUE.name }
        val hasUel = fixtures.any { it.tournament == Tournament.EUROPA_LEAGUE.name }

        assertTrue("Should contain Champions League matches", hasUcl)
        assertTrue("Should contain Europa League matches", hasUel)

        // Verify teams and stadiums are populated
        for (match in fixtures) {
            assertFalse(match.homeTeam.isBlank())
            assertFalse(match.awayTeam.isBlank())
            assertFalse(match.stadium.isBlank())
            assertTrue(match.kickoffTimeMillis > 0)
        }
    }
}
