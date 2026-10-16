package com.example

import com.example.ui.components.TeamCrestCatalog
import com.example.ui.components.TeamCrestRepository
import com.example.ui.components.TeamBadgeProvider
import com.example.ui.components.TeamNameNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TeamBadgeProviderTest {

    @Before
    fun setUp() {
        TeamCrestRepository.clearMemoryCache()
    }

    @Test
    fun testAll2026TournamentClubsHaveBadgeUrls() {
        val testClubs = listOf(
            "Real Madrid CF",
            "Реал Мадрид",
            "FC Barcelona",
            "Барселона",
            "Liverpool FC",
            "Ливерпуль",
            "Manchester City FC",
            "Манчестер Сити",
            "Arsenal FC",
            "Арсенал",
            "FC Bayern München",
            "Бавария",
            "Paris Saint-Germain FC",
            "ПСЖ",
            "FC Internazionale Milano",
            "Интер",
            "Juventus",
            "Ювентус",
            "Milan",
            "Милан",
            "Bayer 04 Leverkusen",
            "Байер",
            "Atalanta BC",
            "Аталанта",
            "Sporting CP",
            "Спортинг",
            "SL Benfica",
            "Бенфика",
            "Celtic FC",
            "Селтик",
            "FK Shakhtar Donetsk",
            "Шахтер",
            "Villarreal CF",
            "Вильярреал",
            "AS Monaco",
            "Монако",
            "Aston Villa FC",
            "Астон Вилла",
            "Atleti",
            "B. Dortmund",
            "S. Bratislava",
            "GNK Dinamo",
            "Bod&#248;/Glimt",
            "Bodø/Glimt",
            "Fenerbah&#231;e",
            "Fenerbahçe",
            "Ferencv&#225;ros",
            "Ferencváros",
            "Club Brugge",
            "LASK",
            "AEK Athens",
            "Viking",
            "Sunderland",
            "Lens",
            "AZ Alkmaar",
            "Celje",
            "Jagiellonia",
            "Lech Poznan",
            "Levski Sofia",
            "N.E.C.",
            "OFI Crete",
            "Omonia",
            "Torreense",
            "Sabah"
        )

        for (club in testClubs) {
            val url = TeamBadgeProvider.getFastBadgeUrl(null, club)
            assertNotNull("Клуб '$club' должен иметь валидный URL эмблемы", url)
            assertTrue("URL эмблемы должен вести на HTTPS CDN: $url", url!!.startsWith("https://"))
            assertTrue("URL эмблемы должен оканчиваться на .png: $url", url.endsWith(".png"))
        }
    }

    @Test
    fun testCrestUrlsAreUniqueAcrossDifferentClubs() {
        val urls = TeamCrestCatalog.entries.map { it.crestUrl }
        assertEquals(
            "Каждый клуб должен иметь уникальный crest URL",
            urls.size,
            urls.toSet().size
        )
    }

    @Test
    fun testFormerSharedIdPairsNoLongerCollide() {
        val pairs = listOf(
            "Celje" to "Maccabi",
            "H. Beer-Sheva" to "Levski Sofia",
            "Omonia" to "Levski Sofia",
            "Ludogorets" to "Sabah",
            "Celje" to "Sabah"
        )
        for ((a, b) in pairs) {
            val urlA = TeamBadgeProvider.getFastBadgeUrl(null, a)
            val urlB = TeamBadgeProvider.getFastBadgeUrl(null, b)
            assertNotNull(urlA)
            assertNotNull(urlB)
            assertNotEquals("Логотипы '$a' и '$b' не должны совпадать", urlA, urlB)
        }
    }

    @Test
    fun testBayernVsBayerCyrillicNotConfused() {
        val bayernVariants = listOf("Bayern", "Бавария", "Байерн", "FC Bayern München")
        val bayerVariants = listOf("Байер", "Bayer 04 Leverkusen", "Leverkusen")

        val bayernUrls = bayernVariants.map { TeamBadgeProvider.getFastBadgeUrl(null, it) }
        val bayerUrls = bayerVariants.map { TeamBadgeProvider.getFastBadgeUrl(null, it) }

        bayernUrls.forEach { assertNotNull(it) }
        bayerUrls.forEach { assertNotNull(it) }

        assertTrue(bayernUrls.distinct().size == 1)
        assertTrue(bayerUrls.distinct().size == 1)
        assertNotEquals(bayernUrls.first(), bayerUrls.first())
        assertTrue(bayernUrls.first()!!.contains("/5.png"))
        assertTrue(bayerUrls.first()!!.contains("/3.png"))
    }

    @Test
    fun testWordBoundaryPreventsSubstringFalsePositives() {
        assertFalse(TeamCrestCatalog.matchesAlias("байерн", "байер"))
        assertTrue(TeamCrestCatalog.matchesAlias("байерн", "байерн"))
        assertFalse(TeamCrestCatalog.matchesAlias("leicester", "lech"))
        assertTrue(TeamCrestCatalog.matchesAlias("lech poznan", "lech"))
    }

    @Test
    fun testDynamicScoringPrefersExactFilename() {
        val exact = TeamCrestRepository.scorePath(
            "levski sofia",
            "logos/Bulgaria - efbet Liga/Levski Sofia.png"
        )
        val weak = TeamCrestRepository.scorePath(
            "levski sofia",
            "logos/England - Premier League/Liverpool.png"
        )
        assertTrue(exact > 0.9)
        assertTrue(weak < 0.5)
        assertTrue(exact > weak)
    }

    @Test
    fun testHtmlEntityUnescaping() {
        assertEquals("Fenerbahçe", TeamNameNormalizer.unescapeHtml("Fenerbah&#231;e"))
        assertEquals("Bodø/Glimt", TeamNameNormalizer.unescapeHtml("Bod&#248;/Glimt"))
        assertEquals("Ferencváros", TeamNameNormalizer.unescapeHtml("Ferencv&#225;ros"))
    }

    @Test
    fun testInitialsGeneration() {
        assertEquals("RM", TeamBadgeProvider.getTeamInitials("Real Madrid"))
        assertEquals("BAR", TeamBadgeProvider.getTeamInitials("FC Barcelona"))
        assertEquals("MC", TeamBadgeProvider.getTeamInitials("Manchester City"))
        assertEquals("LIV", TeamBadgeProvider.getTeamInitials("Liverpool"))
        assertEquals("BAY", TeamBadgeProvider.getTeamInitials("Bayern"))
        assertEquals("BD", TeamBadgeProvider.getTeamInitials("B. Dortmund"))
        assertEquals("ATL", TeamBadgeProvider.getTeamInitials("Atleti"))
    }

    @Test
    fun testVerifiedApiIdsForKnownClubs() {
        assertTrue(
            TeamBadgeProvider.getFastBadgeUrl(null, "Como 1907")!!.contains("/7397.png")
        )
        assertTrue(
            TeamBadgeProvider.getFastBadgeUrl(null, "Sabah")!!.contains("/10233.png")
        )
        assertTrue(
            TeamBadgeProvider.getFastBadgeUrl(null, "Bodø/Glimt")!!.contains("/5721.png")
        )
        assertTrue(
            TeamBadgeProvider.getFastBadgeUrl(null, "Fenerbahçe")!!.contains("/613.png")
        )
    }
}
